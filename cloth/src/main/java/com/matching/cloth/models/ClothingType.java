package com.matching.cloth.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "clothingType")
public class ClothingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int clothingTypeId;

    @Column(name = "typeName", nullable = false, length = 50)
    private String clothingTypeName;

    @OneToMany(mappedBy = "clothingType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Clothing> clothes = new ArrayList<>();

    public ClothingType() {
        super();
    }

    public ClothingType(int clothingTypeId, String clothingTypeName, List<Clothing> clothes) {
        super();
        this.clothingTypeId = clothingTypeId;
        this.clothingTypeName = clothingTypeName;
        this.clothes = clothes;
    }

    public int getClothingTypeId() {
        return clothingTypeId;
    }

    public void setClothingTypeId(int clothingTypeId) {
        this.clothingTypeId = clothingTypeId;
    }

    public String getClothingTypeName() {
        return clothingTypeName;
    }

    public void setClothingTypeName(String clothingTypeName) {
        this.clothingTypeName = clothingTypeName;
    }

    public List<Clothing> getClothes() {
        return clothes;
    }

    public void setClothes(List<Clothing> clothes) {
        this.clothes = clothes;
    }
}