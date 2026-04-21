package controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.AuthProfile;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionServlet extends HttpServlet {


    @Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
                res.setStatus(201);
                res.setContentType("application/json");
                res.setCharacterEncoding("UTF-8");
                PrintWriter out = res.getWriter();
                out.print("{\"key\":\"value\"}");
                out.flush();
	}

}
