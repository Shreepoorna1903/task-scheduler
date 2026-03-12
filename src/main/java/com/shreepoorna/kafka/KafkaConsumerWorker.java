package com.shreepoorna.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreepoorna.aws.S3StorageService;
import com.shreepoorna.core.Task;
import com.shreepoorna.db.TaskDAO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerWorker implements Runnable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerWorker.class);
    private static final String TOPIC = "task-events";
    
    private final KafkaConsumer<String, String> consumer;
    private final TaskDAO taskDAO;
    private final S3StorageService s3Service;
    private final ObjectMapper objectMapper;
    private volatile boolean running = true;

    public KafkaConsumerWorker(String bootstrapServers, String groupId, TaskDAO taskDAO, S3StorageService s3Service) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        
        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(TOPIC));
        this.taskDAO = taskDAO;
        this.s3Service = s3Service;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        LOGGER.info("Kafka consumer worker started");
        
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        Task task = objectMapper.readValue(record.value(), Task.class);
                        processTask(task);
                    } catch (Exception e) {
                        LOGGER.error("Error processing task: {}", e.getMessage());
                    }
                }
            }
        } finally {
            consumer.close();
            LOGGER.info("Kafka consumer worker stopped");
        }
    }

    private void processTask(Task task) {
        LOGGER.info("Processing task: {} of type: {}", task.getId(), task.getType());
        
        try {
            // Update status to PROCESSING
            taskDAO.updateStatus(task.getId(), "PROCESSING");
            
            // Simulate task execution
            Thread.sleep(2000);
            
            // Create mock result
            String result = String.format("{\"taskId\":\"%s\",\"status\":\"success\",\"processedAt\":\"%s\"}", 
                    task.getId(), java.time.Instant.now());
            
            // Store result in S3
            s3Service.uploadTaskResult(task.getId(), result);
            
            // Update status to COMPLETED
            taskDAO.updateStatus(task.getId(), "COMPLETED");
            
            LOGGER.info("Completed task: {} with result stored in S3", task.getId());
            
        } catch (Exception e) {
            LOGGER.error("Failed to process task {}: {}", task.getId(), e.getMessage());
            taskDAO.updateStatus(task.getId(), "FAILED");
        }
    }

    public void stop() {
        running = false;
    }
}