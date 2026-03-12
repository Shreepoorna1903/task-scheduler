package com.shreepoorna;

import com.shreepoorna.api.TaskResource;
import com.shreepoorna.aws.S3StorageService;
import com.shreepoorna.cache.RedisCache;
import com.shreepoorna.db.TaskDAO;
import com.shreepoorna.kafka.KafkaConsumerWorker;
import com.shreepoorna.kafka.KafkaProducerService;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jdbi3.JdbiFactory;
import org.jdbi.v3.core.Jdbi;

public class TaskSchedulerApplication extends Application<TaskSchedulerConfiguration> {

    public static void main(String[] args) throws Exception {
        new TaskSchedulerApplication().run(args);
    }

    @Override
    public String getName() {
        return "task-scheduler";
    }

    @Override
    public void initialize(Bootstrap<TaskSchedulerConfiguration> bootstrap) {
    }

    @Override
    public void run(TaskSchedulerConfiguration configuration, Environment environment) {
        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, configuration.getDataSourceFactory(), "mysql");
        
        final TaskDAO taskDAO = jdbi.onDemand(TaskDAO.class);
        
        // Initialize Kafka producer
        final KafkaProducerService kafkaProducer = new KafkaProducerService(
            configuration.getKafkaBootstrapServers()
        );
        
        // Initialize Redis cache
        final RedisCache redisCache = new RedisCache(
            configuration.getRedisHost(),
            configuration.getRedisPort()
        );
        
        // Initialize S3 storage
        final S3StorageService s3Service = new S3StorageService();
        
        // Initialize Kafka consumer worker
        final KafkaConsumerWorker consumerWorker = new KafkaConsumerWorker(
            configuration.getKafkaBootstrapServers(),
            "task-worker-group",
            taskDAO,
            s3Service
        );
        
        // Start consumer in a separate thread
        Thread consumerThread = new Thread(consumerWorker, "kafka-consumer-worker");
        consumerThread.start();
        
        // Register REST resources
        environment.jersey().register(new TaskResource(taskDAO, kafkaProducer, redisCache));
        
        environment.healthChecks().register("database", new DatabaseHealthCheck(jdbi));
        
        // Shutdown hooks
        environment.lifecycle().manage(new io.dropwizard.lifecycle.Managed() {
            @Override
            public void start() {}
            
            @Override
            public void stop() {
                kafkaProducer.close();
                redisCache.close();
                s3Service.close();
                consumerWorker.stop();
                try {
                    consumerThread.join(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}