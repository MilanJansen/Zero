package com.alliander.webjob;

import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataToJSONConverter implements IDataConverter {	
	private Gson gson = new Gson();
	private JsonParser parser = new JsonParser();
	public DataToJSONConverter(){}	
	
	@Override
	public <E> String convertObject(E object) {		
		String jsonInString = this.gson.toJson(object);
		return jsonInString;
	}
	
	@Override
	public String convertString(String string, String startValue) {
		JSONObject jsonObj = new JSONObject("{" + string + ": " + startValue + "}");		
		return jsonObj.toString(4);
	}
	
	@Override
	public String removeElementFromObject(String object, String key) {
		JsonElement rawJsonElement = this.parser.parse(object);
		JsonObject jsonObject = null;
		if(rawJsonElement.isJsonObject()) {
			jsonObject = rawJsonElement.getAsJsonObject();
			jsonObject.remove(key);
		}
		return jsonObject.toString();
	}
	
	@Override
	public String addToExistingObject(String jsonString, String key, String stringValue) {
		JsonObject jsonWrapper = this.parser.parse(jsonString).getAsJsonObject();
		JsonElement rawJsonElement = this.parser.parse(stringValue);
		if(rawJsonElement.isJsonObject()) {
			JsonObject jsonObject = rawJsonElement.getAsJsonObject();
			jsonWrapper.add(key, jsonObject);
		}
		else if(rawJsonElement.isJsonPrimitive()) {
			JsonElement jsonElement = rawJsonElement.getAsJsonPrimitive();
			jsonWrapper.add(key, jsonElement);
		}	
		return jsonWrapper.toString();		
	}		
}
