package com.matching.cloth.model_mockup;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "luckyListColor")
public class LuckyListColor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int luckyListColorId;

    @ManyToOne
    @JoinColumn(name = "luckyColorId", nullable = false)
    private LuckyColor luckyColor;

    @ManyToOne
    @JoinColumn(name = "colorCategory", nullable = false)
    private ColorCategory colorCategory;

    public LuckyListColor() {
        super();
    }

    public LuckyListColor(int luckyListColorId, LuckyColor luckyColor, ColorCategory colorCategory) {
        this.luckyListColorId = luckyListColorId;
        this.luckyColor = luckyColor;
        this.colorCategory = colorCategory;
    }

    public int getLuckyListColorId() {
        return luckyListColorId;
    }

    public void setLuckyListColorId(int luckyListColorId) {
        this.luckyListColorId = luckyListColorId;
    }

    public LuckyColor getLuckyColor() {
        return luckyColor;
    }

    public void setLuckyColor(LuckyColor luckyColor) {
        this.luckyColor = luckyColor;
    }

    public ColorCategory getColorCategory() {
        return colorCategory;
    }

    public void setColorCategory(ColorCategory colorCategory) {
        this.colorCategory = colorCategory;
    }
}
