package com.indirectionsoftware.runtime.webeditor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDbManager;
import com.indirectionsoftware.backend.database.IDCSystemApplication;
import com.indirectionsoftware.backend.database.IDCSystemUser;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.runtime.IDCURL;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCWebEditController extends HttpServlet {
	
	IDCDbManager dbManager; 
	IDCSystemApplication sysApp;
	
	String dbType, dbURL, dbDriver;
	
//	private Map<String, IDCWebApplication> webApps = new HashMap<String, IDCWebApplication>();

	public static final int NA = -1;
	
	static final int OUT_HTML=0, OUT_XML=1, OUT_JSON=2;
	
	public static final String ACTION_PARM="action";
	static final String EDITOR_PARM="editor";
	static final String USERID_PARM="userid";
	static final String PASSWD_PARM="passwd";
	static final String TENANT_PARM="tenant";
	static final String APPL_PARM="appl";
	static final String TYPEID_PARM="typeid";
	static final String ITEMID_PARM="itemid";
	static final String ITEMDATA_PARM="itemdata";
	static final String ATTRID_PARM="attrid";
	static final String ACTIONID_PARM="actionid";
	static final String SELECTEDIDS="selectedids";
	static final String FORMAT_PARM="format";
	static final String REPORT_PARM="report";
	static final String EXPAND_PARM="expand";
	
	static final String SESSIONID="editsessionid";

	public static final int LOGON=0, LOGOFF=1, 
							OPEN=2, PUBLISH=3, GET=4,
							SETTINGS=5, PUBLISH_CLEAN=6;
	
	private static final Object APP_EDITOR = "0";
	
	String editor;

	/****************************************************************************
     *  init()                                                                  *
     ****************************************************************************/

	public void init(ServletConfig conf) {
		
		IDCUtils.debug("IDCWebAppController starting ...");
		
		dbType = conf.getInitParameter("dbType");
		dbURL = conf.getInitParameter("dbURL");
		dbDriver = conf.getInitParameter("dbDriver");

		IDCUtils.debug("IDCWebAppController: dbType = " + dbType);
		IDCUtils.debug("IDCWebAppController: dbURL = " + dbURL);
		IDCUtils.debug("IDCWebAppController: dbDriver = " + dbDriver);
		
		dbManager = IDCDbManager.getIDCDbManager(dbType, dbURL, dbDriver);
		sysApp = dbManager.getSystemApplication();
		
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		IDCUtils.debug("doGet()");
		dispatch(request, response, null);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		IDCUtils.debug("doPost()");
		dispatch(request, response, null);
	}

	/****************************************************************************
     *  dispatch()                                                              *
     ****************************************************************************/

	public void dispatch(HttpServletRequest request, HttpServletResponse response, String input) throws ServletException, IOException {
		
		IDCUtils.debug("IDCWebAppController Request received: input=" + input);
		
    	PrintWriter out = response.getWriter();
		    
        try {
        	
        	int query = getIntParam(request, ACTION_PARM);
        	if(query == NA) {
        		response.sendRedirect("error.html");
    		} else if(query == LOGON) {
        		logon(request, out);
    		} else {
            	editor = getParam(request, EDITOR_PARM);
        		IDCWebEditContext context =  (IDCWebEditContext) request.getSession().getAttribute(SESSIONID+editor);
        		if(context != null) {
                	if(query == LOGOFF) {
    		        	logoff(request, out, context);
    		    		if(editor.equals(APP_EDITOR)) {
        	        		response.sendRedirect("modeleditor.html");
    		    		} else {
        	        		response.sendRedirect("workfloweditor.html");
    		    		}

            		} else {
                    	sendHTML(out, context.process(request, query, sysApp));
            		}
        		} else {
                	sendText(out, "Please login first !");
        		}

        	}

    		IDCUtils.debug("IDCWebAppController All done ...");

        } catch(java.lang.NumberFormatException ex) {
        } catch(Error er) {
        	sendError(out, er.getMessage());
        }

	}

	/****************************************************************************
	 *  logon()                                                                 *
	 ****************************************************************************/

	private void logon(HttpServletRequest request, PrintWriter out) {

		String userName = getParam(request, USERID_PARM);
		String password = getParam(request, PASSWD_PARM);

		editor = getParam(request, EDITOR_PARM);
		
		String tenantName = null;
		String appName = null;
		
		if(editor.equals(APP_EDITOR)) {
			appName = IDCSystemApplication.ADMIN_APPL;
		} else {
			appName = getParam(request, APPL_PARM);
		}

		IDCUtils.debug("IDCWebAppController.logon: appname = " + appName);
		IDCUtils.debug("IDCWebAppController.logon: username = " + userName);
		IDCUtils.debug("IDCWebAppController.logon: password = " + password);
		IDCUtils.debug("IDCWebAppController.logon: editor = " + editor);

		if(sysApp != null) {
			
			IDCWebEditor webApp = null;
			IDCSystemUser user = sysApp.login(appName, userName, password);
			if(user != null) {
				IDCApplication app = user.getApplication();
				app.connect();
				webApp = new IDCWebEditor(dbManager, app, "", (editor.equals(APP_EDITOR) ? 0 : 1));
				IDCWebEditContext context = IDCWebEditContext.createFirstContext(webApp, user);
				context.stack.stackElement(new IDCURL("Home", IDCURL.HOME));
				request.getSession().setAttribute(SESSIONID + editor, context);
				sendHTML(out, webApp.getFullPage(context));
			} else {
				sendError(out, "Error: could not log in to Model Editor application with provided credentials.");
			}
			
		} else {
			sendError(out, "No DB Manager to login");
		}
    	
	}
       
	/****************************************************************************
	 *  logoff()                                                                 *
	 ****************************************************************************/

	private void logoff(HttpServletRequest request, PrintWriter out, IDCWebEditContext context) {
		
		context.disconnect();
		request.getSession().setAttribute(SESSIONID, null);
		
	}

   	/****************************************************************************/
	 
	static String getActionString(int action, long typeId, long itemId, int attrId) throws Error {
		
		String ret = "IDCWebAppController?" + ACTION_PARM + "=" + action;
		
		if(typeId != NA) {
			ret += "&" + TYPEID_PARM + "=" + typeId;
		}
		
		if(itemId != NA) {
			ret += "&" + ITEMID_PARM + "=" + itemId;
		}
		
		if(attrId != NA) {
			ret += "&" + ATTRID_PARM + "=" + attrId;
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
			ret = paramStrs[0];
		}

		return ret;

	}

	/****************************************************************************
	 *  getBooleanParam()                                                       *
	 ****************************************************************************/

	public static boolean  getBooleanParam(HttpServletRequest request, String param) {
		
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
    	   
		IDCUtils.debug(param + " = " + paramStr + " / ret = " + ret);
    	   
		return ret;
    	   
	}
       
	/****************************************************************************
	 *  getLongParam()                                                          *
	 ****************************************************************************/

	static long getLongParam(HttpServletRequest request, String param) throws Error {

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

	private void sendHTML(PrintWriter out, String text) {
		out.write(text);
        out.close();
	}

	private void sendText(PrintWriter out, String text) {
		sendHTML(out, "<p>" + text + "</p>");
	}

	private void sendError(PrintWriter out, String text) {
		sendHTML(out, "<ERROR>" + text + "</ERROR>");
	}

}
