package com.indirectionsoftware.backend.database;

import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCDatabaseRef;
import com.indirectionsoftware.metamodel.IDCModelParser;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCXMLImportParser;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCAdminDbManager extends IDCDbManager {
	
	/*******************************************************************************************************/
	
	private static final String[] FUNCTIONS = {"Help", "Init", "Deploy", "AddUser", "UpdUser", "AddTestData", "Import", "SetupDev", "SetupStable", "Export", "UpdateSystemFiles"}; 
	private static final int HELP=0, INIT=1, DEPLOY=2, ADDUSER=3, UPDUSER=4, ADDTESTDATA=5, IMPORT=6, SETUPDEV=7, SETUPSTABLE=8, EXPORT=9, UPDATE_SYSTEMFILES=10;
	
	/*******************************************************************************************************/
	
	private IDCAdminApplication adminApp;
	
	private IDCModelParser parser;

	/*******************************************************************************************************/
	
	public IDCAdminDbManager(String dbTypeStr, String server, String name, String params, String driver, String dbUser, String dbPwd) {
		super(dbTypeStr, server, name, params, driver, dbUser, dbPwd);
	}

	/*******************************************************************************************************/
	
	public static void main(String[] args) {
		
		int func = HELP;

		if (args.length < 4) {
            System.err.println("Invalid arguments ...");
        } else {
    		func = decodeArgs(args[0], FUNCTIONS);
        }

		if(func == HELP) {
			usage();
		} else {
			
			IDCAdminDbManager dbManager = getIDCDbAdminManager(args[1], false);
			if(dbManager != null) {
				
				if(dbManager.connect()) {
					
					if(func == INIT) {

						if (args.length != 5) {
				            System.err.println("Invalid arguments ... please specify the Properties file name, the Ontology file name and the MetaModel file name");
				            usage();
				        } else {
//				            System.err.println("YOU SURE YOu WANT TO DO THIS... DID YOU BACKUP EVERYTHING???");
				    		dbManager.init(args[2], args[3], args[4]);
				        }

					} else {

					 IDCSystemUser user = dbManager.adminApp.userLogin(args[2], args[3]);
					 if(user != null) {
						 
						 try {
								
							switch(func) {

								case UPDATE_SYSTEMFILES:
									if (args.length != 6) {
							            System.err.println("Invalid arguments ... please specify the Properties file, the admin user name and password, the MetaModel file and the ModelAdmin Model file");
							            usage();
							        } else {
							        	dbManager.updateSystemKeyValue(SYSMETAMODEL, IDCUtils.readFile(args[4]));
							        	dbManager.updateSystemKeyValue(MODELADMINMODEL, IDCUtils.readFile(args[5]));
							        }
									break;
								
								case DEPLOY:
									if (args.length < 5) {
							            System.err.println("Invalid arguments ... please specify the Properties file, the admin user name and password and the Model file name");
							            usage();
							        } else {
							        	boolean isClean = false;
										if (args.length == 4 && args[3].equals("Clean")) {
											isClean = true;
										}
										dbManager.deployUserApplication(user, args[2], isClean);
							        }
									break;
								
								case ADDTESTDATA:
									dbManager.addTestData(args[2]);
									break;
								
								case ADDUSER:
									dbManager.addUser(args[3], args[4], args[5]);
									break;
								
								case IMPORT:
									if (args.length != 4) {
							            System.err.println("Invalid arguments ... please specify the Properties file, the Application name and the file name");
							            usage();
							        } else {
										dbManager.importData(args[2], args[3]);
							        }
									break;
								
								case EXPORT:
									if (args.length != 4) {
							            System.err.println("Invalid arguments ... please specify the Properties file, the Application name and the file name");
							            usage();
							        } else {
										dbManager.exportData(args[2], args[3]);
							        }
									break;
								
								case SETUPDEV:
									//dbManager.setup(DEVVERSION);
									break;

								case SETUPSTABLE:
									//dbManager.setup(STABLEVERSION);
									break;
							}
							
							dbManager.disconnect();
								
						} catch (Exception e) {
							e.printStackTrace();
						}

					 }
					 
				}

				} else {
		            System.err.println("Can't connect to database schema");
				}
				
			} else {
	            System.err.println("Can't create DB manager");
			}
			
		}
		
	}
	
	/*******************************************************************************************************/
	
	private static void usage() {
		
		IDCUtils.info("Parameters: [function] [properties file name] [[args]] ... where function is:");
		
		IDCUtils.info("  Help: this screen");
		
		IDCUtils.info("  Init: generate the database system tables");
		IDCUtils.info("        args = [admin user name] [admin password] [MetaModel file name] [Ontology file name] [SuperAdmin model file name]");

		IDCUtils.info("              AddTenant: xxx");
		IDCUtils.info("              Deploy: xxx");
		IDCUtils.info("              AddUser: xxx");
		IDCUtils.info("              UpdUser: xxx");
		IDCUtils.info("              AddTestData: xxx");
		
	}

	/*******************************************************************************************************/
	
	public static IDCAdminDbManager getIDCDbAdminManager(String propsFileName, boolean isGetSystemApp, String dbUser, String dbPwd) {

		IDCAdminDbManager ret = null;
		
		try {
			
			Properties props = IDCUtils.loadProperties(propsFileName);
			
			ret = getIDCDbAdminManager(props.getProperty(DBTYPE_PROPS), props.getProperty(DBSERVER_PROPS), props.getProperty(DBNAME_PROPS), props.getProperty(DBPARAMS_PROPS), props.getProperty(DBDRIVER_PROPS), dbUser, dbPwd, isGetSystemApp);

		} catch (IOException e) {
			e.printStackTrace();
		}
    	
		return ret;

	}

	/*******************************************************************************************************/
	
	public static IDCAdminDbManager getIDCDbAdminManager(String dbTypeStr, String server, String name, String params, String driver, String dbUser, String dbPwd, boolean isGetSystemApp) {

		IDCAdminDbManager ret = new IDCAdminDbManager(dbTypeStr, server, name, params, driver, dbUser, dbPwd);
		if(!ret.con.connect()) {
			ret = null;
		} else if(isGetSystemApp) {
			ret.loadAdminApplication();
		}
		
		return ret;

	}
	

	/*******************************************************************************************************/
	// Init
	/*******************************************************************************************************/
	
	public void init(String metamodelName, String ontologyName, String adminModelName, String adminUser, String adminPwd) {
		
		IDCUtils.debug("IDCAdminDbManager.init(): metamodelName=" + metamodelName + " / ontologyName=" + ontologyName  + " / adminModelName=" + adminModelName);

    	generateSystemTable();
    	
		publishSystemKey(SYSMETAMODEL, metamodelName);
		publishSystemKey(SYSONTOLOGY, ontologyName);
		publishSystemKey(MODELADMINMODEL, adminModelName);
		
		loadAdminApplication();
		
		if(adminApp == null) {
			System.err.println("Can't load ModelAdmin application ... ");
		} else {	
			adminApp.init(adminUser, adminPwd);
		}
		
	}
	
	/*******************************************************************************************************/
	// Admin App
	/**************************************************************************************************/

	private void loadAdminApplication() {
		
		IDCApplication app = loadSystemApplication();
		if(app != null) {
			adminApp = new IDCAdminApplication(this, app, this.getParser());
		} else {
			System.err.println("Can't load Admin application ... ");
		}
		
	}
	
	/*******************************************************************************************************/
	
	public IDCAdminApplication getAdminApplication() {
		
		if(adminApp == null) {
			loadAdminApplication();
		}
		
		return adminApp;
		
	}

	/*******************************************************************************************************/
	
	private IDCApplication getApplicationFromXML(String modelXML) {
		return getParser().loadApplication(modelXML);
	}

	/*******************************************************************************************************/
	// Deploy Application
	/*******************************************************************************************************/
	
	public void deployUserApplication(IDCSystemUser user, String modelFileName) {
		deployUserApplication(user, modelFileName, false);
	}
	
	/*******************************************************************************************************/
	
	public void deployUserApplication(IDCSystemUser user, String modelFileName, boolean isClean) {
		String modelXML = IDCUtils.readFile(modelFileName);
		if(modelXML != null) {
			deployUserApplicationFomXML(user, modelXML, isClean);
		} else {
			System.err.println("Can't read model file: " + modelFileName);
		}

	}
	
	/*******************************************************************************************************/
	
	public boolean deployUserApplicationFomXML(IDCSystemUser user, String modelXML, boolean isClean) {

		boolean ret = false;
		
		IDCApplication newAppl = getApplicationFromXML(modelXML);
		if(newAppl != null) {
			
			String newSchemaName = newAppl.getName();
			
			if(newAppl.getDatabaseRef() == null) {
				newSchemaName = IDCDatabaseRef.getDatabaseName(user.getName(), newAppl.initGetName());
				newAppl.setDatabaseConnection(getApplicationDatabaseConnection(newSchemaName));
			}
			
			newAppl.init(user.getUserData());
			
			IDCData oldAppData = adminApp.getUserApplication(user.getUserData(), newAppl.getName());
			if(oldAppData == null) {
				
				adminApp.dropApplicationSchema(newSchemaName);
				adminApp.createApplicationSchema(newSchemaName);
				
				if(newAppl.connect()) {
					newAppl.generateSchema(false);
					adminApp.addUserApplication(user.getUserData(), newAppl.getName(), modelXML);
					ret = true;
				}
				
			} else {
				
				IDCApplication oldAppl = getApplicationFromXML(oldAppData.getString(IDCAdminApplication.APPLICATION_XML));
				if(oldAppl != null) {
					
					String oldSchemaName = oldAppl.getName();
					
					if(oldAppl.getDatabaseRef() == null) {
						oldSchemaName = IDCDatabaseRef.getDatabaseName(user.getName(), oldAppl.initGetName());
						oldAppl.setDatabaseConnection(getApplicationDatabaseConnection(oldSchemaName));
					}
					
					oldAppl.init(user.getUserData());
					
					if(isClean) {
						
						adminApp.dropApplicationSchema(oldSchemaName);
						adminApp.createApplicationSchema(newSchemaName);
						
						if(newAppl.connect()) {
							newAppl.generateSchema(false);
							ret = true;
						}
						
					} else {
						ret = deploy(newAppl, oldAppl);
					}
					
					if(ret) {
						oldAppData.set(IDCAdminApplication.APPLICATION_XML, modelXML);
						oldAppData.save();
					}
					
				}
				
			}
		}
		
		return ret;

	}

	/*******************************************************************************************************/
	
//	public IDCApplication getApplicationFromModelFile(String modelFileName) {
//		return getApplicationFromXML(IDCUtils.readFile(modelFileName));
//	}

	/*******************************************************************************************************/
	
	public IDCModelParser getParser() {
		
		if(parser == null) {
			parser = new IDCModelParser(loadSystemKeyValue(SYSMETAMODEL), this);
		}
		
		return parser;
		
	}

	/**********************************************************************************************************************************************************************************************************************/

	public void addUser(String applName, String userName, String passwd) {

		IDCData adminUser = adminApp.addUser(IDCAdminApplication.DEFAULT_USER, IDCAdminApplication.DEFAULT_PWD);
		adminApp.linkUserToApp(adminUser, applName);

	}

	/*******************************************************************************************************/
	
	public IDCSystemUser login(String applicationName, String userName, String passwd) {
		return adminApp.login(applicationName, userName, passwd);
	}

	/*******************************************************************************************************/
	
	public void addTestData(String appName) {

		IDCSystemUser user = adminApp.login(appName, IDCAdminApplication.DEFAULT_USER, IDCAdminApplication.DEFAULT_PWD);
		if(user != null) {
			IDCApplication app = user.getApplication();
			app.connect();
			app.getNewTestData();
		}

	}

	/*******************************************************************************************************/
	
	public IDCApplication getApplication(String appName) {
		
		IDCApplication ret = null;

		IDCSystemUser user = adminApp.login(appName, IDCAdminApplication.DEFAULT_USER, IDCAdminApplication.DEFAULT_PWD);
		if(user != null) {
			ret = user.getApplication();
			ret.connect();
		}
		
		return ret;

	}


	/*******************************************************************************************************/
	
	public boolean deploy(IDCApplication newAppl, IDCApplication oldAppl) {
		
		boolean ret = false;
		
		IDCUtils.info("Deploy Application ...");

		if(newAppl.connect() && oldAppl.connect()) {
			
			List<IDCType> oldTypes = oldAppl.getTypes();
			
			for(IDCType newType: newAppl.getTypes()) {
				IDCUtils.info("Processing Type = " + newType.getName() + " ... ");
				IDCType oldType = oldAppl.getType(newType.getName());
				updateTypeSchema(oldType, newType);

			}

			for(IDCType oldType: oldTypes) {
				IDCType newType = newAppl.getType(oldType.getName());
				if(newType == null) {
					IDCUtils.info("Removing Type = " + oldType.getName() + " ... ");
					oldType.dropTable();
				}					
			}
			
			ret = true;

		}

		
		IDCUtils.info("Deploying Application Complete.");
		
		return ret;
				
	}

	/*******************************************************************************************************/
	
	private void updateTypeSchema(IDCType oldType, IDCType newType) {
		
		if(oldType == null) {
			IDCUtils.info("new type ... creating table(s)");
			newType.dropTable();
			newType.createTable();
		} else {
			IDCUtils.info("existing type ... checking for updates ...");
			updateTypeSchema2(oldType, newType);
			//oldTypes.remove(oldType);
		}

	}
	
	/*******************************************************************************************************/
	
	private void updateTypeSchema2(IDCType oldType, IDCType newType) {

		IDCDatabaseRef oldDbRef = oldType.getDatabaseRef();
		IDCDatabaseRef newDbRef = newType.getDatabaseRef();
		
		boolean isSameDatabase = (oldDbRef == null && newDbRef == null) || (oldDbRef != null && newDbRef != null && newDbRef.equalsTo(oldDbRef));
		
		List<IDCAttribute> attrsToDrop = new ArrayList<IDCAttribute>();
		List<String> attrsToInitialize = new ArrayList<String>(); 

		if(isSameDatabase) {
			
			for(IDCAttribute newAttr : newType.getAttributes()) {
				
				IDCUtils.debug("Processing Attribute = " + newAttr.getName() + " ... ");

				IDCAttribute oldAttr = oldType.getAttribute(newAttr.getName());
				if(oldAttr != null) {
					IDCUtils.debug("existing Attribute ... ");
					if(isUpdateAttributeNeeded(oldAttr, newAttr)) {
						IDCUtils.debug("update required ... ");
						oldType.dropAttribute(oldAttr);
						newType.insertAttribute(newAttr);
						attrsToInitialize.add(newAttr.getName());
					}
				} else {
					IDCUtils.debug("new Attribute ... ");
					//newType.dropAttribute(newAttr);
					newType.insertAttribute(newAttr);
					attrsToInitialize.add(newAttr.getName());
				}
				
				IDCUtils.debug("");
			}
			
		} else {
			newType.dropTable();
			newType.createTable();
			for(IDCAttribute newAttr : newType.getAttributes()) {
				attrsToInitialize.add(newAttr.getName());
			}
		}
		
		if(attrsToInitialize.size() > 0) {

			List<IDCDataRef> oldRefs = oldType.loadAllDataReferences();
			
			for(IDCDataRef oldRef : oldRefs) {
				
				IDCData oldData = null;
				IDCData newData = null;
				
				oldData = oldType.loadDataRef(oldRef);
				newData = newType.getNewObject();

				for(String attrName : attrsToInitialize) {
				
					IDCAttribute newAttr = newType.getAttribute(attrName);
					IDCAttribute oldAttr = oldType.getAttribute(attrName);
					if(oldAttr != null && !isSameDatabase) {
						Object oldValue = oldData.getValue(oldAttr.getAttributeId());
						newData.setValue(newAttr.getAttributeId(), oldValue);
					} else {
						newData.set(newAttr.getAttributeId(), newAttr.getDefaultValue());
					}
					
				}
				
				if(isSameDatabase) {
					newType.updateObject(newData);
				} else {
					newData.setId(oldData.getId());
					newData.setNamespaceParentRef(oldData.getNamespaceParentRef());
					newType.addObject(newData);
				}
			}
		}

		if(!isSameDatabase) {
			oldType.dropTable();
		} else {

			for(IDCAttribute oldAttr : oldType.getAttributes()) {
				IDCAttribute newAttr = newType.getAttribute(oldAttr.getName());
				if(newAttr == null) {
					IDCUtils.debug("Removing attribute = " + oldAttr.getName() + " ... ");
					oldType.dropAttribute(oldAttr);
				}
			}

		}
		
	}

	/*******************************************************************************************************/
	
	private boolean isUpdateAttributeNeeded(IDCAttribute oldAttr, IDCAttribute newAttr) {
		
		boolean ret = true;
		
		int oldAttrType = oldAttr.getAttributeType();
		int newAttrType = newAttr.getAttributeType();
		
		if(newAttrType == oldAttrType) {
			ret = false;
		} else {
			if(newAttr.getAttributeClass() == oldAttr.getAttributeClass()) {
				ret = false;
			}
		}
		
		return ret;

	}



	/*******************************************************************************************************/
	
	public List<String> getAllApplicationNames() {
		return adminApp.getAllApplicationNames();
	}

	/*******************************************************************************************************/
	
	public String getApplicationXML(String appName) {
		return adminApp.getApplicationXML(appName);
	}

	/*******************************************************************************************************/
	
	public void disconnect() {
		con.disconnect();		
	}
	
	/*******************************************************************************************************/
	
	private void importData(String appName, String fileName) {
		
		IDCSystemUser user = adminApp.login(appName, "adm", "pwd");
		if(user != null) {
			IDCApplication app = user.getApplication();
			app.connect();
    		IDCXMLImportParser importer = new IDCXMLImportParser(app);
    		importer.importXML(new File(fileName));
		}
		
	}
	
	/*******************************************************************************************************/
	
	private void exportData(String appName, String fileName) {
		
		IDCSystemUser user = adminApp.login(appName, "adm", "pwd");
		if(user != null) {
			IDCApplication app = user.getApplication();
			app.connect();
			app.exportXML(fileName, true);
		}
		
	}
	
	/*******************************************************************************************************/
	
	private void setup(String version) {
//		generateSystemDatabase(VERSIONROOT + version + "/config/MetaModel.xml", VERSIONROOT + version + "/config/IDCGlobalLexicon.txt", VERSIONROOT + version + "/config/ModelAdmin.xml");
	}

}