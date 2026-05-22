package com.example.r2dbc.controller;

import com.example.r2dbc.dto.ForgotPasswordRequest;
import com.example.r2dbc.dto.ResetPasswordRequest;
import com.example.r2dbc.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Password Reset", description = "Endpoints for password reset flow")
public class PasswordResetController {
    
    private final PasswordResetService passwordResetService;
    
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Sends a password reset link to the user's email if it exists.")
    @ApiResponse(responseCode = "200", description = "Reset link sent if email exists")
    @SecurityRequirements   // public endpoint — no Bearer token required
    public Mono<ResponseEntity<Map<String, String>>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return passwordResetService.forgotPassword(request.getEmail())
                .thenReturn(ResponseEntity.ok(Map.of("message", "If an account exists for that email, a password reset link has been sent.")));
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets the user password using a valid reset token.")
    @ApiResponse(responseCode = "200", description = "Password successfully reset")
    @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    @SecurityRequirements   // public endpoint — no Bearer token required
    public Mono<ResponseEntity<Map<String, String>>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return passwordResetService.resetPassword(request)
                .thenReturn(ResponseEntity.ok(Map.of("message", "Password has been successfully reset.")));
    }
}
