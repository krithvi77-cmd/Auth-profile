package model;

public class Field {

	private int id;
	private int profileId;
	private String label;
	private String key;
	private String fieldType;
	private boolean isRequired;
	private boolean isSecret;
	private boolean visibleToMember = true;
	private boolean editableByMember = true;
	private String defaultValue;
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

	public void setRequired(boolean required) {
		isRequired = required;
	}

	public boolean isSecret() {
		return isSecret;
	}

	public void setSecret(boolean secret) {
		isSecret = secret;
	}

	public boolean isVisibleToMember() {
		return visibleToMember;
	}

	public void setVisibleToMember(boolean visibleToMember) {
		this.visibleToMember = visibleToMember;
	}

	public boolean isEditableByMember() {
		return editableByMember;
	}

	public void setEditableByMember(boolean editableByMember) {
		this.editableByMember = editableByMember;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
}
