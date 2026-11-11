package com.collegecanteen.web;
import com.collegecanteen.dao.FoodDAO;
import com.collegecanteen.model.Cart;
import com.collegecanteen.model.CartItem;
import com.collegecanteen.model.FoodItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet("/cart")
public class CartServlet extends HttpServlet {
 private final FoodDAO foods=new FoodDAO();
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException { req.setAttribute("cart",cart(req)); req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp").forward(req,resp); }
 @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
   if(!WebUtil.requireLogin(req,resp)) return; if(WebUtil.admin(req)){resp.sendError(HttpServletResponse.SC_FORBIDDEN);return;} req.setCharacterEncoding("UTF-8"); Cart cart=cart(req); String action=req.getParameter("action");
   try { int id=Integer.parseInt(req.getParameter("foodId"));
     if("add".equals(action)) { FoodItem food=foods.findById(id); int q=Integer.parseInt(req.getParameter("quantity")); int current=cart.getItems().stream().filter(item->item.getFoodId()==id).mapToInt(CartItem::getQuantity).sum(); if(food==null||!food.isAvailable()||q<1||q+current>food.getStockQuantity()) throw new IllegalArgumentException(); cart.addItem(new CartItem(id,food.getName(),food.getPrice(),q,food.getImageUrl())); }
     else if("remove".equals(action)) cart.removeItem(id);
     else if("update".equals(action)) { int q=Integer.parseInt(req.getParameter("quantity")); FoodItem food=foods.findById(id); if(q<1) cart.removeItem(id); else if(food==null||q>food.getStockQuantity()) throw new IllegalArgumentException(); else cart.updateQuantity(id,q); }
   } catch(RuntimeException e) { req.getSession().setAttribute("flashError","Please choose a valid quantity within the current stock."); }
   resp.sendRedirect(req.getContextPath()+"/cart");
 }
 @SuppressWarnings("unchecked") private Cart cart(HttpServletRequest req) { Cart c=(Cart)req.getSession().getAttribute("cart"); if(c==null){c=new Cart();req.getSession().setAttribute("cart",c);} return c; }
}

