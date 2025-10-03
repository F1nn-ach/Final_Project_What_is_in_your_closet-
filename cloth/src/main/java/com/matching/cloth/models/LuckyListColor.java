package com.matching.cloth.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "luckyListColor")
public class LuckyListColor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int luckyListColorId;

    @Column(name = "luckyListColorName", nullable = false, length = 50)
    private String luckyListColorName;

    @Column(name = "luckyListHex", nullable = false, length = 7)
    private String luckyListHex;

    @OneToMany(mappedBy = "luckyListColor")
    private List<LuckyColorItem> luckyColorItems = new ArrayList<>();

    public LuckyListColor() {
        super();
    }

    public LuckyListColor(int luckyListColorId, String luckyListColorName, String luckyListHex,
            List<LuckyColorItem> luckyColorItems) {
        super();
        this.luckyListColorId = luckyListColorId;
        this.luckyListColorName = luckyListColorName;
        this.luckyListHex = luckyListHex;
        this.luckyColorItems = luckyColorItems;
    }

    public int getLuckyListColorId() {
        return luckyListColorId;
    }

    public void setLuckyListColorId(int luckyListColorId) {
        this.luckyListColorId = luckyListColorId;
    }

    public String getLuckyListColorName() {
        return luckyListColorName;
    }

    public void setLuckyListColorName(String luckyListColorName) {
        this.luckyListColorName = luckyListColorName;
    }

    public String getLuckyListHex() {
        return luckyListHex;
    }

    public void setLuckyListHex(String luckyListHex) {
        this.luckyListHex = luckyListHex;
    }

    public List<LuckyColorItem> getLuckyColorItems() {
        return luckyColorItems;
    }

    public void setLuckyColorItems(List<LuckyColorItem> luckyColorItems) {
        this.luckyColorItems = luckyColorItems;
    }
}
