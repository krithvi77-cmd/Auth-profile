package authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dao.DBUtil;
import dao.ProfileDAO;
import model.AuthProfile;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OAuthService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final SecureRandom RNG = new SecureRandom();

    private static final Map<String, PendingAuth> STATE_STORE = new ConcurrentHashMap<>();

    private final ProfileDAO profileDAO = new ProfileDAO();

    public String buildAuthorizeUrl(AuthProfile profile, int connectionId, String autoRedirectUri) {
        Map<String, String> cfg = Oauthv2Authenticator.profileFieldValues(profile);

        String authorizeUrl = cfg.get(Oauthv2Authenticator.F_AUTHORIZATION_URL);
        String clientId     = cfg.get(Oauthv2Authenticator.F_CLIENT_ID);
        String scopes       = cfg.get(Oauthv2Authenticator.F_SCOPES);
        String configured   = cfg.get(Oauthv2Authenticator.F_REDIRECT_URI);

        String redirectUri = (configured != null && !configured.trim().isEmpty())
                ? configured.trim()
                : autoRedirectUri;

        String state = newState();
        STATE_STORE.put(state, new PendingAuth(connectionId, redirectUri));

        StringBuilder url = new StringBuilder(authorizeUrl);
        url.append(authorizeUrl.contains("?") ? '&' : '?');
        url.append("response_type=code");
        url.append("&client_id=").append(enc(clientId));
        url.append("&redirect_uri=").append(enc(redirectUri));
        url.append("&state=").append(enc(state));
        url.append("&access_type=offline");

        url.append("&prompt=consent");
        if (scopes != null && !scopes.isEmpty()) {
            url.append("&scope=").append(enc(scopes));
        }

        return url.toString();
    }

    public enum ReconnectOutcome {
        STILL_VALID,
        REFRESHED,
        NEEDS_AUTHORIZE
    }

    public boolean isStillValid(int connectionId) throws SQLException {
        StoredOauth stored = loadOauthRow(connectionId);
        if (stored == null) {
            return false;
        }
        if (stored.expiresAt == null) {

            return true;
        }
        return stored.expiresAt.isAfter(Instant.now());
    }

   
    public ReconnectOutcome reconnect(int connectionId)
            throws SQLException, IOException, InterruptedException {

        StoredOauth stored = loadOauthRow(connectionId);
        if (stored == null) {
            return ReconnectOutcome.NEEDS_AUTHORIZE;
        }


        if (stored.expiresAt == null) {
            return ReconnectOutcome.STILL_VALID;
        }

        if (stored.refreshToken == null || stored.refreshToken.isEmpty()) {
            return ReconnectOutcome.NEEDS_AUTHORIZE;
        }
        int profileId = lookupProfileId(connectionId);
        AuthProfile profile = profileDAO.getByIdUnmasked(profileId);
        if (profile == null) {
            throw new IllegalStateException("Profile " + profileId + " not found for connection " + connectionId);
        }
        Map<String, String> cfg = Oauthv2Authenticator.profileFieldValues(profile);
        String tokenUrl     = cfg.get(Oauthv2Authenticator.F_ACCESS_TOKEN_URL);
        String clientId     = cfg.get(Oauthv2Authenticator.F_CLIENT_ID);
        String clientSecret = cfg.get(Oauthv2Authenticator.F_CLIENT_SECRET);

        JsonNode refreshed;
        try {
            refreshed = exchangeRefreshToken(tokenUrl, stored.refreshToken, clientId, clientSecret);
        } catch (RefreshRejectedException ex) {
            return ReconnectOutcome.NEEDS_AUTHORIZE;
        }

        String newAccessToken  = textOrNull(refreshed, "access_token");
        String newRefreshToken = textOrNull(refreshed, "refresh_token");
        String newExpiresAtIso = null;
        if (refreshed.hasNonNull("expires_in")) {
            long expiresIn = refreshed.get("expires_in").asLong();
            newExpiresAtIso = Instant.now().plusSeconds(expiresIn).toString();
        }
        if (newAccessToken == null || newAccessToken.isEmpty()) {
            return ReconnectOutcome.NEEDS_AUTHORIZE;
        }

        saveRefreshedTokens(connectionId, newAccessToken, newRefreshToken, newExpiresAtIso);
        return ReconnectOutcome.REFRESHED;
    }

    public int completeAuthorization(String code, String state, String autoRedirectUri)
            throws SQLException, IOException, InterruptedException {

        PendingAuth pending = STATE_STORE.remove(state);
        if (pending == null) {
            throw new IllegalArgumentException("Invalid or expired state");
        }

        int connectionId = pending.connectionId;
        String redirectUri = pending.redirectUri != null ? pending.redirectUri : autoRedirectUri;

        int profileId = lookupProfileId(connectionId);
  
        AuthProfile profile = profileDAO.getByIdUnmasked(profileId);
        if (profile == null) {
            throw new IllegalStateException("Profile " + profileId + " not found for connection " + connectionId);
        }

        Map<String, String> cfg = Oauthv2Authenticator.profileFieldValues(profile);
        String tokenUrl     = cfg.get(Oauthv2Authenticator.F_ACCESS_TOKEN_URL);
        String clientId     = cfg.get(Oauthv2Authenticator.F_CLIENT_ID);
        String clientSecret = cfg.get(Oauthv2Authenticator.F_CLIENT_SECRET);


        JsonNode tokenJson = exchangeCode(tokenUrl, code, redirectUri, clientId, clientSecret);

        String accessToken  = textOrNull(tokenJson, "access_token");
        String refreshToken = textOrNull(tokenJson, "refresh_token");
        String expiresAt    = null;
        if (tokenJson.hasNonNull("expires_in")) {
            long expiresIn = tokenJson.get("expires_in").asLong();
            expiresAt = Instant.now().plusSeconds(expiresIn).toString();
        }

        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalStateException("Token response missing access_token: " + tokenJson.toString());
        }

        if (refreshToken == null || refreshToken.isEmpty()) {
            StoredOauth existing = loadOauthRow(connectionId);
            if (existing == null || existing.refreshToken == null || existing.refreshToken.isEmpty()) {
                throw new IllegalStateException(
                        "Provider did not return a refresh_token for connection " + connectionId
                        + ". Check that the authorize URL includes prompt=consent and access_type=offline, "
                        + "and that the OAuth app is registered as 'offline access' / 'authorization code' grant type.");
            }
        }

        saveInitialTokens(connectionId, accessToken, refreshToken, expiresAt);
        return connectionId;
    }

    private JsonNode exchangeCode(String tokenUrl, String code, String redirectUri,
            String clientId, String clientSecret)
            throws IOException, InterruptedException {

        Map<String, String> form = new HashMap<>();
        form.put("grant_type", "authorization_code");
        form.put("code", code);
        form.put("redirect_uri", redirectUri);
        form.put("client_id", clientId);
        form.put("client_secret", clientSecret);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(urlEncode(form)))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException(
                    "Token endpoint returned HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode body = MAPPER.readTree(response.body());
        if (body.hasNonNull("error")) {
            throw new IllegalStateException("Token endpoint error: " + body.toString());
        }
        return body;
    }

    private int lookupProfileId(int connectionId) throws SQLException {
        String sql = "SELECT profile_id FROM connections WHERE id = ?";
        try (java.sql.Connection jdbc = DBUtil.getConnection();
                PreparedStatement ps = jdbc.prepareStatement(sql)) {
            ps.setInt(1, connectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Connection " + connectionId + " not found");
                }
                return rs.getInt(1);
            }
        }
    }

    private void saveInitialTokens(int connectionId, String accessToken,
            String refreshToken, String expiresAtIso) throws SQLException {
        try (java.sql.Connection jdbc = DBUtil.getConnection()) {
            jdbc.setAutoCommit(false);
            try {
                upsertOauth(jdbc, connectionId, accessToken, refreshToken, expiresAtIso);
                markConnectionActive(jdbc, connectionId);
                jdbc.commit();
            } catch (SQLException ex) {
                jdbc.rollback();
                throw ex;
            } finally {
                jdbc.setAutoCommit(true);
            }
        }
    }

 
    private void saveRefreshedTokens(int connectionId, String accessToken,
            String refreshToken, String expiresAtIso) throws SQLException {
        try (java.sql.Connection jdbc = DBUtil.getConnection()) {
            jdbc.setAutoCommit(false);
            try {
                updateOauthTokens(jdbc, connectionId, accessToken, refreshToken, expiresAtIso);
                markConnectionActive(jdbc, connectionId);
                jdbc.commit();
            } catch (SQLException ex) {
                jdbc.rollback();
                throw ex;
            } finally {
                jdbc.setAutoCommit(true);
            }
        }
    }

    private void markConnectionActive(java.sql.Connection jdbc, int connectionId) throws SQLException {
        try (PreparedStatement ps = jdbc.prepareStatement(
                "UPDATE connections SET status = 'active' WHERE id = ?")) {
            ps.setInt(1, connectionId);
            ps.executeUpdate();
        }
    }

    private void upsertOauth(java.sql.Connection jdbc, int connectionId, String accessToken,
            String refreshToken, String expiresAtIso) throws SQLException {
        String sql = "INSERT INTO connection_oauth_values "
                + "(connection_id, access_token, refresh_token, expires_at) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "access_token = VALUES(access_token), "
                + "refresh_token = COALESCE(VALUES(refresh_token), refresh_token), "
                + "expires_at = VALUES(expires_at)";

        try (PreparedStatement ps = jdbc.prepareStatement(sql)) {
            ps.setInt(1, connectionId);
            ps.setString(2, accessToken);
            if (refreshToken != null) {
                ps.setString(3, refreshToken);
            } else {
                ps.setNull(3, Types.LONGVARCHAR);
            }
            if (expiresAtIso != null) {
                ps.setTimestamp(4, Timestamp.from(Instant.parse(expiresAtIso)));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }
            ps.executeUpdate();
        }
    }


    private void updateOauthTokens(java.sql.Connection jdbc, int connectionId, String accessToken,
            String refreshToken, String expiresAtIso) throws SQLException {
        String sql = "UPDATE connection_oauth_values SET "
                + "access_token = ?, "
                + "refresh_token = COALESCE(?, refresh_token), "
                + "expires_at = ? "
                + "WHERE connection_id = ?";

        try (PreparedStatement ps = jdbc.prepareStatement(sql)) {
            ps.setString(1, accessToken);
            if (refreshToken != null) {
                ps.setString(2, refreshToken);
            } else {
                ps.setNull(2, Types.LONGVARCHAR);
            }
            if (expiresAtIso != null) {
                ps.setTimestamp(3, Timestamp.from(Instant.parse(expiresAtIso)));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.setInt(4, connectionId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
  
                throw new SQLException(
                        "updateOauthTokens affected 0 rows for connection_id=" + connectionId
                                + " — row missing or connection_id mismatch");
            }
        }
    }

    private static String newState() {
        byte[] buf = new byte[24];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private static String enc(String s) {
        return s == null ? "" : URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String urlEncode(Map<String, String> form) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : form.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(enc(e.getKey())).append('=').append(enc(e.getValue()));
        }
        return sb.toString();
    }

    private static final class PendingAuth {
        final int connectionId;
        final String redirectUri;

        PendingAuth(int connectionId, String redirectUri) {
            this.connectionId = connectionId;
            this.redirectUri = redirectUri;
        }
    }

 
    private static final class StoredOauth {
        final String accessToken;
        final String refreshToken;
        final Instant expiresAt;

        StoredOauth(String accessToken, String refreshToken, Instant expiresAt) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresAt = expiresAt;
        }
    }


    private static final class RefreshRejectedException extends RuntimeException {
        RefreshRejectedException(String msg) { super(msg); }
    }

    private StoredOauth loadOauthRow(int connectionId) throws SQLException {
        String sql = "SELECT access_token, refresh_token, expires_at "
                + "FROM connection_oauth_values WHERE connection_id = ?";
        try (java.sql.Connection jdbc = DBUtil.getConnection();
                PreparedStatement ps = jdbc.prepareStatement(sql)) {
            ps.setInt(1, connectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String access = rs.getString("access_token");
                String refresh = rs.getString("refresh_token");
                Timestamp ts = rs.getTimestamp("expires_at");
                Instant expiresAt = (ts == null) ? null : ts.toInstant();
                return new StoredOauth(access, refresh, expiresAt);
            }
        }
    }


    private JsonNode exchangeRefreshToken(String tokenUrl, String refreshToken,
            String clientId, String clientSecret)
            throws IOException, InterruptedException {

        Map<String, String> form = new HashMap<>();
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", refreshToken);
        form.put("client_id", clientId);
        form.put("client_secret", clientSecret);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(urlEncode(form)))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        int sc = response.statusCode();
        if (sc / 100 == 4) {
            throw new RefreshRejectedException(
                    "Refresh-token rejected by provider (HTTP " + sc + "): " + response.body());
        }
        if (sc / 100 != 2) {
            throw new IllegalStateException(
                    "Token endpoint returned HTTP " + sc + " on refresh: " + response.body());
        }
        JsonNode body = MAPPER.readTree(response.body());
        if (body.hasNonNull("error")) {
            throw new RefreshRejectedException("Refresh-token error: " + body.toString());
        }
        return body;
    }
}
