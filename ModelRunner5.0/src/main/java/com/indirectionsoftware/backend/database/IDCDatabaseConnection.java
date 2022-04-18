package com.indirectionsoftware.backend.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.indirectionsoftware.utils.IDCUtils;
import com.mysql.jdbc.Driver;

public class IDCDatabaseConnection {

	/************************************************************************************************/

	public static final String DBTYPE_PROPS = "DbType";
	public static final String DBDRIVER_PROPS = "DbDriver";
	public static final String DBURL_PROPS = "DbURL";

	private String 	dbTypeStr, url, driver;

	public static final int UNKNOWNDB=0, MYSQL=1, SQLSERVER=2;
	int dbType = UNKNOWNDB;
	
    private Connection con = null;

    /************************************************************************************************/

    boolean debug;

	/**************************************************************************************************/
	// Constructor ...
	/**************************************************************************************************/

    public IDCDatabaseConnection(String dbTypeStr, String url, String driver, boolean debug) throws Error {

    	this.dbTypeStr = dbTypeStr;
    	this.url = url;
    	this.driver = driver;
    	this.debug = debug;
    	
		if(dbTypeStr.equals("MySQL")) {
			dbType = MYSQL;
		} else if(dbTypeStr.equals("SQLServer")) {
			dbType = SQLSERVER;
		} 

    }

    public static IDCDatabaseConnection getConnection(String propsFileName, boolean debug) throws Error {
    	
    	IDCDatabaseConnection ret = null;
    	
		try {
			Properties props = IDCUtils.loadProperties(propsFileName);
			ret = new IDCDatabaseConnection(props.getProperty(DBTYPE_PROPS), props.getProperty(DBURL_PROPS), props.getProperty(DBDRIVER_PROPS), debug);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return ret;
    	
    }
    
	/**************************************************************************************************/
	// Connect ...
	/**************************************************************************************************/

    public boolean connect() {

    	boolean ret = true;
		
    	if(con == null) {
    		
    		try {
    			IDCUtils.info("Loading Database driver = " + driver);
    			//Class.forName(driver).newInstance();
    			com.mysql.cj.jdbc.Driver Driver  = new com.mysql.cj.jdbc.Driver();

    			IDCUtils.info("Connecting to Database at URL = " + url);
    			con = DriverManager.getConnection(url, "mysql", "password");

    		} catch (Exception e) {
    			e.printStackTrace();
    			ret = false;
    		}

    	}
		
		return ret;
		
    }

	/**************************************************************************************************/
	// Disconnect ...
	/**************************************************************************************************/

    public void disconnect() {

    	try {

    		IDCUtils.info("Closing Database connection to " + url);
    		if(con != null) {
        		con.close();
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

    public IDCDbQueryResult executeQuery(String query, int maxRows) throws SQLException {

    	IDCUtils.traceStart("executeQuery(query=" + query + ")");
    	
    	IDCUtils.dbLog(query);
    	
    	IDCDbQueryResult ret = null;
    	
        ResultSet rs;
    	Statement stmt; 
    	
        stmt = con.createStatement();
        if(maxRows != -1) {
            stmt.setMaxRows(maxRows);
        }
        rs = stmt.executeQuery(query);

        ret = new IDCDbQueryResult(stmt, rs);

        IDCUtils.traceEnd("executeQuery()");

    	return ret;

    }

    public IDCDbQueryResult executeQuery(String query) throws SQLException {

        return executeQuery(query, -1);

    }

    /**********************************************************************************************/

    public void executeUpdate(String query, boolean ignoreException) throws Error{

    	IDCUtils.traceStart("executeUpdate(query=" + query + ")");

    	IDCUtils.dbLog(query);
    	
        try {

           Statement stmt = con.createStatement();
           stmt.executeUpdate(query);
           stmt.close();

        } catch(SQLException e) {
        	if(!ignoreException) {
                throw new Error("SQLException: " + e.getMessage());
        	}
        }

    	IDCUtils.traceEnd("executeUpdate()");

    }

    /************************************************************************************************/

    public void executeDateTimeUpdate(String query, java.sql.Date date, java.sql.Time time) throws Error {

    	IDCUtils.traceStart("executeDateTimeUpdate(query=" + query + ")");
    	
    	IDCUtils.dbLog(query);
    	
        try {

           PreparedStatement stmt = con.prepareStatement(query);
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
			executeUpdate("CREATE DATABASE " + schemaName, true);
		} else {
			executeUpdate("CREATE DATABASE " + schemaName, true);
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
