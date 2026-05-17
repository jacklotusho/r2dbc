package com.example.r2dbc.repository;

import com.example.r2dbc.entity.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface RoleRepository extends R2dbcRepository<Role, Long> {
    
    Mono<Role> findByName(String name);
    
    @Query("SELECT * FROM roles WHERE id IN (:ids)")
    Flux<Role> findByIdIn(List<Long> ids);
}

