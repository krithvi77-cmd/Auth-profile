package clone;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import dao.ConnectionDAO;
import dao.ProfileDAO;
import model.AuthProfile;
import model.Connection;

public class CloneHandler {

	private final ConnectionDAO connectionDAO;
	private final ProfileDAO profileDAO;
	private final Clone cloneService;

	public CloneHandler() {
		this(new ConnectionDAO(), new ProfileDAO(), new Clone());
	}

	public CloneHandler(ConnectionDAO connectionDAO, ProfileDAO profileDAO, Clone cloneService) {
		this.connectionDAO = connectionDAO;
		this.profileDAO = profileDAO;
		this.cloneService = cloneService;
	}

	public Map<String, Object> handle(int sourceId) throws SQLException {
		if (sourceId <= 0) {
			throw new IllegalArgumentException("Invalid connection id");
		}

		Connection source = connectionDAO.getByIdShallow(sourceId);
		if (source == null) {
			throw new IllegalStateException("Connection not found: id=" + sourceId);
		}

		AuthProfile profile = profileDAO.getByIdUnmasked(source.getProfileId());
		if (profile == null) {
			throw new IllegalStateException("Auth profile not found: id=" + source.getProfileId());
		}

		String newName = nextAvailableName(source.getName());
		int newId = cloneService.cloneConnection(source, profile, newName);

		Map<String, Object> body = new HashMap<>();
		body.put("id", newId);
		body.put("sourceId", sourceId);
		body.put("name", newName);
		body.put("profileId", profile.getId());
		body.put("authType", profile.getAuthType());
		body.put("status", source.getStatus());
		body.put("connectionType", source.getConnectionType());
		body.put("valueType", source.getValueType());
		body.put("valueId", source.getValueId());
		body.put("sharedValues", true);
		return body;
	}

	private String nextAvailableName(String baseName) throws SQLException {
		String prefix = (baseName == null || baseName.trim().isEmpty()) ? "connection" : baseName.trim();
		String candidate = prefix + "_clone";
		int suffix = 2;
		while (nameExists(candidate)) {
			candidate = prefix + "_clone_" + suffix;
			suffix++;
		}
		return candidate;
	}

	private boolean nameExists(String name) throws SQLException {
		for (Connection c : connectionDAO.list()) {
			if (c.getName() != null && c.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
}
