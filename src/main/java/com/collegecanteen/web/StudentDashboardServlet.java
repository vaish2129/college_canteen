package com.collegecanteen.web;

import com.collegecanteen.dao.OrderDAO;
import com.collegecanteen.model.Order;
import com.collegecanteen.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/student")
public class StudentDashboardServlet extends HttpServlet {
    private final OrderDAO orders = new OrderDAO();
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!WebUtil.requireLogin(req, resp)) return;
        if (WebUtil.admin(req)) { resp.sendRedirect(req.getContextPath() + "/admin"); return; }
        User user = WebUtil.user(req);
        List<Order> all = orders.getOrdersByStudent(user.getId());
        long active = all.stream().filter(o -> !"COMPLETED".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus())).count();
        BigDecimal spent = all.stream().filter(o -> "COMPLETED".equals(o.getStatus())).map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        req.setAttribute("orders", all.stream().limit(3).toList());
        req.setAttribute("orderCount", all.size()); req.setAttribute("activeOrders", active);
        req.setAttribute("totalSpent", spent); req.setAttribute("prepMinutes", orders.getEstimatedPreparationTime(user.getId()));
        req.getRequestDispatcher("/WEB-INF/jsp/student.jsp").forward(req, resp);
    }
}
