package model;

public class ConnectionOauth {
    private int id;
    private String accessToken;
    private String refreshToken;
    private String expiresAt;
    private String createdAt;
    private String tokenType;
    private String scope;

    public ConnectionOauth() {}

    public ConnectionOauth(int id, String accessToken, String refreshToken, String expiresAt) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
}
