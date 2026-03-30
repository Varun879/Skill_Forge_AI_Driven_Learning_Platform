package com.skillforge.domain.execution.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.skillforge.domain.execution.entity.ExecutionStatus;

@Component
public class DockerExecutor {

    private final boolean dockerEnabled;
    private final long timeoutMs;
    private final long memoryKb;

    public DockerExecutor(
            @Value("${execution.docker.enabled}") boolean dockerEnabled,
            @Value("${execution.limits.timeout-ms}") long timeoutMs,
            @Value("${execution.limits.memory-kb}") long memoryKb) {
        this.dockerEnabled = dockerEnabled;
        this.timeoutMs = timeoutMs;
        this.memoryKb = memoryKb;
    }

    public ExecutionCommandResult execute(String language, String sourceCode, String stdin, Consumer<String> streamOutput) {
        try {
            Path workDir = Files.createTempDirectory("skillforge-exec-");
            LanguageRuntime runtime = LanguageRuntime.from(language);
            Path sourcePath = workDir.resolve(runtime.sourceFile);
            Files.writeString(sourcePath, sourceCode, StandardCharsets.UTF_8);

            ProcessResult compileResult = runtime.compileCmd == null
                    ? ProcessResult.success("", "", 0, false)
                    : runProcess(buildCommand(runtime, workDir, runtime.compileCmd), workDir, "", timeoutMs + 1000, streamOutput);

            if (compileResult.timedOut) {
                return new ExecutionCommandResult(ExecutionStatus.TLE, compileResult.stdout, compileResult.stderr, compileResult.durationMs, memoryKb);
            }
            if (compileResult.exitCode != 0) {
                return new ExecutionCommandResult(ExecutionStatus.COMPILATION_ERROR, compileResult.stdout, compileResult.stderr, compileResult.durationMs, memoryKb);
            }

            ProcessResult runResult = runProcess(buildCommand(runtime, workDir, runtime.runCmd), workDir, stdin, timeoutMs, streamOutput);
            if (runResult.timedOut) {
                return new ExecutionCommandResult(ExecutionStatus.TLE, runResult.stdout, runResult.stderr, runResult.durationMs, memoryKb);
            }
            if (runResult.exitCode != 0) {
                ExecutionStatus status = looksLikeMemoryError(runResult.stderr)
                        ? ExecutionStatus.MEMORY_LIMIT_EXCEEDED
                        : ExecutionStatus.RUNTIME_ERROR;
                return new ExecutionCommandResult(status, runResult.stdout, runResult.stderr, runResult.durationMs, memoryKb);
            }

            return new ExecutionCommandResult(ExecutionStatus.SUCCESS, runResult.stdout, runResult.stderr, runResult.durationMs, memoryKb);
        } catch (Exception ex) {
            return new ExecutionCommandResult(ExecutionStatus.FAILED, "", ex.getMessage(), 0L, memoryKb);
        }
    }

    public String hashSource(String language, String source) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((language + ":" + source).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            return Integer.toHexString((language + source).hashCode());
        }
    }

    private List<String> buildCommand(LanguageRuntime runtime, Path workDir, String script) {
        if (!dockerEnabled) {
            return shellCommand(script);
        }

        String mountPath = workDir.toAbsolutePath().toString().replace("\\", "/");
        String image = runtime.dockerImage;
        long memoryMb = Math.max(64, memoryKb / 1024);

        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");
        cmd.add("--network");
        cmd.add("none");
        cmd.add("--memory");
        cmd.add(memoryMb + "m");
        cmd.add("--cpus");
        cmd.add("1");
        cmd.add("-v");
        cmd.add(mountPath + ":/workspace");
        cmd.add("-w");
        cmd.add("/workspace");
        cmd.add(image);
        cmd.add("sh");
        cmd.add("-lc");
        cmd.add(script);
        return cmd;
    }

    private List<String> shellCommand(String command) {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        return windows ? List.of("cmd", "/c", command) : List.of("sh", "-lc", command);
    }

    private ProcessResult runProcess(List<String> cmd, Path workDir, String stdin, long maxWaitMs, Consumer<String> streamOutput) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(cmd).directory(workDir.toFile());
        Process process = builder.start();

        if (StringUtils.hasText(stdin)) {
            process.getOutputStream().write(stdin.getBytes(StandardCharsets.UTF_8));
        }
        process.getOutputStream().close();

        var executor = Executors.newFixedThreadPool(2);
        CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync(() -> readStream(process.getInputStream(), streamOutput), executor);
        CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(() -> readStream(process.getErrorStream(), streamOutput), executor);

        long start = System.nanoTime();
        boolean finished = process.waitFor(maxWaitMs, TimeUnit.MILLISECONDS);
        long durationMs = Duration.ofNanos(System.nanoTime() - start).toMillis();

        if (!finished) {
            process.destroyForcibly();
            executor.shutdownNow();
            return ProcessResult.success("", "Time limit exceeded", durationMs, true);
        }

        int exit = process.exitValue();
        String stdout = stdoutFuture.get(500, TimeUnit.MILLISECONDS);
        String stderr = stderrFuture.get(500, TimeUnit.MILLISECONDS);
        executor.shutdownNow();
        return new ProcessResult(stdout, stderr, exit, durationMs, false);
    }

    private String readStream(InputStream stream, Consumer<String> sink) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
                sink.accept(line + "\n");
            }
        } catch (IOException ignored) {
        }
        return sb.toString();
    }

    private boolean looksLikeMemoryError(String stderr) {
        String normalized = stderr == null ? "" : stderr.toLowerCase();
        return normalized.contains("outofmemory") || normalized.contains("memoryerror") || normalized.contains("killed");
    }

    public record ExecutionCommandResult(ExecutionStatus status, String stdout, String stderr, long executionTimeMs, long memoryUsageKb) {}

    private record ProcessResult(String stdout, String stderr, int exitCode, long durationMs, boolean timedOut) {
        private static ProcessResult success(String stdout, String stderr, long durationMs, boolean timedOut) {
            return new ProcessResult(stdout, stderr, 0, durationMs, timedOut);
        }
    }

    private record LanguageRuntime(String sourceFile, String compileCmd, String runCmd, String dockerImage) {
        private static LanguageRuntime from(String language) {
            String normalized = language == null ? "" : language.trim().toLowerCase();
            return switch (normalized) {
                case "java" -> new LanguageRuntime("Main.java", "javac Main.java", "java Main", "eclipse-temurin:21-jdk");
                case "c++", "cpp" -> new LanguageRuntime("main.cpp", "g++ main.cpp -O2 -std=c++17 -o main", "./main", "gcc:14");
                case "python", "py" -> new LanguageRuntime("main.py", null, "python3 main.py", "python:3.12");
                default -> throw new IllegalArgumentException("Unsupported language: " + language);
            };
        }
    }
}
