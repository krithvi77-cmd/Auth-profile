package model;

import java.util.List;


public class Field {
  
  private String id;
  private String name;
  private String type; 
  private String labelName;
  private List<Option> options;
  private static int count = 0;
  private static String idPrefix = "FI_";
  private boolean isRequired;
  
  public Field() {
	this.id = idPrefix + count; 
  }
  
  public Field(String name, String type, String labelName, boolean isRequired, List<Option> options) {
	this.id = idPrefix + count;
	this.name = name;
	this.type = type;
	this.label = labelName;
	this.isRequired = isRequired;
	this.setOptions(options);
  }

  public String getId() {
	return id;
  }

  public void setId(String id) {
	this.id = id;
  }

  public String getName() {
	return name;
  }

  public void setName(String name) {
	this.name = name;
  }

  public String getType() {
	return type;
  }

  public void setType(String type) {
	this.type = type;
  }

  public static int getCount() {
	return count;
  }

  public static void setCount(int count) {
	Field.count = count;
  }

  public static String getIdPrefix() {
	return idPrefix;
  }

  public static void setIdPrefix(String idPrefix) {
	Field.idPrefix = idPrefix;
  }

  public String getLabelName() {
	return labelName;
  }

  public void setLabelName(String labelName) {
	this.labelName = labelName;
  }

  public boolean isRequired() {
	return isRequired;
  }

  public void setRequired(boolean isRequired) {
	this.isRequired = isRequired;
  }

  public List<Option> getOptions() {
	return options;
  }

  public void setOptions(List<Option> options) {
	this.options = options;
  }
  
}
