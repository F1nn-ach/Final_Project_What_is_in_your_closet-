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
@Table(name = "luckyColorType")
public class LuckyColorType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int luckyColorTypeId;

    @Column(name = "luckyColorTypeName", nullable = false, length = 50)
    private String luckyColorTypeName;

    @OneToMany(mappedBy = "luckyColorType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LuckyColor> luckyColor = new ArrayList<>();

    public LuckyColorType() {
        super();
    }

    public LuckyColorType(int luckyColorTypeId, String luckyColorTypeName, List<LuckyColor> luckyColor) {
        super();
        this.luckyColorTypeId = luckyColorTypeId;
        this.luckyColorTypeName = luckyColorTypeName;
        this.luckyColor = luckyColor;
    }

    public int getLuckyColorTypeId() {
        return luckyColorTypeId;
    }

    public void setLuckyColorTypeId(int luckyColorTypeId) {
        this.luckyColorTypeId = luckyColorTypeId;
    }

    public String getLuckyColorTypeName() {
        return luckyColorTypeName;
    }

    public void setLuckyColorTypeName(String luckyColorTypeName) {
        this.luckyColorTypeName = luckyColorTypeName;
    }

    public List<LuckyColor> getLuckyColor() {
        return luckyColor;
    }

    public void setLuckyColor(List<LuckyColor> luckyColor) {
        this.luckyColor = luckyColor;
    }
}
