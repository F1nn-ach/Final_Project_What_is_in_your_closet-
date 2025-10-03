package com.matching.cloth.models;

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
@Table(name = "mainColorCategory")
public class MainColorCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int mainColorCategory;

    @Column(name = "mainColorName", nullable = false, length = 50)
    private String mainColorName;

    @Column(name = "mainHex", nullable = false, length = 7)
    private String mainHex;

    @OneToMany(mappedBy = "mainColorCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SubColorCategory> subColors = new ArrayList<>();

    public MainColorCategory() {
        super();
    }

    public MainColorCategory(int mainColorCategory, String mainColorName, String mainHex, List<SubColorCategory> subColors) {
        super();
        this.mainColorCategory = mainColorCategory;
        this.mainColorName = mainColorName;
        this.mainHex = mainHex;
        this.subColors = subColors;
    }

    public int getMainColorCategory() {
        return mainColorCategory;
    }

    public void setMainColorCategory(int mainColorCategory) {
        this.mainColorCategory = mainColorCategory;
    }

    public String getMainColorName() {
        return mainColorName;
    }

    public void setMainColorName(String mainColorName) {
        this.mainColorName = mainColorName;
    }

    public String getMainHex() {
        return mainHex;
    }

    public void setMainHex(String mainHex) {
        this.mainHex = mainHex;
    }

    public List<SubColorCategory> getSubColors() {
        return subColors;
    }

    public void setSubColors(List<SubColorCategory> subColors) {
        this.subColors = subColors;
    }
}
