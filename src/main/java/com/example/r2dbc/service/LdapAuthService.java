package com.example.r2dbc.service;

import com.example.r2dbc.config.LdapProperties;
import com.example.r2dbc.dto.AuthResponse;
import com.example.r2dbc.entity.User;
import com.example.r2dbc.entity.UserRole;
import com.example.r2dbc.repository.RoleRepository;
import com.example.r2dbc.repository.UserRepository;
import com.example.r2dbc.repository.UserRoleRepository;
import com.example.r2dbc.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.naming.directory.Attributes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * LDAP authentication service.
 *
 * Because spring-security-ldap is inherently blocking, every LDAP call is
 * wrapped in {@code Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())}
 * so the reactive pipeline is never blocked.
 *
 * Flow:
 * 1. Authenticate credentials against LDAP/AD (blocking → wrapped)
 * 2. Look up local User row by username
 * 3. If not found → auto-provision: create User with auth_provider=LDAP
 * 4. If found → sync email / roles from LDAP attributes
 * 5. Issue JWT (same as local auth — sub = user.uuid.toString())
 * 6. Return AuthResponse
 */
@Service
public class LdapAuthService {

    private static final Logger log = LoggerFactory.getLogger(LdapAuthService.class);

    /** Placeholder prefix stored in password_hash for LDAP-managed accounts. */
    public static final String LDAP_PASSWORD_PREFIX = "{LDAP}";

    private final AuthenticationManager ldapAuthenticationManager;
    private final LdapTemplate ldapTemplate;
    private final LdapProperties ldapProperties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LdapAuthService(
            AuthenticationManager ldapAuthenticationManager,
            LdapTemplate ldapTemplate,
            LdapProperties ldapProperties,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.ldapAuthenticationManager = ldapAuthenticationManager;
        this.ldapTemplate = ldapTemplate;
        this.ldapProperties = ldapProperties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Authenticate against LDAP/AD, auto-provision local user if needed,
     * sync roles, then issue JWT.
     */
    public Mono<AuthResponse> authenticateWithLdap(String username, String password) {
        return authenticateBlocking(username, password)       // step 1 — blocking wrapped
                .flatMap(auth -> fetchLdapUserData(username)   // fetch LDAP user data using the login username
                        .flatMap(userData -> provisionOrSyncUser(userData, auth)) // steps 2-4
                )
                .flatMap(this::buildAuthResponse);            // step 5-6
    }

    // -------------------------------------------------------------------------
    // Step 1 — LDAP bind (blocking → reactive)
    // -------------------------------------------------------------------------

    /**
     * Performs the blocking LDAP bind on the boundedElastic scheduler.
     * Returns the authenticated {@link Authentication} (with granted authorities populated
     * by the LdapAuthoritiesPopulator / AD provider).
     */
    private Mono<Authentication> authenticateBlocking(String username, String password) {
        return Mono.fromCallable(() -> {
                    UsernamePasswordAuthenticationToken token =
                            new UsernamePasswordAuthenticationToken(username, password);
                    return ldapAuthenticationManager.authenticate(token);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(ex -> {
                    log.warn("LDAP authentication failed for user '{}': {}", username, ex.getMessage());
                    // Normalise all LDAP failures to BadCredentialsException
                    if (ex instanceof BadCredentialsException) {
                        return ex;
                    }
                    return new BadCredentialsException("Invalid LDAP credentials", ex);
                });
    }

    // -------------------------------------------------------------------------
    // Step 2-4 — provision or sync local user
    // -------------------------------------------------------------------------

    private Mono<User> provisionOrSyncUser(LdapUserData userData, Authentication auth) {
        return userRepository.findByUsername(userData.getUsername())
                .flatMap(existingUser -> syncUserFromLdap(existingUser, userData, auth))   // user exists → sync
                .switchIfEmpty(Mono.defer(() -> provisionNewUser(userData, auth))); // not found → create
    }

    /**
     * Sync email and roles for an existing LDAP user.
     * Does NOT sync users whose auth_provider is LOCAL (safety guard).
     */
    private Mono<User> syncUserFromLdap(User user, LdapUserData userData, Authentication auth) {
        if (!"LDAP".equals(user.getAuthProvider())) {
            // Local user with same username — do not let LDAP override it
            throw new BadCredentialsException(
                    "Username '" + user.getUsername() + "' is a local account. Use /auth/login instead.");
        }

        boolean changed = false;
        if (userData.getEmail() != null && !userData.getEmail().equals(user.getEmail())) {
            user.setEmail(userData.getEmail());
            changed = true;
        }
        
        return (changed ? userRepository.save(user) : Mono.just(user))
                .flatMap(savedUser -> syncRoles(savedUser, auth));
    }

    /**
     * Create a brand-new local User row for a user who authenticated via LDAP
     * but has no existing record in the DB.
     */
    private Mono<User> provisionNewUser(LdapUserData userData, Authentication auth) {
        String email = userData.getEmail() != null ? userData.getEmail() : userData.getUsername() + "@ldap.local";

        // Unusable password hash — prevents direct /auth/login
        String placeholderHash = passwordEncoder.encode(
                LDAP_PASSWORD_PREFIX + UUID.randomUUID());

        User newUser = User.builder()
                .username(userData.getUsername())
                .email(email)
                .passwordHash(LDAP_PASSWORD_PREFIX + placeholderHash)
                .authProvider("LDAP")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(newUser)
                // Re-fetch to get DB-generated uuid
                .flatMap(saved -> userRepository.findById(saved.getId()))
                .flatMap(savedWithUuid -> assignDefaultRoles(savedWithUuid, auth));
    }

    // -------------------------------------------------------------------------
    // Role helpers
    // -------------------------------------------------------------------------

    /**
     * Assigns ROLE_USER to a newly provisioned LDAP user.
     * Also assigns ROLE_ADMIN if the user is a member of the configured admin group.
     */
    private Mono<User> assignDefaultRoles(User user, Authentication auth) {
        List<String> rolesToAssign = new ArrayList<>();
        rolesToAssign.add("ROLE_USER");

        if (isLdapAdmin(auth)) {
            rolesToAssign.add("ROLE_ADMIN");
        }

        return Flux.fromIterable(rolesToAssign)
                .flatMap(roleRepository::findByName)
                .flatMap(role -> {
                    UserRole userRole = UserRole.builder()
                            .userId(user.getId())
                            .roleId(role.getId())
                            .build();
                    return userRoleRepository.save(userRole);
                })
                .then(Mono.just(user));
    }

    /**
     * Syncs roles for an existing LDAP user based on current group membership.
     * Removes ROLE_ADMIN if user is no longer in the admin group (or adds it if promoted).
     */
    private Mono<User> syncRoles(User user, Authentication auth) {
        boolean shouldBeAdmin = isLdapAdmin(auth);

        return userRoleRepository.findByUserId(user.getId())
                .collectList()
                .flatMap(existingUserRoles -> {
                    List<Long> existingRoleIds = existingUserRoles.stream()
                            .map(UserRole::getRoleId).toList();
                    return roleRepository.findByIdIn(existingRoleIds).collectList()
                            .flatMap(existingRoles -> {
                                boolean hasAdmin = existingRoles.stream()
                                        .anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));

                                if (shouldBeAdmin && !hasAdmin) {
                                    // Grant ROLE_ADMIN
                                    return roleRepository.findByName("ROLE_ADMIN")
                                            .flatMap(adminRole -> {
                                                UserRole ur = UserRole.builder()
                                                        .userId(user.getId())
                                                        .roleId(adminRole.getId())
                                                        .build();
                                                return userRoleRepository.save(ur);
                                            })
                                            .thenReturn(user);
                                } else if (!shouldBeAdmin && hasAdmin) {
                                    // Revoke ROLE_ADMIN
                                    return roleRepository.findByName("ROLE_ADMIN")
                                            .flatMap(adminRole ->
                                                    userRoleRepository.deleteByUserIdAndRoleId(
                                                            user.getId(), adminRole.getId()))
                                            .thenReturn(user);
                                }
                                return Mono.just(user);
                            });
                });
    }

    /**
     * Returns true when the authenticated LDAP token contains an authority that
     * matches the configured adminGroupDn, or any authority named ADMINS / ROLE_ADMIN.
     */
    private boolean isLdapAdmin(Authentication auth) {
        if (ldapProperties.getAdminGroupDn() != null) {
            for (GrantedAuthority ga : auth.getAuthorities()) {
                if (ldapProperties.getAdminGroupDn().equalsIgnoreCase(ga.getAuthority())
                        || ga.getAuthority().equalsIgnoreCase("ROLE_ADMIN")
                        || ga.getAuthority().equalsIgnoreCase("ROLE_ADMINS")) {
                    return true;
                }
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Step 5-6 — JWT issuance
    // -------------------------------------------------------------------------

    private Mono<AuthResponse> buildAuthResponse(User user) {
        return userRoleRepository.findByUserId(user.getId())
                .map(UserRole::getRoleId)
                .collectList()
                .flatMapMany(roleRepository::findByIdIn)
                .map(role -> role.getName())
                .collectList()
                .map(roles -> {
                    String token = jwtUtil.generateToken(user.getUuid(), roles);
                    return AuthResponse.builder()
                            .token(token)
                            .uuid(user.getUuid())
                            .username(user.getUsername())
                            .roles(roles)
                            .build();
                });
    }

    // -------------------------------------------------------------------------
    // LDAP attribute lookup (blocking → wrapped)
    // -------------------------------------------------------------------------

    /**
     * Fetches user data from LDAP based on configured attributes mapping.
     */
    private Mono<LdapUserData> fetchLdapUserData(String username) {
        return Mono.fromCallable(() -> {
                    String filter = ldapProperties.getUsernameFilter().replace("{0}", username);
                    String searchBase = ldapProperties.getRelativeDn(ldapProperties.getUserSearchBase());

                    List<LdapUserData> results = ldapTemplate.search(
                            searchBase,
                            filter,
                            (Attributes attrs) -> {
                                Map<String, List<String>> mapping = parseAttributesMapping(ldapProperties.getAttributesMapping());

                                String mappedUsername = null;
                                List<String> uidAttrs = mapping.get("uid");
                                if (uidAttrs != null && !uidAttrs.isEmpty()) {
                                    mappedUsername = getAttributeValue(attrs, uidAttrs.get(0));
                                }
                                if (mappedUsername == null) {
                                    mappedUsername = username;
                                }

                                String mappedEmail = null;
                                List<String> emailAttrs = mapping.get("email");
                                if (emailAttrs != null && !emailAttrs.isEmpty()) {
                                    mappedEmail = getAttributeValue(attrs, emailAttrs.get(0));
                                }

                                String mappedName = null;
                                List<String> nameAttrs = mapping.get("name");
                                if (nameAttrs != null) {
                                    List<String> nameParts = new ArrayList<>();
                                    for (String attrName : nameAttrs) {
                                        String val = getAttributeValue(attrs, attrName);
                                        if (val != null) {
                                            nameParts.add(val);
                                        }
                                    }
                                    if (!nameParts.isEmpty()) {
                                        mappedName = String.join(" ", nameParts);
                                    }
                                }

                                return new LdapUserData(mappedUsername, mappedEmail, mappedName);
                            });
                    return results.isEmpty() ? null : results.get(0);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(ex -> {
                    log.warn("Could not fetch LDAP user data for '{}': {}", username, ex.getMessage());
                    // Fallback to basic user data containing the input username
                    return Mono.just(new LdapUserData(username, null, null));
                })
                .switchIfEmpty(Mono.just(new LdapUserData(username, null, null)));
    }

    private String getAttributeValue(Attributes attrs, String attributeName) {
        javax.naming.directory.Attribute attr = attrs.get(attributeName);
        if (attr != null) {
            try {
                return (String) attr.get();
            } catch (javax.naming.NamingException e) {
                return null;
            }
        }
        return null;
    }

    private Map<String, List<String>> parseAttributesMapping(String mappingStr) {
        Map<String, List<String>> mapping = new java.util.HashMap<>();
        if (mappingStr == null || mappingStr.isBlank()) {
            return mapping;
        }
        for (String pair : mappingStr.split(";")) {
            String[] parts = pair.split(":", 2);
            if (parts.length == 2) {
                String localField = parts[0].trim();
                List<String> ldapAttrs = java.util.Arrays.stream(parts[1].split(","))
                        .map(String::trim)
                        .toList();
                mapping.put(localField, ldapAttrs);
            }
        }
        return mapping;
    }

    // -------------------------------------------------------------------------
    // LdapUserData DTO Class
    // -------------------------------------------------------------------------
    public static class LdapUserData {
        private final String username;
        private final String email;
        private final String name;

        public LdapUserData(String username, String email, String name) {
            this.username = username;
            this.email = email;
            this.name = name;
        }

        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getName() { return name; }
    }
}
