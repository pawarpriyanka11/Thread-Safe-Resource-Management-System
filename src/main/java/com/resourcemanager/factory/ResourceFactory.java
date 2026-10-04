package com.resourcemanager.factory;

import com.resourcemanager.model.Resource;
import com.resourcemanager.model.ResourceType;
import com.resourcemanager.model.resources.*;

/**
 * Factory Design Pattern implementation for creating Resource instances.
 * Decouples resource creation from client usage, adhering to Open/Closed Principle (OCP).
 */
public class ResourceFactory {

    public static Resource createMeetingRoom(String id, double rate, int capacity, boolean projector, boolean whiteboard) {
        return new MeetingRoom(id, rate, capacity, projector, whiteboard);
    }

    public static Resource createParkingSpace(String id, double rate, String spotNumber, boolean covered, boolean evCharging) {
        return new ParkingSpace(id, rate, spotNumber, covered, evCharging);
    }

    public static Resource createComputer(String id, double rate, String processor, int ramGB, String os) {
        return new Computer(id, rate, processor, ramGB, os);
    }

    public static Resource createWorkstation(String id, double rate, int monitorCount, boolean standingDesk) {
        return new Workstation(id, rate, monitorCount, standingDesk);
    }

    public static Resource createChargingStation(String id, double rate, double maxKW, String connectorType) {
        return new ChargingStation(id, rate, maxKW, connectorType);
    }
}
