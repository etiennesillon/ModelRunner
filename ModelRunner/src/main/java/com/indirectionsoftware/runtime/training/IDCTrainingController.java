package com.indirectionsoftware.runtime.training;

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
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCURL;
import com.indirectionsoftware.runtime.webapp.IDCWebAppContext;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCTrainingController extends HttpServlet {
	
	IDCDbManager dbManager; 
	IDCSystemApplication sysApp;
	
	String dbType, dbURL, dbDriver;
	
//	private Map<String, IDCWebApplication> webApps = new HashMap<String, IDCWebApplication>();

	static final int NA = -1;
	
	static final int OUT_HTML=0, OUT_XML=1, OUT_JSON=2;
	
	public static final String ACTION_PARM="action";
	static final String USERID_PARM="userid";
	static final String PASSWD_PARM="passwd";
	static final String TENANT_PARM="tenant";
	static final String APPL_PARM="appl";
	static final String TYPEID_PARM="typeid";
	static final String ITEMID_PARM="itemid";
	static final String ATTRID_PARM="attrid";
	static final String ACTIONID_PARM="actionid";
	static final String SELECTEDIDS="selectedids";
	static final String FORMAT_PARM="format";
	static final String REPORT_PARM="report";
	static final String EXPAND_PARM="expand";
	
	static final String SESSIONID="appsessionid";

	public static final int LOGON=0, LOGOFF=1, 
							HOME=2, PROGRAMDETAILS=3,WEEKDETAILS=4, DAYDETAILS=5, UPDATEACTIVITY=6;

	/****************************************************************************
     *  init()                                                                  *
     ****************************************************************************/

	public void init(ServletConfig conf) {
		
		IDCUtils.debug("IDCWebAppController starting ...");
		
		dbType = conf.getInitParameter("dbType");
		dbURL = conf.getInitParameter("dbURL");
		dbDriver = conf.getInitParameter("dbDriver");

		String dbLogName = conf.getInitParameter("dbLogName");
		String debugLevel = conf.getInitParameter("debugLevel");
		String minDebugLevel = conf.getInitParameter("minDebugLevel");
		if(dbLogName != null && dbLogName.length() > 0) {
			IDCUtils.startDbLog(dbLogName);
		}
		if(debugLevel != null && debugLevel.length() > 0) {
			IDCUtils.setDebugLevel(debugLevel);
		}
		if(minDebugLevel != null && minDebugLevel.length() > 0) {
			IDCUtils.setMinDebugLevel(minDebugLevel);
		}
		

		System.out.println(">>> IDCWebAppController: dbType = " + dbType);
		System.out.println(">>> IDCWebAppController: dbURL = " + dbURL);
		System.out.println(">>> IDCWebAppController: dbDriver = " + dbDriver);
		System.out.println(">>> IDCWebAppController: dbLogName = " + dbLogName);
		System.out.println(">>> IDCWebAppController: debugLevel = " + debugLevel);
		System.out.println(">>> IDCWebAppController: minDebugLevel = " + minDebugLevel);
		
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
        		response.sendRedirect("index.html");
    		} else if(query == LOGON) {
        		logon(request, out);
    		} else {
        		IDCTrainingContext context =  (IDCTrainingContext) request.getSession().getAttribute(SESSIONID);
        		if(context != null) {
                	if(query == LOGOFF) {
    		        	logoff(request, out, context);
    	        		response.sendRedirect("training.html");
            		} else {
                    	sendHTML(out, context.process(request, query));
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

		String appName = "Training";
		String userName = getParam(request, USERID_PARM);
		String password = getParam(request, PASSWD_PARM);

		IDCUtils.debug("IDCWebAppController.logon: username = " + userName);
		IDCUtils.debug("IDCWebAppController.logon: password = " + password);

		if(sysApp != null) {
			
			IDCSystemUser admUser = sysApp.login(appName, IDCSystemApplication.DEFAULT_USER, IDCSystemApplication.DEFAULT_PWD);
			IDCTrainingApp trainingApp = null;
			if(admUser != null) {
				IDCApplication app = admUser.getApplication();
				app.connect();
				IDCData user = logon(app, userName, password);
				if(user != null) {
					trainingApp = new IDCTrainingApp(app, "");
					IDCTrainingContext context = IDCTrainingContext.createFirstContext(trainingApp, user);
					context.stack.stackElement(new IDCURL("Home", IDCURL.HOME));
					request.getSession().setAttribute(SESSIONID, context);
					sendHTML(out, context.process(request, HOME));
				} else {
					sendError(out, "Error: could not log in to application " + appName + " with provided credentials.");
				}
			} else {
				sendError(out, "Error: could not log in to application " + appName);
			}
			
		} else {
			sendError(out, "No DB Manager to login");
		}
    	
	}
       
	/****************************************************************************/

	private IDCData logon(IDCApplication app, String userName, String password) {
		
		IDCData ret = null;
		
		IDCType userType = app.getType("User");
		if(userType != null) {
			ret = userType.requestSingleData("(UserName == '" + userName + "') AND (Password == '" + password + "')");
		}
		return ret;
		
	}

	/****************************************************************************
	 *  logoff()                                                                 *
	 ****************************************************************************/

	private void logoff(HttpServletRequest request, PrintWriter out, IDCTrainingContext context) {
		
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
