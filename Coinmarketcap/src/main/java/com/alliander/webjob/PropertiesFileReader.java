package com.alliander.webjob;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;

public class PropertiesFileReader {	
	
	/** Reads all properties from configuration file */
	public static Hashtable<String, String> loadPropertyValues(String configFileName) {		
		ResourceBundle sslPropertieBundle = ResourceBundle.getBundle(configFileName);
		Enumeration<String> bundleKeys = sslPropertieBundle.getKeys();
		Hashtable<String, String> configTable = new Hashtable<>();
		String key;
		String value;
		while (bundleKeys.hasMoreElements()) {
			key = bundleKeys.nextElement();
			value = sslPropertieBundle.getString(key);
			configTable.put(key, value);			
		}
		return configTable;
	}
}
