package com.example.r2dbc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Corporate LDAP / Active Directory login request")
public class LdapLoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "LDAP username (uid / sAMAccountName)", example = "john.doe")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "LDAP password", example = "secret")
    private String password;

    public LdapLoginRequest() {}

    public LdapLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
