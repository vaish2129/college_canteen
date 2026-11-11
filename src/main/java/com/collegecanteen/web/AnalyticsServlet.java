package com.collegecanteen.web;
import com.collegecanteen.dao.OrderDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet("/analytics")
public class AnalyticsServlet extends HttpServlet {
 private final OrderDAO orders=new OrderDAO();
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
   if(!WebUtil.requireAdmin(req,resp)) return; req.setAttribute("analytics",orders.getSalesAnalytics());
   req.getRequestDispatcher("/WEB-INF/jsp/analytics.jsp").forward(req,resp);
 }
}
