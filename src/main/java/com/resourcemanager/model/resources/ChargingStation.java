package com.resourcemanager.model.resources;

import com.resourcemanager.model.Resource;
import com.resourcemanager.model.ResourceType;

/**
 * Concrete Resource representing an EV / Device Charging Station.
 */
public class ChargingStation extends Resource {
    private final double maxOutputKW;
    private final String connectorType;

    public ChargingStation(String resourceId, double hourlyRate, double maxOutputKW, String connectorType) {
        super(resourceId, ResourceType.CHARGING_STATION, hourlyRate);
        this.maxOutputKW = maxOutputKW;
        this.connectorType = connectorType;
    }

    public double getMaxOutputKW() {
        return maxOutputKW;
    }

    public String getConnectorType() {
        return connectorType;
    }

    @Override
    public String getDetails() {
        return String.format("Power: %.1f kW, Connector: %s", maxOutputKW, connectorType);
    }
}
