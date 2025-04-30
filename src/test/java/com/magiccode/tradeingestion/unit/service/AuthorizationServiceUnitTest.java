package com.magiccode.tradeingestion.unit.service;

import com.magiccode.tradeingestion.service.AuthorizationService;
import com.magiccode.tradeingestion.service.AuthorizationResult;
import com.magiccode.tradeingestion.service.AuthorizationContext;
import com.magiccode.tradeingestion.service.Operation;
import com.magiccode.tradeingestion.service.Resource;
import com.magiccode.tradeingestion.service.DefaultAuthorizationService;
import com.magiccode.tradeingestion.model.Deal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DefaultAuthorizationService.
 * This test class verifies the authorization logic and caching behavior of the service.
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceUnitTest {

    @Mock
    private Principal principal;
    
    @Mock
    private Deal testDeal;
    
    private DefaultAuthorizationService authorizationService;
    private AuthorizationContext testContext;

    @BeforeEach
    void setUp() {
        // Initialize test data
        when(testDeal.getDealId()).thenReturn("TEST-DEAL-001");
        
        testContext = AuthorizationContext.builder()
            .resourceId(testDeal.getDealId())
            .classificationLevel("CONFIDENTIAL")
            .jurisdiction("AU")
            .requiredRoles(Set.of("trader", "risk-analyst"))
            .attributes(Map.of("desk", "Rates-Sydney"))
            .build();
            
        // Initialize service
        authorizationService = new DefaultAuthorizationService();
    }

    @Test
    void authorize_Success() {
        // Arrange
        when(principal.getName()).thenReturn("trader_jdoe");

        // Act
        AuthorizationResult result = authorizationService.authorize(
            principal,
            Operation.WRITE,
            Resource.DEAL,
            testContext
        );

        // Assert
        assertTrue(result.isAuthorized());
        assertEquals("Authorized", result.getReason());
    }

    @Test
    void authorize_Unauthorized_NullPrincipal() {
        // Act
        AuthorizationResult result = authorizationService.authorize(
            null,
            Operation.WRITE,
            Resource.DEAL,
            testContext
        );

        // Assert
        assertFalse(result.isAuthorized());
        assertEquals("Invalid principal or context", result.getReason());
    }

    @Test
    void authorize_Unauthorized_NullContext() {
        // Act
        AuthorizationResult result = authorizationService.authorize(
            principal,
            Operation.WRITE,
            Resource.DEAL,
            null
        );

        // Assert
        assertFalse(result.isAuthorized());
        assertEquals("Invalid principal or context", result.getReason());
    }

    @Test
    void authorize_Unauthorized_EmptyUserId() {
        // Arrange
        when(principal.getName()).thenReturn("");

        // Act
        AuthorizationResult result = authorizationService.authorize(
            principal,
            Operation.WRITE,
            Resource.DEAL,
            testContext
        );

        // Assert
        assertFalse(result.isAuthorized());
        assertEquals("Invalid user ID", result.getReason());
    }

    @Test
    void authorize_Unauthorized_NullUserId() {
        // Arrange
        when(principal.getName()).thenReturn(null);

        // Act
        AuthorizationResult result = authorizationService.authorize(
            principal,
            Operation.WRITE,
            Resource.DEAL,
            testContext
        );

        // Assert
        assertFalse(result.isAuthorized());
        assertEquals("Invalid user ID", result.getReason());
    }
} 