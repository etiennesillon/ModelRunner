package com.indirectionsoftware.runtime;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCAdminDbManager;
import com.indirectionsoftware.backend.database.IDCDbManager;
import com.indirectionsoftware.backend.database.IDCSuperAdminDbManager;
import com.indirectionsoftware.backend.database.IDCSuperAdminApplication;
import com.indirectionsoftware.backend.database.IDCAdminApplication;
import com.indirectionsoftware.backend.database.IDCSystemUser;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.runtime.IDCURL;
import com.indirectionsoftware.runtime.nlu.IDCLanguageEngine;
import com.indirectionsoftware.utils.IDCEmail;
import com.indirectionsoftware.utils.IDCUtils;

public abstract class IDCController extends HttpServlet {
	
	public static final int NA = -1;
	
	static final int OUT_HTML=0, OUT_XML=1, OUT_JSON=2;
	
	public static final String ACTION_PARM="action";
	
	static final String SESSIONID="appsessionid";

	/****************************************************************************
     *  init()                                                                  *
     ****************************************************************************/

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		IDCUtils.debug("IDCController.doGet()");
		dispatch(request, response, null);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		IDCUtils.debug("IDCController.doPost()");
		dispatch(request, response, null);
	}

	abstract protected void dispatch(HttpServletRequest request, HttpServletResponse response, String input)  throws ServletException, IOException;
	
	/****************************************************************************
	 *  checkMandatoryFields()                                                  *
	 ****************************************************************************/
	
	protected boolean checkMandatoryFields(HttpServletRequest request, String[] fields) {

		boolean ret = true;
		
		for(String field : fields) {
			
			String value = getParam(request, field);
			if(value == null || value.length() ==0) {
				ret = false;
				break;
			}
			
		}
		
		return ret;
		
	}
      
	/****************************************************************************
	 *  getParam()                                                              *
	 ****************************************************************************/

	public static String getParam(HttpServletRequest request, String param) {
          	
		String ret = "";
		
		Map map = request.getParameterMap();
		
		String[] paramStrs = (String[]) map.get(param);
		
		if(paramStrs != null) {
			try {
			    ret = java.net.URLDecoder.decode(paramStrs[0], StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
			}
		}

		return ret;

	}

	/****************************************************************************
	 *  getBooleanParam()                                                       *
	 ****************************************************************************/

	protected static boolean  getBooleanParam(HttpServletRequest request, String param) {
		
		int value = getIntParam(request, param);
          	   
		return value > 0;
          	   
	}

	/****************************************************************************
	 *  getIntParam()                                                           *
	 ****************************************************************************/

	public static int getIntParam(HttpServletRequest request, String param) throws Error {

		int ret = NA;

		String paramStr = getParam(request, param);
   	   	
		if(paramStr.length() > 0) {
			try {
				ret = Integer.parseInt(paramStr);
			} catch(java.lang.NumberFormatException ex) {
			
			}
		
		}
    	   
		IDCUtils.debug("IDCController.getIntParam():" +  param + " = " + paramStr + " / ret = " + ret);
    	   
		return ret;
    	   
	}
       
	/****************************************************************************
	 *  getLongParam()                                                          *
	 ****************************************************************************/

	protected static long getLongParam(HttpServletRequest request, String param) throws Error {

		long ret = -1;

		String paramStr = getParam(request, param);
   	   	
		if(paramStr.length() > 0) {
			try {
				ret = Long.parseLong(paramStr);
			} catch(java.lang.NumberFormatException ex) {
			
			}
		
		}
    	   
		IDCUtils.debug(param + " = " + paramStr + " / ret = " + ret);
    	   
		return ret;
    	   
	}

	/****************************************************************************
	 *  sendHTML()                                                              *
	 ****************************************************************************/

	protected void sendHTML(PrintWriter out, String text) {
		out.write(text);
        out.close();
	}

	protected void sendText(PrintWriter out, String text) {
		sendHTML(out, "<p>" + text + "</p>");
	}

	protected void sendError(PrintWriter out, String text) {
		sendHTML(out, "<p>Error: " + text + "</p>");
	}

    /****************************************************************************/

	public static IDCDbManager servletInit(ServletConfig conf, boolean isSuperAdmin) {
		
		IDCDbManager ret = null;
		
		IDCUtils.debug("servletInit starting ...");
		
		String dbType = conf.getInitParameter(IDCDbManager.DBTYPE_PROPS);
		String dbDriver = conf.getInitParameter(IDCDbManager.DBDRIVER_PROPS);
		String dbServer = conf.getInitParameter(IDCDbManager.DBSERVER_PROPS);
		String dbName = conf.getInitParameter(IDCDbManager.DBNAME_PROPS);
		String dbParams = conf.getInitParameter(IDCDbManager.DBPARAMS_PROPS);
		
		IDCUtils.debug(">>> IDCWebAppController: dbType = " + dbType);
		IDCUtils.debug(">>> IDCWebAppController: dbDriver = " + dbDriver);
		IDCUtils.debug(">>> IDCWebAppController: dbServer = " + dbServer);
		IDCUtils.debug(">>> IDCWebAppController: dbName = " + dbName);
		IDCUtils.debug(">>> IDCWebAppController: dbParams = " + dbParams);
		
		if(isSuperAdmin) {
			ret = IDCSuperAdminDbManager.getIDCDbSuperAdminManager(dbType, dbServer, dbName, dbParams, dbDriver, null, null);
		} else {
			ret = IDCAdminDbManager.getIDCDbAdminManager(dbType, dbServer, dbName, dbParams, dbDriver, null, null, true);
		}
		    	
		String dbLogLevel = conf.getInitParameter(IDCDbManager.DBLOGLEVEL_PROPS);
		String dbLogMinLevel = conf.getInitParameter(IDCDbManager.DBLOGMINLEVEL_PROPS);
		
		IDCUtils.debug(">>> IDCWebAppController: dbLogLevel = " + dbLogLevel);
		IDCUtils.debug(">>> IDCWebAppController: dbLogMinLevel = " + dbLogMinLevel);

		if(dbLogLevel != null && dbLogLevel.length() > 0) {
			IDCUtils.setDebugLevel(dbLogLevel);
		}

		if(dbLogMinLevel != null && dbLogMinLevel.length() > 0) {
			IDCUtils.setMinDebugLevel(dbLogMinLevel);
		}
		
		return ret;
			
	}
	

}
