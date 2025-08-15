package com.tymbl.jobs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Factory service that manages multiple portal crawling implementations.
 * Routes requests to the appropriate portal service based on portal name.
 */
@Service
public class PortalCrawlingFactory {
    
    @Autowired
    private List<PortalCrawlingService> portalServices;
    
    /**
     * Get the appropriate portal service for the given portal name
     * @param portalName The name of the portal
     * @return PortalCrawlingService implementation for the portal
     * @throws IllegalArgumentException if no service can handle the portal
     */
    public PortalCrawlingService getPortalService(String portalName) {
        for (PortalCrawlingService service : portalServices) {
            if (service.canHandlePortal(portalName)) {
                return service;
            }
        }
        throw new IllegalArgumentException("No portal service found for portal: " + portalName);
    }
    
    /**
     * Check if a portal is supported
     * @param portalName The name of the portal
     * @return true if the portal is supported, false otherwise
     */
    public boolean isPortalSupported(String portalName) {
        return portalServices.stream()
            .anyMatch(service -> service.canHandlePortal(portalName));
    }
    
    /**
     * Get all supported portal names
     * @return List of supported portal names
     */
    public List<String> getSupportedPortals() {
        return portalServices.stream()
            .map(service -> {
                if (service instanceof com.tymbl.jobs.service.impl.FounditPortalCrawlingService) {
                    return "foundit";
                } else if (service instanceof com.tymbl.jobs.service.impl.LinkedInPortalCrawlingService) {
                    return "linkedin";
                }
                // Add more portal mappings as they are implemented
                return "unknown";
            })
            .distinct()
            .collect(java.util.stream.Collectors.toList());
    }
}
