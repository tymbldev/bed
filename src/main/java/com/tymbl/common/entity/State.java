package com.tymbl.common.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "states")
public class State {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Column(length = 10)
    private String code;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;
    
    public State(String name, Country country) {
        this.name = name;
        this.country = country;
    }
    
    public State(String name, String code, Country country) {
        this.name = name;
        this.code = code;
        this.country = country;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return Objects.equals(id, state.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Transient
    public String getDisplayName() {
        return name + ", " + (country != null ? country.getName() : "");
    }
} 