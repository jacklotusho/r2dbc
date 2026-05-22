package com.example.r2dbc.controller;

import com.example.r2dbc.dto.AuthResponse;
import com.example.r2dbc.dto.LdapLoginRequest;
import com.example.r2dbc.service.LdapAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth/ldap")
@Tag(name = "LDAP Auth", description = "Corporate LDAP / Active Directory authentication")
public class LdapAuthController {

    private final LdapAuthService ldapAuthService;

    public LdapAuthController(LdapAuthService ldapAuthService) {
        this.ldapAuthService = ldapAuthService;
    }

    @Operation(
            summary = "Login with corporate LDAP / AD credentials",
            description = """
                    Authenticates the user against the company LDAP directory or Active Directory.
                    On first login the user is auto-provisioned in the local database.
                    On subsequent logins email and role membership are synced from LDAP.
                    Issues the same JWT as the standard /auth/login endpoint.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "LDAP authentication successful — JWT returned",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid LDAP credentials"),
            @ApiResponse(responseCode = "400", description = "Validation error (blank username/password)")
    })
    @SecurityRequirements   // this endpoint is public — no Bearer token required
    @PostMapping("/login")
    public Mono<AuthResponse> ldapLogin(@Valid @RequestBody LdapLoginRequest request) {
        return ldapAuthService.authenticateWithLdap(request.getUsername(), request.getPassword());
    }
}
