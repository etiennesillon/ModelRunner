package com.indirectionsoftware.backend.database;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCModelParser;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCDbManager {
	
	protected final static String SYSTAB = "$$SystemTable", SYSTAB_KEYCOL = "_Key", SYSTAB_VALUECOL = "_Value"; 
	protected final static String SYSMETAMODEL="MM"; 
	protected final static String SYSONTOLOGY="ONT"; 
	protected final static String MODELADMINMODEL="MAM"; 
	
	/*******************************************************************************************************/
	
	public static final String DBTYPE_PROPS = "MRDbType";
	public static final String DBDRIVER_PROPS = "MRDbDriver";
	public static final String DBSERVER_PROPS = "MRDbServer";
	public static final String DBNAME_PROPS = "MRDbName";
	public static final String DBPARAMS_PROPS = "MRDbParams";
	public static final String DBLOGLEVEL_PROPS = "MRDbLogLevel";
	public static final String DBLOGMINLEVEL_PROPS = "MRDbLogMinLevel";
	
	protected DatabaseMetaData md;
	
	protected IDCDatabaseConnection con;
	
	protected IDCModelParser parser;
	
	protected String dbTypeStr, server, name, params, driver, url, dbUser, dbPwd;
	
	/*******************************************************************************************************/
	
	public IDCDbManager(String dbTypeStr, String server, String name, String params, String driver, String dbUser, String dbPwd) {
		
		if(dbUser == null) {
			dbUser = IDCSecurityManager.dbUser;
			dbPwd = IDCSecurityManager.dbPwd;
		}
		
		this.dbTypeStr = dbTypeStr;
		this.server = server;
		this.name = name;
		this.params = params;
		this.driver = driver;
		this.dbUser = dbUser;
		this.dbPwd = dbPwd;
		
		con = new IDCDatabaseConnection(dbTypeStr, getDbURL(), driver, dbUser, dbPwd, true);

	}
	
	/*******************************************************************************************************/
	
	public IDCDatabaseConnection getApplicationDatabaseConnection(String name) {
		return new IDCDatabaseConnection(dbTypeStr, getDbURL(name), driver, dbUser, dbPwd, true);
	}
	
	/*******************************************************************************************************/
	
	public String getDbURL() {
		
		if(url == null) {
			url = getDbURL(server, name, params);
		}
		
		return url;
		
	}
	
	/*******************************************************************************************************/
	
	public String getDbURL(String name) {
		return getDbURL(server, name, params);
	}
	
	/*******************************************************************************************************/
	
	public static String getDbURL(String server, String name, String params) {
		
		String ret = server;
		
		if(!ret.endsWith("/")) {
			ret += "/";
		}
		
		ret += name;

		if(params != null && params.length() > 0) {
			ret += "?" + params;
		}
		
		return ret;
		
	}
	
	/*******************************************************************************************************/
	
	protected IDCModelParser getParser() {
		
		if(parser == null) {
			parser = new IDCModelParser(loadSystemKeyValue(SYSMETAMODEL), this);
		}
		
		return parser;
		
	}

	/**********************************************************************************************************************************************************************************************************************/

	protected void generateSystemTable() {

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
	
	protected void publishSystemKey(String key, String fn) {

		String value = IDCUtils.readFile(fn);

		if(value != null) {
			insertSystemKeyValue(key, value);
		} else {
			IDCUtils.error("Error: can't read " + fn);		
		}
		
	}
	
	/**************************************************************************************************/

	protected void insertSystemKeyValue(String key, String value) {

    	try {
    		
			String query = "INSERT INTO " + SYSTAB + " (" + SYSTAB_KEYCOL + ", " + SYSTAB_VALUECOL + ") VALUES ('" + key  + "' , '" + value + "')";
			IDCUtils.debug(query);
			con.executeUpdate(query, false);
			
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
		
	}

	/*******************************************************************************************************/
	
	protected void updateSystemApplication(String fn) {

		String value = IDCUtils.readFile(fn);

		if(value != null) {
			insertSystemKeyValue(MODELADMINMODEL, value);
		} else {
			IDCUtils.info("Error: can't read " + fn);		
		}
		
	}
	
	/**************************************************************************************************/

	protected void updateSystemKeyValue(String key, String value) {

    	try {
    		
    		String query = "UPDATE " + SYSTAB + " SET " + SYSTAB_VALUECOL + " = '" + value + "' WHERE " + SYSTAB_KEYCOL + " = '" + key + "'";
			IDCUtils.debug(query);
    		con.executeUpdate(query, false);
			
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
		
	}

	/**************************************************************************************************/

	protected String loadSystemKeyValue(String key) {

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

	protected static int decodeArgs(String s, String[] functions) {

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
	
	public IDCApplication loadSystemApplication() {
		
		IDCApplication ret =  null;
		
		String systemAppModelXML = loadSystemKeyValue(MODELADMINMODEL);
		if(systemAppModelXML != null) {
			ret = getParser().loadApplication(systemAppModelXML);
			if(ret != null) {
				ret.setDatabaseConnection(con);
				ret.init();
	        	if(!ret.checkIntegrity()) {
	        		ret = null;
	        	}

			}
		}
		
		return ret;
		
	}
	
	/*******************************************************************************************************/
	
	public boolean connect() {

		boolean ret = false;
		
		if(con.connect()) {
			ret = true;;
		}
		
		return ret;
		
	}
	
	/*******************************************************************************************************/
	
	public void disconnect() {
		con.disconnect();		
	}
	
	/**********************************************************************************************************************************************************************************************************************/

	/**********************************************************************************************************************************************************************************************************************/

	protected void test2() {

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
	
	protected List<String>  getCatalogNames() {

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
	
	protected List<String>  getSchemaNames() {

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
	
	protected List<List<String>> getTables(String cat) {

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
	
	protected List<List<String>> getColumns(String cat, String table) {

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

}