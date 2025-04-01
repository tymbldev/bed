package com.tymbl.common.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "locations")
public class Location {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "City is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;
    
    @NotNull(message = "Country is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;
    
    private String zipCode;
    
    // For dropdown display - calculated from city, country
    @Column(nullable = false)
    private String displayName;
    
    // For remote or global locations
    private boolean isRemote;
    
    // Specific address details - optional
    private String addressLine1;
    private String addressLine2;
    
    public Location(Country country, boolean isRemote) {
        this.country = country;
        this.isRemote = isRemote;
        updateDisplayName();
    }
    
    public Location(City city, Country country) {
        this.city = city;
        this.country = country;
        updateDisplayName();
    }
    
    public Location(City city, Country country, String zipCode) {
        this.city = city;
        this.country = country;
        this.zipCode = zipCode;
        updateDisplayName();
    }
    
    @PrePersist
    @PreUpdate
    private void updateDisplayName() {
        if (isRemote) {
            this.displayName = "Remote - " + (country != null ? country.getName() : "Global");
            return;
        }
        
        StringBuilder display = new StringBuilder();
        
        if (city != null) {
            display.append(city.getName());
        }
        
        if (country != null) {
            if (display.length() > 0) {
                display.append(", ");
            }
            display.append(country.getName());
        }
        
        if (zipCode != null && !zipCode.isEmpty()) {
            display.append(" ").append(zipCode);
        }
        
        this.displayName = display.length() > 0 ? display.toString() : "Unknown Location";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location that = (Location) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getLocationDisplay() {
        if (city == null || country == null) {
            return null;
        }
        StringBuilder display = new StringBuilder();
        display.append(city.getName());
        display.append(", ");
        display.append(country.getName());
        if (zipCode != null && !zipCode.trim().isEmpty()) {
            display.append(" ");
            display.append(zipCode);
        }
        return display.toString();
    }
} 