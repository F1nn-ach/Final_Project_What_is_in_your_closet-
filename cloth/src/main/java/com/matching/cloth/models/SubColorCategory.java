package com.matching.cloth.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "subColorCategory")
public class SubColorCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int subColorCategoryId;

    @Column(name = "subColorName", nullable = false, length = 50)
    private String subColorName;

    @Column(name = "subHex", nullable = false, length = 7)
    private String subHex;

    @ManyToOne
    @JoinColumn(name = "mainColorCategory", nullable = false)
    private MainColorCategory mainColorCategory;

    public SubColorCategory() {
        super();
    }

    public SubColorCategory(int subColorCategoryId, String subColorName, String subHex, MainColorCategory mainColorCategory) {
        super();
        this.subColorCategoryId = subColorCategoryId;
        this.subColorName = subColorName;
        this.subHex = subHex;
        this.mainColorCategory = mainColorCategory;
    }

    public int getSubColorCategoryId() {
        return subColorCategoryId;
    }

    public void setSubColorCategoryId(int subColorCategoryId) {
        this.subColorCategoryId = subColorCategoryId;
    }

    public String getSubColorName() {
        return subColorName;
    }

    public void setSubColorName(String subColorName) {
        this.subColorName = subColorName;
    }

    public String getSubHex() {
        return subHex;
    }

    public void setSubHex(String subHex) {
        this.subHex = subHex;
    }

    public MainColorCategory getMainColorCategory() {
        return mainColorCategory;
    }

    public void setMainColorCategory(MainColorCategory mainColorCategory) {
        this.mainColorCategory = mainColorCategory;
    }
}
