package com.resourcemanager.model.resources;

import com.resourcemanager.model.Resource;
import com.resourcemanager.model.ResourceType;

/**
 * Concrete Resource representing a Meeting Room.
 */
public class MeetingRoom extends Resource {
    private final int capacity;
    private final boolean hasProjector;
    private final boolean hasWhiteboard;

    public MeetingRoom(String resourceId, double hourlyRate, int capacity, boolean hasProjector, boolean hasWhiteboard) {
        super(resourceId, ResourceType.MEETING_ROOM, hourlyRate);
        this.capacity = capacity;
        this.hasProjector = hasProjector;
        this.hasWhiteboard = hasWhiteboard;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isHasProjector() {
        return hasProjector;
    }

    public boolean isHasWhiteboard() {
        return hasWhiteboard;
    }

    @Override
    public String getDetails() {
        return String.format("Capacity: %d seats, Projector: %s, Whiteboard: %s",
                capacity, hasProjector ? "Yes" : "No", hasWhiteboard ? "Yes" : "No");
    }
}
