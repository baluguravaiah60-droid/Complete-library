package com.library.dao;

import com.library.config.DBConnection;
import com.library.model.Book;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private static final String SELECT =
        "SELECT b.id,b.isbn,b.title,b.author,b.publisher,b.publication_year,c.name AS category,"
      + "b.total_copies,b.available_copies,b.shelf FROM books b JOIN categories c ON c.id=b.category_id ";

    public List<Book> search(String q, String category, boolean availableOnly) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1=1 ");
        List<String> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append("AND (b.title LIKE ? OR b.author LIKE ? OR b.isbn LIKE ? OR c.name LIKE ?) ");
            String like = "%" + q.trim() + "%";
            for (int i = 0; i < 4; i++) params.add(like);
        }
        if (category != null && !category.isBlank()) { sql.append("AND c.name = ? "); params.add(category); }
        if (availableOnly) sql.append("AND b.available_copies > 0 ");
        sql.append("ORDER BY b.title LIMIT 100");
        List<Book> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setString(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Book b = new Book();
                    b.id = rs.getInt("id");
                    b.isbn = rs.getString("isbn");
                    b.title = rs.getString("title");
                    b.author = rs.getString("author");
                    b.publisher = rs.getString("publisher");
                    int y = rs.getInt("publication_year");
                    b.publicationYear = rs.wasNull() ? null : y;
                    b.category = rs.getString("category");
                    b.totalCopies = rs.getInt("total_copies");
                    b.availableCopies = rs.getInt("available_copies");
                    b.shelf = rs.getString("shelf");
                    out.add(b);
                }
            }
        }
        return out;
    }

    public List<String> categories() throws SQLException {
        List<String> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM categories ORDER BY name")) {
            while (rs.next()) out.add(rs.getString(1));
        }
        return out;
    }

    /** Throws SQLIntegrityConstraintViolationException on duplicate ISBN or unknown category. */
    public void add(String isbn, String title, String author, String publisher, Integer year,
                    String category, int copies, String shelf) throws SQLException {
        String sql = "INSERT INTO books (isbn,title,author,publisher,publication_year,category_id,total_copies,available_copies,shelf) "
                   + "VALUES (?,?,?,?,?,(SELECT id FROM categories WHERE name=?),?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ps.setString(2, title);
            ps.setString(3, author);
            ps.setString(4, publisher);
            if (year == null) ps.setNull(5, Types.SMALLINT); else ps.setInt(5, year);
            ps.setString(6, category);
            ps.setInt(7, copies);
            ps.setInt(8, copies);
            ps.setString(9, shelf);
            ps.executeUpdate();
        }
    }
}
