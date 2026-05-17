package com.example.r2dbc.security;

import com.example.r2dbc.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class JwtAuthenticationWebFilter implements WebFilter {
    
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    
    public JwtAuthenticationWebFilter(JwtUtil jwtUtil, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Skip JWT validation for public endpoints
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }
        
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtUtil.validateToken(token)) {
                UUID userUuid = jwtUtil.extractUserUuid(token);
                
                return authService.resolveUserByUuid(userUuid)
                        .flatMap(userDetails -> {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userUuid, // principal is UUID object
                                            null,
                                            userDetails.getAuthorities()
                                    );
                            
                            return chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                        })
                        .switchIfEmpty(Mono.defer(() -> chain.filter(exchange)));
            }
        }
        
        return chain.filter(exchange);
    }
}
