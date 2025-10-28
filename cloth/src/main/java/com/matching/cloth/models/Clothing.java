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
@Table(name = "clothing")
public class Clothing {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "clothingId")
	private int clothingId;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "clothingTypeId", nullable = false)
	private ClothingType clothingType;

	@OneToMany(mappedBy = "clothing", cascade = CascadeType.ALL)
	private List<ClothingColor> clothingColors = new ArrayList<>();

	@Column(name = "clothingImage", length = 255)
	private String clothingImage;

	public Clothing() {
		super();
	}

	public Clothing(User user, ClothingType clothingType, String clothingImage) {
		super();
		this.user = user;
		this.clothingType = clothingType;
		this.clothingImage = clothingImage;
	}

	public int getClothingId() {
		return clothingId;
	}

	public void setClothingId(int clothingId) {
		this.clothingId = clothingId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ClothingType getClothingType() {
		return clothingType;
	}

	public void setClothingType(ClothingType clothingType) {
		this.clothingType = clothingType;
	}

	public List<ClothingColor> getClothingColors() {
		return clothingColors;
	}

	public void setClothingColors(List<ClothingColor> clothingColors) {
		this.clothingColors = clothingColors;
	}

	public ColorCategory getDominantColor() {
		return clothingColors.stream()
				.max(java.util.Comparator.comparing(ClothingColor::getPercentage))
				.map(ClothingColor::getColorCategory)
				.orElse(null);
	}

	public String getClothingImage() {
		return clothingImage;
	}

	public void setClothingImage(String clothingImage) {
		this.clothingImage = clothingImage;
	}
}
