package service;

import authentication.OAuthService;
import authentication.Oauthv2Authenticator;
import dao.ConnectionDAO;
import dao.ProfileDAO;
import model.AuthProfile;
import model.Connection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionService {

	private final ConnectionDAO connectionDAO;
	private final ProfileDAO profileDAO;
	private final OAuthService oauthService;

	public ConnectionService() {
		this(new ConnectionDAO(), new ProfileDAO(), new OAuthService());
	}

	public ConnectionService(ConnectionDAO connectionDAO, ProfileDAO profileDAO, OAuthService oauthService) {
		this.connectionDAO = connectionDAO;
		this.profileDAO = profileDAO;
		this.oauthService = oauthService;
	}

	public Map<String, Object> create(int profileId, Connection conn, String redirectUri)
			throws SQLException {
		AuthProfile profile = loadAndValidateProfile(profileId);

		if (conn.getValueType() != null || conn.getValueId() != null) {
			throw new IllegalArgumentException(
					"Client must not set valueType or valueId; these are managed by the server.");
		}

		int id = connectionDAO.create(profile, conn);

		String authorizeUrl = null;
		if (profile.getAuthType() == Oauthv2Authenticator.AUTH_TYPE) {
			authorizeUrl = oauthService.buildAuthorizeUrl(profile, id, redirectUri);
		}
		return buildCreateResponse(id, conn, profile, authorizeUrl);
	}

	public Connection getById(int id) throws SQLException {
		return connectionDAO.getById(id);
	}

	public List<Connection> list() throws SQLException {
		return connectionDAO.list();
	}

	public void updateName(int connectionId, String newName) throws SQLException {
		Connection existing = connectionDAO.getById(connectionId);
		if (existing == null) {
			throw new IllegalArgumentException("Connection not found: id=" + connectionId);
		}
		if (newName == null || newName.trim().isEmpty()) {
			throw new IllegalArgumentException("Connection name is required");
		}

		AuthProfile profile = profileDAO.getByIdUnmasked(existing.getProfileId());
		if (profile == null) {
			throw new IllegalStateException("Profile not found: id=" + existing.getProfileId());
		}

		existing.setName(newName.trim());
		connectionDAO.update(profile, existing);
	}

	public Map<String, Object> reconnectWithValues(int connectionId, Map<String, String> newValues)
			throws SQLException {
		Connection existing = connectionDAO.getById(connectionId);
		if (existing == null) {
			throw new IllegalArgumentException("Connection not found: id=" + connectionId);
		}

		AuthProfile profile = profileDAO.getByIdUnmasked(existing.getProfileId());
		if (profile == null) {
			throw new IllegalStateException("Profile not found: id=" + existing.getProfileId());
		}

		if (profile.getAuthType() == Oauthv2Authenticator.AUTH_TYPE) {
			throw new IllegalArgumentException(
					"Use reconnectOAuth() for OAuth connections.");
		}

		connectionDAO.reconnect(profile, connectionId, newValues);

		Map<String, Object> body = new HashMap<>();
		body.put("id", connectionId);
		body.put("name", existing.getName());
		body.put("profileId", profile.getId());
		body.put("authType", profile.getAuthType());
		body.put("status", "active");
		body.put("connectionType", existing.getConnectionType());
		return body;
	}

	public Map<String, Object> reconnectOAuth(int connectionId, String redirectUri)
			throws SQLException, IOException, InterruptedException {
		Connection existing = connectionDAO.getById(connectionId);
		if (existing == null) {
			throw new IllegalArgumentException("Connection not found: id=" + connectionId);
		}

		AuthProfile profile = profileDAO.getByIdUnmasked(existing.getProfileId());
		if (profile == null) {
			throw new IllegalStateException("Profile not found: id=" + existing.getProfileId());
		}

		if (profile.getAuthType() != Oauthv2Authenticator.AUTH_TYPE) {
			throw new IllegalArgumentException(
					"Use reconnectWithValues() for non-OAuth connections.");
		}

		Map<String, Object> body = new HashMap<>();
		body.put("id", connectionId);
		body.put("name", existing.getName());
		body.put("profileId", profile.getId());
		body.put("authType", profile.getAuthType());
		body.put("connectionType", "oauth");

		OAuthService.ReconnectOutcome outcome = oauthService.reconnect(connectionId);
		switch (outcome) {
			case STILL_VALID:
			case REFRESHED:
				body.put("status", "active");
				body.put("refreshed", outcome == OAuthService.ReconnectOutcome.REFRESHED);
				break;
			case NEEDS_AUTHORIZE:
			default:
				String authorizeUrl = oauthService.buildAuthorizeUrl(profile, connectionId, redirectUri);
				body.put("status", "inactive");
				body.put("authorizeUrl", authorizeUrl);
				break;
		}
		return body;
	}

	public boolean delete(int connectionId) throws SQLException {
		Connection existing = connectionDAO.getById(connectionId);
		if (existing == null) {
			return false;
		}

		AuthProfile profile = profileDAO.getByIdUnmasked(existing.getProfileId());
		if (profile != null && profile.getAuthType() == Oauthv2Authenticator.AUTH_TYPE) {
			oauthService.revokeToken(connectionId);
		}

		return connectionDAO.delete(connectionId);
	}

	private AuthProfile loadAndValidateProfile(int profileId) throws SQLException {
		if (profileId <= 0) {
			throw new IllegalArgumentException("authProfileId is required");
		}
		AuthProfile profile = profileDAO.getByIdUnmasked(profileId);
		if (profile == null) {
			throw new IllegalArgumentException("Auth profile not found: id=" + profileId);
		}
		return profile;
	}

	private Map<String, Object> buildCreateResponse(int id, Connection conn, AuthProfile profile,
			String authorizeUrl) {
		Map<String, Object> body = new HashMap<>();
		body.put("id", id);
		body.put("name", conn.getName());
		body.put("profileId", profile.getId());
		body.put("authType", profile.getAuthType());
		body.put("status", conn.getStatus());
		body.put("connectionType", conn.getConnectionType());
		body.put("valueType", conn.getValueType());
		body.put("valueId", conn.getValueId());
		if (authorizeUrl != null) {
			body.put("authorizeUrl", authorizeUrl);
		}
		return body;
	}
}
