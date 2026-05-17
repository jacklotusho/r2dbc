package com.example.r2dbc.config;

import com.example.r2dbc.entity.User;
import com.example.r2dbc.entity.UserRole;
import com.example.r2dbc.repository.RoleRepository;
import com.example.r2dbc.repository.UserRepository;
import com.example.r2dbc.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {
    
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder) {
        
        return args -> {
            log.info("Starting data initialization...");
            
            // Check if admin user already exists
            userRepository.findByUsername("admin")
                    .switchIfEmpty(
                            // Create admin user if not exists
                            roleRepository.findByName("ROLE_ADMIN")
                                    .flatMap(adminRole -> 
                                            roleRepository.findByName("ROLE_USER")
                                                    .flatMap(userRole -> {
                                                        User admin = User.builder()
                                                                .username("admin")
                                                                .email("admin@example.com")
                                                                .passwordHash(passwordEncoder.encode("admin123"))
                                                                .enabled(true)
                                                                .createdAt(LocalDateTime.now())
                                                                .build();
                                                        
                                                        return userRepository.save(admin)
                                                                .flatMap(savedAdmin -> {
                                                                    // Assign both ROLE_USER and ROLE_ADMIN
                                                                    UserRole ur1 = UserRole.builder()
                                                                            .userId(savedAdmin.getId())
                                                                            .roleId(userRole.getId())
                                                                            .build();
                                                                    
                                                                    UserRole ur2 = UserRole.builder()
                                                                            .userId(savedAdmin.getId())
                                                                            .roleId(adminRole.getId())
                                                                            .build();
                                                                    
                                                                    return Flux.concat(
                                                                            userRoleRepository.save(ur1),
                                                                            userRoleRepository.save(ur2)
                                                                    ).then(Mono.just(savedAdmin));
                                                                })
                                                                .doOnSuccess(admin1 -> 
                                                                        log.info("Created default admin user - username: admin, password: admin123, uuid: {}", 
                                                                                admin1.getUuid())
                                                                );
                                                    })
                                    )
                    )
                    .doOnSuccess(user -> {
                        if (user != null) {
                            log.info("Admin user already exists with uuid: {}", user.getUuid());
                        }
                    })
                    .doOnError(error -> log.error("Error during data initialization", error))
                    .subscribe();
            
            log.info("Data initialization completed");
        };
    }
}
