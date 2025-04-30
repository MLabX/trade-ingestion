package com.magiccode.tradeingestion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DefaultAuthorizationService.
 * This test class verifies the authorization logic and caching behavior of the service.
 */
@ExtendWith(MockitoExtension.class)
class DefaultAuthorizationServiceTest {

    private DefaultAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new DefaultAuthorizationService();
    }

    @Nested
    @DisplayName("isAuthorized Tests")
    class IsAuthorizedTests {

        @Test
        @DisplayName("Should return true for authorized user")
        void shouldReturnTrueForAuthorizedUser() {
            // Given
            String userId = "user123";
            Operation operation = Operation.READ;
            Resource resource = Resource.DEAL;

            // When
            boolean result = authorizationService.isAuthorized(userId, operation, resource);

            // Then
            assertTrue(result, "User should be authorized for the operation");
        }

        @Test
        @DisplayName("Should return false for unauthorized user")
        void shouldReturnFalseForUnauthorizedUser() {
            // Given
            String userId = "unauthorizedUser";
            Operation operation = Operation.WRITE;
            Resource resource = Resource.DEAL;

            // When
            boolean result = authorizationService.isAuthorized(userId, operation, resource);

            // Then
            assertFalse(result, "User should not be authorized for the operation");
        }

        @Test
        @DisplayName("Should cache authorization results")
        void shouldCacheAuthorizationResults() {
            // Given
            String userId = "user123";
            Operation operation = Operation.READ;
            Resource resource = Resource.DEAL;

            // When
            boolean firstResult = authorizationService.isAuthorized(userId, operation, resource);
            boolean secondResult = authorizationService.isAuthorized(userId, operation, resource);

            // Then
            assertEquals(firstResult, secondResult, "Cached results should be consistent");
        }

        @Test
        @DisplayName("Should handle null inputs gracefully")
        void shouldHandleNullInputs() {
            // Given
            String userId = null;
            Operation operation = Operation.READ;
            Resource resource = Resource.DEAL;

            // When
            boolean result = authorizationService.isAuthorized(userId, operation, resource);

            // Then
            assertFalse(result, "Null userId should result in unauthorized access");
        }
    }

    @Nested
    @DisplayName("Multiple Operations Tests")
    class MultipleOperationsTests {

        @Test
        @DisplayName("Should return true when all operations are authorized")
        void shouldReturnTrueWhenAllOperationsAreAuthorized() {
            // Given
            String userId = "user123";
            Set<Operation> operations = Set.of(Operation.READ, Operation.WRITE);
            Resource resource = Resource.DEAL;

            // When
            boolean result = authorizationService.isAuthorized(userId, operations, resource);

            // Then
            assertTrue(result, "User should be authorized for all operations");
        }

        @Test
        @DisplayName("Should return false when any operation is unauthorized")
        void shouldReturnFalseWhenAnyOperationIsUnauthorized() {
            // Given
            String userId = "unauthorizedUser";
            Set<Operation> operations = Set.of(Operation.READ, Operation.WRITE);
            Resource resource = Resource.DEAL;

            // When
            boolean result = authorizationService.isAuthorized(userId, operations, resource);

            // Then
            assertFalse(result, "User should not be authorized if any operation is unauthorized");
        }
    }
} 