package model;

import java.util.ArrayList;
import java.util.List;

public class AuthType {

	private int id;
	private String name;
	private List<Field> fields;

	public AuthType() {
		this.fields = new ArrayList<>();
	}

	public AuthType(String name, List<Field> fields) {
		this.name = name;
		this.fields = fields != null ? fields : new ArrayList<>();
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

	public List<Field> getFields() { 
		return fields;
	}
	public void setFields(List<Field> fields) { 
		this.fields = fields;
	}
}