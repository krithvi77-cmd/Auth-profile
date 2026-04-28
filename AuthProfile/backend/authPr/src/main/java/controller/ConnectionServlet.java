package controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import authentication.OAuthService;
import authentication.Oauthv2Authenticator;
import dao.ConnectionDAO;
import dao.ProfileDAO;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.AuthProfile;
import model.Connection;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ConnectionServlet extends HttpServlet {

	private final ObjectMapper mapper = new ObjectMapper();
	private final ProfileDAO profileDAO = new ProfileDAO();
	private final ConnectionDAO connectionDAO = new ConnectionDAO();
	private final OAuthService oauthService = new OAuthService();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String pathInfo = req.getPathInfo();

		if (pathInfo != null && pathInfo.equals("/callback")) {
			handleOAuthCallback(req, res);
			return;
		}

		try {
			if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
				writeJson(res, 200, connectionDAO.list());
				return;
			}

			if (pathInfo.endsWith("/status")) {
				String idPart = pathInfo.substring(1, pathInfo.length() - "/status".length());
				Integer statusId = parseId("/" + idPart);
				if (statusId == null) {
					writeError(res, 400, "Invalid connection id");
					return;
				}
				Connection statusConn = connectionDAO.getById(statusId);
				if (statusConn == null) {
					writeError(res, 404, "Connection not found: id=" + statusId);
					return;
				}
				AuthProfile statusProfile = profileDAO.getByIdUnmasked(statusConn.getProfileId());
				boolean valid = (statusProfile != null
						&& statusProfile.getAuthType() == Oauthv2Authenticator.AUTH_TYPE)
						? oauthService.isStillValid(statusId)
						: true;
				Map<String, Object> statusBody = new HashMap<>();
				statusBody.put("id", statusId);
				statusBody.put("valid", valid);
				writeJson(res, 200, statusBody);
				return;
			}

			Integer id = parseId(pathInfo);
			if (id == null) {
				writeError(res, 400, "Invalid connection id");
				return;
			}
			Connection conn = connectionDAO.getById(id);
			if (conn == null) {
				writeError(res, 404, "Connection not found: id=" + id);
				return;
			}
			writeJson(res, 200, conn);

		} catch (Exception e) {
			e.printStackTrace();
			writeError(res, 500, e.getMessage());
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String pathInfo = req.getPathInfo();
		Integer id = parseId(pathInfo);
		if (id == null) {
			writeError(res, 400, "Invalid connection id");
			return;
		}
		try {
			Connection incoming = mapper.readValue(req.getInputStream(), Connection.class);
			if (incoming == null) {
				writeError(res, 400, "Request body is empty");
				return;
			}

			
			Connection existing = connectionDAO.getById(id);
			if (existing == null) {
				writeError(res, 404, "Connection not found: id=" + id);
				return;
			}
			int profileId = incoming.getProfileId() > 0 ? incoming.getProfileId() : existing.getProfileId();

			AuthProfile profile = profileDAO.getByIdUnmasked(profileId);
			if (profile == null) {
				writeError(res, 404, "Auth profile not found: id=" + profileId);
				return;
			}

			incoming.setId(id);
			incoming.setProfileId(profileId);

			connectionDAO.update(profile, incoming);

			Map<String, Object> body = new HashMap<>();
			body.put("id", id);
			body.put("name", incoming.getName());
			body.put("profileId", profile.getId());
			body.put("authType", profile.getAuthType());
			body.put("status", incoming.getStatus());

			if (profile.getAuthType() == Oauthv2Authenticator.AUTH_TYPE) {
				String redirectUri = buildRedirectUri(req);
				String authorizeUrl = oauthService.buildAuthorizeUrl(profile, id, redirectUri);
				body.put("authorizeUrl", authorizeUrl);
			}

			writeJson(res, 200, body);

		} catch (IllegalArgumentException bad) {
			writeError(res, 400, bad.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			writeError(res, 500, e.getMessage());
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String pathInfo = req.getPathInfo();
		Integer id = parseId(pathInfo);
		if (id == null) {
			writeError(res, 400, "Invalid connection id");
			return;
		}
		try {
			boolean removed = connectionDAO.delete(id);
			if (!removed) {
				writeError(res, 404, "Connection not found: id=" + id);
				return;
			}
			res.setStatus(204);
		} catch (Exception e) {
			e.printStackTrace();
			writeError(res, 500, e.getMessage());
		}
	}

	private Integer parseId(String pathInfo) {
		if (pathInfo == null) return null;
		String trimmed = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
		if (trimmed.isEmpty()) return null;
		int slash = trimmed.indexOf('/');
		if (slash >= 0){
			 trimmed = trimmed.substring(0, slash);
		};
		try {
			return Integer.parseInt(trimmed);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

		String pathInfo = req.getPathInfo();
		if (pathInfo != null && pathInfo.endsWith("/reconnect")) {
			handleReconnect(req, res);
			return;
		}

		try {
			Connection conn = mapper.readValue(req.getInputStream(), Connection.class);

			if (conn == null) {
				writeError(res, 400, "Request body is empty");
				return;
			}
			if (conn.getProfileId() <= 0) {
				writeError(res, 400, "authProfileId is required");
				return;
			}
			AuthProfile profile = profileDAO.getByIdUnmasked(conn.getProfileId());
			if (profile == null) {
				writeError(res, 404, "Auth profile not found: id=" + conn.getProfileId());
				return;
			}

			int id = connectionDAO.create(profile, conn);

			Map<String, Object> body = new HashMap<>();
			body.put("id", id);
			body.put("name", conn.getName());
			body.put("profileId", profile.getId());
			body.put("authType", profile.getAuthType());
			body.put("status", conn.getStatus());

			if (profile.getAuthType() == Oauthv2Authenticator.AUTH_TYPE) {
				String redirectUri = buildRedirectUri(req);
				String authorizeUrl = oauthService.buildAuthorizeUrl(profile, id, redirectUri);
				body.put("authorizeUrl", authorizeUrl);
			}

			writeJson(res, 201, body);

		} catch (IllegalArgumentException bad) {
			writeError(res, 400, bad.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			writeError(res, 500, e.getMessage());
		}
	}


	private void handleReconnect(HttpServletRequest req, HttpServletResponse res) throws IOException {
		Integer id = parseId(req.getPathInfo());
		if (id == null) {
			writeError(res, 400, "Invalid connection id");
			return;
		}
		try {
			Connection existing = connectionDAO.getById(id);
			if (existing == null) {
				writeError(res, 404, "Connection not found: id=" + id);
				return;
			}
			AuthProfile profile = profileDAO.getByIdUnmasked(existing.getProfileId());
			if (profile == null) {
				writeError(res, 404, "Auth profile not found: id=" + existing.getProfileId());
				return;
			}

			Map<String, Object> body = new HashMap<>();
			body.put("id", id);
			body.put("name", existing.getName());
			body.put("profileId", profile.getId());
			body.put("authType", profile.getAuthType());

			if (profile.getAuthType() == Oauthv2Authenticator.AUTH_TYPE) {
				OAuthService.ReconnectOutcome outcome = oauthService.reconnect(id);
				switch (outcome) {
					case STILL_VALID:
					case REFRESHED:
						body.put("status", "active");
						body.put("refreshed", outcome == OAuthService.ReconnectOutcome.REFRESHED);
						writeJson(res, 200, body);
						return;
					case NEEDS_AUTHORIZE:
					default:
						String redirectUri = buildRedirectUri(req);
						String authorizeUrl = oauthService.buildAuthorizeUrl(profile, id, redirectUri);
						body.put("status", "inactive");
						body.put("authorizeUrl", authorizeUrl);
						writeJson(res, 200, body);
						return;
				}
			}

			Map<String, String> values = readReconnectValues(req);
			connectionDAO.reconnect(profile, id, values);

			body.put("status", "active");
			writeJson(res, 200, body);

		} catch (IllegalArgumentException bad) {
			writeError(res, 400, bad.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			writeError(res, 500, e.getMessage());
		}
	}


	@SuppressWarnings("unchecked")
	private Map<String, String> readReconnectValues(HttpServletRequest req) throws IOException {
		try {
			Map<String, Object> root = mapper.readValue(req.getInputStream(), Map.class);
			if (root == null) return new HashMap<>();
			Object inner = root.get("values");
			Map<String, Object> source = (inner instanceof Map) ? (Map<String, Object>) inner : root;
			Map<String, String> out = new HashMap<>();
			for (Map.Entry<String, Object> e : source.entrySet()) {
				if (e.getKey() == null){
				   continue;
				} 
				out.put(e.getKey(), e.getValue() == null ? null : String.valueOf(e.getValue()));
			}
			return out;
		} catch (com.fasterxml.jackson.core.JsonParseException ex) {
			return new HashMap<>();
		} catch (com.fasterxml.jackson.databind.exc.MismatchedInputException ex) {
			return new HashMap<>();
		}
	}
		

	private void handleOAuthCallback(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String code  = req.getParameter("code");
		String state = req.getParameter("state");
		String error = req.getParameter("error");

		if (error != null) {
			writePopupResult(res, false, "Provider returned error: " + error);
			return;
		}
		if (code == null || state == null) {
			writePopupResult(res, false, "Missing code or state");
			return;
		}

		try {
			String redirectUri = buildRedirectUri(req);
			int connectionId = oauthService.completeAuthorization(code, state, redirectUri);
			writePopupResult(res, true, "Connection " + connectionId + " authorised");
		} catch (Exception ex) {
			ex.printStackTrace();
			writePopupResult(res, false, ex.getMessage());
		}
	}


	private String buildRedirectUri(HttpServletRequest req) {
		StringBuilder sb = new StringBuilder();
		sb.append("https://krithvishai-99v1t1cy-8080.zcodecorp.in/authPr/api/connection/callback");
		return sb.toString();
	}

	
	private void writePopupResult(HttpServletResponse res, boolean ok, String message) throws IOException {
		res.setStatus(ok ? 200 : 400);
		res.setContentType("text/html; charset=UTF-8");
		String safeMsg = message == null ? "" : message.replace("</", "<\\/").replace("'", "\\'");
		String html = "<!doctype html><html><body>"
				+ "<script>"
				+ "(function(){try{"
				+ "  if(window.opener){window.opener.postMessage({type:'oauth_result',ok:" + ok + ",message:'" + safeMsg + "'},'*');}"
				+ "}catch(e){}"
				+ "window.close();"
				+ "})();"
				+ "</script>"
				+ "<p>" + (ok ? "Authorisation successful. You can close this window." : "Authorisation failed: " + safeMsg) + "</p>"
				+ "</body></html>";
		PrintWriter out = res.getWriter();
		out.print(html);
		out.flush();
	}

	private void writeJson(HttpServletResponse res, int status, Object body) throws IOException {
		res.setStatus(status);
		res.setContentType("application/json");
		res.setCharacterEncoding("UTF-8");
		PrintWriter out = res.getWriter();
		out.print(mapper.writeValueAsString(body));
		out.flush();
	}

	private void writeError(HttpServletResponse res, int status, String message) throws IOException {
		Map<String, Object> body = new HashMap<>();
		body.put("error", message);
		writeJson(res, status, body);
	}
}
