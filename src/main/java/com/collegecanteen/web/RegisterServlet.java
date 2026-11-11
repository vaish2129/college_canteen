package com.collegecanteen.web;

import com.collegecanteen.dao.UserDAO;
import com.collegecanteen.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final UserDAO users = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String username = value(req, "username").trim();
        String password = value(req, "password");
        String fullName = value(req, "fullName").trim();
        String email = value(req, "email").trim();

        if (username.length() < 3 || password.length() < 8 || fullName.isBlank() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            req.setAttribute("error", "Enter a name, valid email, username (3+ characters), and password (8+ characters).");
            req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
            return;
        }

        Student student = new Student();
        student.setUsername(username);
        student.setPassword(password);
        student.setFullName(fullName);
        student.setEmail(email);
        student.setPhone(value(req, "phone").trim());
        student.setDepartment(value(req, "department").trim());
        student.setYear(value(req, "year").trim());
        student.setStudentId(value(req, "studentId").trim());

        try {
            users.registerStudent(student);
            resp.sendRedirect(req.getContextPath() + "/login?registered=1");
        } catch (RuntimeException e) {
            req.setAttribute("error", "That username may already be registered. Please try another.");
            req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
        }
    }

    private String value(HttpServletRequest req, String key) {
        String v = req.getParameter(key);
        return v == null ? "" : v;
    }
}
