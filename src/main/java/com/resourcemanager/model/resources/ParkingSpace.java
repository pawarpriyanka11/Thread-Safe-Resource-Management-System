package com.resourcemanager.model.resources;

import com.resourcemanager.model.Resource;
import com.resourcemanager.model.ResourceType;

/**
 * Concrete Resource representing a Parking Space.
 */
public class ParkingSpace extends Resource {
    private final String spotNumber;
    private final boolean isCovered;
    private final boolean supportsEVCharging;

    public ParkingSpace(String resourceId, double hourlyRate, String spotNumber, boolean isCovered, boolean supportsEVCharging) {
        super(resourceId, ResourceType.PARKING_SPACE, hourlyRate);
        this.spotNumber = spotNumber;
        this.isCovered = isCovered;
        this.supportsEVCharging = supportsEVCharging;
    }

    public String getSpotNumber() {
        return spotNumber;
    }

    public boolean isCovered() {
        return isCovered;
    }

    public boolean isSupportsEVCharging() {
        return supportsEVCharging;
    }

    @Override
    public String getDetails() {
        return String.format("Spot: %s, Covered: %s, EV Charger: %s",
                spotNumber, isCovered ? "Yes" : "No", supportsEVCharging ? "Yes" : "No");
    }
}
