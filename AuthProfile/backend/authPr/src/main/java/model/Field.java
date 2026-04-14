package model;

import java.util.List;

public class Field {

	private int id;
	private String name;
	private String type;
	private String label;
	private List<Option> options;
	private static int count = 0;
	private boolean isRequired;

	public Field() {
		this.id = ++count;
	}

	public Field(String name, String type, String labelName, boolean isRequired, List<Option> options) {
		this.id = ++count;
		this.name = name;
		this.type = type;
		this.label = labelName;
		this.isRequired = isRequired;
		this.setOptions(options);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String labelName) {
		this.label = labelName;
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
