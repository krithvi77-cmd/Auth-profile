package model;

public class Option {
	private static int count = 0;
	private int id;
	private String label;
	private String value;

	public Option() {
		this.id = ++count;

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}