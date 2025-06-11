package com.dentistdss.reporting.model.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Placeholder JPA entity for database configuration
 * The reporting service primarily uses existing tables from other services
 * and MongoDB for its own metadata storage.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Entity
@Table(name = "reporting_metadata")
public class ReportingEntity {
    
    @Id
    private Long id;
    
    private String name;
    
    // Constructors
    public ReportingEntity() {}
    
    public ReportingEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
