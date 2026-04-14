package controller;

import java.util.ArrayList;

import model.AuthProfile;
import model.Field;
import com.fasterxml.jackson.databind.ObjectMapper;


public class AuthPrController {
	
	public static void main(String[] args) {
   	 initialize();
   }
	   
    ArrayList<AuthProfile> AuthProfiles	 = new ArrayList<AuthProfile>();
    
    public static void initialize() {
    	
    	
    	ObjectMapper mapper = new ObjectMapper();
    	
    	List<AuthType> authTypes = mapper.readValue(
    		    new File("config.json"),
    		    new TypeReference<List<AuthType>>() {}
    		);
    	

    	    for (AuthType auth : profile.getAuthTypes()) {
    	        System.out.println("  AuthType: " + auth.getType());

    	        for (Field field : auth.getFields()) {
    	            System.out.println("    Field: " + field.getName());

    	            if (field.getOptions() != null && !field.getOptions().isEmpty()) {
    	                for (Options opt : field.getOptions()) {
    	                    System.out.println("      Option: " + opt.getLabel() + " (" + opt.getValue() + ")");
    	                }
    	            }
    	    }
    	 
    	}
    }
    
    
    
}
