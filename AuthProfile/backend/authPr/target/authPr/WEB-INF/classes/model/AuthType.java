package model;

import java.util.ArrayList;

public class AuthType {
	  
    private static int count = 0;
    private String idPrefix = "AT_";
	private String id = idPrefix + count;
	private String name;
	private ArrayList<Field> fields;	
	
	public AuthType() {
		this.id = idPrefix + ++count;
	}
	
	public AuthType(String name, ArrayList<Field> fields) {
		this.name = name;
		this.fields = fields;
		this.id = idPrefix + ++count;
	}

	public String getIdPrefix() {
		return idPrefix;
	}

	public void setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
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

	public ArrayList<Field> getFields() {
		return fields;
	}

	public void setFields(ArrayList<Field> fields) {
		this.fields = fields;
	}
		
}
