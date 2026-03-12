package com.shreepoorna;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import io.dropwizard.db.DataSourceFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class TaskSchedulerConfiguration extends Configuration {
    
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();
    
    @NotNull
    private String kafkaBootstrapServers = "localhost:9092";
    
    @NotNull
    private String redisHost = "localhost";
    
    private int redisPort = 6379;

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    @JsonProperty("kafkaBootstrapServers")
    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    @JsonProperty("kafkaBootstrapServers")
    public void setKafkaBootstrapServers(String kafkaBootstrapServers) {
        this.kafkaBootstrapServers = kafkaBootstrapServers;
    }

    @JsonProperty("redisHost")
    public String getRedisHost() {
        return redisHost;
    }

    @JsonProperty("redisHost")
    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    @JsonProperty("redisPort")
    public int getRedisPort() {
        return redisPort;
    }

    @JsonProperty("redisPort")
    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }
}