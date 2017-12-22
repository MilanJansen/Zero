package com.alliander.webjob;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import com.alliander.jaxb2.monitoring.ActualMeterReadsAsyncRequest;
import com.alliander.jaxb2.monitoring.ActualMeterReadsAsyncResponse;
import com.alliander.jaxb2.monitoring.ActualMeterReadsRequest;
import com.alliander.jaxb2.monitoring.ActualMeterReadsResponse;

/**
 * @author Milan Jansen
 */

public class SmartMeterDataController extends Thread {
	/** Config file containing web job event properties */
	private final String EVENT_CONFIG_FILE_NAME = "data_controller_config";
	/** Database data requester */
	private IDatabaseDataRequester databaseDataRequester;
	/** Web Service data requester */
	private IWebServiceDataRequester webServiceDataRequester;
	/** Data to JSON converter */
	private IDataConverter dataToJSONConverter;
	/** Data sender */
	private IDataSender eventHubDataSender;
	/** Request object */
	private ActualMeterReadsRequest request;
	/** Async response object */
	private ActualMeterReadsAsyncResponse asyncResponse;
	/** Global iterator */
	private int startValue;
	/** Variables from properties file. */
	private int timeInterval;
	private boolean withRetry;
	private int delayBeforeGetResponse;
	private int delayPerGetResponse;
	private int delayBeforeRetry;
	private int durationRetryInterval;
	private int maxRetryCount;
	private int setSize;

	public SmartMeterDataController(IDatabaseDataRequester databaseDataRequester,
			IWebServiceDataRequester webServiceDataRequester, IDataConverter dataToJSONConverter,
			IDataSender eventHubDataSender) {
		this.databaseDataRequester = databaseDataRequester;
		this.dataToJSONConverter = dataToJSONConverter;
		this.eventHubDataSender = eventHubDataSender;
		this.webServiceDataRequester = webServiceDataRequester;
		/** Wsdl generated model objects */
		this.request = new ActualMeterReadsRequest();
		this.asyncResponse = new ActualMeterReadsAsyncResponse();
		/** Variables from properties file. */
		Hashtable<String, String> propertyList = PropertiesFileReader.loadPropertyValues(this.EVENT_CONFIG_FILE_NAME);
		propertyList = handleProperties(propertyList);
		this.timeInterval = Integer.parseInt(propertyList.get("timeInterval"));
		this.withRetry = Boolean.parseBoolean(propertyList.get("withRetry"));
		this.delayBeforeGetResponse = Integer.parseInt(propertyList.get("delayBeforeGetResponse"));
		this.delayPerGetResponse = Integer.parseInt(propertyList.get("delayPerGetResponse"));
		this.delayBeforeRetry = Integer.parseInt(propertyList.get("delayBeforeRetry"));
		this.durationRetryInterval = Integer.parseInt(propertyList.get("durationRetryInterval"));
		this.maxRetryCount = Integer.parseInt(propertyList.get("maxRetryCount"));
		this.setSize = Integer.parseInt(propertyList.get("setSize"));
	}

	@Override
	public void run() {
		String selectDeviceIdentification = "SELECT TOP (200) deviceIdentification FROM dbo.SmartMeterDevice ORDER BY deviceIdentification";
		ArrayList<String> deviceIdentificationList = new ArrayList<String>();
		/** Runs continuous with a default time interval of 5 minutes */
		while (true) {
			deviceIdentificationList = this.databaseDataRequester.getDataWithRetry(selectDeviceIdentification);
			getMeterReadsMultipleDevices(deviceIdentificationList, this.delayBeforeGetResponse, this.withRetry);
			try {
				Thread.sleep(this.timeInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void getMeterReadsMultipleDevices(ArrayList<String> deviceIdentificationList, int delayBeforeGetResponse,
			boolean withRetry) {
		Map<String, String> unhandledIdList = new TreeMap<String, String>();
		this.startValue = 0;
		while (this.startValue < deviceIdentificationList.size()) {
			unhandledIdList = sendRequestsPerSet(deviceIdentificationList, this.setSize, this.startValue);
			try {
				Thread.sleep(delayBeforeGetResponse);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			unhandledIdList = getResponsePerId(unhandledIdList, this.delayPerGetResponse);
			if (!unhandledIdList.isEmpty() && withRetry) {
				retryGetResponsePerId(unhandledIdList, this.delayPerGetResponse, this.maxRetryCount,
						this.delayBeforeRetry, this.durationRetryInterval);
			}
		}
	}

	private Map<String, String> sendRequestsPerSet(ArrayList<String> deviceIdentificationList, int setSize,
			int startValue) {
		Map<String, String> unhandledIdList = new TreeMap<String, String>();
		for (int i = 0; i < setSize && i + startValue < deviceIdentificationList.size(); i++) {
			String deviceIdentification = deviceIdentificationList.get(startValue + i);
			this.request.setDeviceIdentification(deviceIdentification);
			this.asyncResponse = this.webServiceDataRequester.requestData(this.request);
			unhandledIdList = handleAsyncResponse(this.asyncResponse, unhandledIdList, deviceIdentification);
			this.startValue++;
		}
		return unhandledIdList;
	}

	private Map<String, String> handleAsyncResponse(ActualMeterReadsAsyncResponse asyncResponse,
			Map<String, String> unhandledIdList, String deviceIdentification) {
		if (asyncResponse != null) {
			this.asyncResponse = asyncResponse;
			unhandledIdList.put(this.asyncResponse.getDeviceIdentification(), this.asyncResponse.getCorrelationUid());
		} else {
			MessageLogger.warnLogger("Didn't receive a response from device: " + deviceIdentification);
		}
		return unhandledIdList;
	}

	protected Map<String, String> getResponsePerId(Map<String, String> unhandledIdList, int delayPerGetResponse) {
		Iterator<Entry<String, String>> i = unhandledIdList.entrySet().iterator();
		ActualMeterReadsAsyncRequest asyncRequest = new ActualMeterReadsAsyncRequest();
		while (i.hasNext()) {
			Entry<String, String> asyncResponseIdPair = i.next();
			String deviceIdentification = asyncResponseIdPair.getKey();
			String correlationUid = asyncResponseIdPair.getValue();
			asyncRequest.setDeviceIdentification(deviceIdentification);
			asyncRequest.setCorrelationUid(correlationUid);
			ActualMeterReadsResponse responseObject = this.webServiceDataRequester.requestData(asyncRequest);
			if (handleResponse(responseObject, deviceIdentification)) {
				i.remove();
			}
			try {
				Thread.sleep(delayPerGetResponse);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return unhandledIdList;
	}

	private boolean handleResponse(ActualMeterReadsResponse responseObject, String deviceIdentification) {
		if (responseObject != null) {
			ActualMeterReadsResponse response = new ActualMeterReadsResponse();
			response = responseObject;
			String jsonStringMeterData = convertData(response, deviceIdentification);
			if (jsonStringMeterData != null) {
				System.out.println(jsonStringMeterData);
				// this.eventHubDataSender.sendData(jsonStringMeterData);
			}
			return true;
		}
		MessageLogger.warnLogger("Didn't receive meter reads from device: " + deviceIdentification);
		return false;
	}

	private void retryGetResponsePerId(final Map<String, String> unhandledIdList, final int delayPerGetResponse,
			final int maxRetryCount, final int delay, final int duration) {
		final Timer timer = new Timer();
		final TimerTask timerTask = new TimerTask() {
			private int retryCount = 0;
			private Map<String, String> mUnhandledIdList = unhandledIdList;

			@Override
			public void run() {
				this.mUnhandledIdList = getResponsePerId(this.mUnhandledIdList, delayPerGetResponse);
				this.retryCount++;
				if (this.retryCount == maxRetryCount || this.mUnhandledIdList.isEmpty()) {
					timer.cancel();
				}
			}
		};
		timer.scheduleAtFixedRate(timerTask, delay, duration);
	}

	private Hashtable<String, String> handleProperties(Hashtable<String, String> propertyList) {
		/** Handle timeInterval property | Default time interval is 5 minutes */
		String timeIntervalString = propertyList.get("timeInterval");
		int defaultTimeInterval = 5 * 60 * 1000;
		try {
			Integer.parseInt(timeIntervalString);
		} catch (NumberFormatException e) {
			propertyList.replace("timeInterval", String.valueOf(defaultTimeInterval));
			MessageLogger
					.warnLogger("Invalid time interval input - Using the default time interval of 5 minutes instead.");
		}
		/** Handle withRetry property | Default is true */
		String withRetryString = propertyList.get("withRetry");
		boolean defaultWithRetry = true;
		if (!withRetryString.toLowerCase().equals("true") && !withRetryString.toLowerCase().equals("false")) {
			propertyList.replace("time_interval", String.valueOf(defaultWithRetry));
			MessageLogger.warnLogger("Invalid withRetry input - Using the default value true instead.");
		}
		/**
		 * Handle delayBeforeGetResponse property | Default time interval is 250
		 * milliseconds
		 */
		String delayBeforeGetResponseString = propertyList.get("delayBeforeGetResponse");
		int defaultDelayBeforeGetResponse = 250;
		try {
			Integer.parseInt(delayBeforeGetResponseString);
		} catch (NumberFormatException e) {
			propertyList.replace("delayBeforeGetResponse", String.valueOf(defaultDelayBeforeGetResponse));
			MessageLogger.warnLogger(
					"Invalid delayBeforeGetResponse input - Using the default of 250 milliseconds instead.");
		}
		/**
		 * Handle delayPerGetResponse property | Default time interval is 250
		 * milliseconds
		 */
		String delayPerGetResponseString = propertyList.get("delayPerGetResponse");
		int defaultDelayPerGetResponse = 250;
		try {
			Integer.parseInt(delayPerGetResponseString);
		} catch (NumberFormatException e) {
			propertyList.replace("delayPerGetResponse", String.valueOf(defaultDelayPerGetResponse));
			MessageLogger
					.warnLogger("Invalid delayPerGetResponse input - Using the default of 250 milliseconds instead.");
		}
		/** Handle delayPerGetResponse property | Default is 2 */
		String maxRetryCountString = propertyList.get("maxRetryCount");
		int defaultMaxRetryCount = 2;
		try {
			Integer.parseInt(maxRetryCountString);
		} catch (NumberFormatException e) {
			propertyList.replace("maxRetryCount", String.valueOf(defaultMaxRetryCount));
			MessageLogger.warnLogger("Invalid maxRetryCount input - Using the default of 2 instead.");
		}
		/** Handle setSize property | Default is 50 */
		String setSizeString = propertyList.get("setSize");
		int defaultSetSize = 50;
		try {
			Integer.parseInt(setSizeString);
		} catch (NumberFormatException e) {
			propertyList.replace("setSize", String.valueOf(defaultSetSize));
			MessageLogger.warnLogger("Invalid setSize input - Using the default of 50 instead.");
		}
		/** Handle delayBeforeRetry property | Default is 20 seconds */
		String delayBeforeRetryString = propertyList.get("delayBeforeRetry");
		int defaultDelayBeforeRetry = 20000;
		try {
			Integer.parseInt(delayBeforeRetryString);
		} catch (NumberFormatException e) {
			propertyList.replace("delayBeforeRetry", String.valueOf(defaultDelayBeforeRetry));
			MessageLogger.warnLogger("Invalid delayBeforeRetry input - Using the default of 20 seconds instead.");
		}
		/** Handle durationRetryInterval property | Default is 20 seconds */
		String durationRetryIntervalString = propertyList.get("durationRetryInterval");
		int defaultDurationRetryInterval = 20000;
		try {
			Integer.parseInt(durationRetryIntervalString);
		} catch (NumberFormatException e) {
			propertyList.replace("durationRetryInterval", String.valueOf(defaultDurationRetryInterval));
			MessageLogger.warnLogger("Invalid durationRetryInterval input - Using the default of 20 seconds instead.");
		}
		return propertyList;
	}

	private String convertData(ActualMeterReadsResponse response, String smartMeterID) {
		String smartMeterDataJSONString = null;
		if (response != null) {
			/** Create JSON string device wrapper */
			String idKey = "deviceId";
			String idValue = this.dataToJSONConverter.convertObject(smartMeterID);
			smartMeterDataJSONString = this.dataToJSONConverter.convertString(idKey, idValue);
			/** Add logTime attribute to device wrapper */
			Date date = new Date();
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			String dateString = formatGregorianCalendarToDateString(calendar);
			String logTimeKey = "logTime";
			String logTimeValue = this.dataToJSONConverter.convertObject(dateString);
			smartMeterDataJSONString = this.dataToJSONConverter.addToExistingObject(smartMeterDataJSONString,
					logTimeKey, logTimeValue);
			/** Add eventType attribute to device wrapper */
			String eventTypeKey = "eventType";
			String eventTypeValue = "energyData";
			smartMeterDataJSONString = this.dataToJSONConverter.addToExistingObject(smartMeterDataJSONString,
					eventTypeKey, eventTypeValue);
			/** Add activeEnergyExport attribute to device wrapper */
			String activeEnergyExportKey = "activeEnergyExport";
			String activeEnergyExportValue = this.dataToJSONConverter
					.convertObject(response.getActiveEnergyExport().getValue());
			smartMeterDataJSONString = this.dataToJSONConverter.addToExistingObject(smartMeterDataJSONString,
					activeEnergyExportKey, activeEnergyExportValue);
			/** Add activeEnergyImport attribute to device wrapper */
			String activeEnergyImportKey = "activeEnergyImport";
			String activeEnergyImportValue = this.dataToJSONConverter
					.convertObject(response.getActiveEnergyImport().getValue());
			smartMeterDataJSONString = this.dataToJSONConverter.addToExistingObject(smartMeterDataJSONString,
					activeEnergyImportKey, activeEnergyImportValue);
		} else {
			MessageLogger.warnLogger("Didn't receive requested data");
		}
		return smartMeterDataJSONString;
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
