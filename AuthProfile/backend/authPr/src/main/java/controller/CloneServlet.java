package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import clone.CloneHandler;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CloneServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final ObjectMapper mapper = new ObjectMapper();
	private final CloneHandler cloneHandler = new CloneHandler();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
		Integer id = parseId(req.getPathInfo());
		if (id == null) {
			writeError(res, 400, "Invalid connection id");
			return;
		}

		try {
			Map<String, Object> body = cloneHandler.handle(id);
			writeJson(res, 201, body);
		} catch (IllegalArgumentException bad) {
			writeError(res, 400, bad.getMessage());
		} catch (IllegalStateException notFound) {
			writeError(res, 404, notFound.getMessage());
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
		if (slash >= 0) {
			trimmed = trimmed.substring(0, slash);
		}
		try {
			return Integer.parseInt(trimmed);
		} catch (NumberFormatException ex) {
			return null;
		}
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
