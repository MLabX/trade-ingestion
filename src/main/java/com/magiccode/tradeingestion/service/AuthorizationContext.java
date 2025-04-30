package com.magiccode.tradeingestion.service;

import java.util.Map;
import java.util.Set;

/**
 * Represents the context for an authorization decision.
 * Contains information about the resource, its classification level,
 * jurisdiction, required roles, and additional attributes.
 */
public class AuthorizationContext {
    private final String resourceId;
    private final String classificationLevel;
    private final String jurisdiction;
    private final Set<String> requiredRoles;
    private final Map<String, String> attributes;

    private AuthorizationContext(Builder builder) {
        this.resourceId = builder.resourceId;
        this.classificationLevel = builder.classificationLevel;
        this.jurisdiction = builder.jurisdiction;
        this.requiredRoles = builder.requiredRoles;
        this.attributes = builder.attributes;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getClassificationLevel() {
        return classificationLevel;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public Set<String> getRequiredRoles() {
        return requiredRoles;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Builder class for AuthorizationContext.
     */
    public static class Builder {
        private String resourceId;
        private String classificationLevel;
        private String jurisdiction;
        private Set<String> requiredRoles;
        private Map<String, String> attributes;

        public Builder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder classificationLevel(String classificationLevel) {
            this.classificationLevel = classificationLevel;
            return this;
        }

        public Builder jurisdiction(String jurisdiction) {
            this.jurisdiction = jurisdiction;
            return this;
        }

        public Builder requiredRoles(Set<String> requiredRoles) {
            this.requiredRoles = requiredRoles;
            return this;
        }

        public Builder attributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public AuthorizationContext build() {
            return new AuthorizationContext(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
} 