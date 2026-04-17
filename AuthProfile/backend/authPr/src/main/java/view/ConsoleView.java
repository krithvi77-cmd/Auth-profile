package view;

import java.util.List;
import java.util.Scanner;

import model.AuthType;
import model.Field;
import model.Option;

public class ConsoleView {

	Scanner user = new Scanner(System.in);

	public int dashBoard() {
		System.out.println("1) Create auth profile");
		int input = user.nextInt();
		return input;
	}

	public int listAuthProfile(List<AuthType> authTypes) {
		int count = 1;
		for (AuthType auth : authTypes) {
			System.out.println(count++ +" AuthType: " + auth.getName());
		}
		System.out.println("Please select the type");
		int a = user.nextInt();
		user.nextLine();
		return a;

	}

	public String getFieldValue(Field field) {
		System.out.println("Enter the value for " + field.getLabel());
		if (field.getType().equals("String")) {
			return user.nextLine();
		} else if (field.getType().equals("select")) {
			int count = 1;
			for (Option opt : field.getOptions()) {
				System.out.println(count++ + opt.getLabel());
			}
			String value = field.getOptions().get(user.nextInt()).getValue();
			user.nextLine();
			return value;
		}
		return null;
	}

	public String getValue(String string) {
		System.out.println("please Enter the value for " + string);
		return user.nextLine();
	}

}
