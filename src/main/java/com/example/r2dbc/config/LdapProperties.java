package com.example.r2dbc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ldap")
public class LdapProperties {

    /** LDAP server URL, e.g. ldap://localhost:389 */
    private String url = "ldap://localhost:389";

    /** Base DN for all LDAP searches, e.g. dc=example,dc=com */
    private String baseDn = "dc=example,dc=com";

    /** Search base alias to support custom searchbase property */
    private String searchbase;

    /**
     * DN pattern for direct user bind authentication.
     * {0} is replaced with the username supplied at login.
     * e.g. uid={0},ou=users
     */
    private String userDnPattern = "uid={0},ou=users";

    /** OU under baseDn to search for group membership, e.g. ou=groups */
    private String groupSearchBase = "ou=groups";

    /**
     * LDAP filter to find groups that contain the user.
     * {0} is replaced with the user's full DN.
     * e.g. (member={0})
     */
    private String groupSearchFilter;

    /** Manager/service-account DN used for searches (bind DN) */
    private String managerDn = "cn=admin,dc=example,dc=com";

    /** Manager/service-account password */
    private String managerPassword;

    // ---- Active Directory variant (optional) ----

    /** AD domain, e.g. example.com — only used when adUrl is set */
    private String adDomain;

    /** AD LDAP URL, e.g. ldap://ad.example.com — enables AD mode when set */
    private String adUrl;

    /**
     * LDAP attribute that holds the user's email address.
     * Standard attribute is "mail"; some AD setups use "userPrincipalName".
     */
    private String mailAttribute = "mail";

    /**
     * LDAP group whose members should receive ROLE_ADMIN.
     * If a user belongs to this group, ROLE_ADMIN is granted alongside ROLE_USER.
     * e.g. cn=admins,ou=groups,dc=example,dc=com
     */
    private String adminGroupDn;

    // ---- New Properties ----

    /** Search filter to find a user by their username (e.g. at login), e.g. (&(mail={0})) */
    private String usernameFilter = "(uid={0})";

    /** Search filter to find a user by their email address, e.g. (&(mail={0})(objectclass=person)) */
    private String emailFilter = "(&(mail={0})(objectclass=person))";

    /** Mapping from LDAP attributes to local User fields, e.g. uid:uid;email:mail;name:givenName,sn */
    private String attributesMapping = "uid:uid;email:mail";

    /** DN of the OU to search for users, relative or absolute, e.g. ou=users */
    private String userSearchBase = "";

    /** The attribute on groups that contains the member's DN, e.g. member */
    private String groupMemberAttribute = "member";

    /** Prefix for LDAP groups when mapped to Spring Security roles, e.g. "ROLE_" */
    private String groupPrefix = "ROLE_";

    // ---- Getters & Setters ----

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getBaseDn() {
        if (searchbase != null && !searchbase.isBlank()) {
            return searchbase;
        }
        return baseDn;
    }
    public void setBaseDn(String baseDn) { this.baseDn = baseDn; }

    public String getSearchbase() { return searchbase; }
    public void setSearchbase(String searchbase) { this.searchbase = searchbase; }

    public String getUserDnPattern() { return userDnPattern; }
    public void setUserDnPattern(String userDnPattern) { this.userDnPattern = userDnPattern; }

    public String getGroupSearchBase() { return groupSearchBase; }
    public void setGroupSearchBase(String groupSearchBase) { this.groupSearchBase = groupSearchBase; }

    public String getGroupSearchFilter() {
        if (groupSearchFilter != null && !groupSearchFilter.isBlank()) {
            return groupSearchFilter;
        }
        return "(" + (groupMemberAttribute != null ? groupMemberAttribute : "member") + "={0})";
    }
    public void setGroupSearchFilter(String groupSearchFilter) { this.groupSearchFilter = groupSearchFilter; }

    public String getManagerDn() { return managerDn; }
    public void setManagerDn(String managerDn) { this.managerDn = managerDn; }

    public String getManagerPassword() { return managerPassword; }
    public void setManagerPassword(String managerPassword) { this.managerPassword = managerPassword; }

    public String getAdDomain() { return adDomain; }
    public void setAdDomain(String adDomain) { this.adDomain = adDomain; }

    public String getAdUrl() { return adUrl; }
    public void setAdUrl(String adUrl) { this.adUrl = adUrl; }

    public String getMailAttribute() { return mailAttribute; }
    public void setMailAttribute(String mailAttribute) { this.mailAttribute = mailAttribute; }

    public String getAdminGroupDn() { return adminGroupDn; }
    public void setAdminGroupDn(String adminGroupDn) { this.adminGroupDn = adminGroupDn; }

    public String getUsernameFilter() { return usernameFilter; }
    public void setUsernameFilter(String usernameFilter) { this.usernameFilter = usernameFilter; }

    public String getEmailFilter() { return emailFilter; }
    public void setEmailFilter(String emailFilter) { this.emailFilter = emailFilter; }

    public String getAttributesMapping() { return attributesMapping; }
    public void setAttributesMapping(String attributesMapping) { this.attributesMapping = attributesMapping; }

    public String getUserSearchBase() { return userSearchBase; }
    public void setUserSearchBase(String userSearchBase) { this.userSearchBase = userSearchBase; }

    public String getGroupMemberAttribute() { return groupMemberAttribute; }
    public void setGroupMemberAttribute(String groupMemberAttribute) { this.groupMemberAttribute = groupMemberAttribute; }

    public String getGroupPrefix() { return groupPrefix; }
    public void setGroupPrefix(String groupPrefix) { this.groupPrefix = groupPrefix; }

    /** Returns true when Active Directory mode is enabled (adUrl is configured). */
    public boolean isAdMode() {
        return adUrl != null && !adUrl.isBlank();
    }

    /**
     * Helper to resolve relative DN by stripping baseDn if it's included as absolute.
     */
    public String getRelativeDn(String fullDn) {
        if (fullDn == null) {
            return "";
        }
        String cleanFull = fullDn.trim().toLowerCase();
        String currentBase = getBaseDn();
        String cleanBase = currentBase != null ? currentBase.trim().toLowerCase() : "";
        if (!cleanBase.isEmpty() && cleanFull.endsWith(cleanBase)) {
            String relative = fullDn.substring(0, fullDn.length() - currentBase.length()).trim();
            if (relative.endsWith(",")) {
                relative = relative.substring(0, relative.length() - 1).trim();
            }
            return relative;
        }
        return fullDn;
    }
}
