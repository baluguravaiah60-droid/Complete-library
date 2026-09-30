package com.library.dao;

import com.library.config.DBConnection;
import com.library.model.User;
import com.library.util.PasswordUtil;
import java.sql.*;

public class UserDAO {

    /** Returns the user if username, password and role match and the account is active; otherwise null. */
    public User authenticate(String username, String password, String role) throws SQLException {
        String sql = "SELECT id, username, full_name, password_hash, role FROM users WHERE username = ? AND role = ? AND status = 'ACTIVE'";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, role);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !PasswordUtil.matches(password, rs.getString("password_hash"))) return null;
                User u = new User();
                u.id = rs.getInt("id");
                u.username = rs.getString("username");
                u.fullName = rs.getString("full_name");
                u.role = rs.getString("role");
                return u;
            }
        }
    }

    public int count() throws SQLException {
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Creates a user (and a members row for MEMBER) in one transaction. */
    public void create(String username, String email, String fullName, String password, String role) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO users (username,email,full_name,password_hash,role) VALUES (?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, email);
                    ps.setString(3, fullName);
                    ps.setString(4, PasswordUtil.hash(password));
                    ps.setString(5, role);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { k.next(); userId = k.getInt(1); }
                }
                if ("MEMBER".equals(role)) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO members (user_id, membership_expiry) VALUES (?, DATE_ADD(CURDATE(), INTERVAL 1 YEAR))")) {
                        ps.setInt(1, userId);
                        ps.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
