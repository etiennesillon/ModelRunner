package com.indirectionsoftware.runtime.webapp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indirectionsoftware.backend.database.IDCDbManager;
import com.indirectionsoftware.backend.database.IDCSystemApplication;
import com.indirectionsoftware.backend.database.IDCSystemUser;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.runtime.IDCURL;
import com.indirectionsoftware.runtime.nlu.IDCLanguageEngine;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCWebAppController extends HttpServlet {
	
	IDCDbManager dbManager; 
	IDCSystemApplication sysApp;
	
	String dbType, dbURL, dbDriver;
	
	//	private Map<String, IDCWebApplication> webApps = new HashMap<String, IDCWebApplication>();

	public static final int NA = -1;
	
	static final int OUT_HTML=0, OUT_XML=1, OUT_JSON=2;
	
	public static final String ACTION_PARM="action";
	static final String USERID_PARM="userid";
	static final String PASSWD_PARM="passwd";
	static final String TENANT_PARM="tenant";
	static final String APPL_PARM="appl";
	public static final String APPID_PARM="appid";
	static final String TYPEID_PARM="typeid";
	static final String ITEMID_PARM="itemid";
	static final String ATTRID_PARM="attrid";
	static final String ACTIONID_PARM="actionid";
	static final String SELECTEDIDS="selectedids";
	static final String FORMAT_PARM="format";
	static final String REPORT_PARM="report";
	static final String EXPAND_PARM="expand";
	static final String CONTENT_PARM="content";
	
	static final String SESSIONID="appsessionid";

	public static final int LOGON=0, LOGOFF=1, 
							GETTYPELIST=2, GETITEMDETAILS=3, 
							UPDATEITEM=4, UPDATEITEMREFRESH=5, UPDATEITEMSAVE=6, UPDATEITEMCANCEL=7,  
							CREATEITEM=8, CREATECHILDITEM=9,
							DELETEITEM=10, DELETESELECTEDITEMS=11, REMOVESELECTEDITEMS=12,  
							SELECTREF=13, SELECTREFOK=14, SELECTREFCANCEL=15,
							SELECTREFLIST=16, SELECTREFLISTOK=17, SELECTREFLISTCANCEL=18, RELOADREFTREE=19, 
							BACK=20, FORWARD=21,
							
							NEXTPAGE=22, PREVPAGE=23, 
							
							SEARCH=24,POSTSEARCH=25, HELP=26, REMOVECONTEXT=27,  
							UPDATENAMESPACE=28, UPDATEDOMAIN=29, EXECUTEACTION=30, 
							CLOSECONTEXT=31, UPDATELIST=32, POSTUPDATELIST=33,
							SETTINGS=34, TODO=35, TOGGLETODO=36,
							
							SORTLIST=37, EDITSETTINGS=38, UPDATESETTINGS=39, UPDATESETTINGSCANCEL=40,
							
							IMPORT=41, EXECUTEACTIONUPLOAD=42,
							
							SHOWGRAPHVIEW=43,
							TYPESEARCH=44,
							REPORTS=45,
							EXPORTLIST=46,
							
							SPEAK=47;
	
	private static final String LEXICON_FILE = "IDCGlobalLexicon.txt";

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
        		response.sendRedirect("error.html");
    		} else if(query == LOGON) {
        		logon(request, out);
    		} else {
    			String appId = getParam(request, APPID_PARM);
        		IDCWebAppContext context =  (IDCWebAppContext) request.getSession().getAttribute(SESSIONID+appId);
        		if(context != null) {
                	if(query == LOGOFF) {
    		        	logoff(request, out, context, appId);
    	        		response.sendRedirect("index.html");
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

		String appName = getParam(request, APPL_PARM);
		String userName = getParam(request, USERID_PARM);
		String password = getParam(request, PASSWD_PARM);

		IDCUtils.debug("IDCWebAppController.logon: appname = " + appName);
		IDCUtils.debug("IDCWebAppController.logon: username = " + userName);
		IDCUtils.debug("IDCWebAppController.logon: password = " + password);

		if(sysApp != null) {
			
			IDCSystemUser user = sysApp.login(appName, userName, password);
			IDCWebApplication webApp = null;
			if(user != null) {
				
				IDCApplication app = user.getApplication();
				app.connect();
				
				File configDir = new File(System.getProperty("catalina.base"), "conf");
				File lexiconFile = new File(configDir, LEXICON_FILE);
				
				app.getOntology().loadLexicon(lexiconFile);
				
				webApp = new IDCWebApplication(app, "");

				IDCLanguageEngine nluEngine = new IDCLanguageEngine(app);
				app.setNLUEngine(nluEngine);

				IDCWebAppContext context = IDCWebAppContext.createFirstContext(webApp, user);
				context.stack.stackElement(new IDCURL("Home", IDCURL.HOME));
				request.getSession().setAttribute(SESSIONID + app.getName(), context);
				sendHTML(out, webApp.getFullPage(context));
				
			} else {
				sendError(out, "Error: could not log in to application " + appName + " with provided credentials.");
			}
			
		} else {
			sendError(out, "No DB Manager to login");
		}
    	
	}
       
	/****************************************************************************
	 *  logoff()                                                                 *
	 ****************************************************************************/

	private void logoff(HttpServletRequest request, PrintWriter out, IDCWebAppContext context, String appId) {
		
		context.disconnect();
		request.getSession().setAttribute(SESSIONID + appId, null);
		
	}

	/****************************************************************************
	 *  getParam()                                                              *
	 ****************************************************************************/

	public static String getParam(HttpServletRequest request, String param) {
          	
		String ret = "";
		
		Map map = request.getParameterMap();
		
		String[] paramStrs = (String[]) map.get(param);
		
		if(paramStrs != null) {
//			ret = paramStrs[0];
			try {
			    ret = java.net.URLDecoder.decode(paramStrs[0], StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
			    // not going to happen - value came from JDK's own StandardCharsets
			}
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
