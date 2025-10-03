package com.matching.cloth.model_mockup;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cloth")
public class Clothing {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "clothId")
	private int clothId;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "clothTypeId", nullable = false)
	private ClothingType clothType;

	@ManyToOne
	@JoinColumn(name = "colorCategoryId")
	private ColorCategory colorCategory;

	@Column(name = "clothImage", length = 255)
	private String clothImage;

	public Clothing() {
		super();
	}

	public Clothing(User user, ClothingType clothType, ColorCategory colorCategory, String clothImage,
			List<String> colorHex) {
		super();
		this.user = user;
		this.clothType = clothType;
		this.colorCategory = colorCategory;
		this.clothImage = clothImage;
	}

	public int getClothId() {
		return clothId;
	}

	public void setClothId(int clothId) {
		this.clothId = clothId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ClothingType getClothType() {
		return clothType;
	}

	public void setClothType(ClothingType clothType) {
		this.clothType = clothType;
	}

	public ColorCategory getColorCategory() {
		return colorCategory;
	}

	public void setColorCategory(ColorCategory colorCategory) {
		this.colorCategory = colorCategory;
	}

	public String getClothImage() {
		return clothImage;
	}

	public void setClothImage(String clothImage) {
		this.clothImage = clothImage;
	}
}
