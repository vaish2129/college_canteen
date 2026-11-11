package com.collegecanteen.web;
import com.collegecanteen.dao.UserDAO;
import com.collegecanteen.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
 private final UserDAO users = new UserDAO();
 @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
   if (WebUtil.user(req) != null) { resp.sendRedirect(req.getContextPath() + (WebUtil.admin(req) ? "/admin" : "/student")); return; }
   req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
 }
 @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
   User user = users.authenticate(value(req,"username").trim(), value(req,"password"));
   if (user == null) { req.setAttribute("error", "Username or password is incorrect."); req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp); return; }
   req.changeSessionId(); req.getSession(true).setAttribute("user", user);
   resp.sendRedirect(req.getContextPath() + ("ADMIN".equalsIgnoreCase(user.getRole()) ? "/admin" : "/student"));
 }
 private String value(HttpServletRequest req,String key) { String v=req.getParameter(key); return v == null ? "" : v; }
}
