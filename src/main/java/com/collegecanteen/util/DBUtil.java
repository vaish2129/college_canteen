package com.collegecanteen.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DBUtil {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/college_canteen_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private DBUtil() { }
    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new ExceptionInInitializerError(e); }
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(System.getenv().getOrDefault("CANTEEN_DB_URL", DEFAULT_URL),
                System.getenv().getOrDefault("CANTEEN_DB_USER", "root"), System.getenv().getOrDefault("CANTEEN_DB_PASSWORD", "root"));
    }
    public static void closeResources(ResultSet rs, PreparedStatement ps, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException ignored) { }
        try { if (ps != null) ps.close(); } catch (SQLException ignored) { }
        try { if (conn != null) conn.close(); } catch (SQLException ignored) { }
    }
}
