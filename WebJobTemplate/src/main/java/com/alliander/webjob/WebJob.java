package com.alliander.webjob;

/**
 * @author Milan Jansen
 */

public class WebJob {
	/** Config file containing Web Service properties */
	private static final String SSL_CONFIG_FILE_NAME = "ssl_config";
	/** Config file containing Event Hub properties */
	private static final String EVENT_HUB_CONFIG_FILE_NAME = "event_hub_config";
	/** Config file containing Database properties */
	private static final String DATABASE_CONFIG_FILE_NAME = "database_config";
	/** Config file containing management web service properties */
	private static final String MANAGEMENT_SERVICE_CONFIG_FILE_NAME = "management_service_config";
	/** Config file containing monitoring web service properties */
	private static final String MONITORING_SERVICE_CONFIG_FILE_NAME = "monitoring_service_config";
	/** SSL Manager */
	private static ISSLManager sslManager;
	/** Data to JSON converter */
	private static IDataConverter dataToJSONConverter;
	/** Event hub data sender */
	private static IDataSender eventHubDataSender;
	/** Database data requester */
	private static IDatabaseDataRequester databaseDataRequester;
	/** Web Service data requester management service */
	private static IWebServiceDataRequester managementServiceDataRequester;
	/** Web Service data requester management service */
	private static IWebServiceDataRequester monitoringServiceDataRequester;
	/** Smart meter data controller */
	private static SmartMeterDataController smartMeterDataController;
	/** Smart meter devices controller */
	private static SmartMeterDevicesController smartMeterDevicesController;
	/** Thread for SmartMeterDevicesController */
	private static Thread threadSMDevicesController;
	/** Thread for SmartMeterDataController */
	private static Thread threadSMDataController;

	public static void main(String[] args) {
		/**
		 * When only one controller is used, then there is no need to use
		 * Threads. In that case the controller can be started directly by
		 * calling <my_controller>.run(). When two or more controllers are
		 * used, they can be run simultaneously by using threads.
		 */

		/** Initialize classes used by both controllers */
		sslManager = new SSLManager(SSL_CONFIG_FILE_NAME);
		dataToJSONConverter = new DataToJSONConverter();
		eventHubDataSender = new EventHubDataSender(EVENT_HUB_CONFIG_FILE_NAME);

		/** Initialize classes used only by SmartMeterDevicesController */
		//managementServiceDataRequester = new WebServiceDataRequester(MANAGEMENT_SERVICE_CONFIG_FILE_NAME,
				//sslManager.initSslContext(), sslManager.initHostnameVerifier());
		/** Initialize SmartMeterDevicesController */
		//smartMeterDevicesController = new SmartMeterDevicesController(managementServiceDataRequester,
				//dataToJSONConverter, eventHubDataSender);
		/** Start thread for SmartMeterDevicesController */
		//threadSMDevicesController = new Thread(smartMeterDevicesController);
		//threadSMDevicesController.start();

		/** Initialize classes used only by SmartMeterDataController */
		monitoringServiceDataRequester = new WebServiceDataRequester(MONITORING_SERVICE_CONFIG_FILE_NAME,
				sslManager.initSslContext(), sslManager.initHostnameVerifier());
		databaseDataRequester = new DatabaseDataRequester(DATABASE_CONFIG_FILE_NAME);
		/** Initialize SmartMeterDataController */
		smartMeterDataController = new SmartMeterDataController(databaseDataRequester, monitoringServiceDataRequester,
				dataToJSONConverter, eventHubDataSender);
		/** Start thread for SmartMeterDataController */
		threadSMDataController = new Thread(smartMeterDataController);
		threadSMDataController.start();
	}
}
