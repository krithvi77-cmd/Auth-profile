package model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Connection {

	public static final String VALUE_TYPE_VALUES = "VALUES";
	public static final String VALUE_TYPE_OAUTH  = "OAUTH";

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

	@JsonProperty("valueType")
	@JsonAlias({ "value_type" })
	private String valueType;

	@JsonProperty("valueId")
	@JsonAlias({ "value_id" })
	private Integer valueId;

	@JsonIgnore
	private ConnectionOauth oauthData;

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

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public Integer getValueId() {
		return valueId;
	}

	public void setValueId(Integer valueId) {
		this.valueId = valueId;
	}

	public ConnectionOauth getOauthData() {
		return oauthData;
	}

	public void setOauthData(ConnectionOauth oauthData) {
		this.oauthData = oauthData;
	}

	@JsonProperty("connectionType")
	public String getConnectionType() {
		if (valueType == null) return null;
		return valueType.toLowerCase();
	}

	@JsonIgnore
	public boolean isValuesType() {
		return VALUE_TYPE_VALUES.equalsIgnoreCase(valueType);
	}

	@JsonIgnore
	public boolean isOauthType() {
		return VALUE_TYPE_OAUTH.equalsIgnoreCase(valueType);
	}
}
