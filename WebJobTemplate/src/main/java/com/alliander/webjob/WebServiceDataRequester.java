package com.alliander.webjob;

import java.util.Hashtable;

import javax.net.ssl.*;
import javax.xml.namespace.QName;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

public class WebServiceDataRequester implements IWebServiceDataRequester {
	private String soapURI;
	private String[] messageHead;
	private boolean sslUsage;
	private SSLContext sslContext;
	private X509HostnameVerifier hostnameVerifier;
	private String contextPath;

	public WebServiceDataRequester(String configFileName, SSLContext sslContext,
			X509HostnameVerifier hostnameVerifier) {
		initWebServiceDataRequester(configFileName);
		this.sslContext = sslContext;
		this.hostnameVerifier = hostnameVerifier;
		this.sslUsage = true;
	}

	public WebServiceDataRequester(String configFileName) {
		initWebServiceDataRequester(configFileName);
		this.sslUsage = false;
	}

	private void initWebServiceDataRequester(String configFileName) {
		/** Reads properties from configuration file */
		Hashtable<String, String> configTable = PropertiesFileReader.loadPropertyValues(configFileName);
		this.soapURI = configTable.get("soapURI");
		this.contextPath = configTable.get("contextPath");
		String[] messageHead = { configTable.get("applicationName"), configTable.get("username"),
				configTable.get("organizationName") };
		this.messageHead = messageHead;
	}

	@Override
	public <T1 extends Object, T2 extends Object> T2 requestData(T1 requestObject) {
		T2 responseObject;
		WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
		if (this.sslUsage) {
			webServiceTemplate = initWebServiceTemplateWithContext(this.contextPath, this.sslContext,
					this.hostnameVerifier);
		} else {
			webServiceTemplate = initWebServiceTemplate(this.contextPath);
		}		
		try{
			responseObject = (T2) webServiceTemplate.marshalSendAndReceive(this.soapURI, requestObject,
					initSoapActionCallback(this.messageHead));
		}catch(SoapFaultClientException e) {
			responseObject = null;
		}
		return responseObject;
	}

	private WebServiceTemplate initWebServiceTemplateWithContext(String contextPath, SSLContext sslContext,
			X509HostnameVerifier hostnameVerifier) {
		WebServiceTemplate webServiceTemplate = initWebServiceTemplate(contextPath);
		HttpComponentsMessageSender sender = new HttpComponentsMessageSender();
		HttpClient client = sender.getHttpClient();
		SchemeSocketFactory socketFactory = new SSLSocketFactory(sslContext, hostnameVerifier);
		Scheme scheme = new Scheme("https", 443, socketFactory);
		client.getConnectionManager().getSchemeRegistry().register(scheme);
		webServiceTemplate.setMessageSender(sender);
		return webServiceTemplate;
	}

	private WebServiceTemplate initWebServiceTemplate(String contextPath) {
		WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setContextPath(contextPath);
		webServiceTemplate.setMarshaller(marshaller);
		webServiceTemplate.setUnmarshaller(marshaller);
		return webServiceTemplate;
	}

	private SoapActionCallback initSoapActionCallback(String[] messageHead) {
		final String NAMESPACE_URI = "http://www.alliander.com/schemas/osgp/common/2014/10";
		final String PREFIX = "ns2";

		SoapActionCallback requestCallback = new SoapActionCallback("") {
			@Override
			public void doWithMessage(WebServiceMessage message) {
				SaajSoapMessage soapMessage = (SaajSoapMessage) message;
				SoapHeader soapHeader = soapMessage.getSoapHeader();

				QName applicationNameQName = new QName(NAMESPACE_URI, "ApplicationName", PREFIX);
				SoapHeaderElement applicationNameHeader = soapHeader.addHeaderElement(applicationNameQName);
				applicationNameHeader.setText(messageHead[0]);

				QName userNameQName = new QName(NAMESPACE_URI, "UserName", PREFIX);
				SoapHeaderElement userNameHeader = soapHeader.addHeaderElement(userNameQName);
				userNameHeader.setText(messageHead[1]);

				QName organisationIdentificationQName = new QName(NAMESPACE_URI, "OrganisationIdentification", PREFIX);
				SoapHeaderElement organisationIdentificationHeader = soapHeader
						.addHeaderElement(organisationIdentificationQName);
				organisationIdentificationHeader.setText(messageHead[2]);
			}
		};
		return requestCallback;
	}
}
