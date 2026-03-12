package com.shreepoorna.api;

import com.shreepoorna.cache.RedisCache;
import com.shreepoorna.core.Task;
import com.shreepoorna.db.TaskDAO;
import com.shreepoorna.kafka.KafkaProducerService;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {
    
    private final TaskDAO taskDAO;
    private final KafkaProducerService kafkaProducer;
    private final RedisCache redisCache;

    public TaskResource(TaskDAO taskDAO, KafkaProducerService kafkaProducer, RedisCache redisCache) {
        this.taskDAO = taskDAO;
        this.kafkaProducer = kafkaProducer;
        this.redisCache = redisCache;
    }

    @POST
    public Response createTask(@Valid Task task) {
        if (task.getId() == null || task.getId().isEmpty()) {
            task.setId(UUID.randomUUID().toString());
        }
        
        if (task.getStatus() == null) {
            task.setStatus("PENDING");
        }
        
        // Save to database
        taskDAO.insert(task);
        
        // Cache the task
        redisCache.cacheTask(task);
        
        // Publish to Kafka
        kafkaProducer.publishTask(task);
        
        return Response.status(Response.Status.CREATED)
                .entity(task)
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getTask(@PathParam("id") String id) {
        // Try cache first
        Task cachedTask = redisCache.getTask(id);
        if (cachedTask != null) {
            return Response.ok(cachedTask).build();
        }
        
        // Cache miss - get from database
        return taskDAO.findById(id)
                .map(task -> {
                    redisCache.cacheTask(task);
                    return Response.ok(task).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    public Response listTasks(@QueryParam("status") String status,
                              @DefaultValue("50") @QueryParam("limit") int limit) {
        List<Task> tasks;
        if (status != null && !status.isEmpty()) {
            tasks = taskDAO.findByStatus(status, limit);
        } else {
            tasks = taskDAO.findAll(limit);
        }
        return Response.ok(tasks).build();
    }
}