package com.skillforge.config;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseAdminConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAdminConfig.class);
    private final ResourceLoader resourceLoader;

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    public FirebaseAdminConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try {
            FirebaseApp.initializeApp(buildOptions());
            log.info("Firebase Admin initialized successfully");
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to initialize Firebase Admin. Set GOOGLE_APPLICATION_CREDENTIALS or configure firebase.service-account-path.",
                    e);
        }
    }

    private FirebaseOptions buildOptions() throws IOException {
        try {
            return FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .build();
        } catch (IOException defaultCredentialsError) {
            if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
                throw defaultCredentialsError;
            }

            Resource resource = resourceLoader.getResource(serviceAccountPath);
            if (!resource.exists()) {
                throw defaultCredentialsError;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                return FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(inputStream))
                        .build();
            }
        }
    }
}
