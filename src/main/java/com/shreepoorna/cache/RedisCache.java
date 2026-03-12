package com.shreepoorna.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreepoorna.core.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisCache {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCache.class);
    private static final int TTL_SECONDS = 300; // 5 minutes
    
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    public RedisCache(String host, int port) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        this.jedisPool = new JedisPool(poolConfig, host, port);
        this.objectMapper = new ObjectMapper();
    }

    public void cacheTask(Task task) {
        try (var jedis = jedisPool.getResource()) {
            String key = "task:" + task.getId();
            String value = objectMapper.writeValueAsString(task);
            jedis.setex(key, TTL_SECONDS, value);
            LOGGER.debug("Cached task: {}", task.getId());
        } catch (Exception e) {
            LOGGER.error("Failed to cache task: {}", e.getMessage());
        }
    }

    public Task getTask(String taskId) {
        try (var jedis = jedisPool.getResource()) {
            String key = "task:" + taskId;
            String value = jedis.get(key);
            if (value != null) {
                LOGGER.debug("Cache hit for task: {}", taskId);
                return objectMapper.readValue(value, Task.class);
            }
            LOGGER.debug("Cache miss for task: {}", taskId);
            return null;
        } catch (Exception e) {
            LOGGER.error("Failed to get cached task: {}", e.getMessage());
            return null;
        }
    }

    public void invalidateTask(String taskId) {
        try (var jedis = jedisPool.getResource()) {
            String key = "task:" + taskId;
            jedis.del(key);
            LOGGER.debug("Invalidated cache for task: {}", taskId);
        } catch (Exception e) {
            LOGGER.error("Failed to invalidate cache: {}", e.getMessage());
        }
    }

    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}