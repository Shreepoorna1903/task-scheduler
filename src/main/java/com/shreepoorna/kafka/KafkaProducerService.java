package com.shreepoorna.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreepoorna.core.Task;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaProducerService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "task-events";
    
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;

    public KafkaProducerService(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        
        this.producer = new KafkaProducer<>(props);
        this.objectMapper = new ObjectMapper();
    }

    public void publishTask(Task task) {
        try {
            String taskJson = objectMapper.writeValueAsString(task);
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, task.getId(), taskJson);
            
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    LOGGER.error("Failed to publish task {}: {}", task.getId(), exception.getMessage());
                } else {
                    LOGGER.info("Published task {} to partition {}", task.getId(), metadata.partition());
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error publishing task: {}", e.getMessage());
        }
    }

    public void close() {
        producer.close();
    }
}
