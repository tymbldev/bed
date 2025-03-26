package com.tymbl.common.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "countries")
public class Country {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false, unique = true, length = 2)
    private String code;
    
    private String phoneCode;
    
    public Country(String name, String code) {
        this.name = name;
        this.code = code;
    }
    
    public Country(String name, String code, String phoneCode) {
        this.name = name;
        this.code = code;
        this.phoneCode = phoneCode;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return Objects.equals(id, country.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 