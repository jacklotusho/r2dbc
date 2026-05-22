package com.example.r2dbc.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

@Configuration
@EnableConfigurationProperties(LdapProperties.class)
public class LdapConfig {

    private final LdapProperties ldapProperties;

    public LdapConfig(LdapProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
    }

    /**
     * Spring Security LDAP context source — used for bind authentication and searches.
     * Credentials are the manager/service-account DN and password.
     */
    @Bean
    public DefaultSpringSecurityContextSource ldapContextSource() {
        String url = ldapProperties.getUrl();
        if (url != null && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        DefaultSpringSecurityContextSource contextSource =
                new DefaultSpringSecurityContextSource(url + "/" + ldapProperties.getBaseDn());
        if (ldapProperties.getManagerDn() != null && !ldapProperties.getManagerDn().isBlank()) {
            contextSource.setUserDn(ldapProperties.getManagerDn());
            contextSource.setPassword(ldapProperties.getManagerPassword() != null
                    ? ldapProperties.getManagerPassword() : "");
        }
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    /**
     * Standard LdapContextSource bean — used by LdapTemplate for arbitrary searches.
     * Separate from the security context source so LdapTemplate can be injected anywhere.
     */
    @Bean
    public LdapContextSource ldapContextSourceForTemplate() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapProperties.getUrl());
        contextSource.setBase(ldapProperties.getBaseDn());
        if (ldapProperties.getManagerDn() != null && !ldapProperties.getManagerDn().isBlank()) {
            contextSource.setUserDn(ldapProperties.getManagerDn());
            contextSource.setPassword(ldapProperties.getManagerPassword() != null
                    ? ldapProperties.getManagerPassword() : "");
        }
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    /**
     * LdapTemplate — used by LdapAuthService to fetch user attributes (email, memberOf).
     */
    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(ldapContextSourceForTemplate());
    }

    /**
     * Blocking AuthenticationManager for standard LDAP.
     *
     * Uses BindAuthenticator (tries the user DN pattern) combined with
     * DefaultLdapAuthoritiesPopulator (reads group membership).
     *
     * This bean is used by LdapAuthService.  Because spring-security-ldap is
     * inherently blocking, all calls must be wrapped in
     * Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic()).
     */
    @Bean
    public AuthenticationManager ldapAuthenticationManager() {
        if (ldapProperties.isAdMode()) {
            // --- Active Directory path ---
            ActiveDirectoryLdapAuthenticationProvider adProvider =
                    new ActiveDirectoryLdapAuthenticationProvider(
                            ldapProperties.getAdDomain(),
                            ldapProperties.getAdUrl());
            // AD provider returns groups automatically; no extra populator needed.
            adProvider.setConvertSubErrorCodesToExceptions(true);
            adProvider.setUseAuthenticationRequestCredentials(true);
            return authentication -> adProvider.authenticate(authentication);
        }

        // --- Standard LDAP path ---
        DefaultSpringSecurityContextSource ctx = ldapContextSource();

        // Authenticates user by binding with userDnPattern
        BindAuthenticator authenticator = new BindAuthenticator(ctx);
        if (ldapProperties.getUserDnPattern() != null && !ldapProperties.getUserDnPattern().isBlank()) {
            authenticator.setUserDnPatterns(new String[]{ldapProperties.getUserDnPattern()});
        }

        // Optionally add a search filter so the authenticator can also locate users
        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
                ldapProperties.getRelativeDn(ldapProperties.getUserSearchBase()),
                ldapProperties.getUsernameFilter(),
                ctx);
        authenticator.setUserSearch(userSearch);

        // Populate granted authorities from group membership
        DefaultLdapAuthoritiesPopulator authoritiesPopulator =
                new DefaultLdapAuthoritiesPopulator(ctx, ldapProperties.getRelativeDn(ldapProperties.getGroupSearchBase()));
        authoritiesPopulator.setGroupSearchFilter(ldapProperties.getGroupSearchFilter());
        authoritiesPopulator.setRolePrefix(ldapProperties.getGroupPrefix() != null ? ldapProperties.getGroupPrefix() : "");
        authoritiesPopulator.setConvertToUpperCase(true);

        LdapAuthenticationProvider provider =
                new LdapAuthenticationProvider(authenticator, authoritiesPopulator);

        return provider::authenticate;
    }
}
