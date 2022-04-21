package com.indirectionsoftware.backend.database;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCModelData;
import com.indirectionsoftware.metamodel.IDCModelParser;
import com.indirectionsoftware.metamodel.IDCPackage;
import com.indirectionsoftware.metamodel.IDCType;

public class IDCSystemApplication {

	public final static String APPLICATION_TYPE = "$$Application"; 
	public final static String APPLICATION_NAME = "Name", APPLICATION_XML = "XML"; 

	public final static String USER_TYPE = "$$User";
	public final static String USER_NAME = "Name", USER_PASSWD = "Password", USER_APPS = "Applications";
	public static final int USER_APPS_N = 2;
	
	public final static String DEFAULT_USER = "adm";
	public final static String DEFAULT_PWD = "pwd";
	
	public final static String ADMIN_ROLE = "Admin";
	public final static String ROLE_TYPE = "$$Role";
	public final static String ROLE_NAME = "Name", ROLE_PASSWD = "Password", ROLE_APPS = "Applications";

	public final static String ADMIN_APPL = "$$Admin";

	/*******************************************************************************************************/
	
	private IDCDbManager dbManager;
	private IDCApplication systemApp;
	private IDCModelParser parser;
	
	/*******************************************************************************************************/
	// System Application
	/*******************************************************************************************************/
	
	public IDCSystemApplication(IDCDbManager dbManager, IDCApplication modelAdminAppl, IDCModelParser parser) {
		this.dbManager = dbManager;
		this.systemApp = modelAdminAppl;
		this.parser = parser;
		systemApp.connect();
	}
	
	/*******************************************************************************************************/
	
	public IDCApplication getSystemApplication() {
		return systemApp;
	}
	
	/*******************************************************************************************************/
	
	public void generateSystemSchema() {
		systemApp.generateSchema(true);
	}

	/*******************************************************************************************************/
	
	public String getModelXML() {
		return systemApp.getModelXML();
	}

	public String getName() {
		return systemApp.getName();
	}
	
	/*******************************************************************************************************/
	// Applications
	/*******************************************************************************************************/
	
	public void addApplication(String applicationName, String modelXML) {

		IDCData newAppl = systemApp.getType(APPLICATION_TYPE).getNewObject();
		newAppl.set(APPLICATION_NAME, applicationName);
		newAppl.set(APPLICATION_XML, modelXML);
		newAppl.save();

	}

	/*******************************************************************************************************/
	
	public IDCData getApplication(long appId) {
		return systemApp.getType(APPLICATION_TYPE).loadDataObject(appId);
	}

	/*******************************************************************************************************/
	
	public IDCData getApplication(String applicationName) {
		return systemApp.getType(APPLICATION_TYPE).requestSingleData(APPLICATION_NAME + " == '" + applicationName + "'");
	}

	/*******************************************************************************************************/
	
	public List<IDCData> getAllApplications() {
		return systemApp.getType(APPLICATION_TYPE).loadAllDataObjects();
	}

	/*******************************************************************************************************/
	
	public List<String> getAllApplicationNames() {

		List<String> ret = new ArrayList<String>();
		
		for(IDCData app : getAllApplications()) {
			ret.add(app.getName());
		}
		
		return ret;
		
	}

	/*******************************************************************************************************/
	
	public String getApplicationXML(String applicationName) {
		
		String ret = null;
		
		IDCData appl = getApplication(applicationName);
		if(appl != null) {
			ret = appl.getString(APPLICATION_XML);
		}
		
		return ret;

	}

	/*******************************************************************************************************/
	
	public void createApplicationSchema(String schemaName) {
		systemApp.getDatabaseRef().getConnection().createSchema(schemaName);
	}

	/*******************************************************************************************************/
	
	public void dropApplicationSchema(String schemaName) {
		systemApp.getDatabaseRef().getConnection().dropSchema(schemaName);
	}

	/*******************************************************************************************************/
	// Users
	/*******************************************************************************************************/
	
	public IDCData addUser(String userName, String userPwd) {

		IDCData ret = null;
		
		ret = systemApp.getType(USER_TYPE).getNewObject();
		ret.set(USER_NAME, userName);
		ret.set(USER_PASSWD, userPwd);
		ret.save();
		
		return ret;

	}
	
	/*******************************************************************************************************/
	
	public IDCData getUser(String userName) {
		return systemApp.getType(USER_TYPE).requestSingleData(USER_NAME + " == '" + userName + "'");
	}

	/*******************************************************************************************************/
	
	public void linkDefaultUserToApp(String applicationName) {
		
		IDCData defaultUser = getUser(DEFAULT_USER);
		if(defaultUser != null) {
			linkUserToApp(defaultUser, applicationName);
		}

	}

	/*******************************************************************************************************/
	
	public void linkUserToApp(IDCData user, String applicationName) {
		
		IDCData app = getApplication(applicationName);
		if(app != null) {
			IDCData userApp = getUserApp(user, applicationName);
			if(userApp == null) {
				user.insertReference(USER_APPS, app);
				user.save();
			}
		}

	}

	/*******************************************************************************************************/
	
	public IDCSystemUser login(String applicationName, String userName, String passwd) {

		IDCSystemUser ret = null;
		
		IDCData user = getUser(userName);
		if(user != null) {
			if(user.getString(IDCSystemApplication.USER_NAME).equals(userName) && user.getString(IDCSystemApplication.USER_PASSWD).equals(passwd)) {
				IDCData applicationData = getApplication(applicationName);
				if(applicationData != null) {
					IDCData userApp = getUserApp(user, applicationName);
					if(userApp != null) {
						IDCApplication app = parser.loadApplication(applicationData.getString(IDCSystemApplication.APPLICATION_XML));
						if(app != null) {
							if(!applicationName.equals(ADMIN_APPL)) {
								IDCApplication sysAppCopy = dbManager.getNewSystemApplication();
								sysAppCopy.init();
								app.addPackages(sysAppCopy.getPackages(), true);
							}
							app.init(user);
				        	if(app.checkIntegrity()) {
								ret = new IDCSystemUser(user, app);
								app.setUser(ret);
				        	}
						}
					}
				}
			}
		}

		return ret;
		
	}

	/*******************************************************************************************************/
	
	public IDCData getUserApp(IDCData user, String applicationName) {

		IDCData ret = null;
		
		for(IDCData userApp : user.getList(IDCSystemApplication.USER_APPS)) {
			if(userApp.getString(IDCSystemApplication.APPLICATION_NAME).equals(applicationName)) {
				ret = userApp;
			}
		}

		return ret;
		
	}

}