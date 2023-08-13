package Client;

import HelloApp.*;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.omg.CORBA.*;

public class HelloClient {
	static Hello helloImpl;

	public static void main(String args[]) throws Exception {
		try {
			
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// get the root naming context
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			// Use NamingContextExt instead of NamingContext. This is
			// part of the Interoperable naming Service.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// resolve the Object Reference in Naming
			String name = "Hello";
			helloImpl = HelloHelper.narrow(ncRef.resolve_str(name));

//			System.out.println("Obtained a handle on server object: " + helloImpl);
			System.out.println(helloImpl.sayHello());
			boolean[] authStatus = {false};
			String[] userData = new String[2];
			int[] quitValue = { 1 };
			if(args.length != 4) quitValue[0] = 0;
			do {
				
				authStatus[0] = authenticationProcess(userData, args);
				
				String userType = userData[1].compareTo("C") == 0 ? "Customer" : "Manager";

				System.out.println("Welcome " + userType + " " + userData[0]);
				
				

				while (quitValue[0] != 0) {
					try {
						if (userData[1].compareTo("C") == 0)
							getCustomerOptions(quitValue, userData,authStatus);
						else
							getManagerOptions(quitValue, userData,authStatus);
					} catch (Error r) {
						System.out.println("Some error occurred! Please try again!");
					} catch (Exception e) {
						System.out.println("Some error occurred! Please try again!");
					}
				}
				
				if(quitValue[0] == 0 && args.length == 4) {
					quitValue[0] = 1;
				}
				if( args.length != 4 ) return;
			} while (authStatus[0] == false);

			

			// helloImpl.shutdown();

		} catch (Exception e) {
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
			if(args.length != 4) {
				throw e;
			}
		}
	}

	private static void getManagerOptions(int[] quitValue, String[] userData, boolean[] authStatus) throws IOException {
		// TODO Auto-generated method stub
		String username = userData[0];
		
		String menu = helloImpl.getMenu();
		
		Map<String, Integer> menuValues = stringToMap(menu, String.class, Integer.class);
		System.out.println("////////////////////////////////////////////////");
		System.out.println("////////////////////////////////////////////////");
		System.out.println("Please select you options: ");
		System.out.println("[1] Show current Orders\n[2]Quit");
		System.out.println("////////////////////////////////////////////////");
		System.out.println("////////////////////////////////////////////////");
		int userInput = getNumInput();
		switch (userInput) {
		case 1:
			System.out.println("Current orders");
			long startTimeMenu = System.currentTimeMillis();
			String orderStatusValues =  helloImpl.getAllActiveOrders() ;
			long endTimeMenu = System.currentTimeMillis();
			String[] orderStatusValuesMaps = orderStatusValues.split("\n+");
			float grandTotal = (float) 0.0;
			int updateCost = 0;
			for (int i = 0; i < orderStatusValuesMaps.length; i++) {
				HashMap<String, Integer> statusHash = stringToMap(orderStatusValuesMaps[i], String.class,
						Integer.class);
				String orderValus = "";
				float totalCost = 0;
				String orderForUser = "";
				for (Map.Entry<String, Integer> entry : statusHash.entrySet()) {
					String key = entry.getKey();
					Integer value = entry.getValue();
					if (value == -1 || value == -2) {
						orderForUser = key;
						continue;
					}
					orderValus += " "+key+": "+value.toString()+" ";
					totalCost += value* menuValues.get(key);
					updateCost += 1;
					//System.out.println((i + 1) + " \t\t\t " + key + " \t\t\t " + value.toString());
				}
				grandTotal += totalCost;
				if(updateCost > 0)
				System.out.println( "Order User: "+orderForUser+" Order No.: "+(i+1)+orderValus+" and cost for this order is: $"+totalCost );
				else System.out.println("No orders present");
				System.out.println("Time elapsed get active orders: "+ (endTimeMenu - startTimeMenu));
			}
			System.out.println("Your grand total is: $"+grandTotal);
			break;
		case 2:
			System.out.println("Exit");
			quitValue[0] = 0;
			authStatus[0] = false;
			return;
		default:
			System.out.println("Please select correct option.");
			return;
		}

	}

	public static <T> T convert(String str, Class<T> clazz) {
		if (clazz == String.class) {
			return clazz.cast(str);
		} else if (clazz == Integer.class) {
			return clazz.cast(Integer.valueOf(str));
		} else if (clazz == Double.class) {
			return clazz.cast(Double.valueOf(str));
		}

		// Throw an exception if the class is not supported
		throw new IllegalArgumentException("Unsupported class: " + clazz.getName());
	}

	public static <K, V> HashMap<K, V> stringToMap(String str, Class<K> keyClass, Class<V> valueClass) {
		// Remove the curly braces and split the string into key-value pairs
		if( str.length() == 0 ) return new HashMap<K, V>();
		String[] keyValuePairs = str.replaceAll("[{}]", "").split(",");
		
		// Create a new HashMap
		HashMap<K, V> map = new HashMap<>();
		// Loop through the key-value pairs and add them to the HashMap
		for (String pair : keyValuePairs) {
			String[] entry = pair.split("=");
			K key = convert(entry[0].replaceAll("\n", "").trim(), keyClass);
			V value = convert(entry[1].replaceAll("\n", "").trim(), valueClass);
			map.put(key, value);
		}
		// Return the HashMap
		return map;
	}

	public static int getNumInput() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String s = br.readLine();
		int choice = -1;
		try {
			choice = Integer.valueOf(s);
		} catch (NumberFormatException e) {
			System.out.println("Input is not a number. Try again.");
			return choice;
		}
		if (choice < 0) {
			System.out.println("Negative input. Try again.");
			choice = -1;
		}
		return choice;
	}

	public static String getStringInput() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String s = br.readLine();
		return s;
	}

	public static boolean authenticationProcess(String[] userData, String[] args) throws Exception {
		System.out.println("Please select your role: \n[1] Customer \n[2] Manager");
		
		Random r = new Random();
		int low = 1;
		int high = 3;
		int result = r.nextInt(high-low) + low;

		String userNameToGive = result == 1 ? "peeyush" : "john";
		String passwordToGive = result == 1 ? "Hello" : "pass";
		
		if(args.length != 4) {
			
			
			try {
				int userInput = result;
				System.out.print("Please provide your username: ");
				String username = userNameToGive;//getStringInput();
				System.out.print("Please provide password: ");
				String password = passwordToGive; //getStringInput();
				switch (userInput) {
				case 1:
					
					boolean authStatus = helloImpl.authenticate(username, password, "C");
					userData[0] = username;
					userData[1] = "C";
					return authStatus;
				case 2:
					boolean authStatus2 = helloImpl.authenticate(username, password, "M");
					userData[0] = username;
					userData[1] = "M";
					return authStatus2;

				}
			} catch (Exception err) {
				System.out.println("Some error occurred in authentication!");
			}
			
		}else {
			
			try {
				int userInput = getNumInput();
				System.out.print("Please provide your username: ");
				String username = getStringInput();
				System.out.print("Please provide password: ");
				String password = getStringInput();
				long startTimeAuth = System.currentTimeMillis();
				switch (userInput) {
				case 1:
					boolean authStatus = helloImpl.authenticate(username, password, "C");
					userData[0] = username;
					userData[1] = "C";
					long endTimeAuth = System.currentTimeMillis();
					System.out.println("Time for auth: "+( endTimeAuth - startTimeAuth ));
					return authStatus;
				case 2:
					boolean authStatus2 = helloImpl.authenticate(username, password, "M");
					userData[0] = username;
					userData[1] = "M";
					long endTimeAuth2 = System.currentTimeMillis();
					System.out.println("Time for auth: "+( endTimeAuth2 - startTimeAuth ));
					return authStatus2;

				}
			} catch (Exception err) {
				System.out.println("Some error occurred in authentication!");
				throw err;
			}
			
		}
		return false;

	}

	public static void getCustomerOptions(int[] quitValue, String[] userData, boolean[] authStatus) throws Exception {
		String username = userData[0];
		long startTimeMenu = System.currentTimeMillis();
		String menu = helloImpl.getMenu();
		long endTimeMenu = System.currentTimeMillis();
		Map<String, Integer> menuValues = stringToMap(menu, String.class, Integer.class);
		System.out.println("////////////////////////////////////////////////");
		System.out.println("////////////////////////////////////////////////");
		System.out.println("Please select you options: ");
		System.out.println("[1] Show Menu\n[2] Place Order\n[3] Show Order Status\n[4]Quit");
		System.out.println("////////////////////////////////////////////////");
		System.out.println("////////////////////////////////////////////////");
		int userInput = getNumInput();
		switch (userInput) {
		case 1:
			System.out.println("////////////////////////////////////////////////");
			System.out.println("/////////////////////MENU///////////////////////");
			System.out.println("\tItem \t\t\t Cost");
			for (Map.Entry<String, Integer> entry : menuValues.entrySet()) {
				String key = entry.getKey();
				Integer value = entry.getValue();
				System.out.println("\t" + key + " \t\t\t " + '$' + value.toString());
			}
			System.out.println("////////////////////////////////////////////////");
			System.out.println("////////////////////////////////////////////////");
			System.out.println("Time elapsed menu: "+( endTimeMenu - startTimeMenu ));
			break;
		case 2:
			System.out.println("Place order");
			Map<String, Integer> orderValues = new TreeMap<String, Integer>();
			// Map<String, Integer> menuValuesForOrder = stringToMap(menuString,
			// String.class, Integer.class);
			Collection<String> menuItems = menuValues.keySet();
			int itemNumber = -1;
			do {
				System.out.println("////////////////////////////////////////////////");
				System.out.println("////////////////////////////////////////////////");
				System.out.println("Please choose a dish or 0 to exit:");
				int index = 1;
				for (Map.Entry<String, Integer> entry : menuValues.entrySet()) {
					String key = entry.getKey();
					System.out.println(index + " \t\t\t " + key);
					index += 1;
				}
				System.out.println("////////////////////////////////////////////////");
				System.out.println("////////////////////////////////////////////////");
				itemNumber = getNumInput();
				String keySelected = "";
				if (itemNumber > 0 && itemNumber - 1 < menuValues.size()) {
					keySelected = (String) menuItems.toArray()[itemNumber - 1];
				} else {
					break;
				}
				System.out.println("Please select quantity for " + keySelected + " :");
				int qunatity = getNumInput();
				if (qunatity <= 0) {
					continue;
				}
				if (orderValues.containsKey(keySelected)) {
					int oldCount = orderValues.get(keySelected);
					orderValues.put(keySelected, oldCount + qunatity);
				} else {
					orderValues.put(keySelected, qunatity);
				}

			} while (itemNumber != 0);
			if (orderValues.size() > 0) {
				float totalCost = (float) 0.0;
				for (Map.Entry<String, Integer> entry : orderValues.entrySet()) {
					String key = entry.getKey();
					Integer value = entry.getValue();
					totalCost += value* menuValues.get(key);
				}
				String jsonString = orderValues.toString();
				System.out.println("Your Order is: " + jsonString);
				System.out.println("Your total will be: "+totalCost);
				System.out.println("Confirm :\n[1]Yes\n[2]No");
				int confirmation = getNumInput();
				if (confirmation == 1) {
					orderValues.put(username, -1);
					orderValues.put("A", -2);
					jsonString = orderValues.toString();
					long startTimeOrderPost = System.currentTimeMillis();
					boolean op = helloImpl.postOrder(jsonString);
					long endTimeOrderPost = System.currentTimeMillis();

					if (op)
						System.out.println("Order Placed successfully!");
					else
						System.out.println("Sorry! Your order was not placed");
					System.out.println("Time elapsed to order:"+ ( endTimeOrderPost - startTimeOrderPost ));
				} else
					System.out.println("Order discarded!");
			} else {
				System.out.println("Nothing selected. Order discarded!");
			}
			break;
		case 3:
			System.out.println("Show order status");
			long orderStatusStartTime = System.currentTimeMillis();
			String orderStatusValues = helloImpl.orderStatus(username);
			long orderStatusEndTime = System.currentTimeMillis();
			String[] orderStatusValuesMaps = orderStatusValues.split("\n+");
			System.out.println("Time elapsed for custom order:"+( orderStatusEndTime - orderStatusStartTime ));
			System.out.println("Orders for user: "+username);
			int countUpdate = 0;
			float grandTotal = (float) 0.0;
			for (int i = 0; i < orderStatusValuesMaps.length; i++) {
				HashMap<String, Integer> statusHash = stringToMap(orderStatusValuesMaps[i], String.class,
						Integer.class);
				String orderValus = "";
				float totalCost = 0;
				for (Map.Entry<String, Integer> entry : statusHash.entrySet()) {
					countUpdate += 1;
					String key = entry.getKey();
					Integer value = entry.getValue();
					if (value == -1 || value == -2)
						continue;
					orderValus += " "+key+": "+value.toString()+" ";
					totalCost += value* menuValues.get(key);
					//System.out.println((i + 1) + " \t\t\t " + key + " \t\t\t " + value.toString());
				}
				grandTotal += totalCost;
				if( countUpdate != 0 )
					System.out.println( "Order No: "+(i+1)+orderValus+" and cost for this order is: $"+totalCost );
				else System.out.println("No order present");
			}
			System.out.println("Your grand total is: $"+grandTotal);
			break;
		case 4:
			System.out.println("Exit");
			quitValue[0] = 0;
			authStatus[0] = false;
			return;
		default:
			System.out.println("Please select correct option.");
			return;
		}
		return;
	}
}