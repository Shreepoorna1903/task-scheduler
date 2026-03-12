package com.shreepoorna;

import com.codahale.metrics.health.HealthCheck;
import org.jdbi.v3.core.Jdbi;

public class DatabaseHealthCheck extends HealthCheck {
    
    private final Jdbi jdbi;

    public DatabaseHealthCheck(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    protected Result check() throws Exception {
        jdbi.withHandle(handle -> 
            handle.execute("SELECT 1")
        );
        return Result.healthy();
    }
}