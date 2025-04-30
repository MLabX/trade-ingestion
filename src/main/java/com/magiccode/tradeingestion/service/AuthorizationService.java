package com.magiccode.tradeingestion.service;

import java.security.Principal;
import java.util.Set;

/**
 * Service responsible for checking if a user is authorized to perform an operation on a resource.
 * This interface defines the contract for authorization checks in the system.
 */
public interface AuthorizationService {
    /**
     * Checks if a user is authorized to perform an operation on a resource.
     *
     * @param userId The ID of the user requesting authorization
     * @param operation The operation being requested
     * @param resource The resource the operation is being performed on
     * @return true if the user is authorized, false otherwise
     */
    boolean isAuthorized(String userId, Operation operation, Resource resource);

    /**
     * Checks if a user is authorized to perform multiple operations on a resource.
     * This is useful for batch operations or when multiple permissions need to be checked at once.
     *
     * @param userId The ID of the user requesting authorization
     * @param operations The set of operations being requested
     * @param resource The resource the operations are being performed on
     * @return true if the user is authorized for all operations, false otherwise
     */
    boolean isAuthorized(String userId, Set<Operation> operations, Resource resource);

    /**
     * Checks if a user is authorized to perform an operation on a resource with additional context.
     * This method provides more detailed authorization checks based on the context of the request.
     *
     * @param principal The principal representing the user
     * @param operation The operation being requested
     * @param resource The resource the operation is being performed on
     * @param context The authorization context containing additional information
     * @return An AuthorizationResult containing the decision and reason
     */
    AuthorizationResult authorize(Principal principal, Operation operation, Resource resource, AuthorizationContext context);
} 