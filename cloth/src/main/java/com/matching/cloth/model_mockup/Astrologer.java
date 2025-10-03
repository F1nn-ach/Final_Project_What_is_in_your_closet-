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
@Table(name = "astrologer")
public class Astrologer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "astrologerId")
    private int astrologerId;
    
    @Column(name = "astrologerName", nullable = false, length = 100)
    private String astrologerName;
    
    @OneToMany(mappedBy = "astrologer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LuckyColor> luckyColors = new ArrayList<>();

	public Astrologer() {
		super();
	}

	public Astrologer(int astrologerId, String astrologerName, List<LuckyColor> luckyColors) {
		super();
		this.astrologerId = astrologerId;
		this.astrologerName = astrologerName;
		this.luckyColors = luckyColors;
	}

	public int getAstrologerId() {
		return astrologerId;
	}

	public void setAstrologerId(int astrologerId) {
		this.astrologerId = astrologerId;
	}

	public String getAstrologerName() {
		return astrologerName;
	}

	public void setAstrologerName(String astrologerName) {
		this.astrologerName = astrologerName;
	}

	public List<LuckyColor> getLuckyColors() {
		return luckyColors;
	}

	public void setLuckyColors(List<LuckyColor> luckyColors) {
		this.luckyColors = luckyColors;
	}
}