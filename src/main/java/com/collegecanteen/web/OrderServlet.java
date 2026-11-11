package com.collegecanteen.web;

import com.collegecanteen.dao.OrderDAO;
import com.collegecanteen.model.Cart;
import com.collegecanteen.model.Order;
import com.collegecanteen.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@WebServlet("/orders")
public class OrderServlet extends HttpServlet {
    private final OrderDAO orders = new OrderDAO();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!WebUtil.requireLogin(req, resp)) return;
        User user = WebUtil.user(req);
        req.setAttribute("orders", WebUtil.admin(req) ? orders.getAllOrders() : orders.getOrdersByStudent(user.getId()));
        req.getRequestDispatcher("/WEB-INF/jsp/orders.jsp").forward(req, resp);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!WebUtil.requireLogin(req, resp)) return;
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        try {
            if ("checkout".equals(action)) {
                if (WebUtil.admin(req)) throw new IllegalArgumentException("Only student accounts can place orders.");
                Cart cart = (Cart) req.getSession().getAttribute("cart");
                String method = req.getParameter("paymentMethod");
                if (cart == null || cart.isEmpty() || !("CASH".equals(method) || "DEMO_ONLINE".equals(method)))
                    throw new IllegalArgumentException("Your cart is empty or payment method is invalid.");
                orders.placeOrder(WebUtil.user(req), cart, method);
                cart.clear();
                req.getSession().setAttribute("flashSuccess", "Order placed. Track its progress here.");
                resp.sendRedirect(req.getContextPath() + "/orders");
                return;
            }
            int id = Integer.parseInt(req.getParameter("orderId"));
            Order order = orders.findById(id);
            if (order == null) throw new IllegalArgumentException("Order not found.");
            if (WebUtil.admin(req)) {
                String status = req.getParameter("status");
                if (!Set.of("PENDING", "ACCEPTED", "PREPARING", "READY", "COMPLETED", "CANCELLED").contains(status))
                    throw new IllegalArgumentException("Invalid order status.");
                if (!orders.updateOrderStatus(id, status)) throw new IllegalArgumentException("Order status could not be updated.");
            } else if (order.getStudentId() != WebUtil.user(req).getId() || !"cancel".equals(action) || !orders.cancelOrder(id)) {
                throw new IllegalArgumentException("This order cannot be cancelled.");
            }
            req.getSession().setAttribute("flashSuccess", "Order updated.");
        } catch (RuntimeException e) {
            req.getSession().setAttribute("flashError", e.getMessage() == null ? "Unable to update order." : e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/orders");
    }
}
