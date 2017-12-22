package com.alliander.webjob;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

import com.alliander.jaxb2.management.Device;
import com.alliander.jaxb2.management.GetDevicesRequest;
import com.alliander.jaxb2.management.GetDevicesResponse;

/**
 * @author Milan Jansen
 */

public class SmartMeterDevicesController extends Thread {
	/** Config file containing web job event properties */
	private final String EVENT_CONFIG_FILE_NAME = "devices_controller_config";
	/** Web Service data requester */
	private IWebServiceDataRequester webServiceDataRequester;
	/** Data to JSON converter */
	private IDataConverter dataToJSONConverter;
	/** Data sender */
	private IDataSender eventHubDataSender;
	/** Request object */
	private GetDevicesRequest request;
	/** Response object */
	private GetDevicesResponse response;

	public SmartMeterDevicesController(IWebServiceDataRequester webServiceDataRequester,
			IDataConverter dataToJSONConverter, IDataSender eventHubDataSender) {
		this.dataToJSONConverter = dataToJSONConverter;
		this.eventHubDataSender = eventHubDataSender;
		this.webServiceDataRequester = webServiceDataRequester;
		/** Wsdl generated model objects */
		this.request = new GetDevicesRequest();
		this.response = new GetDevicesResponse();
	}

	@Override
	public void run() {
		int timeInterval = getTimeInterval();
		int i;
		int totalPages;

		/** Runs continuous */
		while (true) {
			i = 0;
			this.request.setPage(i);
			this.response = this.webServiceDataRequester.requestData(this.request);
			totalPages = this.response.getDevicePage().getTotalPages();
			for (i = 0; i < totalPages; i++) {
				List<Device> devices = this.response.getDevicePage().getDevices();
				for (Device device : devices) {
					device = checkNullValues(device);
					String jsonStringDevices = convertData(device);
					if (jsonStringDevices != null) {						
						this.eventHubDataSender.sendData(jsonStringDevices);
					}
				}
				if ((i + 1) < totalPages) {
					this.request.setPage(i + 1);
					this.response = this.webServiceDataRequester.requestData(this.request);
				}
			}
			try {
				Thread.sleep(timeInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private Device checkNullValues(Device device) {
		if (device.getDeviceUid() == null) {
			device.setDeviceUid("null");
		}
		if (device.getDeviceIdentification() == null) {
			device.setDeviceIdentification("null");
		}
		if (device.getDeviceType() == null) {
			device.setDeviceType("null");
		}
		return device;
	}

	private int getTimeInterval() {
		Hashtable<String, String> configTable = PropertiesFileReader.loadPropertyValues(this.EVENT_CONFIG_FILE_NAME);
		String timeIntervalString = configTable.get("timeInterval");
		/** Default time interval is 12 hours */
		int defaultTimeInterval = 43200000;
		int timeInterval;
		try {
			timeInterval = Integer.parseInt(timeIntervalString);
		} catch (NumberFormatException e) {
			timeInterval = defaultTimeInterval;
		}
		return timeInterval;
	}

	private String convertData(Device device) {
		String deviceJSONString = null;
		if (device != null) {
			/** Create JSON string device wrapper */
			deviceJSONString = this.dataToJSONConverter.convertObject(device);
			/** Add logTime attribute to device wrapper */
			Date date = new Date();
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			String dateString = formatGregorianCalendarToDateString(calendar);
			String logTimeKey = "logTime";
			String logTimeValue = this.dataToJSONConverter.convertObject(dateString);
			deviceJSONString = this.dataToJSONConverter.addToExistingObject(deviceJSONString,
					logTimeKey, logTimeValue);
			/** Add eventType attribute to device wrapper */
			String eventTypeWrapperName = "eventType";
			String eventTypeWrapper = "deviceData";
			deviceJSONString = this.dataToJSONConverter.addToExistingObject(deviceJSONString, eventTypeWrapperName,
					eventTypeWrapper);
		} else {
			MessageLogger.warnLogger("Didn't receive requested data");
		}
		return deviceJSONString;
	}

	private String formatGregorianCalendarToDateString(GregorianCalendar calendar) {
		TimeZone tz = TimeZone.getTimeZone("Europe/Amsterdam");
		int offset = tz.getOffset(new Date().getTime()) / 1000 / 60 / 60;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss" + "+0" + offset + ":00");
		df.setTimeZone(tz);
		Date date = calendar.getTime();
		String dateString = df.format(date);
		return dateString;
	}
}
