package com.matching.cloth.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cloth")
public class Clothing {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "clothId")
	private int clothId;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false) // เปลี่ยนชื่อ column ให้ชัดเจน
	private User user;

	@ManyToOne
	@JoinColumn(name = "clothTypeId", nullable = false)
	private ClothingType clothType;

	@ManyToOne
	@JoinColumn(name = "subColorCategoryId")
	private SubColorCategory subColorCategory;

	@Column(name = "clothImage", length = 255)
	private String clothImage;

	@ElementCollection
	@Column(name = "colorHex", nullable = false)
	private List<String> colorHex = new ArrayList<>();

	public Clothing() {
		super();
	}

	public Clothing(User user, ClothingType clothType, SubColorCategory subColorCategory, String clothImage,
			List<String> colorHex) {
		super();
		this.user = user;
		this.clothType = clothType;
		this.subColorCategory = subColorCategory;
		this.clothImage = clothImage;
		this.colorHex = colorHex;
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

	public SubColorCategory getSubColorCategory() {
		return subColorCategory;
	}

	public void setSubColorCategory(SubColorCategory subColorCategory) {
		this.subColorCategory = subColorCategory;
	}

	public List<String> getColorHex() {
		return colorHex;
	}

	public void setColorHex(List<String> colorHex) {
		this.colorHex = colorHex;
	}

	public String getClothImage() {
		return clothImage;
	}

	public void setClothImage(String clothImage) {
		this.clothImage = clothImage;
	}
}
