package com.indirectionsoftware.backend.database;

import java.io.IOException;
import java.util.Properties;

import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCSuperAdminDbManager extends IDCDbManager {
	
	private IDCSuperAdminApplication superAdminApp;

	/*******************************************************************************************************/
	
	public IDCSuperAdminDbManager(String dbTypeStr, String server, String name, String params, String driver, String dbUser, String dbPwd) {
		super(dbTypeStr, server, name, params, driver, dbUser, dbPwd);
	}

	/*******************************************************************************************************/
	
	public static IDCSuperAdminDbManager getIDCDbSuperAdminManager(String propsFileName, String dbUser, String dbPwd) {

		IDCSuperAdminDbManager ret = null;
		
		try {
			
			Properties props = IDCUtils.loadProperties(propsFileName);
			
			ret = getIDCDbSuperAdminManager(props.getProperty(DBTYPE_PROPS), props.getProperty(DBSERVER_PROPS), props.getProperty(DBNAME_PROPS), props.getProperty(DBPARAMS_PROPS), props.getProperty(DBDRIVER_PROPS), dbUser, dbPwd);

		} catch (IOException e) {
			e.printStackTrace();
		}
    	
		return ret;

	}

	/*******************************************************************************************************/
	
	public static IDCSuperAdminDbManager getIDCDbSuperAdminManager(String dbTypeStr, String server, String name, String params, String driver, String dbUser, String dbPwd) {

		IDCSuperAdminDbManager ret = new IDCSuperAdminDbManager(dbTypeStr, server, name, params, driver, dbUser, dbPwd);
		if(ret != null && !ret.con.connect()) {
			ret = null;
		}
		
		return ret;

	}
	
	/*******************************************************************************************************/
	
	public void init(String metamodelName, String superAdminModelName) {
		
		IDCUtils.info("IDCSuperAdminDbManager.init(): metamodelName=" + metamodelName + " / superAdminModelName=" + superAdminModelName);
		
    	generateSystemTable();
    	
		publishSystemKey(SYSMETAMODEL, metamodelName);
		publishSystemKey(MODELADMINMODEL, superAdminModelName);
		
		initSuperAdminApplication();
		
		if(superAdminApp != null) {
			superAdminApp.generateSchema();
		}

	}
	
	/**************************************************************************************************/

	private void initSuperAdminApplication() {
		
		IDCApplication app = loadSystemApplication();
		if(app != null) {
			superAdminApp = new IDCSuperAdminApplication(app);
		} else {
			System.err.println("Can't load SuperAdmin application ... ");
		}
		
	}
	
	/**************************************************************************************************/

	public IDCSuperAdminApplication getSuperAdminApplication() {
		
		if(superAdminApp == null) {
			initSuperAdminApplication();	
		}
		
		return  superAdminApp;
		
	}

}