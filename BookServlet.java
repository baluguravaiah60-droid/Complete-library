package com.library.controller;

import com.library.dao.BookDAO;
import com.library.model.User;
import com.library.util.Json;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(urlPatterns = {"/api/books", "/api/categories"})
public class BookServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(BookServlet.class.getName());
    private final BookDAO books = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if (req.getServletPath().equals("/api/categories")) {
                Json.send(resp, 200, books.categories());
                return;
            }
            Json.send(resp, 200, books.search(req.getParameter("q"), req.getParameter("category"),
                    "true".equals(req.getParameter("available"))));
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Book query failed", e);
            Json.error(resp, 500, "Could not load books.");
        }
    }

    /** Add a book. Admin and librarian only. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User u = (User) req.getSession().getAttribute("user");
        if (u == null || u.role.equals("MEMBER")) { Json.error(resp, 403, "Only staff can add books."); return; }
        String isbn = p(req, "isbn"), title = p(req, "title"), author = p(req, "author");
        String category = p(req, "category");
        int copies;
        Integer year = null;
        try {
            copies = Integer.parseInt(p(req, "copies"));
            if (!p(req, "year").isEmpty()) year = Integer.parseInt(p(req, "year"));
        } catch (NumberFormatException e) { Json.error(resp, 400, "Copies and year must be numbers."); return; }
        if (!isbn.matches("\\d{10}|\\d{13}")) { Json.error(resp, 400, "ISBN must be 10 or 13 digits."); return; }
        if (title.isEmpty() || author.isEmpty() || category.isEmpty()) { Json.error(resp, 400, "Title, author and category are required."); return; }
        if (copies < 1) { Json.error(resp, 400, "Copies must be at least 1."); return; }
        try {
            books.add(isbn, title, author, p(req, "publisher"), year, category, copies, p(req, "shelf"));
            Json.send(resp, 201, Map.of("ok", true));
        } catch (SQLIntegrityConstraintViolationException e) {
            Json.error(resp, 409, "A book with this ISBN already exists, or the category is unknown.");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Add book failed", e);
            Json.error(resp, 500, "Could not save the book.");
        }
    }

    private static String p(HttpServletRequest r, String k) { String v = r.getParameter(k); return v == null ? "" : v.trim(); }
}
