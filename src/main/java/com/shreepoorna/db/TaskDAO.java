package com.shreepoorna.db;

import com.shreepoorna.core.Task;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

@RegisterBeanMapper(Task.class)
public interface TaskDAO {
    
    @SqlUpdate("INSERT INTO tasks (id, type, payload, status, created_at, updated_at) " +
               "VALUES (:id, :type, :payload, :status, NOW(), NOW())")
    void insert(@BindBean Task task);
    
    @SqlQuery("SELECT * FROM tasks WHERE id = :id")
    Optional<Task> findById(@Bind("id") String id);
    
    @SqlQuery("SELECT * FROM tasks WHERE status = :status ORDER BY created_at DESC LIMIT :limit")
    List<Task> findByStatus(@Bind("status") String status, @Bind("limit") int limit);
    
    @SqlQuery("SELECT * FROM tasks ORDER BY created_at DESC LIMIT :limit")
    List<Task> findAll(@Bind("limit") int limit);
    
    @SqlUpdate("UPDATE tasks SET status = :status, updated_at = NOW() WHERE id = :id")
    void updateStatus(@Bind("id") String id, @Bind("status") String status);
}