package com.example.r2dbc.exception;

/**
 * Thrown when a user whose password_hash starts with "{LDAP}"
 * attempts to log in via the standard /auth/login endpoint.
 * These users must authenticate through /auth/ldap/login instead.
 */
public class LdapUserException extends RuntimeException {

    public LdapUserException(String message) {
        super(message);
    }
}
