package com.indirectionsoftware.backend.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.indirectionsoftware.utils.IDCUtils;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;

public class IDCDatabaseConnection {

	/************************************************************************************************/

	private String 	url, driver, user, pwd;

	public static final int UNKNOWNDB=0, MYSQL=1, SQLSERVER=2;
	int dbType = UNKNOWNDB;
	
    private Connection con = null;
    
    /************************************************************************************************/

    boolean debug;

	/**************************************************************************************************/
	// Constructor ...
	/**************************************************************************************************/

    public IDCDatabaseConnection(String dbTypeStr, String url, String driver, String user, String pwd, boolean debug) throws Error {

    	this.url = url;
    	this.driver = driver;
    	this.debug = debug;
    	this.user = user;
    	this.pwd = pwd;
    	
    	IDCUtils.debug("IDCDatabaseConnection(): url = " + url + " / user = " + user + " / pwd = " + pwd);

		if(dbTypeStr.equals("MySQL")) {
			dbType = MYSQL;
		} else if(dbTypeStr.equals("SQLServer")) {
			dbType = SQLSERVER;
		} 

    }

	/**************************************************************************************************/
	// Connect ...
	/**************************************************************************************************/

    public boolean connect() {
    	
    	boolean ret = true;
		
    	
    	if(con == null) {
    		
    		try {
    			
    			com.mysql.cj.jdbc.Driver driver  = new com.mysql.cj.jdbc.Driver();

    			IDCUtils.info("Connecting to Database at URL = " + url);
    			con = DriverManager.getConnection(url, user, pwd);
    			
    		} catch (Exception e) {
    			
    			IDCUtils.error("Error connecting to database: " + e.getMessage());
    			ret = false;
    		}

    	}
		
		return ret;
		
    }
    
	static final int timeout = 1;

	/**************************************************************************************************/
	// Set timeouts ...
	/**************************************************************************************************/

    public void setupConnectionTimeout(String var) throws SQLException {
    	
    	IDCUtils.traceStart("setupConnectionTimeout(var=" + var + ")");
    	
    	con.createStatement().executeUpdate("SET GLOBAL " + var + "=" + timeout);

        IDCUtils.traceEnd("setupConnectionTimeout()");

    }

	/**************************************************************************************************/
	// Disconnect ...
	/**************************************************************************************************/

    public void disconnect() {

    	try {
    		
    		IDCUtils.info("Closing Database connection to " + url);
    		if(con != null) {
        		con.close();
        		con = null;
    		}

    	} catch(Exception e) {
    	}

    }

	/**************************************************************************************************/
	// Start Transaction ...
	/**************************************************************************************************/

    public void startTransaction() throws Error {

    	IDCUtils.traceStart("startTransaction()");

    	try {
			con.setAutoCommit(false);
		} catch (SQLException e) {
    		throw new Error("SQLException: " + e.getMessage());
		}

    	IDCUtils.traceEnd("startTransaction()");

    }

	/**************************************************************************************************/
	// End Transaction ...
	/**************************************************************************************************/

    public void endTransaction(boolean isCommit) throws Error {

    	IDCUtils.traceStart("endTransaction(isCommit=" + isCommit + ")");

    	try {

			if(isCommit) {
    			con.commit();
    		} else {
    			con.rollback();
			}
        	con.setAutoCommit(true);

    	} catch(SQLException e) {
    		throw new Error("SQLException: " + e.getMessage());
    	}

    	IDCUtils.traceEnd("endTransaction()");

    }

	/**************************************************************************************************/
	// Execute queries ...
	/**************************************************************************************************/

    public IDCDbQueryResult executeQuery(String query) throws SQLException {
        return executeQuery(query, -1);
    }

    public IDCDbQueryResult executeQuery(String query, int maxRows) throws SQLException {

    	IDCUtils.traceStart("executeQuery(query=" + query + ")");
    	
    	IDCDbQueryResult ret = null;
    	
    	try {   		
    		ret = executeSingleQuery(query, maxRows);
    	} catch(CommunicationsException ex) {
    		IDCUtils.info("IDCDatabaseConnection.executeQuery(): caught Exception: + " + ex.getMessage());
    		con = null;
    		if(connect()) {
    			IDCUtils.info("IDCDatabaseConnection.executeQuery(): connection reset!");
        		ret = executeSingleQuery(query, maxRows);
    		} else {
    			IDCUtils.error("IDCDatabaseConnection.executeQuery(): error reseting connection!");
    			throw new SQLException("IDCDatabaseConnection.executeQuery(): error reseting connection!");
    		}
        }
    	
        IDCUtils.traceEnd("executeQuery()");

    	return ret;

    }

    public IDCDbQueryResult executeSingleQuery(String query, int maxRows) throws SQLException {

    	IDCUtils.traceStart("executeSingleQuery(query=" + query + ")");
    	
    	IDCDbQueryResult ret = null;
    	
        ResultSet rs;
    	Statement stmt; 
    	
		stmt = con.createStatement();
        if(maxRows != -1) {
            stmt.setMaxRows(maxRows);
        }
        
        rs = stmt.executeQuery(query);

        ret = new IDCDbQueryResult(stmt, rs);
        
        IDCUtils.traceEnd("executeSingleQuery()");

    	return ret;

    }

    /**********************************************************************************************/

    public void executeUpdate(String query, boolean ignoreException) throws Error {

    	IDCUtils.traceStart("executeUpdate(query=" + query + ")");

    	try {

        	try {   		
        		executeSingleUpdate(query, ignoreException);
        	} catch(CommunicationsException ex) {
        		IDCUtils.info("IDCDatabaseConnection.executeUpdate(): caught Exception: + " + ex.getMessage());
        		con = null;
        		if(connect()) {
        			IDCUtils.info("IDCDatabaseConnection.executeUpdate(): connection reset!");
            		executeSingleUpdate(query, ignoreException);
        		} else {
        			IDCUtils.error("IDCDatabaseConnection.executeUpdate(): error reseting connection!");
        			ignoreException = false;
        			throw new SQLException("IDCDatabaseConnection.executeUpdate(): error reseting connection!");
        		}

        	}
    	
    	} catch(SQLException e) {
    		IDCUtils.info("IDCDatabaseConnection.executeUpdate(): caught SQLException: " + e.getMessage());
        	if(!ignoreException) {
                throw new Error("SQLException: " + e.getMessage());
        	}
        }

    	IDCUtils.traceEnd("executeUpdate()");

    }

    /**********************************************************************************************/

    public void executeSingleUpdate(String query, boolean ignoreException) throws SQLException {

    	IDCUtils.traceStart("executeSingleUpdate(query=" + query + ")");

    	Statement stmt = con.createStatement();;
        stmt.executeUpdate(query);
        stmt.close();

    	IDCUtils.traceEnd("executeSingleUpdate()");

    }

    /************************************************************************************************/

    public void executeDateTimeUpdate(String query, java.sql.Date date, java.sql.Time time) throws Error {

    	IDCUtils.traceStart("executeDateTimeUpdate(query=" + query + ")");
    	
        try {

        	PreparedStatement stmt;

    		try {
        		stmt = con.prepareStatement(query);
        	} catch(CommunicationsException ex) {
        		con = null;
        		if(connect()) {
        			IDCUtils.info("IDCDatabaseConnection.executeDateTimeUpdate(): connection reset!");
        			stmt = con.prepareStatement(query);
        		} else {
        			IDCUtils.error("IDCDatabaseConnection.executeDateTimeUpdate(): error reseting connection!");
        			throw new SQLException("IDCDatabaseConnection.executeDateTimeUpdate(): error reseting connection!");
        		}
        	}

    		stmt.setDate(1, date);
    		stmt.setTime(2, time);
    		stmt.executeUpdate();
    		stmt.close();

        } catch(SQLException e) {
             throw new Error("SQLException: " + e.getMessage());
        }

    	IDCUtils.traceEnd("executeDateTimeUpdate()");

    }
    
	/**************************************************************************************************/
	
    public DatabaseMetaData getDatabaseMetadata() {
		
		DatabaseMetaData ret = null;
		
		try {
			if(con != null) {
				ret = con.getMetaData();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
		
	}
	
	/**************************************************************************************************/
	
	public void dropTable(String tableName) {

		if(dbType == MYSQL) {
			executeUpdate("DROP TABLE IF EXISTS " + tableName, false);
		} else {
			executeUpdate("DROP TABLE " + tableName, true);
		}
		
	}

	/**************************************************************************************************/
	
	public String getLongTextColType() {

		String ret = null;
		
		switch(dbType) {
		
			case MYSQL:
				ret = "LONGTEXT";
				break;
				
			case SQLSERVER:
				ret = "VARCHAR(MAX)";
				break;
				
			default:
				ret = "VARCHAR(5000)";
				break;
				
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	
	public String getDoubleColType() {

		String ret = null;
		
		switch(dbType) {
		
			case SQLSERVER:
				ret = "FLOAT";
				break;
				
			default:
				ret = "DOUBLE";
				break;
				
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	
	public String getCreateTablePart1() {

		String ret = null;
		
		switch(dbType) {
		
		case SQLSERVER:
				ret = "PRIMARY KEY IDENTITY";
				break;
				
			default:
				ret = "AUTO_INCREMENT";
				break;
				
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	
	public String getCreateTablePart2(String tableIdColName) {

		String ret = null;
		
		switch(dbType) {
		
		case SQLSERVER:
				ret = ")";
				break;
				
			default:
				if(tableIdColName != null) {
					ret = ", PRIMARY KEY (" + tableIdColName + ")";
				}
				//ret += ") TYPE=InnoDB";
				ret += ")";
				break;
				
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	
	public String getColType(String colType) {

		String ret = colType;

		if(colType != null) {
			if(colType.equals("LONGTEXT")) {
				ret = getLongTextColType();
			} else if(colType.equals("DOUBLE")) {
				ret = getDoubleColType();
			} 
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	
	public void createSchema(String schemaName) {
		if(dbType == MYSQL) {
			executeUpdate("CREATE DATABASE " + schemaName, false);
		} else {
			executeUpdate("CREATE DATABASE " + schemaName, false);
		}
	}

	/**************************************************************************************************/
	
	public void dropSchema(String schemaName) {
		if(dbType == MYSQL) {
			executeUpdate("DROP DATABASE " + schemaName, true);
		} else {
			executeUpdate("DROP DATABASE " + schemaName, true);
		}
	}

}