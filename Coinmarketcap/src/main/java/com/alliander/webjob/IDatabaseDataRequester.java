package com.alliander.webjob;

import java.util.ArrayList;

public interface IDatabaseDataRequester {	
	
	/** Executes a given select statement and returns the results */
	ArrayList<String> getData(String query);
	/** getData with a connection retry */
	ArrayList<String> getDataWithRetry(String query);
	/** Executes a given update statement */
	void updateData(String query);

}
