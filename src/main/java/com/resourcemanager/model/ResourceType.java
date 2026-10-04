package com.resourcemanager.model;

/**
 * Enumeration representing the supported types of resources in the system.
 * Easily extensible by adding new enum values without altering core business logic.
 */
public enum ResourceType {
    MEETING_ROOM("Meeting Room"),
    PARKING_SPACE("Parking Space"),
    COMPUTER("Computer"),
    WORKSTATION("Workstation"),
    CHARGING_STATION("Charging Station");

    private final String displayName;

    ResourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
