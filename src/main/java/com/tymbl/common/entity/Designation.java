package com.tymbl.common.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "designations")
public class Designation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false, unique = true)
    private String title;
    
    private String description;
    
    private Integer level;
    
    public Designation(String title) {
        this.title = title;
    }
    
    public Designation(String title, String description, Integer level) {
        this.title = title;
        this.description = description;
        this.level = level;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Designation that = (Designation) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}