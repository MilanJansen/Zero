package CGI.Stage.FrictionFree;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import com.alliander.jaxb2.management.Device;
import com.alliander.webjob.DataToJSONConverter;
import com.alliander.webjob.IDataConverter;

/** Tests all methods of the DataToJSONConverter class */

public class TestDataToJSONConverter {
	IDataConverter dataToJSONConverter;
	String testObjectString;

	@Before
	public void initDataConverter() {
		this.dataToJSONConverter = new DataToJSONConverter();
		this.testObjectString = "{\"activated\":false,\"hasSchedule\":false,\"publicKeyPresent\":false}";
	}	
	
	public Device createTestObject() {
		Device testDataObject = new Device();
		testDataObject.setActivated(false);
		return testDataObject;
	}
	
	@Test
	public void testConvertObject() {
		String jsonObjectString = this.dataToJSONConverter.convertObject(createTestObject());
		assertEquals(this.testObjectString, jsonObjectString);
	}

	@Test
	public void testAddVariableToExistingObject() {
		String testObjectString = "{\"activated\":false,\"hasSchedule\":false,\"publicKeyPresent\":false,\"logTime\":5}";
		String key = "logTime";
		String value = "5";
		String jsonObjectString = this.dataToJSONConverter.addToExistingObject(this.testObjectString, key, value);
		assertEquals(testObjectString, jsonObjectString);
	}

	@Test
	public void testAddObjectToExistingObject() {
		String testWrapperObject = "{\"SmartMeterDevice\":{}}";
		String testAddObject = "{\"activated\":false,\"hasSchedule\":false,\"publicKeyPresent\":false}";
		String expectedObject = "{\"SmartMeterDevice\":{\"activated\":false,\"hasSchedule\":false,\"publicKeyPresent\":false}}";
		String key = "SmartMeterDevice";
		String jsonObjectString = this.dataToJSONConverter.addToExistingObject(testWrapperObject, key, testAddObject);
		assertEquals(expectedObject, jsonObjectString);
	}

	@Test
	public void testConvertString() {
		String expectedJsonString = "{\"logTime\": \"startValue\"}";
		String string = "logTime";
		String jsonString = this.dataToJSONConverter.convertString(string, "startValue");
		assertEquals(expectedJsonString, jsonString);
	}	
}
