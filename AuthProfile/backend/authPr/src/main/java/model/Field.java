package model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Field {

	private int id;
	private int profileId;
	private String key;
	private String label;          
	private String fieldType;       
	private String defaultValue;    
	private boolean isCustom;      
	private String placement;       
	private int position;

	public Field() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getProfileId() {
		return profileId;
	}

	public void setProfileId(int profileId) {
		this.profileId = profileId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@JsonProperty("isCustom")
	public boolean isCustom() {
		return isCustom;
	}

	@JsonProperty("isCustom")
	public void setCustom(boolean custom) {
		isCustom = custom;
	}

	public String getPlacement() {
		return placement;
	}

	public void setPlacement(String placement) {
		this.placement = placement;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
}
