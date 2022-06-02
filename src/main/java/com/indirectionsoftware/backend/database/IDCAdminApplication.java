package com.indirectionsoftware.backend.database;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCDatabaseRef;
import com.indirectionsoftware.metamodel.IDCModelData;
import com.indirectionsoftware.metamodel.IDCModelParser;
import com.indirectionsoftware.metamodel.IDCPackage;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCAdminApplication {

	public final static String ACCOUNT_TYPE = "$$Account"; 
	public final static String ACCOUNT_NAME = "Name", ACCOUNT_APPS = "Applications", ACCOUNT_USERS = "Users", ACCOUNT_TEAMS = "Teams", ACCOUNT_ADMINS = "AdminUsers", ORIG_USER = "OriginalUser"; 
	public static final int    ACCOUNT_APPS_N = 1, ACCOUNT_USERS_N = 2;

	public final static String APPLICATION_TYPE = "$$Application"; 
	public final static String APPLICATION_NAME = "Name", APPLICATION_XML = "XML"; 

	public final static String USER_TYPE = "$$User";
	public final static String USER_NAME = "Name", USER_PASSWD = "Password", USER_EMAIL = "Email", USER_APPS = "Applications",          USER_APP_ROLES = "ApplicationRoles";
	public static final int USER_APPS_N = 2,      USER_APP_ROLES_N = 2;
	
	public final static String APPROLE_TYPE = "$$ApplicationRole";
	public final static String APPROLE_APP = "Application", APPROLE_ROLE = "Role";
	public static final int APPROLE_ROLE_USER = 0, APPROLE_ROLE_ADMIN = 1, APPROLE_ROLE_EDITOR = 2, APPROLE_ROLE_SUPERADMIN = 3;
	
	public final static String USERROLE_TYPE = "$$UserRole";
	public final static String USERROLE_APP = "Application", USERROLE_ROLE = "Role";
	public static final int USERROLE_ROLE_USER = 2;
	
	
	public final static String ADMIN_ROLE = "Admin";
	public final static String ROLE_TYPE = "$$Role";
	public final static String ROLE_NAME = "Name", ROLE_PASSWD = "Password", ROLE_APPS = "Applications";

	public final static String ADMIN_APPL = "$$Admin";
	
	/*******************************************************************************************************/
	
	private IDCAdminDbManager dbAdminManager;
	private IDCApplication adminApp;
	private IDCModelParser parser;
	
	/*******************************************************************************************************/
	// System Application
	/*******************************************************************************************************/
	
	public IDCAdminApplication(IDCAdminDbManager dbManager, IDCApplication modelAdminAppl, IDCModelParser parser) {
		this.dbAdminManager = dbManager;
		this.adminApp = modelAdminAppl;
		this.parser = parser;
		adminApp.connect();
	}
	
	/*******************************************************************************************************/
	
	public IDCApplication getadminApplication() {
		return adminApp;
	}
	
	/*******************************************************************************************************/
	
	public String getModelXML() {
		return adminApp.getModelXML();
	}

	public String getName() {
		return adminApp.getName();
	}
	
	/*******************************************************************************************************/
	
	public void generateSystemSchema() {
		adminApp.generateSchema(true);
	}

	/*******************************************************************************************************/
	/*******************************************************************************************************/
	// INIT WITHOUT ACCOUNTS
	/*******************************************************************************************************/	
	/*******************************************************************************************************/
	
	public void init(String adminUserName, String adminPwd) {
		
		adminApp.generateSchema(true);
		
		IDCData adminUser = addUser(adminUserName, adminPwd);
		
		IDCData newApp = addUserApplication(adminUser, getName(), getModelXML());
		
	}
	
	/*******************************************************************************************************/
	// Users
	/*******************************************************************************************************/
	
	public IDCData addUser(String userName, String userPwd) {

		IDCData ret = null;
		
		ret = adminApp.getType(USER_TYPE).getNewObject();
		
		ret.set(USER_NAME, userName);
		ret.set(USER_PASSWD, userPwd);
		
		ret.save(false, false);
		
		return ret;

	}
	
	/*******************************************************************************************************/
	
	public IDCData getUser(String userName) {
		return adminApp.getType(USER_TYPE).requestSingleData(USER_NAME + " == '" + userName + "'");
	}

	/*******************************************************************************************************/
	
	public IDCSystemUser userLogin(String userName, String passwd) {

		IDCSystemUser ret = null;
		
		IDCData user = getUser(userName);
		if(user != null) {
			if(user.getString(USER_PASSWD).equals(passwd)) {
				ret = new IDCSystemUser(user, null);
				IDCUtils.info("All logged in :)");
			}
		}

		return ret;
		
	}
	
	/*******************************************************************************************************/
	
	public IDCSystemUser applicationLogin(String applicationName, String userName, String passwd) {

		IDCSystemUser ret = null;
		
		IDCData user = getUser(userName);
		if(user != null) {
			
			if(user.getString(USER_PASSWD).equals(passwd)) {
				
				IDCData applicationData = getUserApplication(user, applicationName);
				if(applicationData != null) {
					
					IDCApplication app = parser.loadApplication(applicationData.getString(IDCAdminApplication.APPLICATION_XML));
					if(app != null) {
						if(!applicationName.equals(ADMIN_APPL)) {
							IDCApplication sysAppCopy = dbAdminManager.loadSystemApplication();
							sysAppCopy.init();
							String schemaName = IDCDatabaseRef.getDatabaseName(userName, applicationName);
							app.setDatabaseConnection(dbAdminManager.getApplicationDatabaseConnection(schemaName));
							app.addPackages(sysAppCopy.getPackages(), true, sysAppCopy.getDatabaseConnection());
						} else {
							app.setDatabaseConnection(dbAdminManager.con);
						}

						app.init(user);
						
						String lexiconStr = dbAdminManager.loadSystemKeyValue(IDCAdminDbManager.SYSONTOLOGY);
						app.getOntology().loadLexicon(lexiconStr);

			        	if(app.checkIntegrity()) {
			        		
							ret = new IDCSystemUser(user, app);
							app.setUser(ret);
							
							IDCUtils.info("All logged in :)");

						} else {
							IDCUtils.info("IDCAdminApplication.applicationLogin(): app failed integrity test");
			        	}
					} else {
						IDCUtils.info("IDCAdminApplication.applicationLogin(): couldn't initialise application");
					}
					
				} else {
					IDCUtils.info("IDCAdminApplication.applicationLogin(): couldn't load application record");
				}
				
			} else {
				IDCUtils.info("IDCAdminApplication.applicationLogin(): wrong password");
			}
			
		} else {
			IDCUtils.info("IDCAdminApplication.applicationLogin(): couldn't find user");
		}

		return ret;
		
	}

	/*******************************************************************************************************/
	// Applications
	/*******************************************************************************************************/
	
	public IDCData addUserApplication(IDCData user, String applicationName, String modelXML) {

		IDCData ret = adminApp.getType(APPLICATION_TYPE).getNewObject();
		
		ret.setNamespaceParentRef(user.getAsParentRef(USER_APPS_N));
		
		ret.set(APPLICATION_NAME, applicationName);
		ret.set(APPLICATION_XML, modelXML);
		
		ret.save(false, false);
		
		return ret;

	}

	/*******************************************************************************************************/
	
	public IDCData getUserApplication(IDCData user, String applicationName) {
		return adminApp.getType(APPLICATION_TYPE).requestSingleData(APPLICATION_NAME + " == '" + applicationName + "'", user.getAsParentRef(USER_APPS_N));
	}

	/*******************************************************************************************************/
	
	public IDCData getApplication(long appId) {
		return adminApp.getType(APPLICATION_TYPE).loadDataObject(appId);
	}
	
	/*******************************************************************************************************/
	
	public List<IDCData> getUserApplications(IDCData user) {
		return user.getList(USER_APPS_N);
	}

	/*******************************************************************************************************/
	
	public List<IDCData> getAllApplications() {
		return adminApp.getType(APPLICATION_TYPE).loadAllDataObjects();
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
	
	public String getApplicationXML(IDCData account, String applicationName) {
		
		String ret = null;
		
		IDCData appl = getAccountApplication(account, applicationName);
		if(appl != null) {
			ret = appl.getString(APPLICATION_XML);
		}
		
		return ret;

	}

	/*******************************************************************************************************/
	
	public void createApplicationSchema(String schemaName) {
		adminApp.getDatabaseConnection().createSchema(schemaName);
	}

	/*******************************************************************************************************/
	
	public void dropApplicationSchema(String schemaName) {
		adminApp.getDatabaseConnection().dropSchema(schemaName);
	}

	/*******************************************************************************************************/
	
	public String getAllUsersCSV() {
		
		String ret = "Name, Password, Applications\n" ;
		
		for(IDCData reg : adminApp.getType(USER_TYPE).loadAllDataObjects()) {
			ret += reg.getString(USER_NAME) + "," + reg.getString(USER_PASSWD) + ",";
			String apps = "";
			for(IDCData app : reg.getList(USER_APPS)) {
				if(apps.length() > 0) {
					
				}
				apps += (apps.length() > 0 ? " & " : "") + app.getName();
			}
			ret += apps + "\n";
		}
		
		return ret;

	}
	
	/*******************************************************************************************************/
	/*******************************************************************************************************/
	// INIT WITH ACCOUNTS
	/*******************************************************************************************************/	
	/*******************************************************************************************************/
	// Accounts
	/*******************************************************************************************************/
	
	public IDCData createAccount(String accountName, String userName, String pwd, String userEmail) {
		
		IDCData ret = adminApp.getType(ACCOUNT_TYPE).getNewObject();
		ret.set(ACCOUNT_NAME, accountName);
		ret.save(false, false);

		IDCData adminUser = addAccountUser(ret, userName, pwd, userEmail); 
		
		ret.insertReference(ACCOUNT_ADMINS, adminUser);
		
		ret.set(ORIG_USER, adminUser);
		ret.save(false, false);

		return ret;

	}
	

	
	/*******************************************************************************************************/
	
	public IDCData getAccount(String accountName) {
		return adminApp.getType(USER_TYPE).requestDataByName(accountName);
	}
	
	/*******************************************************************************************************/
	// Users
	/*******************************************************************************************************/
	
	public IDCData addAccountUser(IDCData account, String userName, String userPwd, String userEmail) {

		IDCData ret = null;
		
		ret = adminApp.getType(USER_TYPE).getNewObject();
		
		ret.setNamespaceParentRef(account.getAsParentRef(ACCOUNT_USERS_N));
		
		ret.set(USER_NAME, userName);
		ret.set(USER_PASSWD, userPwd);
		
		ret.save(false, false);
		
		return ret;

	}
	
	/*******************************************************************************************************/
	
	public void setUserAppRole(IDCData user, IDCData app, int roleType) {
		
		IDCData appRole = getUserAppRole(user, app);
		
		if(appRole == null) {
			appRole = adminApp.getType(APPROLE_TYPE).getNewObject();
			appRole.setNamespaceParentRef(user.getAsParentRef(USER_APP_ROLES_N));
			appRole.set(APPROLE_APP, app);
		}
		
		appRole.set(APPROLE_ROLE, roleType);
		appRole.save();

	}
	
	/*******************************************************************************************************/
	
	public IDCData getUserAppRole(IDCData user, IDCData app) {

		IDCData ret = null;
		
		for(IDCData userAppRole : user.getList(USER_APP_ROLES)) {
			if(userAppRole.getData(USERROLE_APP).getId() == app.getId()) {
				ret = userAppRole;
			}
		}

		return ret;
		
	}

	/*******************************************************************************************************/
	
	public IDCSystemUser applicationLogin(String applicationName, String userName, String passwd, int role) {

		IDCSystemUser ret = null;
		
		IDCData user = getUser(userName);
		if(user != null) {
			
			if(user.getString(USER_PASSWD).equals(passwd)) {
				
				IDCData account = user.getNamespaceParent();
				IDCData applicationData = getAccountApplication(account, applicationName);
				if(applicationData != null) {
					
					IDCData userAppRole = getUserAppRole(user, applicationData);
					if(userAppRole != null && userAppRole.getInt(APPROLE_ROLE) >= role) {
						
						IDCApplication app = parser.loadApplication(applicationData.getString(IDCAdminApplication.APPLICATION_XML));
						if(app != null) {
							if(!applicationName.equals(ADMIN_APPL)) {
								IDCApplication sysAppCopy = dbAdminManager.loadSystemApplication();
								sysAppCopy.init();
								app.addPackages(sysAppCopy.getPackages(), true, sysAppCopy.getDatabaseConnection());
							}

							app.init(user);
							
							String lexiconStr = dbAdminManager.loadSystemKeyValue(IDCAdminDbManager.SYSONTOLOGY);
							app.getOntology().loadLexicon(lexiconStr);

				        	if(app.checkIntegrity()) {
								ret = new IDCSystemUser(account, user, app, role);
								app.setUser(ret);
								
								IDCUtils.info("All logged in :)");

				        	}
						}
					}
					
				}
				
			}
			
		}

		return ret;
		
	}
	
	

	public IDCData addAccountApplication(IDCData account, String applicationName, String modelXML) {

		IDCData ret = adminApp.getType(APPLICATION_TYPE).getNewObject();
		
		ret.setNamespaceParentRef(account.getAsParentRef(ACCOUNT_APPS_N));
		
		ret.set(APPLICATION_NAME, applicationName);
		ret.set(APPLICATION_XML, modelXML);
		
		ret.save(false, false);
		
		return ret;

	}

	/*******************************************************************************************************/
	
	public IDCData getAccountApplication(IDCData account, String applicationName) {
		return adminApp.getType(APPLICATION_TYPE).requestSingleData(APPLICATION_NAME + " == '" + applicationName + "'", account.getAsParentRef(ACCOUNT_APPS_N));
	}




}