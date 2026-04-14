package model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AuthProfile {

	private static int count = 0;
	private int id;
	private String name;
	private AuthType authtype;
	private String createdBy;
	private Date updatedOn;
	Map<Integer, String> values;

	public AuthProfile(String name, AuthType authtype, String createdBy, Date updatedOn,
			Map<Integer, String> authPrValue) {
		this.name = name;
		this.authtype = authtype;
		this.createdBy = createdBy;
		this.updatedOn = updatedOn;
		this.values = authPrValue;
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

	public Map<Integer, String> getValues() {
		return values;
	}

	public void setValues(Map<Integer, String> values2) {
		this.values = values2;
	}

}
