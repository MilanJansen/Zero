package com.alliander.webjob;

import java.io.IOException;
import java.util.Hashtable;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public class EventHubDataSender implements IDataSender {
	private String namespaceName;
	private String eventHubName;
	private String sasKeyName;
	private String sasKey;
	private EventHubClient ehClient;

	public EventHubDataSender(String configFileName) {
		/** Reads properties from configuration file */
		Hashtable<String, String> configTable = PropertiesFileReader.loadPropertyValues(configFileName);
		this.namespaceName = configTable.get("namespaceName");
		this.eventHubName = configTable.get("eventHubName");
		this.sasKeyName = configTable.get("sasKeyName");
		this.sasKey = configTable.get("sasKey");
		
		ConnectionStringBuilder connStr = new ConnectionStringBuilder(this.namespaceName, this.eventHubName,
				this.sasKeyName, this.sasKey);
		try {
			this.ehClient = EventHubClient.createFromConnectionStringSync(connStr.toString());
		} catch (ServiceBusException | IOException e) {
			MessageLogger.errorLogger(e, "Failed to innitialize EventHubClient");
		}
	}

	@Override
	public void sendData(String jsonString) {
		byte[] payloadBytes;
		try {
			payloadBytes = jsonString.getBytes("UTF-8");
			EventData sendEvent = new EventData(payloadBytes);
			this.ehClient.send(sendEvent);
		} catch (IOException e) {
			MessageLogger.errorLogger(e, "Failed to send the data to the Event hub");
		}
	}
}
