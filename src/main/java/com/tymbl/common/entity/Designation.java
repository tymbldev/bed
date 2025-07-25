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
    @Column(nullable = true, unique = true)
    private String name;
    
    @Column(nullable = true)
    private Integer level;
    
    @Column(nullable = true)
    private boolean enabled = true;
    
    @Column(name = "similar_designations_by_name", columnDefinition = "TEXT")
    private String similarDesignationsByName;
    
    @Column(name = "similar_designations_by_id", columnDefinition = "TEXT")
    private String similarDesignationsById;
    
    @Column(name = "similar_designations_processed", nullable = false)
    private boolean similarDesignationsProcessed = false;
    
    @Column(name = "processed_name")
    private String processedName;
    
    @Column(name = "processed_name_generated", nullable = false)
    private boolean processedNameGenerated = false;
    
    public Designation(String title) {
        this.name = title;
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