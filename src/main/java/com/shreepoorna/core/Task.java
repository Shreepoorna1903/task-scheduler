package com.shreepoorna.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class Task {
    
    @JsonProperty
    private String id;
    
    @JsonProperty
    private String type;
    
    @JsonProperty
    private String payload;
    
    @JsonProperty
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    
    @JsonProperty
    private Instant createdAt;
    
    @JsonProperty
    private Instant updatedAt;

    // Constructors
    public Task() {}

    public Task(String id, String type, String payload, String status) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}