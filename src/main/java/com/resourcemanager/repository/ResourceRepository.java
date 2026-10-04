package com.resourcemanager.repository;

import com.resourcemanager.model.Resource;
import com.resourcemanager.model.ResourceAllocation;
import com.resourcemanager.model.ResourceType;
import com.resourcemanager.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction separating persistent data storage from business logic (DIP & SRP).
 */
public interface ResourceRepository {
    void saveResource(Resource resource);
    Optional<Resource> findResourceById(String resourceId);
    boolean removeResource(String resourceId);
    List<Resource> findAllResources();
    List<Resource> findResourcesByType(ResourceType type);
    List<Resource> findAvailableResourcesByType(ResourceType type);

    void saveUser(User user);
    Optional<User> findUserById(String userId);
    List<User> findAllUsers();

    void saveAllocation(ResourceAllocation allocation);
    Optional<ResourceAllocation> findAllocationById(String allocationId);
    Optional<ResourceAllocation> findActiveAllocationByResource(String resourceId);
    List<ResourceAllocation> findAllAllocations();
}
