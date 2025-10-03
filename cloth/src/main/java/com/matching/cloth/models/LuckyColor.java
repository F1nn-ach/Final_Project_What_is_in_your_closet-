package com.matching.cloth.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "luckyColor")
public class LuckyColor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int luckyColorId;

    @Column(name = "luckyDayOfWeek", nullable = false, length = 20)
    private String luckyDayOfWeek;

    @Column(name = "year", nullable = false)
    private int year;

    @ManyToOne
    @JoinColumn(name = "luckyColorTypeId", nullable = false)
    private LuckyColorType luckyColorType;

    @OneToMany(mappedBy = "luckyColor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LuckyColorItem> luckyColorItems = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "astrologerId", nullable = false)
    private Astrologer astrologer;

    public LuckyColor() {
        super();
    }

    public LuckyColor(int luckyColorId, String luckyDayOfWeek, int year, LuckyColorType luckyColorType,
            List<LuckyColorItem> luckyColorItems, Astrologer astrologer) {
        super();
        this.luckyColorId = luckyColorId;
        this.luckyDayOfWeek = luckyDayOfWeek;
        this.year = year;
        this.luckyColorType = luckyColorType;
        this.luckyColorItems = luckyColorItems;
        this.astrologer = astrologer;
    }

    public int getLuckyColorId() {
        return luckyColorId;
    }

    public void setLuckyColorId(int luckyColorId) {
        this.luckyColorId = luckyColorId;
    }

    public String getLuckyDayOfWeek() {
        return luckyDayOfWeek;
    }

    public void setLuckyDayOfWeek(String luckyDayOfWeek) {
        this.luckyDayOfWeek = luckyDayOfWeek;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public LuckyColorType getLuckyColorType() {
        return luckyColorType;
    }

    public void setLuckyColorType(LuckyColorType luckyColorType) {
        this.luckyColorType = luckyColorType;
    }

    public Astrologer getAstrologer() {
        return astrologer;
    }

    public void setAstrologer(Astrologer astrologer) {
        this.astrologer = astrologer;
    }

    public List<LuckyColorItem> getLuckyColorItems() {
        return luckyColorItems;
    }

    public void setLuckyColorItems(List<LuckyColorItem> luckyColorItems) {
        this.luckyColorItems = luckyColorItems;
    }
}
