package com.indirectionsoftware.backend.database;

import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCDatabaseRef;
import com.indirectionsoftware.metamodel.IDCModelParser;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCXMLImportParser;
import com.indirectionsoftware.runtime.IDCURL;
import com.indirectionsoftware.runtime.webapp.IDCWebAppContext;
import com.indirectionsoftware.runtime.webapp.IDCWebApplication;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCDbManager {
	
	/* System Table: includes MetaModel and other stuff if needed later */
	public final static String SYSTAB = "$$SystemTable", SYSTAB_KEYCOL = "_Key", SYSTAB_VALUECOL = "_Value"; 
	public final static String SYSMETAMODEL="MM"; 
	public final static String MODELADMINMODEL="MAM"; 
	
	public static final String VERSIONROOT = "/Users/etienne/code/projects/ModelRunner";
	public static final String PROPERTIES = "/deploy/runtime.properties";
	public static final String STABLEVERSION = "v4.0";
	public static final String DEVVERSION = "dev";
	
	/*******************************************************************************************************/
	
	private static final String[] FUNCTIONS = {"Help", "Init", "Deploy", "AddUser", "UpdUser", "AddTestData", "Import", "SetupDev", "SetupStable", "Export", "UpdateSystemFiles"}; 
	private static final int HELP=0, INIT=1, DEPLOY=2, ADDUSER=3, UPDUSER=4, ADDTESTDATA=5, IMPORT=6, SETUPDEV=7, SETUPSTABLE=8, EXPORT=9, UPDATE_SYSTEMFILES=10;
	
	public static final String META_MODEL = "MetaModel";
	
	private static final int NUM_TEST_OBJS=5000;
	
	/*******************************************************************************************************/
	
	private DatabaseMetaData md;
	
	private IDCSystemApplication systemApp;
	private IDCDatabaseConnection con;
	
	private IDCModelParser parser;

	/*******************************************************************************************************/
	
	public static void main(String[] args) {
		
		int func=0;

		if (args.length < 2) {
            System.err.println("Invalid arguments ...");
        } else {
    		func = decodeArgs(args[0], FUNCTIONS);
        }

		if(func==0) {
			usage();
		} else {
			
			IDCDbManager dbManager = getIDCDbManager(args[1], false);
			if(dbManager != null) {
				
				try {
					
					switch(func) {

						case INIT:
							if (args.length != 4) {
					            System.err.println("Invalid arguments ... please specify the Properties file, the MetaModel file and the ModelAdmin Model file");
					            usage();
					        } else {
					            System.err.println("YOU SURE YOu WANT TO DO THIS... DID YOU BACKUP EVERYTHING???");
//					    		dbManager.generateSystemDatabase(args[2], args[3]);
					        }
							break;
						
						case UPDATE_SYSTEMFILES:
							if (args.length != 4) {
					            System.err.println("Invalid arguments ... please specify the Properties file, the MetaModel file and the ModelAdmin Model file");
					            usage();
					        } else {
					        	dbManager.updateSystemKeyValue(SYSMETAMODEL, IDCUtils.readFile(args[2]));
					        	dbManager.updateSystemKeyValue(MODELADMINMODEL, IDCUtils.readFile(args[3]));
					        }
							break;
						
						case DEPLOY:
							if (args.length < 3) {
					            System.err.println("Invalid arguments ... please specify the Properties file and the Model file name");
					            usage();
					        } else {
					        	boolean isClean = false;
								if (args.length == 4 && args[3].equals("Clean")) {
									isClean = true;
								}
								dbManager.deployApplication(args[2], isClean);
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
							dbManager.setup(DEVVERSION);
							break;

						case SETUPSTABLE:
							dbManager.setup(STABLEVERSION);
							break;
					}
					
					dbManager.disconnect();
					
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
	            System.err.println("Can't connect to database schema");
			}
			
		}
		
	}
	
	/*******************************************************************************************************/
	
	private static void usage() {
		
		IDCUtils.info("Parameters: [function] [properties file] [[arguments]]");
		IDCUtils.info("       where function is : ");
		IDCUtils.info("              Help: this screen");
		IDCUtils.info("              Init: generate the database system tables");
		IDCUtils.info("              AddTenant: xxx");
		IDCUtils.info("              Deploy: xxx");
		IDCUtils.info("              AddUser: xxx");
		IDCUtils.info("              UpdUser: xxx");
		IDCUtils.info("              AddTestData: xxx");
		
	}
	
	/*******************************************************************************************************/
	
	public static IDCDbManager getIDCDbManager(String propsFileName) {
		return getIDCDbManager(propsFileName, true);
	}
	
	public static IDCDbManager getIDCDbManager(String propsFileName, boolean isGetSystemApp) {

		IDCDbManager ret = new IDCDbManager();
		
		ret.con = IDCDatabaseConnection.getConnection(propsFileName, true);
		if(!ret.con.connect()) {
			ret = null;
		}
		
		if(isGetSystemApp) {
			ret.getSystemApplication();
		}
		
		return ret;

	}

	// called by IDCService
	public static IDCDbManager getIDCDbManager(String dbTypeStr, String url, String driver) {

		IDCDbManager ret = new IDCDbManager();
		
		ret.con = new IDCDatabaseConnection(dbTypeStr, url, driver, true);
		if(!ret.con.connect()) {
			ret = null;
		}
		
		ret.getSystemApplication();
		
		return ret;

	}
	
	/*******************************************************************************************************/
	
	public void generateSystemDatabase(String metamodelName, String modelAdminModelName) {
		
    	generateSystemTable();
    	
		publishSystemKey(SYSMETAMODEL, metamodelName);
		publishSystemKey(MODELADMINMODEL, modelAdminModelName);
		
		IDCSystemApplication adminApp = getSystemApplication();
		if(adminApp == null) {
			System.err.println("Can't load ModelAdmin application ... ");
		} else {
			
			adminApp.generateSystemSchema();

			adminApp.addApplication(adminApp.getName(), adminApp.getModelXML());
			
			IDCData adminUser = adminApp.addUser(IDCSystemApplication.DEFAULT_USER, IDCSystemApplication.DEFAULT_PWD);
			adminApp.linkUserToApp(adminUser, adminApp.getName());

		}

	}
	
	/**************************************************************************************************/

	public IDCSystemApplication getSystemApplication() {
		return getSystemApplication(false);
	}
	
	public IDCSystemApplication getSystemApplication(boolean isReload) {
		
		if(systemApp == null || isReload) { // 
			IDCApplication app = getNewSystemApplication();
			if(app != null) {
				app.init();
	        	if(app.checkIntegrity()) {
	        		systemApp = new IDCSystemApplication(this, app, getParser());
	        	}
			}
		}
		
		return systemApp;
		
	}
	
	/*******************************************************************************************************/
	
	public IDCApplication getNewSystemApplication() {
		
		IDCApplication ret =  null;
		
		String systemAppModelXML = loadSystemKeyValue(MODELADMINMODEL);
		if(systemAppModelXML != null) {
			ret = getApplicationFromXML(systemAppModelXML);
			if(ret != null) {
				ret.setConnection(con);
			}
		}
		
		return ret;
		
	}
	
	/*******************************************************************************************************/
	
	private IDCApplication getApplicationFromXML(String modelXML) {
		return getParser().loadApplication(modelXML);
	}

	/*******************************************************************************************************/
	
	public void deployApplication(String modelFileName) {
		deployApplication(modelFileName, false);
	}
	
	public void deployApplication(String modelFileName, boolean isClean) {
		String modelXML = IDCUtils.readFile(modelFileName);
		if(modelXML != null) {
			deployApplicationFomXML(modelXML, isClean);
		} else {
			System.err.println("Can't read model file: " + modelFileName);
		}

	}
	
	public boolean deployApplicationFomXML(String modelXML, boolean isClean) {

		boolean ret = false;
		
		IDCSystemApplication adminApp = getSystemApplication();

		IDCApplication newAppl = getApplicationFromXML(modelXML);
		if(newAppl != null) {
			
			newAppl.init();
			
			IDCData oldAppData = adminApp.getApplication(newAppl.getName());
			if(oldAppData == null) {
				
				adminApp.createApplicationSchema(newAppl.getName());
				if(newAppl.connect()) {
					newAppl.generateSchema(false);
					adminApp.addApplication(newAppl.getName(), modelXML);
					adminApp.linkDefaultUserToApp(newAppl.getName());
					ret = true;
				}
				
			} else {
				
				IDCApplication oldAppl = getApplicationFromXML(oldAppData.getString(IDCSystemApplication.APPLICATION_XML));
				if(oldAppl != null) {
					
					oldAppl.init();
					
					if(isClean) {
						
						adminApp.dropApplicationSchema(newAppl.getName());
						adminApp.createApplicationSchema(newAppl.getName());
						
						if(newAppl.connect()) {
							newAppl.generateSchema(false);
							adminApp.linkDefaultUserToApp(newAppl.getName());
							ret = true;
						}
						
					} else {
						ret = deploy(newAppl, oldAppl);
					}
					
					if(ret) {
						oldAppData.set(IDCSystemApplication.APPLICATION_XML, modelXML);
						oldAppData.save();
					}
					
				}
				
			}
		}
		
		return ret;

	}

	/*******************************************************************************************************/
	
	public boolean updateApplicationXML(String tenantName, String modelXML) {

		boolean ret = false;
		
		IDCSystemApplication adminApp = getSystemApplication();

		IDCApplication newAppl = getApplicationFromXML(modelXML);
		if(newAppl != null) {
			
			newAppl.init();
			
			IDCData oldAppData = adminApp.getApplication(newAppl.getName());
			if(oldAppData != null) {
				oldAppData.set(IDCSystemApplication.APPLICATION_XML, modelXML);
				oldAppData.save();
				ret = true;
			}
		}
		
		return ret;

	}

	/*******************************************************************************************************/
	
	public IDCApplication getApplicationFromModelFile(String modelFileName) {
		return getApplicationFromXML(IDCUtils.readFile(modelFileName));
	}

	/*******************************************************************************************************/
	
	public IDCModelParser getParser() {
		
		if(parser == null) {
			parser = new IDCModelParser(loadSystemKeyValue(SYSMETAMODEL), this);
		}
		
		return parser;
		
	}

	/**********************************************************************************************************************************************************************************************************************/

	public void addUser(String applName, String userName, String passwd) {

		IDCSystemApplication adminApp = getSystemApplication();
		IDCData adminUser = adminApp.addUser(IDCSystemApplication.DEFAULT_USER, IDCSystemApplication.DEFAULT_PWD);
		adminApp.linkUserToApp(adminUser, applName);

	}

	/*******************************************************************************************************/
	
	public IDCSystemUser login(String applicationName, String userName, String passwd) {
		return getSystemApplication().login(applicationName, userName, passwd);
	}

	/*******************************************************************************************************/
	
	public void addTestData(String appName) {

		IDCSystemApplication sysApp = getSystemApplication();
		if(sysApp == null) {
			System.err.println("Can't load ModelAdmin application ... ");
		} else {
			
			IDCSystemUser user = sysApp.login(appName, IDCSystemApplication.DEFAULT_USER, IDCSystemApplication.DEFAULT_PWD);
			if(user != null) {
				IDCApplication app = user.getApplication();
				app.connect();
				app.getNewTestData();
			}

		}

	}

	/*******************************************************************************************************/
	
	public IDCApplication getApplication(String appName) {
		
		IDCApplication ret = null;

		IDCSystemApplication sysApp = getSystemApplication();
		if(sysApp == null) {
			System.err.println("Can't load ModelAdmin application ... ");
		} else {
			IDCSystemUser user = sysApp.login(appName, IDCSystemApplication.DEFAULT_USER, IDCSystemApplication.DEFAULT_PWD);
			if(user != null) {
				ret = user.getApplication();
				ret.connect();
			}

		}
		
		return ret;

	}

	/**********************************************************************************************************************************************************************************************************************/

	public void generateSystemTable() {

    	try {
    		
			con.dropTable(SYSTAB);
			
			String query = "CREATE TABLE " + SYSTAB + " (" +  SYSTAB_KEYCOL + " VARCHAR(50), " + SYSTAB_VALUECOL+ " " + con.getLongTextColType() + ")";
			IDCUtils.debug(query);
			con.executeUpdate(query, false);

    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
		
	}

	/*******************************************************************************************************/
	
	private void publishSystemKey(String key, String fn) {

		String value = IDCUtils.readFile(fn);

		if(value != null) {
			insertSystemKeyValue(key, value);
		} else {
			IDCUtils.info("Error: can't read " + fn);		
		}
		
	}
	
	/**************************************************************************************************/

	public void insertSystemKeyValue(String key, String value) {

    	try {
    		
			String query = "INSERT INTO " + SYSTAB + " (" + SYSTAB_KEYCOL + ", " + SYSTAB_VALUECOL + ") VALUES ('" + key  + "' , '" + value + "')";
			IDCUtils.debug(query);
			con.executeUpdate(query, false);
			
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
		
	}

	/*******************************************************************************************************/
	
	public void updateSystemApplication(String fn) {

		String value = IDCUtils.readFile(fn);

		if(value != null) {
			insertSystemKeyValue(MODELADMINMODEL, value);
			getSystemApplication(true);
		} else {
			IDCUtils.info("Error: can't read " + fn);		
		}
		
	}
	
	/**************************************************************************************************/

	public void updateSystemKeyValue(String key, String value) {

    	try {
    		
    		String query = "UPDATE " + SYSTAB + " SET " + SYSTAB_VALUECOL + " = '" + value + "' WHERE " + SYSTAB_KEYCOL + " = '" + key + "'";
			IDCUtils.debug(query);
    		con.executeUpdate(query, false);
			
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
		
	}

	/**************************************************************************************************/

	public String loadSystemKeyValue(String key) {

		String ret = null;
		
    	try {
    		
			String query = "SELECT " + SYSTAB_VALUECOL + " FROM " + SYSTAB + " WHERE " + SYSTAB_KEYCOL + " = '" + key + "'";
			IDCUtils.debug(query);
            IDCDbQueryResult dbRes = con.executeQuery(query);
            ResultSet rs = dbRes.getResultSet();
			
        	while (rs.next()) {
                ret = rs.getString(SYSTAB_VALUECOL);
          	}
        	
        	dbRes.close();

    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
		
    	return ret;
    	
	}
	
	/**********************************************************************************************************************************************************************************************************************/

	public static int decodeArgs(String s, String[] functions) {

		int ret = 0;
		
		int nFunc=0;
		for(String func : functions) {
			if(func.equalsIgnoreCase(s)) {
				ret = nFunc;
				break;
			}
			nFunc++;
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
		
		boolean isSameDatabase = newDbRef.equalsTo(oldDbRef);
		
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

	/**********************************************************************************************************************************************************************************************************************/

	/**********************************************************************************************************************************************************************************************************************/

	public void test2() {

		for(String cat : getCatalogNames()) {
			IDCUtils.debug("catalog = " + cat);
			for(List<String> table : getTables(cat)) {
				IDCUtils.debug("> table: " + table.get(2));
				int ntparm=0;
				for(String parm : table) {
					IDCUtils.debug(ntparm++ + " : " + parm);
				}

				for(List<String> col : getColumns(cat, table.get(2))) {
					IDCUtils.debug("> column: " + col.get(3));
					ntparm=0;
					for(String parm : col) {
						IDCUtils.debug(ntparm++ + " : " + parm);
					}
				}
			}
		}
		
	}

	/*******************************************************************************************************/
	
	private List<String>  getCatalogNames() {

		List<String> ret = new ArrayList<String>();
		
    	try {
    		
			ResultSet rs = md.getCatalogs();
			while(rs.next()) {
				ret.add(rs.getString("TABLE_CAT"));
			}
			
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
		
    	return ret;
    	
	}

	/*******************************************************************************************************/
	
	private List<String>  getSchemaNames() {

		List<String> ret = new ArrayList<String>();
		
    	try {
    		
			ResultSet rs = md.getSchemas();
			while(rs.next()) {
				String schema = rs.getString("TABLE_SCHEM");
				String cat = rs.getString("TABLE_CATALOG");
				ret.add(schema + "*" + cat);
			}
			
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
		
    	return ret;
    	
	}

	/*******************************************************************************************************/
	
	private List<List<String>> getTables(String cat) {

		List<List<String>> ret = new ArrayList<List<String>>();
		
    	try {
    		
			ResultSet rs = md.getTables(cat, null, null, null);
			while(rs.next()) {

				List<String> table = new ArrayList<String>();
				table.add(rs.getString("TABLE_CAT"));
				table.add(rs.getString("TABLE_SCHEM"));
				table.add(rs.getString("TABLE_NAME"));
				table.add(rs.getString("TABLE_TYPE"));
				table.add(rs.getString("REMARKS"));
				//table.add(rs.getString("TYPE_CAT"));
				table.add(rs.getString("TABLE_CAT"));
				//table.add(rs.getString("TYPE_SCHEM"));
				//table.add(rs.getString("SELF_REFERENCING_COL_NAME"));
				//table.add(rs.getString("REF_GENERATION"));
				ret.add(table);
			}
			
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
		
		
		return ret;
		
	}

	/*******************************************************************************************************/
	
	private List<List<String>> getColumns(String cat, String table) {

		List<List<String>> ret = new ArrayList<List<String>>();
		
    	try {
    		
			ResultSet rs = md.getColumns(cat, null, table, null);
			while(rs.next()) {

				List<String> col = new ArrayList<String>();
				col.add(rs.getString("TABLE_CAT"));
				col.add(rs.getString("TABLE_SCHEM"));
				col.add(rs.getString("TABLE_NAME"));
				col.add(rs.getString("COLUMN_NAME"));
				col.add(rs.getString("DATA_TYPE"));
				col.add(rs.getString("TYPE_NAME"));
				col.add(rs.getString("COLUMN_SIZE"));
				col.add(rs.getString("BUFFER_LENGTH"));
				col.add(rs.getString("DECIMAL_DIGITS"));
				col.add(rs.getString("NUM_PREC_RADIX"));
				col.add(rs.getString("NULLABLE"));
				col.add(rs.getString("REMARKS"));
				col.add(rs.getString("COLUMN_DEF"));
				col.add(rs.getString("SQL_DATA_TYPE"));
				col.add(rs.getString("SQL_DATETIME_SUB"));
				col.add(rs.getString("CHAR_OCTET_LENGTH"));
				col.add(rs.getString("ORDINAL_POSITION"));
				col.add(rs.getString("IS_NULLABLE"));
				//col.add(rs.getString("SCOPE_CATLOG"));
				//col.add(rs.getString("SCOPE_SCHEMA"));
				//col.add(rs.getString("SCOPE_TABLE"));
				//col.add(rs.getString("SOURCE_DATA_TYPE"));
				ret.add(col);
			}
			
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
		
		
		return ret;
		
	}

	/*******************************************************************************************************/
	
	public List<String> getAllApplicationNames() {
		return getSystemApplication().getAllApplicationNames();
	}

	/*******************************************************************************************************/
	
	public String getApplicationXML(String appName) {
		return getSystemApplication().getApplicationXML(appName);
	}

	/*******************************************************************************************************/
	
	public void disconnect() {
		con.disconnect();		
	}
	
	/*******************************************************************************************************/
	
	private void importData(String appName, String fileName) {
		
		IDCSystemApplication sysApp = getSystemApplication();
		
		IDCSystemUser user = sysApp.login(appName, "adm", "pwd");
		if(user != null) {
			IDCApplication app = user.getApplication();
			app.connect();
    		IDCXMLImportParser importer = new IDCXMLImportParser(app);
    		importer.importXML(new File(fileName));
		}
		
	}
	
	/*******************************************************************************************************/
	
	private void exportData(String appName, String fileName) {
		
		IDCSystemApplication sysApp = getSystemApplication();
		
		IDCSystemUser user = sysApp.login(appName, "adm", "pwd");
		if(user != null) {
			IDCApplication app = user.getApplication();
			app.connect();
			app.exportXML(fileName, true);
		}
		
	}
	
	/*******************************************************************************************************/
	
	private void setup(String version) {
		generateSystemDatabase(VERSIONROOT + version + "/config/MetaModel.xml", VERSIONROOT + version + "/config/ModelAdmin.xml");
	}

}