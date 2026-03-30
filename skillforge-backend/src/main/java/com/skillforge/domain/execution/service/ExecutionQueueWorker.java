package com.skillforge.domain.execution.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExecutionQueueWorker {

    private static final Logger log = LoggerFactory.getLogger(ExecutionQueueWorker.class);

    private final ExecutionQueue executionQueue;
    private final ExecutionService executionService;
    private final int batchSize;

    public ExecutionQueueWorker(
            ExecutionQueue executionQueue,
            ExecutionService executionService,
            @Value("${execution.queue.batch-size}") int batchSize) {
        this.executionQueue = executionQueue;
        this.executionService = executionService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${execution.queue.poll-ms}")
    public void pollAndExecute() {
        List<Long> ids = executionQueue.drain(batchSize);
        for (Long id : ids) {
            try {
                executionService.processQueuedExecution(id);
            } catch (Exception ex) {
                log.error("Execution job failed for id={}", id, ex);
            }
        }
    }
}
