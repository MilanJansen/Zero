package com.alliander.webjob;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Hashtable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;

public class SSLManager implements ISSLManager {
	private String ksPassword;
	private String keyPassword;
	private String ksResourcePath;
	private String ksType;
	private String sslContextType;
	private String tsResourcePath;
	private String tsPassword;
	private String tsType;

	public SSLManager(String configFileName) {
		/** Reads properties from configuration file */
		Hashtable<String, String> configTable = PropertiesFileReader.loadPropertyValues(configFileName);
		this.ksPassword = configTable.get("ksPassword");
		this.keyPassword = configTable.get("keyPassword");
		this.ksResourcePath = configTable.get("ksResourcePath");
		this.ksType = configTable.get("ksType");
		this.sslContextType = configTable.get("sslContextType");
		this.tsResourcePath = configTable.get("tsResourcePath");
		this.tsPassword = configTable.get("tsPassword");
		this.tsType = configTable.get("tsType");
	}

	@Override
	public SSLContext initSslContext() {
		TrustManager[] trustManagers = initTrustManagers(this.tsResourcePath, this.tsPassword, this.tsType);
		KeyManager[] keyManagers = initKeyManagers(this.ksResourcePath, this.ksPassword, this.keyPassword, this.ksType);
		setDefaultHostnameVerifier();
		try {
			SSLContext sc = SSLContext.getInstance(this.sslContextType);
			sc.init(keyManagers, trustManagers, new java.security.SecureRandom());
			MessageLogger.infoLogger("Succeeded to peform SSL Handshake");
			return sc;

		} catch (final GeneralSecurityException e) {
			MessageLogger.errorLogger(e, "Failed to peform SSL Handshake");
			return null;
		}
	}	

	@Override
	public X509HostnameVerifier initHostnameVerifier() {
		X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				try {
					session.getPeerPrincipal();
				} catch (SSLPeerUnverifiedException e) {
				}
				return true;
			}
			@Override
			public void verify(String host, SSLSocket ssl) throws IOException {
			}			
			@Override
			public void verify(String host, X509Certificate cert) throws SSLException {
				Date date = new Date();
				try {
					cert.checkValidity(date);
					cert.checkValidity();
				} catch (CertificateExpiredException | CertificateNotYetValidException e1) {
					MessageLogger.errorLogger(e1, "The Certificate is not valid or expired");
				}				
			}
			@Override
			public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
			}
		};

		return hostnameVerifier;
	}
	
	private void setDefaultHostnameVerifier() {		
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}
	
	/** Initializes key managers */
	private KeyManager[] initKeyManagers(String ksResourcePath, String ksPassword, String keyPassword, String ksType) {
		KeyManagerFactory keyFactory;
		try {
			KeyStore keyStore = KeyStore.getInstance(ksType);
			InputStream keyStream = new FileInputStream(ksResourcePath);
			keyStore.load(keyStream, ksPassword.toCharArray());
			keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyFactory.init(keyStore, keyPassword.toCharArray());
			keyStream.close();
		} catch (final GeneralSecurityException | IOException e) {
			keyFactory = null;
			MessageLogger.errorLogger(e, "Failed to access the KeyStore");
		}
		KeyManager[] keyManagers = keyFactory.getKeyManagers();
		return keyManagers;
	}
	
	/** Initializes trust managers */
	private TrustManager[] initTrustManagers(String tsResourcePath, String tsPassword, String tsType) {
		TrustManagerFactory trustFactory;
		try {
			KeyStore trustStore = KeyStore.getInstance(tsType);
			InputStream trustStream = new FileInputStream(tsResourcePath);
			trustStore.load(trustStream, tsPassword.toCharArray());
			trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustFactory.init(trustStore);
			trustStream.close();
		} catch (final GeneralSecurityException | IOException e) {
			trustFactory = null;			
			MessageLogger.errorLogger(e, "Failed to access the TrustStore");
		}
		TrustManager[] trustManagers = trustFactory.getTrustManagers();
		return trustManagers;
	}

}
