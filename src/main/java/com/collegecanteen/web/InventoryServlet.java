package com.collegecanteen.web;
import com.collegecanteen.dao.FoodDAO;
import com.collegecanteen.model.FoodItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
@WebServlet("/inventory")
public class InventoryServlet extends HttpServlet {
 private final FoodDAO foods=new FoodDAO();
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
   if(!WebUtil.requireAdmin(req,resp)) return;
   req.setAttribute("foodItems",foods.getAllFoodItems()); req.getRequestDispatcher("/WEB-INF/jsp/inventory.jsp").forward(req,resp);
 }
 @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
   if(!WebUtil.requireAdmin(req,resp)) return;
   try {
     String action=req.getParameter("action");
     if("delete".equals(action)) foods.deleteFoodItem(integer(req,"id"));
     else {
       FoodItem f=new FoodItem(); f.setName(req.getParameter("name").trim()); f.setCategory(req.getParameter("category").trim()); f.setPrice(new BigDecimal(req.getParameter("price")));
       f.setStockQuantity(integer(req,"stockQuantity")); f.setImageUrl(imageUrl(req.getParameter("imageUrl"))); f.setDescription(empty(req.getParameter("description")));
       f.setPreparationTimeMinutes(Math.max(1,integer(req,"preparationTimeMinutes"))); f.setAvailable(f.getStockQuantity()>0 && req.getParameter("available")!=null);
       if(f.getName().isBlank()||f.getCategory().isBlank()||f.getPrice().signum()<0||f.getStockQuantity()<0) throw new IllegalArgumentException();
       if("update".equals(action)){ f.setId(integer(req,"id")); foods.updateFoodItem(f); } else foods.addFoodItem(f);
     }
     req.getSession().setAttribute("flashSuccess","Inventory saved.");
   } catch(RuntimeException e) { req.getSession().setAttribute("flashError","Check item details and try again."); }
   resp.sendRedirect(req.getContextPath()+"/inventory");
 }
 private int integer(HttpServletRequest req,String n){return Integer.parseInt(req.getParameter(n));}
 private String empty(String value){return value==null||value.isBlank()?null:value.trim();}
 private String imageUrl(String value){ String url=empty(value); if(url==null) return null; java.net.URI uri=java.net.URI.create(url); if(!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme())) throw new IllegalArgumentException("Image URL must use HTTP or HTTPS"); return url; }
}

