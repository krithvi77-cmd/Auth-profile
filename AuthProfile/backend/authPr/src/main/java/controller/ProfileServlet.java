package controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import dao.ProfileDAO;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.AuthProfile;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileServlet extends HttpServlet {

	private final ProfileDAO dao = new ProfileDAO();
	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		try {
			Integer id = pathId(req);
			if (id == null) {
				List<AuthProfile> all = dao.getAll();
				writeJson(res, 200, all);
			} else {
				AuthProfile p = dao.getById(id);
				if (p == null)
					writeError(res, 404, "Profile not found");
				else {
					writeJson(res, 200, p);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			writeError(res, 500, e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
		try {
			AuthProfile profile = mapper.readValue(req.getInputStream(), AuthProfile.class);

			String err = validate(profile);
			if (err != null) {
				writeError(res, 400, err);
				return;
			}

			int id = dao.create(profile);
			AuthProfile saved = dao.getById(id);
			writeJson(res, 201, saved);
		} catch (Exception e) {
			e.printStackTrace();
			writeError(res, 500, e.getMessage());
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
		try {
			Integer id = pathId(req);
			if (id == null) {
				writeError(res, 400, "Missing id in URL");
				return;
			}

			AuthProfile profile = mapper.readValue(req.getInputStream(), AuthProfile.class);
			profile.setId(id);

			String err = validate(profile);
			if (err != null) {
				writeError(res, 400, err);
				return;
			}

			boolean ok = dao.update(profile);
			if (!ok) {
				writeError(res, 404, "Profile not found");
				return;
			}

			writeJson(res, 200, dao.getById(id));
		} catch (Exception e) {
			e.printStackTrace();
			writeError(res, 500, e.getMessage());
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
		try {
			Integer id = pathId(req);
			if (id == null) {
				writeError(res, 400, "Missing id in URL");
				return;
			}

			boolean ok = dao.delete(id);
			if (!ok) {
				writeError(res, 404, "Profile not found");
				return;
			}

			Map<String, Object> body = new HashMap<>();
			body.put("success", true);
			body.put("id", id);
			writeJson(res, 200, body);
		} catch (Exception e) {
			e.printStackTrace();
			writeError(res, 500, e.getMessage());
		}
	}

	private Integer pathId(HttpServletRequest req) {
		String info = req.getPathInfo();
		if (info == null || info.equals("/"))
			return null;
		try {
			return Integer.parseInt(info.replaceAll("^/+", "").split("/")[0]);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private String validate(AuthProfile p) {
		if (p == null)
			return "Request body is empty";
		if (p.getName() == null || p.getName().trim().isEmpty())
			return "name is required";
		if (p.getAuthType() <= 0)
			return "auth_type is required";
		return null;
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
