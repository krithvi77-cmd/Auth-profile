package controller;

import java.util.*;
import model.*;

import dao.AuthTypeDAO;
import dao.FieldDAO;

public class ApiServlet {
	
	AuthTypeDAO authData = new AuthTypeDAO();
	FieldDAO fieldData =  new FieldDAO();

	public List<AuthType> getAllAuth() throws Exception {
		List<AuthType> authtypes = authData.getAll();
		for(AuthType atf : authtypes) {
			atf.setFields(fieldData.getByAuthType(atf.getId()));
		}
		return authtypes;

	}
   
}
