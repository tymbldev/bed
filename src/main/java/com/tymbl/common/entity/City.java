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
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;
    
    private String zipCode;
    
    public City(String name, Country country) {
        this.name = name;
        this.country = country;
    }
    
    public City(String name, Country country, String zipCode) {
        this.name = name;
        this.country = country;
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
        
        if (country != null) {
            display.append(", ").append(country.getName());
        }
        
        if (zipCode != null && !zipCode.isEmpty()) {
            display.append(" ").append(zipCode);
        }
        
        return display.toString();
    }
} 