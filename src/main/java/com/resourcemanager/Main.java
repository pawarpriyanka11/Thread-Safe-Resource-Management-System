package com.resourcemanager;

import com.resourcemanager.factory.ResourceFactory;
import com.resourcemanager.model.*;
import com.resourcemanager.observer.AuditLoggerObserver;
import com.resourcemanager.repository.InMemoryResourceRepository;
import com.resourcemanager.repository.ResourceRepository;
import com.resourcemanager.service.AllocationService;
import com.resourcemanager.service.PaymentService;
import com.resourcemanager.service.ResourceManager;
import com.resourcemanager.strategy.AllocationStrategy;
import com.resourcemanager.strategy.FirstAvailableStrategy;
import com.resourcemanager.strategy.LeastUsedStrategy;
import com.resourcemanager.strategy.RandomAvailableStrategy;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Command-Line Interface (CLI) application and live concurrency test runner.
 */
public class Main {
    private static ResourceRepository repository;
    private static ResourceManager resourceManager;
    private static AllocationService allocationService;

    public static void main(String[] args) {
        initSystem();
        seedData();

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        printHeader();

        while (!exit) {
            printMenu();
            System.out.print("Enter choice [1-9]: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> registerUser(scanner);
                case "2" -> addResource(scanner);
                case "3" -> viewResources();
                case "4" -> changeAllocationStrategy(scanner);
                case "5" -> allocateResource(scanner);
                case "6" -> releaseResource(scanner);
                case "7" -> viewAllocations();
                case "8" -> runLiveConcurrencySimulation();
                case "9" -> {
                    exit = true;
                    System.out.println("\nThank you for using Thread-Safe Resource Management System. Goodbye!");
                }
                default -> System.out.println("Invalid choice! Please select an option between 1 and 9.");
            }
            System.out.println();
        }
        scanner.close();
    }

    private static void initSystem() {
        repository = new InMemoryResourceRepository();
        resourceManager = new ResourceManager(repository, new FirstAvailableStrategy());
        resourceManager.addObserver(new AuditLoggerObserver());
        allocationService = new AllocationService(resourceManager, new PaymentService());
    }

    private static void seedData() {
        // Seed default users
        resourceManager.registerUser(new User("U101", "Alice Johnson", "alice@example.com"));
        resourceManager.registerUser(new User("U102", "Bob Smith", "bob@example.com"));
        resourceManager.registerUser(new User("U103", "Charlie Davis", "charlie@example.com"));

        // Seed default resources
        resourceManager.registerResource(ResourceFactory.createMeetingRoom("MR-201", 45.0, 8, true, true));
        resourceManager.registerResource(ResourceFactory.createMeetingRoom("MR-202", 70.0, 16, true, false));

        resourceManager.registerResource(ResourceFactory.createParkingSpace("PS-101", 15.0, "A-12", true, true));
        resourceManager.registerResource(ResourceFactory.createParkingSpace("PS-102", 10.0, "B-05", false, false));

        resourceManager.registerResource(ResourceFactory.createComputer("WS-301", 25.0, "Intel i9-13900K", 64, "Linux Ubuntu 22.04"));
        resourceManager.registerResource(ResourceFactory.createComputer("WS-302", 20.0, "Apple M3 Max", 32, "macOS Sonoma"));

        resourceManager.registerResource(ResourceFactory.createWorkstation("DESK-01", 12.0, 2, true));
        resourceManager.registerResource(ResourceFactory.createChargingStation("EV-FAST-01", 30.0, 150.0, "CCS Combo 2"));
    }

    private static void printHeader() {
        System.out.println("================================================================");
        System.out.println("   THREAD-SAFE RESOURCE MANAGEMENT SYSTEM (LLD Project)         ");
        System.out.println("================================================================");
    }

    private static void printMenu() {
        System.out.println("----------------------------------------------------------------");
        System.out.println("1. Register User");
        System.out.println("2. Add Resource");
        System.out.println("3. View Resources & Status");
        System.out.println("4. Switch Allocation Strategy (Current: " + resourceManager.getAllocationStrategy().getStrategyName() + ")");
        System.out.println("5. Allocate Resource");
        System.out.println("6. Release Resource");
        System.out.println("7. View Active & Past Allocations");
        System.out.println("8. Run Live Multithreaded Concurrency Stress Test");
        System.out.println("9. Exit");
        System.out.println("----------------------------------------------------------------");
    }

    private static void registerUser(Scanner scanner) {
        System.out.print("Enter User ID (e.g. U104): ");
        String userId = scanner.nextLine().trim();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();

        try {
            User user = new User(userId, name, email);
            resourceManager.registerUser(user);
            System.out.println("SUCCESS: User registered -> " + user);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void addResource(Scanner scanner) {
        System.out.println("Select Resource Type:");
        System.out.println("1. Meeting Room");
        System.out.println("2. Parking Space");
        System.out.println("3. Computer");
        System.out.println("4. Workstation");
        System.out.println("5. Charging Station");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        System.out.print("Enter Resource ID (e.g. MR-203): ");
        String id = scanner.nextLine().trim();
        System.out.print("Enter Hourly Rate ($): ");
        double rate = Double.parseDouble(scanner.nextLine().trim());

        Resource resource = switch (choice) {
            case "1" -> ResourceFactory.createMeetingRoom(id, rate, 12, true, true);
            case "2" -> ResourceFactory.createParkingSpace(id, rate, "C-01", true, true);
            case "3" -> ResourceFactory.createComputer(id, rate, "AMD Ryzen 9", 32, "Windows 11");
            case "4" -> ResourceFactory.createWorkstation(id, rate, 2, true);
            case "5" -> ResourceFactory.createChargingStation(id, rate, 50.0, "Type 2");
            default -> null;
        };

        if (resource != null) {
            resourceManager.registerResource(resource);
            System.out.println("SUCCESS: Resource registered -> " + resource);
        } else {
            System.out.println("ERROR: Invalid resource type selected.");
        }
    }

    private static void viewResources() {
        List<Resource> resources = repository.findAllResources();
        System.out.println("\n----------------------------- REGISTERED RESOURCES -----------------------------");
        System.out.printf("%-10s | %-16s | %-12s | %-10s | %-10s | %s%n",
                "ID", "Type", "Status", "Rate ($/h)", "Usage", "Details");
        System.out.println("--------------------------------------------------------------------------------");
        for (Resource r : resources) {
            System.out.printf("%-10s | %-16s | %-12s | $%-9.2f | %-10d | %s%n",
                    r.getResourceId(), r.getResourceType(), r.getStatus(), r.getHourlyRate(), r.getUsageCount(), r.getDetails());
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void changeAllocationStrategy(Scanner scanner) {
        System.out.println("\nSelect Allocation Strategy:");
        System.out.println("1. First Available Strategy (Fastest)");
        System.out.println("2. Least Used Strategy (Promotes Wear-and-Tear Load Balancing)");
        System.out.println("3. Random Available Strategy");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        AllocationStrategy strategy = switch (choice) {
            case "1" -> new FirstAvailableStrategy();
            case "2" -> new LeastUsedStrategy();
            case "3" -> new RandomAvailableStrategy();
            default -> null;
        };

        if (strategy != null) {
            resourceManager.setAllocationStrategy(strategy);
            System.out.println("SUCCESS: Allocation strategy changed to -> " + strategy.getStrategyName());
        } else {
            System.out.println("ERROR: Invalid strategy selected.");
        }
    }

    private static void allocateResource(Scanner scanner) {
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine().trim();

        System.out.println("Mode: 1. Auto-select by Resource Type | 2. Specify Exact Resource ID");
        System.out.print("Choice: ");
        String mode = scanner.nextLine().trim();

        try {
            if ("1".equals(mode)) {
                System.out.println("Available Resource Types:");
                for (ResourceType t : ResourceType.values()) {
                    System.out.println("- " + t.name());
                }
                System.out.print("Enter Resource Type Name: ");
                String typeStr = scanner.nextLine().trim().toUpperCase();
                ResourceType type = ResourceType.valueOf(typeStr);

                ResourceAllocation allocation = allocationService.allocateAvailableResource(userId, type);
                System.out.println("SUCCESS: Resource Allocated -> " + allocation);
            } else if ("2".equals(mode)) {
                System.out.print("Enter Resource ID: ");
                String resourceId = scanner.nextLine().trim();

                ResourceAllocation allocation = allocationService.allocateSpecificResource(userId, resourceId);
                System.out.println("SUCCESS: Resource Allocated -> " + allocation);
            } else {
                System.out.println("ERROR: Invalid mode selected.");
            }
        } catch (Exception e) {
            System.out.println("ALLOCATION FAILED: " + e.getMessage());
        }
    }

    private static void releaseResource(Scanner scanner) {
        System.out.print("Enter Allocation ID to Release: ");
        String allocId = scanner.nextLine().trim();

        try {
            ResourceAllocation allocation = allocationService.releaseResource(allocId);
            System.out.println("SUCCESS: Resource Released -> " + allocation);
        } catch (Exception e) {
            System.out.println("RELEASE FAILED: " + e.getMessage());
        }
    }

    private static void viewAllocations() {
        List<ResourceAllocation> allocations = repository.findAllAllocations();
        System.out.println("\n------------------------------ SYSTEM ALLOCATIONS ------------------------------");
        System.out.printf("%-12s | %-8s | %-10s | %-10s | %-20s | %-10s%n",
                "Alloc ID", "User ID", "Res ID", "Status", "Start Time", "Cost ($)");
        System.out.println("--------------------------------------------------------------------------------");
        for (ResourceAllocation a : allocations) {
            System.out.printf("%-12s | %-8s | %-10s | %-10s | %-20s | $%-9.2f%n",
                    a.getAllocationId(), a.getUser().getUserId(), a.getResource().getResourceId(),
                    a.getAllocationStatus(), a.getStartTime().toLocalTime().toString().substring(0, 8), a.getTotalCost());
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void runLiveConcurrencySimulation() {
        System.out.println("\n================================================================");
        System.out.println("  STARTING MULTITHREADED CONCURRENCY STRESS TEST                ");
        System.out.println("  Scenario: 10 Parallel Threads Requesting SAME Resource (MR-201)");
        System.out.println("================================================================");

        // Ensure MR-201 is available
        var mrOpt = repository.findResourceById("MR-201");
        if (mrOpt.isPresent() && !mrOpt.get().isAvailable()) {
            var activeAlloc = repository.findActiveAllocationByResource("MR-201");
            activeAlloc.ifPresent(a -> resourceManager.releaseResource(a.getAllocationId()));
        }

        int totalThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CyclicBarrier barrier = new CyclicBarrier(totalThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= totalThreads; i++) {
            final String uId = "U10" + ((i % 3) + 1); // Alternates users U101, U102, U103
            final int threadNum = i;

            executor.submit(() -> {
                try {
                    System.out.println("Thread-" + threadNum + " ready and waiting at barrier...");
                    barrier.await(); // Synchronizes start time across all 10 threads!

                    resourceManager.allocateResource(uId, "MR-201");
                    System.out.println(">>> [SUCCESS] Thread-" + threadNum + " ACQUIRED lock and allocated MR-201!");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("--- [EXPECTED REJECTION] Thread-" + threadNum + " received: " + e.getMessage());
                    failureCount.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n------------------- CONCURRENCY TEST RESULTS -------------------");
        System.out.println("Total Competing Threads : " + totalThreads);
        System.out.println("Successful Allocations  : " + successCount.get() + " (MUST BE EXACTLY 1)");
        System.out.println("Rejected Requests       : " + failureCount.get() + " (MUST BE EXACTLY 9)");
        System.out.println("Execution Time          : " + elapsedTime + " ms");
        System.out.println("Resource MR-201 Status  : " + repository.findResourceById("MR-201").get().getStatus());
        System.out.println("Race Condition Check    : " + (successCount.get() == 1 ? "PASSED (THREAD SAFE)" : "FAILED (RACE CONDITION DETECTED!)"));
        System.out.println("----------------------------------------------------------------\n");
    }
}
