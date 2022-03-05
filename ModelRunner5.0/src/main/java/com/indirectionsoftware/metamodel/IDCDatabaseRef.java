package com.indirectionsoftware.metamodel;

import java.io.PrintWriter;
import java.sql.DatabaseMetaData;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDatabaseConnection;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCDatabaseRef extends IDCModelData {
	
	private String 	dbType, url, driver;
	private boolean debug;
	
	private IDCDatabaseConnection con; // used to share same connection between System App ann IDCDbManager
	
	public static final int DBTYPE=START_ATTR, URL=START_ATTR+1, DRIVER=START_ATTR+2, DEBUG=START_ATTR+3;

	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCDatabaseRef(IDCModelData parent, long id, List<Object> values) {
		super(parent, IDCModelData.DATABASEREF, id, values);
	}
	
	/**************************************************************************************************/

	public void init() {
		init(null);
	}
	
	public void init(IDCData userData) {
		
		if(isInitRequired()) {

			super.init(userData);
			
			dbType = getString(DBTYPE);
			url = getString(URL);
//////////////////////////////////////////////////////////////////////////////////////////
			url = IDCUtils.replaceAll(url, "-=AND=-", "&");
//////////////////////////////////////////////////////////////////////////////////////////
			driver = getString(DRIVER);
			debug = IDCUtils.translateBoolean(getString(DEBUG));
			
			if(con == null) {
				con = new IDCDatabaseConnection(dbType, url, driver, debug);
			}
		
		}
		
	}

	/**************************************************************************************************/

	public void setConnection(IDCDatabaseConnection con) {
		this.con = con;
	}

	/**************************************************************************************************/
	// Database Reference methods ...
	/**************************************************************************************************/
	
	public String getDatabaseType() {
		return dbType;
	}

	public DatabaseMetaData getDatabaseMetadata() {
		return con.getDatabaseMetadata();
	}

	/**************************************************************************************************/

	public String getURLId() {
		return url;
	}

	/**************************************************************************************************/

	public String getDriver() {
		return driver;
	}

	/**************************************************************************************************/

	public IDCDatabaseConnection getConnection() {
		return con;
	}

	/**************************************************************************************************/

	public boolean equalsTo(IDCDatabaseRef otherRef) {

		boolean ret = false;
		
		if(getDatabaseType().equals(otherRef.getDatabaseType()) && getURLId().equals(otherRef.getURLId())) {
			ret = true;
		}

		return ret;
		
	}


	
}