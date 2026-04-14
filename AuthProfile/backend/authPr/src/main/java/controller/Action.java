package controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;

import model.*;
import view.ConsoleView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Action {

	ArrayList<AuthProfile> authProfiles = new ArrayList<AuthProfile>();
	ConsoleView cslView = new ConsoleView();
	List<AuthType> authTypes = null;

	public void startApplication() {
		initialize();
		if (cslView.dashBoard() == 1) {
			AuthProfile authpr = createAuthProfile();
			if (authpr == null) {
				return;
			}
			authProfiles.add(authpr);
			System.out.println(authpr.getName());
		}

	}

	public void initialize() {
		ObjectMapper mapper = new ObjectMapper();

		try {

			authTypes = mapper.readValue(new File("config.json"), new TypeReference<List<AuthType>>() {
			});
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public AuthProfile createAuthProfile() {

		if (authTypes == null) {
			System.out.print("no Auth types is present");
			return null;
		}
		int ntype = cslView.listAuthProfile(authTypes);
		AuthType authType = authTypes.get(ntype - 1);
		String createdBy = "krithvi";
		Date updatedOn = new Date();
		updatedOn.getTime();
		Map<Integer, String> authPrValue = setFieldValue(authType);
		if (authType != null && createdBy != null && updatedOn != null && authPrValue != null) {
			return new AuthProfile(authPrValue.get(Integer.valueOf(1)), authType, createdBy, updatedOn, authPrValue);
		}
		return null;
	}

	private Map<Integer, String> setFieldValue(AuthType authType) {
		Map<Integer, String> values = new HashMap<Integer, String>();
		for (Field field : authType.getFields()) {
			String value = cslView.getFieldValue(field);
			values.put(field.getId(), value);
		}
		return values;
	}

	public static void testAuthTypes(List<AuthType> authTypes) {
		for (AuthType auth : authTypes) {
			System.out.println("  AuthType: " + auth.getName());
			for (Field field : auth.getFields()) {
				System.out.println("    Field: " + field.getName());
				if (field.getOptions() != null && !field.getOptions().isEmpty()) {
					for (Option opt : field.getOptions()) {
						System.out.println("      Option: " + opt.getLabel() + " (" + opt.getValue() + ")");
					}
				}
			}

		}
	}

}
