package com.example.r2dbc.config;

import com.example.r2dbc.security.JwtAuthenticationWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationWebFilter jwtAuthenticationWebFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    
    public SecurityConfig(
            JwtAuthenticationWebFilter jwtAuthenticationWebFilter,
            CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationWebFilter = jwtAuthenticationWebFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> Mono.fromRunnable(() -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        }))
                )
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints
                        .pathMatchers(HttpMethod.POST,
                                "/auth/register",
                                "/auth/login",
                                "/auth/forgot-password",
                                "/auth/reset-password"
                        ).permitAll()
                        .pathMatchers(HttpMethod.GET, "/auth/**").permitAll()
                        
                        // Swagger/OpenAPI endpoints and static resources
                        .pathMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**"
                        ).permitAll()
                        
                        // Static resources - allow all GET requests to root and static files
                        .pathMatchers(HttpMethod.GET,
                                "/",
                                "/index.html",
                                "/*.html",
                                "/*.css",
                                "/*.js",
                                "/*.ico",
                                "/favicon.ico"
                        ).permitAll()
                        
                        // Authenticated endpoints
                        .pathMatchers("/api/users/me").authenticated()
                        
                        // Admin only endpoints
                        .pathMatchers("/api/users").hasRole("ADMIN")
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // All other requests require authentication
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}

