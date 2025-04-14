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
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "state")
    private String state;
    
    @Column(name = "country")
    private String country;
    
    @Column(name = "zip_code")
    private String zipCode;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "city_id")
    private Long cityId;
    
    @Column(name = "country_id")
    private Long countryId;
    
    // For remote or global locations
    private boolean isRemote;
    
    // Specific address details - optional
    private String addressLine1;
    private String addressLine2;
    
    public Location(Country country, boolean isRemote) {
        this.country = country.getName();
        this.countryId = country.getId();
        this.isRemote = isRemote;
        updateDisplayName();
    }
    
    public Location(City city, Country country) {
        this.city = city.getName();
        this.country = country.getName();
        this.cityId = city.getId();
        this.countryId = country.getId();
        updateDisplayName();
    }
    
    public Location(City city, Country country, String zipCode) {
        this.city = city.getName();
        this.country = country.getName();
        this.zipCode = zipCode;
        this.cityId = city.getId();
        this.countryId = country.getId();
        updateDisplayName();
    }
    
    @PrePersist
    @PreUpdate
    private void updateDisplayName() {
        if (isRemote) {
            this.displayName = "Remote - " + (country != null ? country : "Global");
            return;
        }
        
        StringBuilder display = new StringBuilder();
        
        if (city != null) {
            display.append(city);
        }
        
        if (country != null) {
            if (display.length() > 0) {
                display.append(", ");
            }
            display.append(country);
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
        display.append(city);
        display.append(", ");
        display.append(country);
        if (zipCode != null && !zipCode.trim().isEmpty()) {
            display.append(" ");
            display.append(zipCode);
        }
        return display.toString();
    }
} 