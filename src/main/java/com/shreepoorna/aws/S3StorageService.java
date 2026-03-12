package com.shreepoorna.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

public class S3StorageService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(S3StorageService.class);
    private static final String BUCKET_NAME = "shreepoorna-task-scheduler-results";
    
    private final S3Client s3Client;

    public S3StorageService() {
        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    public void uploadTaskResult(String taskId, String result) {
        try {
            String key = "results/" + taskId + ".json";
            
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .contentType("application/json")
                    .build();
            
            s3Client.putObject(putRequest, RequestBody.fromString(result));
            
            LOGGER.info("Uploaded result for task {} to S3: s3://{}/{}", taskId, BUCKET_NAME, key);
        } catch (Exception e) {
            LOGGER.error("Failed to upload to S3 for task {}: {}", taskId, e.getMessage());
        }
    }

    public String downloadTaskResult(String taskId) {
        try {
            String key = "results/" + taskId + ".json";
            
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();
            
            String result = new String(s3Client.getObject(getRequest).readAllBytes());
            LOGGER.info("Downloaded result for task {} from S3", taskId);
            return result;
        } catch (IOException e) {
            LOGGER.error("Failed to download from S3 for task {}: {}", taskId, e.getMessage());
            return null;
        }
    }

    public void close() {
        if (s3Client != null) {
            s3Client.close();
        }
    }
}