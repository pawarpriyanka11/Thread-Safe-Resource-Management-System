package com.resourcemanager.model.resources;

import com.resourcemanager.model.Resource;
import com.resourcemanager.model.ResourceType;

/**
 * Concrete Resource representing a Computer workstation/server.
 */
public class Computer extends Resource {
    private final String processor;
    private final int ramGB;
    private final String os;

    public Computer(String resourceId, double hourlyRate, String processor, int ramGB, String os) {
        super(resourceId, ResourceType.COMPUTER, hourlyRate);
        this.processor = processor;
        this.ramGB = ramGB;
        this.os = os;
    }

    public String getProcessor() {
        return processor;
    }

    public int getRamGB() {
        return ramGB;
    }

    public String getOs() {
        return os;
    }

    @Override
    public String getDetails() {
        return String.format("Processor: %s, RAM: %dGB, OS: %s", processor, ramGB, os);
    }
}
