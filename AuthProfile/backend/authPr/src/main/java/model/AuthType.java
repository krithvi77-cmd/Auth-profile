package model;

import java.util.ArrayList;

public class AuthType {

	private static int count = 0;

	private int id;
	private String name;
	private ArrayList<Field> fields;

	public AuthType() {
		this.id = ++count;
	}

	public AuthType(String name, ArrayList<Field> fields) {
		this.name = name;
		this.fields = fields;
		this.id = ++count;
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

	public ArrayList<Field> getFields() {
		return fields;
	}

	public void setFields(ArrayList<Field> fields) {
		this.fields = fields;
	}

}
