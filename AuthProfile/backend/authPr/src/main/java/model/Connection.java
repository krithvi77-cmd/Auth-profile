package model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Connection {

	private int id;

	@JsonProperty("authProfileId")
	@JsonAlias({ "profileId", "profile_id" })
	private int profileId;

	private Integer userId;
	private String name;
	private String status = "active";
	private String createdAt;
	private Map<String, String> values = new java.util.HashMap<>();

	private List<ConnectionValue> fields = new ArrayList<>();


	public Connection() {
	}


	@JsonProperty("values")
	public void setValues(Map<String, String> values) {
		this.values = values != null ? new java.util.HashMap<>(values) : new java.util.HashMap<>();
		this.fields = new ArrayList<>();
		if (values == null) {
			return;
		}
		for (Map.Entry<String, String> e : values.entrySet()) {
			if (e.getKey() == null) continue;
			ConnectionValue cv = new ConnectionValue(e.getKey(), e.getValue());
			this.fields.add(cv);
		}
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

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	@JsonProperty("values")
	public Map<String, String> getValues() {
		if ((values == null || values.isEmpty()) && fields != null && !fields.isEmpty()) {
			Map<String, String> mapped = new java.util.HashMap<>();
			for (ConnectionValue field : fields) {
				if (field != null && field.getKey() != null) {
					mapped.put(field.getKey(), field.getValue());
				}
			}
			values = mapped;
		}
		return values;
	}

	public List<ConnectionValue> getFields() {
		return fields;
	}

	public void setFields(List<ConnectionValue> fields) {
		this.fields = fields != null ? fields : new ArrayList<>();
		this.values = new java.util.HashMap<>();
		for (ConnectionValue field : this.fields) {
			if (field != null && field.getKey() != null) {
				this.values.put(field.getKey(), field.getValue());
			}
		}
	}

}
