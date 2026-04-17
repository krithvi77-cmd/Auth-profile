package model;

import java.util.Date;
import java.util.List;
import model.Option;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import controller.Action;

public class Field {
	
	private String label;
	private String key;
	private String fieldType;
	private boolean isRequired;
	private boolean isSecret;
	private boolean isVisibleToMembers;
	private boolean isEditableByMembers;
	private String value;
	private int position;
	private Date createdON;
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getFieldType() {
		return fieldType;
	}
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}
	public boolean isRequired() {
		return isRequired;
	}
	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}
	public boolean isSecret() {
		return isSecret;
	}
	public void setSecret(boolean isSecret) {
		this.isSecret = isSecret;
	}
	public boolean isVisibleToMembers() {
		return isVisibleToMembers;
	}
	public void setVisibleToMembers(boolean isVisibleToMembers) {
		this.isVisibleToMembers = isVisibleToMembers;
	}
	public boolean isEditableByMembers() {
		return isEditableByMembers;
	}
	public void setEditableByMembers(boolean isEditableByMembers) {
		this.isEditableByMembers = isEditableByMembers;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public Date getCreatedON() {
		return createdON;
	}
	public void setCreatedON(Date createdON) {
		this.createdON = createdON;
	}
	
	
}
