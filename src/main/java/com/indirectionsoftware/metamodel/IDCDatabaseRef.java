package com.indirectionsoftware.metamodel;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDatabaseConnection;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCDatabaseRef extends IDCModelData {
	
	private String 	dbType, url, driver, user, password;
	private boolean debug;
	
	public static final int DBTYPE=START_ATTR, URL=START_ATTR+1, DRIVER=START_ATTR+2, USER=START_ATTR+3, PASSWORD=START_ATTR+4, DEBUG=START_ATTR+5;

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
			user = getString(USER);
			password = getString(PASSWORD);
			
			debug = IDCUtils.translateBoolean(getString(DEBUG));
			
			setDatabaseConnection(new IDCDatabaseConnection(dbType, url, driver, user, password, debug));
		
		}
		
	}

	/**************************************************************************************************/

	public IDCDatabaseRef clone(IDCModelData parent, String name) {
		
		IDCDatabaseRef ret = null;
		
		List<Object> newValues = new ArrayList<Object>();
		
		for(Object val : values) {
			newValues.add(val);
		}
		
		newValues.set(DBTYPE, dbType);
		newValues.set(URL, getNewDatabaseURLForName(name));
		newValues.set(DRIVER, driver);
		newValues.set(USER, user);
		newValues.set(PASSWORD, password);
		newValues.set(DEBUG, debug);
		
		ret = new IDCDatabaseRef(parent, -1, newValues);
		ret.init();
		
		return ret;
		
	}
	
	/**************************************************************************************************/
	// Database Reference methods ...
	/**************************************************************************************************/
	
	public String getDatabaseType() {
		return dbType;
	}

	public DatabaseMetaData getDatabaseMetadata() {
		return getDatabaseConnection().getDatabaseMetadata();
	}

	/**************************************************************************************************/

	public String getURL() {
		return url;
	}

	/**************************************************************************************************/

	public String getDriver() {
		return driver;
	}

	/**************************************************************************************************/

	public boolean equalsTo(IDCDatabaseRef otherRef) {

		boolean ret = false;
		
		if(getDatabaseType().equals(otherRef.getDatabaseType()) && getURL().equals(otherRef.getURL())) {
			ret = true;
		}

		return ret;
		
	}

	/**************************************************************************************************/

	public String getDatabaseName() {
		
		String ret = url;
		
		int end = url.indexOf('?');
		if(end != -1) {
			ret = url.substring(0, end);
		}
		
		int start = ret.lastIndexOf('/');
		
		ret = ret.substring(start + 1);
		
		return ret;
		
	}

	/**************************************************************************************************/

	public String getNewDatabaseURLForName(String name) {
		
		String ret = null;

		String suffix = "";
		String prefix = null;
		
		String temp = url;
		
		int end = url.indexOf('?');
		if(end != -1) {
			suffix = url.substring(end);
			temp = url.substring(0, end);
		}
		
		int start = temp.lastIndexOf('/');		
		prefix = temp.substring(0,start+1);
		
		ret = prefix + name + suffix;
		
		return ret;
		
	}

	/**************************************************************************************************/

	public static String getDatabaseName(String userName, String appName) {
		
		String ret = "IDC";
		
		String name = userName + appName;
		
		for(int i=0; i < name.length(); i++) {
			char c = name.charAt(i);
			if(c != '.' && c != '@') {
				ret += c;
			}
			
		}
		
		return ret;
		
	}

}