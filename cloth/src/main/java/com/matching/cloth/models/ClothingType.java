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
@Table(name = "clothType")
public class ClothingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int clothTypeId;
    
    @Column(name = "typeName", nullable = false, length = 50)
    private String typeName;

    @OneToMany(mappedBy = "clothType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Clothing> clothes = new ArrayList<>();
    
	public ClothingType() {
		super();
	}

	public ClothingType(int clothTypeId, String typeName, List<Clothing> clothes) {
		super();
		this.clothTypeId = clothTypeId;
		this.typeName = typeName;
		this.clothes = clothes;
	}

    public int getClothTypeId() {
        return clothTypeId;
    }

    public void setClothTypeId(int clothTypeId) {
        this.clothTypeId = clothTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public List<Clothing> getClothes() {
        return clothes;
    }

    public void setClothes(List<Clothing> clothes) {
        this.clothes = clothes;
    }
}