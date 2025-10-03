package com.matching.cloth.model_mockup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "clothingColor")
public class ClothingColor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int clothingColorId;

    @ManyToOne
    @JoinColumn(name = "clothingId", nullable = false)
    private Clothing clothing;

    @ManyToOne
    @JoinColumn(name = "colorCategoryId", nullable = false)
    private ColorCategory colorCategory;

    @Column(name = "percentage", nullable = false)
    private float percentage;

    public ClothingColor() {
    }

    public ClothingColor(int clothingColorId, Clothing clothing, ColorCategory colorCategory, float percentage) {
        this.clothingColorId = clothingColorId;
        this.clothing = clothing;
        this.colorCategory = colorCategory;
        this.percentage = percentage;
    }

    public int getClothingColorId() {
        return clothingColorId;
    }

    public void setClothingColorId(int clothingColorId) {
        this.clothingColorId = clothingColorId;
    }

    public Clothing getClothing() {
        return clothing;
    }

    public void setClothing(Clothing clothing) {
        this.clothing = clothing;
    }

    public ColorCategory getColorCategory() {
        return colorCategory;
    }

    public void setColorCategory(ColorCategory colorCategory) {
        this.colorCategory = colorCategory;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}
