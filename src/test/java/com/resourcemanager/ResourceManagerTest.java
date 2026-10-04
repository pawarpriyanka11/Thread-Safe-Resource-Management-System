package com.resourcemanager;

import com.resourcemanager.exception.InvalidAllocationException;
import com.resourcemanager.exception.ResourceNotFoundException;
import com.resourcemanager.exception.ResourceUnavailableException;
import com.resourcemanager.exception.UserNotFoundException;
import com.resourcemanager.factory.ResourceFactory;
import com.resourcemanager.model.*;
import com.resourcemanager.repository.InMemoryResourceRepository;
import com.resourcemanager.repository.ResourceRepository;
import com.resourcemanager.service.AllocationService;
import com.resourcemanager.service.PaymentService;
import com.resourcemanager.service.ResourceManager;
import com.resourcemanager.strategy.FirstAvailableStrategy;
import com.resourcemanager.strategy.LeastUsedStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive Unit and Multithreaded Concurrency Test Suite.
 */
class ResourceManagerTest {

    private ResourceRepository repository;
    private ResourceManager resourceManager;
    private AllocationService allocationService;
    private User user1;
    private User user2;
    private Resource meetingRoom1;
    private Resource meetingRoom2;

    @BeforeEach
    void setUp() {
        repository = new InMemoryResourceRepository();
        resourceManager = new ResourceManager(repository, new FirstAvailableStrategy());
        allocationService = new AllocationService(resourceManager, new PaymentService());

        user1 = new User("U101", "Alice Smith", "alice@example.com");
        user2 = new User("U102", "Bob Jones", "bob@example.com");

        resourceManager.registerUser(user1);
        resourceManager.registerUser(user2);

        meetingRoom1 = ResourceFactory.createMeetingRoom("MR-101", 50.0, 10, true, true);
        meetingRoom2 = ResourceFactory.createMeetingRoom("MR-102", 75.0, 20, true, true);

        resourceManager.registerResource(meetingRoom1);
        resourceManager.registerResource(meetingRoom2);
    }

    @Test
    @DisplayName("1. Successful Resource Allocation")
    void testSuccessfulAllocation() {
        ResourceAllocation allocation = resourceManager.allocateResource("U101", "MR-101");

        assertNotNull(allocation);
        assertEquals("U101", allocation.getUser().getUserId());
        assertEquals("MR-101", allocation.getResource().getResourceId());
        assertEquals(ResourceStatus.ALLOCATED, meetingRoom1.getStatus());
        assertEquals(AllocationStatus.ACTIVE, allocation.getAllocationStatus());
    }

    @Test
    @DisplayName("2. Resource Release")
    void testResourceRelease() {
        ResourceAllocation allocation = resourceManager.allocateResource("U101", "MR-101");
        assertEquals(ResourceStatus.ALLOCATED, meetingRoom1.getStatus());

        ResourceAllocation released = resourceManager.releaseResource(allocation.getAllocationId());

        assertNotNull(released);
        assertEquals(ResourceStatus.AVAILABLE, meetingRoom1.getStatus());
        assertEquals(AllocationStatus.COMPLETED, released.getAllocationStatus());
        assertNotNull(released.getEndTime());
    }

    @Test
    @DisplayName("3. Resource Unavailable Exception on Double Allocation")
    void testResourceUnavailable() {
        resourceManager.allocateResource("U101", "MR-101");

        assertThrows(ResourceUnavailableException.class, () -> {
            resourceManager.allocateResource("U102", "MR-101");
        });
    }

    @Test
    @DisplayName("4. Invalid User Exception")
    void testInvalidUser() {
        assertThrows(UserNotFoundException.class, () -> {
            resourceManager.allocateResource("NON_EXISTENT_USER", "MR-101");
        });
    }

    @Test
    @DisplayName("5. Invalid Resource Exception")
    void testInvalidResource() {
        assertThrows(ResourceNotFoundException.class, () -> {
            resourceManager.allocateResource("U101", "NON_EXISTENT_ROOM");
        });
    }

    @Test
    @DisplayName("6. Multithreaded Concurrency Test - Multiple Users Requesting Same Resource")
    void testConcurrentAllocationSameResourceOnlyOneSucceeds() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CyclicBarrier barrier = new CyclicBarrier(numberOfThreads); // Synchronizes thread start time
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Register 10 test users
        for (int i = 1; i <= numberOfThreads; i++) {
            resourceManager.registerUser(new User("USER_" + i, "Test User " + i, "user" + i + "@test.com"));
        }

        for (int i = 1; i <= numberOfThreads; i++) {
            final String uId = "USER_" + i;
            executor.submit(() -> {
                try {
                    barrier.await(); // Wait until all 10 threads are ready
                    resourceManager.allocateResource(uId, "MR-101");
                    successCount.incrementAndGet();
                } catch (ResourceUnavailableException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All threads should complete within 5 seconds.");
        assertEquals(1, successCount.get(), "EXACTLY ONE thread must successfully allocate the resource!");
        assertEquals(9, failureCount.get(), "EXACTLY N-1 threads must fail due to ResourceUnavailableException!");
        assertEquals(ResourceStatus.ALLOCATED, meetingRoom1.getStatus());
    }

    @Test
    @DisplayName("7. Concurrent Allocation Across Multiple Resources")
    void testConcurrentAllocationMultipleResources() throws InterruptedException {
        int threadsPerRoom = 10;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(20);

        for (int i = 1; i <= 20; i++) {
            resourceManager.registerUser(new User("CONC_USER_" + i, "User " + i, "u@test.com"));
        }

        AtomicInteger room1Allocations = new AtomicInteger(0);
        AtomicInteger room2Allocations = new AtomicInteger(0);

        for (int i = 1; i <= 10; i++) {
            final String uid = "CONC_USER_" + i;
            executor.submit(() -> {
                try {
                    resourceManager.allocateResource(uid, "MR-101");
                    room1Allocations.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        for (int i = 11; i <= 20; i++) {
            final String uid = "CONC_USER_" + i;
            executor.submit(() -> {
                try {
                    resourceManager.allocateResource(uid, "MR-102");
                    room2Allocations.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(1, room1Allocations.get(), "Room 1 should be allocated exactly once.");
        assertEquals(1, room2Allocations.get(), "Room 2 should be allocated exactly once.");
    }

    @Test
    @DisplayName("8. Switching Allocation Strategies (FirstAvailable vs LeastUsed)")
    void testSwitchingAllocationStrategies() {
        // Register computer resources with different starting usage counts
        Resource comp1 = ResourceFactory.createComputer("C-1", 10.0, "Intel i7", 16, "Ubuntu");
        Resource comp2 = ResourceFactory.createComputer("C-2", 10.0, "Intel i9", 32, "Ubuntu");

        resourceManager.registerResource(comp1);
        resourceManager.registerResource(comp2);

        // Strategy 1: First Available
        resourceManager.setAllocationStrategy(new FirstAvailableStrategy());
        ResourceAllocation alloc1 = resourceManager.allocateAvailableResource("U101", ResourceType.COMPUTER);
        assertEquals("C-1", alloc1.getResource().getResourceId());
        resourceManager.releaseResource(alloc1.getAllocationId());

        // Allocate C-1 again to increase its usage count to 2
        ResourceAllocation alloc2 = resourceManager.allocateAvailableResource("U101", ResourceType.COMPUTER);
        assertEquals("C-1", alloc2.getResource().getResourceId());
        resourceManager.releaseResource(alloc2.getAllocationId());

        // C-1 usageCount = 2, C-2 usageCount = 0

        // Strategy 2: Switch to LeastUsedStrategy
        resourceManager.setAllocationStrategy(new LeastUsedStrategy());

        ResourceAllocation allocLeastUsed = resourceManager.allocateAvailableResource("U102", ResourceType.COMPUTER);
        assertEquals("C-2", allocLeastUsed.getResource().getResourceId(), "LeastUsedStrategy must pick C-2 because it has lower usage count than C-1!");
    }

    @Test
    @DisplayName("9. Invalid Release of Unallocated Resource")
    void testInvalidRelease() {
        assertThrows(InvalidAllocationException.class, () -> {
            resourceManager.releaseResource("NON_EXISTENT_ALLOCATION_ID");
        });
    }
}
