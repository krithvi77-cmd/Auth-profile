package model;

import java.util.Date;
import java.util.HashMap;

public class AuthProfile {
	
	private static final String idPrefix = "AP";
	private static int count = 0 ;
	private String id ;
	private String name;
	private AuthType authtype;
	private String createdBy;
	private Date updatedOn;
	HashMap<String,String> values;
	
	
	
	public AuthProfile(String name, AuthType authtype, String createdBy, Date updatedOn,HashMap<String, String> values) {
		this.name = name;
		this.authtype = authtype;
		this.createdBy = createdBy;
		this.updatedOn = updatedOn;
		this.values = values;
		this.id = idPrefix + ++count;
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


	public AuthType getAuthtype() {
		return authtype;
	}


	public void setAuthtype(AuthType authtype) {
		this.authtype = authtype;
	}


	public String getCreatedBy() {
		return createdBy;
	}


	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}


	public Date getUpdatedOn() {
		return updatedOn;
	}


	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}


	public HashMap<String, String> getValues() {
		return values;
	}


	public void setValues(HashMap<String, String> values) {
		this.values = values;
	}	
	
}
