package com.matching.cloth.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "luckyColorItem")
public class LuckyColorItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer luckyColorItemId;

    @ManyToOne
    @JoinColumn(name = "luckyColorId", nullable = false)
    private LuckyColor luckyColor;

    @ManyToOne
    @JoinColumn(name = "luckyListColorId", nullable = false)
    private LuckyListColor luckyListColor;

    public LuckyColorItem() {
        super();
    }

    public LuckyColorItem(Integer luckyColorItemId, LuckyColor luckyColor, LuckyListColor luckyListColor) {
        this.luckyColorItemId = luckyColorItemId;
        this.luckyColor = luckyColor;
        this.luckyListColor = luckyListColor;
    }

    public Integer getLuckyColorItemId() {
        return luckyColorItemId;
    }

    public void setLuckyColorItemId(Integer luckyColorItemId) {
        this.luckyColorItemId = luckyColorItemId;
    }

    public LuckyColor getLuckyColor() {
        return luckyColor;
    }

    public void setLuckyColor(LuckyColor luckyColor) {
        this.luckyColor = luckyColor;
    }

    public LuckyListColor getLuckyListColor() {
        return luckyListColor;
    }

    public void setLuckyListColor(LuckyListColor luckyListColor) {
        this.luckyListColor = luckyListColor;
    }
}
