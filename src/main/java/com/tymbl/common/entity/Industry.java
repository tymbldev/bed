package com.tymbl.common.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "industries")
public class Industry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;
    
    private String description;
    
    public Industry(String name) {
        this.name = name;
    }
    
    public Industry(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Industry that = (Industry) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 