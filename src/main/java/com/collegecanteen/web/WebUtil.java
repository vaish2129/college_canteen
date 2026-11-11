package com.collegecanteen.web;

import com.collegecanteen.model.User;
import jakarta.servlet.http.HttpServletRequest;

public final class WebUtil {
    private WebUtil() { }
    public static User user(HttpServletRequest request) {
        return request.getSession(false) == null ? null : (User) request.getSession(false).getAttribute("user");
    }
    public static boolean admin(HttpServletRequest request) {
        User user = user(request);
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
    public static boolean requireLogin(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        if (user(request) != null) return true;
        response.sendRedirect(request.getContextPath() + "/login"); return false;
    }
    public static boolean requireAdmin(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        if (admin(request)) return true;
        response.sendError(user(request) == null ? 401 : 403); return false;
    }
}
