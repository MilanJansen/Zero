package com.alliander.webjob;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.X509HostnameVerifier;

public interface ISSLManager {
	
	/** Initializes a hostname verifier */
	public X509HostnameVerifier initHostnameVerifier();
	/** Initializes a ssl context */
	public SSLContext initSslContext();

}
