package model;

public class ConnectionValue {
    private int id;
    private int fieldId;
    private String key;
    private String value;
    private String createdAt;

    public ConnectionValue() {}

    public ConnectionValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public ConnectionValue(int id, int fieldId, String key, String value) {
        this.id = id;
        this.fieldId = fieldId;
        this.key = key;
        this.value = value;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFieldId() { return fieldId; }
    public void setFieldId(int fieldId) { this.fieldId = fieldId; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
