package com.alliander.webjob;

public interface IDataConverter {	
	
	/** Converts any object to JSON object string */
	public <E> String convertObject(E object);
	/** Adds a key value pair to a JSON object string */
	public String addToExistingObject(String object, String key, String value);
	/** Converts a string to a JSON object string */
	public String convertString(String string, String startValue);
	/** Removes a key value pair from a JSON object string */
	public String removeElementFromObject(String object, String key);
}
