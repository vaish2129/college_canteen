package com.collegecanteen.dao;

import com.collegecanteen.model.Admin;
import com.collegecanteen.model.Student;
import com.collegecanteen.model.User;
import com.collegecanteen.util.DBUtil;
import com.collegecanteen.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            User user;
            String stored;
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                stored = rs.getString("password");
                if (!PasswordUtil.verify(password, stored)) return null;
                user = "ADMIN".equalsIgnoreCase(rs.getString("role")) ? new Admin() : new Student();
                user.setId(rs.getInt("id")); user.setUsername(rs.getString("username")); user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email")); user.setPhone(rs.getString("phone")); user.setRole(rs.getString("role"));
            }
            if (!stored.startsWith("pbkdf2$")) {
                try (PreparedStatement update = conn.prepareStatement("UPDATE users SET password = ? WHERE id = ?")) {
                    update.setString(1, PasswordUtil.hash(password)); update.setInt(2, user.getId()); update.executeUpdate();
                }
            }
            return user;
        } catch (SQLException e) { throw new RuntimeException("Database authentication failed", e); }
    }
    public boolean registerStudent(Student student) {
        String userSql = "INSERT INTO users (username, password, full_name, email, phone, role) VALUES (?, ?, ?, ?, ?, 'STUDENT')";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, student.getUsername()); ps.setString(2, PasswordUtil.hash(student.getPassword()));
                ps.setString(3, student.getFullName()); ps.setString(4, student.getEmail()); ps.setString(5, student.getPhone());
                ps.executeUpdate();
                int userId;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("User ID was not generated");
                    userId = keys.getInt(1);
                }
                try (PreparedStatement profile = conn.prepareStatement("INSERT INTO students (user_id, department, year_level, student_id) VALUES (?, ?, ?, ?)")) {
                    profile.setInt(1, userId); profile.setString(2, student.getDepartment()); profile.setString(3, student.getYear()); profile.setString(4, student.getStudentId()); profile.executeUpdate();
                }
                conn.commit(); return true;
            } catch (SQLException | RuntimeException e) { conn.rollback(); throw e; }
        } catch (SQLException e) { throw new RuntimeException("Student registration failed", e); }
    }

    public User findByUsername(String username) {
        String sql = "SELECT id, username, full_name, email, phone, role FROM users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) { User u = new User(); u.setId(rs.getInt("id")); u.setUsername(rs.getString("username")); u.setFullName(rs.getString("full_name")); u.setEmail(rs.getString("email")); u.setPhone(rs.getString("phone")); u.setRole(rs.getString("role")); return u; } }
        } catch (SQLException e) { throw new RuntimeException("User lookup failed", e); }
        return null;
    }

    public List<Student> getStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.full_name, u.email, u.phone, s.department, s.year_level, s.student_id FROM users u JOIN students s ON s.user_id = u.id WHERE u.role = 'STUDENT' ORDER BY u.full_name";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { Student s = new Student(); s.setId(rs.getInt("id")); s.setUsername(rs.getString("username")); s.setFullName(rs.getString("full_name")); s.setEmail(rs.getString("email")); s.setPhone(rs.getString("phone")); s.setDepartment(rs.getString("department")); s.setYear(rs.getString("year_level")); s.setStudentId(rs.getString("student_id")); students.add(s); }
        } catch (SQLException e) { throw new RuntimeException("Student lookup failed", e); }
        return students;
    }
}
