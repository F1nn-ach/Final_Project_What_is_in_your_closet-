package com.matching.cloth.model_mockup;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "colorCategory")
public class ColorCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int colorCategoryId;

    @Column(name = "colorCategoryName", length = 25, nullable = false)
    private String colorCategoryName;

    @Column(name = "colorCategoryHex", length = 7, nullable = false)
    private String colorCategoryHex;

    @OneToMany(mappedBy = "colorCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClothingColor> clothingColor = new ArrayList<>();

    public ColorCategory() {
        super();
    }

    public int getColorCategoryId() {
        return colorCategoryId;
    }

    public void setColorCategoryId(int colorCategoryId) {
        this.colorCategoryId = colorCategoryId;
    }

    public String getColorCategoryName() {
        return colorCategoryName;
    }

    public void setColorCategoryName(String colorCategoryName) {
        this.colorCategoryName = colorCategoryName;
    }

    public String getColorCategoryHex() {
        return colorCategoryHex;
    }

    public void setColorCategoryHex(String colorCategoryHex) {
        this.colorCategoryHex = colorCategoryHex;
    }

    public List<ClothingColor> getClothingColor() {
        return clothingColor;
    }

    public void setClothingColor(List<ClothingColor> clothingColor) {
        this.clothingColor = clothingColor;
    }
}
