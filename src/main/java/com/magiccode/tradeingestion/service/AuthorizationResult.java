package com.magiccode.tradeingestion.service;

/**
 * Represents the result of an authorization decision.
 * Contains the authorization outcome and an optional reason.
 */
public class AuthorizationResult {
    private final boolean authorized;
    private final String reason;

    public AuthorizationResult(boolean authorized, String reason) {
        this.authorized = authorized;
        this.reason = reason;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public String getReason() {
        return reason;
    }
} 