package com.alliander.webjob;

import java.util.ArrayList;
import java.util.Hashtable;
import java.sql.*;

public class DatabaseDataRequester implements IDatabaseDataRequester {
	private String servername;
	private String databasename;
	private String username;
	private String password;
	private String connectionString;
	private Connection connection;

	public DatabaseDataRequester(String databaseConfigFileName) {
		/** Reads properties from configuration file */
		Hashtable<String, String> configTable = PropertiesFileReader.loadPropertyValues(databaseConfigFileName);
		this.servername = configTable.get("servername");
		this.databasename = configTable.get("databasename");
		this.username = configTable.get("username");
		this.password = configTable.get("password");

		java.lang.System.setProperty("java.net.preferIPv4Stack", "true");

		/** Creates a database connection */
		this.connectionString = formatSQLServerConnectionString(this.servername, this.databasename, this.username,
				this.password);
		this.connection = createConnection();
	}
	
	private Connection createConnection() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(this.connectionString);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}

	@Override
	public ArrayList<String> getDataWithRetry(String query) {
		ArrayList<String> queryResult = new ArrayList<String>();
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = this.connection.createStatement();
		} catch (Exception e) {
			this.connection = createConnection();
			return getData(query);
		}
		try {
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				queryResult.add(resultSet.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (statement != null)
				try {
					statement.close();
				} catch (Exception e) {
				}
			if (this.connection != null)
				try {
					this.connection.close();
				} catch (Exception e) {
				}
			if (resultSet != null)
				try {
					resultSet.close();
				} catch (Exception e) {
				}
		}
		return queryResult;
	}

	@Override
	public ArrayList<String> getData(String query) {
		ArrayList<String> queryResult = new ArrayList<String>();
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = this.connection.createStatement();
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				queryResult.add(resultSet.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (statement != null)
				try {
					statement.close();
				} catch (Exception e) {
				}
			if (this.connection != null)
				try {
					this.connection.close();
				} catch (Exception e) {
				}
			if (resultSet != null)
				try {
					resultSet.close();
				} catch (Exception e) {
				}
		}
		return queryResult;
	}

	@Override
	public void updateData(String updateQuery) {
		PreparedStatement ps = null;
		try {
			ps = this.connection.prepareStatement(updateQuery);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (this.connection != null)
				try {
					this.connection.close();
				} catch (Exception e) {
				}
			if (ps != null)
				try {
					ps.close();
				} catch (Exception e) {
				}
		}
	}

	private String formatSQLServerConnectionString(String servername, String databasename, String username,
			String password) {
		String connectionString = "jdbc:sqlserver://" + servername + ".database.windows.net:1433;" + "database="
				+ databasename + ";" + "user=" + username + ";" + "password=" + password + ";" + "encrypt=true;"
				+ "trustServerCertificate=false;" + "hostNameInCertificate=*.database.windows.net;"
				+ "loginTimeout=30;";
		return connectionString;
	}
}
