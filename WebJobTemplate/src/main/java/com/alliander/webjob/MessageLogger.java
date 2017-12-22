package com.alliander.webjob;

import org.apache.log4j.Logger;

/** Note: When running this application as a web job in FFaaS the log4j library causes an error. 
 *  To solve this place the code lines that make use of log4j in comment blocks.
 * */

public class MessageLogger {
	private static Logger logger = Logger.getRootLogger();	
	
	/** Used to log error level messages */
	public static void errorLogger(Exception e, String message) {		
		if (logger.isDebugEnabled()) {
			logger.error(message + ": " + e);
		} else if (logger.isInfoEnabled()) {
			logger.error(message);
		}
	}
	
	/** Used to log information level messages */
	public static void infoLogger(String message) {	
		logger.info(message);
	}
	
	/** Used to log warning level messages */
	public static void warnLogger(String message) {		
		logger.warn(message);
	}
}
