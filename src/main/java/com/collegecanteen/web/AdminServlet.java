package com.collegecanteen.web;
import com.collegecanteen.dao.FoodDAO;
import com.collegecanteen.dao.OrderDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
 private final OrderDAO orders=new OrderDAO(); private final FoodDAO foods=new FoodDAO();
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
   if(!WebUtil.requireAdmin(req,resp)) return;
   req.setAttribute("analytics",orders.getSalesAnalytics()); req.setAttribute("orders",orders.getAllOrders()); req.setAttribute("lowStock",orders.getLowStockItems());
   req.setAttribute("foodItems",foods.getAllFoodItems()); req.getRequestDispatcher("/WEB-INF/jsp/admin.jsp").forward(req,resp);
 }
}
