package com.tymbl.common.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "cities")
public class City {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "country_id")
    private Long countryId;
    
    @Column(name = "zip_code")
    private String zipCode;
    
    public City(String name, Country country) {
        this.name = name;
        this.countryId = country.getId();
    }
    
    public City(String name, Country country, String zipCode) {
        this.name = name;
        this.countryId = country.getId();
        this.zipCode = zipCode;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return Objects.equals(id, city.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Transient
    public String getDisplayName() {
        StringBuilder display = new StringBuilder(name);
        
        if (countryId != null) {
            Country country = new Country();
            country.setId(countryId);
            display.append(", ").append(country.getName());
        }
        
        if (zipCode != null && !zipCode.isEmpty()) {
            display.append(" ").append(zipCode);
        }
        
        return display.toString();
    }
} 