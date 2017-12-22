package com.alliander.webjob;

public interface IWebServiceDataRequester {

	/** Requests data from a web service */
	public <T1 extends Object, T2 extends Object> T2 requestData(T1 requestObject);

}
