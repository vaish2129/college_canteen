package com.collegecanteen.web;
import com.collegecanteen.dao.FoodDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet("/menu")
public class MenuServlet extends HttpServlet {
 private final FoodDAO foods = new FoodDAO();
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
   req.setAttribute("foods",foods.getAvailableFoodItems()); req.setAttribute("categories",foods.getCategories()); req.getRequestDispatcher("/WEB-INF/jsp/menu.jsp").forward(req,resp);
 }
}
