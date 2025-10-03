package com.matching.cloth.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "colorTheory")
public class ColorTheory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int colorTheoryId;

    @Column(name = "colorTheoryName", nullable = false, length = 20)
    private String colorTheoryName;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    public ColorTheory() {
        super();
    }

    public ColorTheory(int colorTheoryId, String colorTheoryName, String description) {
        super();
        this.colorTheoryId = colorTheoryId;
        this.colorTheoryName = colorTheoryName;
        this.description = description;
    }

    public int getColorTheoryId() {
        return colorTheoryId;
    }

    public void setColorTheoryId(int colorTheoryId) {
        this.colorTheoryId = colorTheoryId;
    }

    public String getColorTheoryName() {
        return colorTheoryName;
    }

    public void setColorTheoryName(String colorTheoryName) {
        this.colorTheoryName = colorTheoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
