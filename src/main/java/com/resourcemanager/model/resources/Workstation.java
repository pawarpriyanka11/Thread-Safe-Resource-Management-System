package com.resourcemanager.model.resources;

import com.resourcemanager.model.Resource;
import com.resourcemanager.model.ResourceType;

/**
 * Concrete Resource representing an Office Desk/Workstation.
 */
public class Workstation extends Resource {
    private final int monitorCount;
    private final boolean standingDesk;

    public Workstation(String resourceId, double hourlyRate, int monitorCount, boolean standingDesk) {
        super(resourceId, ResourceType.WORKSTATION, hourlyRate);
        this.monitorCount = monitorCount;
        this.standingDesk = standingDesk;
    }

    public int getMonitorCount() {
        return monitorCount;
    }

    public boolean isStandingDesk() {
        return standingDesk;
    }

    @Override
    public String getDetails() {
        return String.format("Monitors: %d, Standing Desk: %s", monitorCount, standingDesk ? "Yes" : "No");
    }
}
