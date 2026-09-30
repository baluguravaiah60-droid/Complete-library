package com.library.controller;

import com.library.dao.UserDAO;
import com.library.model.User;
import com.library.util.Json;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(urlPatterns = {"/api/login", "/api/logout", "/api/me"})
public class AuthServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(AuthServlet.class.getName());
    private final UserDAO users = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getServletPath().equals("/api/logout")) {
            HttpSession s = req.getSession(false);
            if (s != null) s.invalidate();
            Json.send(resp, 200, Map.of("ok", true));
            return;
        }
        String username = trim(req.getParameter("username"));
        String password = req.getParameter("password");
        String role = trim(req.getParameter("role")).toUpperCase();
        if (username.isEmpty() || password == null || password.isEmpty() || !role.matches("ADMIN|LIBRARIAN|MEMBER")) {
            Json.error(resp, 400, "Enter username, password and role.");
            return;
        }
        try {
            User u = users.authenticate(username, password, role);
            if (u == null) { Json.error(resp, 401, "Wrong username, password or role."); return; }
            HttpSession old = req.getSession(false);
            if (old != null) old.invalidate(); // new session id after login
            req.getSession(true).setAttribute("user", u);
            Json.send(resp, 200, u);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Login failed", e);
            Json.error(resp, 500, "Service unavailable. Try again shortly.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession(false);
        Object u = s == null ? null : s.getAttribute("user");
        if (u == null) Json.error(resp, 401, "Not signed in.");
        else Json.send(resp, 200, u);
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
}
