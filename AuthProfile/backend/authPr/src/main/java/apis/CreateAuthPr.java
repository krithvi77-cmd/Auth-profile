package apis;

import java.io.PrintWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.ApiServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CreateAuthPr extends HttpServlet {
	
	public void doPost(HttpServletRequest request, HttpServletResponse response){
	     ApiServlet  controller =  new ApiServlet(); 	        
	     try {
	    	 
	    	 
	    	 
	    	 
	    	 
	    	 
	    	 
	    	 
	    	 
	    	 
	    	 
	    	 ObjectMapper mapper = new ObjectMapper();
	    	
	    	 String jsonString =  mapper.writeValueAsString(controller.getAllAuth());
	    	 System.out.println(jsonString);
	    	 response.setContentType("application/json");
	         response.setCharacterEncoding("UTF-8");
	         PrintWriter out = response.getWriter();
	         out.print(jsonString);
	         out.flush();
	    	 
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

}
