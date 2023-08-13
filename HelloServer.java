package Server;

import HelloApp.*;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

class HelloImpl extends HelloPOA {
	private ORB orb;

	public void setORB(ORB orb_val) {
		orb = orb_val;
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
		String[] keyValuePairs = str.substring(1, str.length() - 1).split(",");

		// Create a new HashMap
		HashMap<K, V> map = new HashMap<>();

		// Loop through the key-value pairs and add them to the HashMap
		for (String pair : keyValuePairs) {
			String[] entry = pair.split("=");
			K key = convert(entry[0], keyClass);
			V value = convert(entry[1], valueClass);
			map.put(key, value);
		}

		// Return the HashMap
		return map;
	}

	// implement sayHello() method
	public String sayHello() {
		return "\nHello world !!\n";
	}

	public boolean authenticate(String username, String passwords, String role) {
		InputStream f = HelloImpl.class.getClassLoader().getResourceAsStream("database/authFile.txt");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(f));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
			}
			String st = sb.toString();
//			String st = br.readLine();
			String[] splittedAuthData = st.split("\\R+");
			for (int i = 0; i < splittedAuthData.length; i++) {
				String[] newSplit = splittedAuthData[i].split("\\;+");
				try {
					if (newSplit[0].trim().toLowerCase().compareTo(username.trim().toLowerCase()) == 0
							&& newSplit[1].trim().compareTo(passwords.trim()) == 0
							&& newSplit[2].trim().toLowerCase().compareTo(role.trim().toLowerCase()) == 0) {
						return true;
					}
				} catch (Exception e) {
					throw e;
				}
			}
			br.close();
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public String getMenu() {
		InputStream f = HelloImpl.class.getClassLoader().getResourceAsStream("database/menu.txt");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(f));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
			}
			String fileContent = sb.toString();
			return fileContent;
		} catch (Exception e) {
			return "Error!";
		}
	}

	@Override
	public boolean postOrder(String order) {
		// TODO Auto-generated method stub
		String filePath = "database/orders.txt";
		try {
			FileWriter writer = new FileWriter(filePath, true);
			writer.write(order + "\n");
			writer.close();
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public String[] getUserAndStatusInMap(HashMap<String, Integer> orderDetailsMap) {
		String[] userStatus = { "", "" };
		for (Map.Entry<String, Integer> entry : orderDetailsMap.entrySet()) {
			if (entry.getValue() == -1)
				userStatus[0] = entry.getKey();
			if (entry.getValue() == -2)
				userStatus[1] = entry.getKey();
		}
		return userStatus;
	}

	@Override
	public String orderStatus(String username) {
		// TODO Auto-generated method stub
		InputStream f = HelloImpl.class.getClassLoader().getResourceAsStream("database/orders.txt");
		String results = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(f));
			String line;
			while ((line = br.readLine()) != null) {
				HashMap<String, Integer> orderDetailsMap = stringToMap(line, String.class, Integer.class);

				String[] userAndStatus = getUserAndStatusInMap(orderDetailsMap);
				
				if (userAndStatus[0].trim().toLowerCase().compareTo(username.trim().toLowerCase()) == 0 && userAndStatus[1].trim().compareTo("A") == 0) {
					results += line+"\n";
				}
			}

			return results;
		} catch (Exception e) {
			return "Error!";
		}
	}

	@Override
	public String getAllActiveOrders() {
		InputStream f = HelloImpl.class.getClassLoader().getResourceAsStream("database/orders.txt");
		String results = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(f));
			String line;
			while ((line = br.readLine()) != null) {
				HashMap<String, Integer> orderDetailsMap = stringToMap(line, String.class, Integer.class);

				String[] userAndStatus = getUserAndStatusInMap(orderDetailsMap);
				if( userAndStatus[1].trim().compareTo("A") != 0 ) continue;
				results += line+"\n";
				
			}

			return results;
		} catch (Exception e) {
			return "Error!";
		}
	}

	// implement shutdown() method
	// public void shutdown() {
	// orb.shutdown(false);
	// }
}

public class HelloServer {
	
	public static void main(String args[]) {
		try {
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			HelloImpl helloImpl = new HelloImpl();
			helloImpl.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(helloImpl);
			Hello href = HelloHelper.narrow(ref);

			// get the root naming context
			// NameService invokes the name service
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			// Use NamingContextExt which is part of the Interoperable
			// Naming Service (INS) specification.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// bind the Object Reference in Naming
			String name = "Hello";
			NameComponent path[] = ncRef.to_name(name);
			ncRef.rebind(path, href);

			System.out.println("HelloServer ready and waiting ...\n");

			// wait for invocations from clients
			orb.run();
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println("HelloServer Exiting ...");

	}
}