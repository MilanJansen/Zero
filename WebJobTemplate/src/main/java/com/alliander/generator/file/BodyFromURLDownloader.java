package com.alliander.generator.file;

import java.io.*;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import com.alliander.webjob.MessageLogger;
import com.alliander.webjob.SSLManager;

public class BodyFromURLDownloader {
	/** URL of wanted body */
	private static final String URI = "https://coinmarketcap.com";
	/** Path plus filename plus extension to target location */
	private static final String FILENAME_PATH = "D:\\Repo\\WebJobTemplate\\WebJobTemplate\\src\\main\\java\\com\\alliander\\generator\\file\\coinmarketcap.html";
	
	/**
	 * WSDL public-lighting:
	 * ../osgp-adapter-ws-publiclighting/wsdl/PublicLighting/
	 * PublicLightingAdHocManagement.wsdl WSDL smart-metering-monitoring:
	 * ../osgp-adapter-ws-smartmetering/src/main/webapp/WEB-INF/wsdl/
	 * smartmetering/SmartMeteringMonitoring.wsdl WSDL
	 * smart-metering-management:
	 * ../osgp-adapter-ws-smartmetering/src/main/webapp/WEB-INF/wsdl/
	 * smartmetering/SmartMeteringManagement.wsdl
	 */

	public static void main(String[] args) throws Exception {
		HttpsURLConnection con;
		con = createConnection();
		/** Uncomment when SSL verification is needed */
		/**
		 * SSLContext sslContext;
		 * SSLManager sslManager = new SSLManager("ssl_config");
		 * sslContext = sslManager.initSslContext();
		 * con = createConnection(sslContext);
		 */		
		generateFileFromUrl(con, FILENAME_PATH);
	}

	private static void generateFileFromUrl(HttpsURLConnection con, String filename) {
		try {
			java.io.BufferedInputStream in = new java.io.BufferedInputStream(con.getInputStream());
			java.io.FileOutputStream fos = new java.io.FileOutputStream(filename);
			java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
			byte[] data = new byte[1024];
			int i = 0;
			while ((i = in.read(data, 0, 1024)) >= 0) {
				bout.write(data, 0, i);
			}
			bout.close();
			in.close();
			MessageLogger.infoLogger("Succeeded to create/update file");
		} catch (Exception e) {
			MessageLogger.errorLogger(e, "Failed to create/update file");
		}
	}

	private static HttpsURLConnection createConnection(SSLContext sslContext) {
		HttpsURLConnection con = createConnection();
		con.setSSLSocketFactory(sslContext.getSocketFactory());
		return con;
	}

	private static HttpsURLConnection createConnection() {
		HttpsURLConnection con;
		try {
			con = (HttpsURLConnection) new URL(URI).openConnection();
		} catch (IOException e) {
			MessageLogger.errorLogger(e, "Failed to create a connection");
			return null;
		}
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setRequestProperty("Accept-Encoding", "text/xml");
		con.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");		
		MessageLogger.infoLogger("Succeeded to create a connection");
		return con;
	}
}