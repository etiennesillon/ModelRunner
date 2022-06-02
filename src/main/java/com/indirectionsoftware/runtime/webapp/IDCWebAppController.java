package com.indirectionsoftware.runtime.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.indirectionsoftware.backend.database.IDCAdminApplication;
import com.indirectionsoftware.backend.database.IDCAdminDbManager;
import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCSuperAdminApplication;
import com.indirectionsoftware.backend.database.IDCSystemUser;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.runtime.IDCController;
import com.indirectionsoftware.runtime.IDCURL;
import com.indirectionsoftware.runtime.nlu.IDCLanguageEngine;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCWebAppController extends IDCController {
	
	IDCAdminDbManager dbManager; 
	IDCAdminApplication adminApp;
	
	//	private Map<String, IDCWebApplication> webApps = new HashMap<String, IDCWebApplication>();

	public static final int NA = -1;
	
	static final int OUT_HTML=0, OUT_XML=1, OUT_JSON=2;
	
	public static final String ACTION_PARM="action";
	public static final String USERID_PARM="userid";
	public static final String PASSWD_PARM="passwd";
	public static final String PASSWD2_PARM="passwd2";
	static final String ROLE_PARM="role";
	static final String TENANT_PARM="tenant";
	static final String APPL_PARM="appl";
	static final String EMAIL_PARM="email";
	static final String ORG_PARM="org";
	static final String TOKEN_PARM="token";
	public static final String CONTEXTID_PARM="contextid";
	static final String TYPEID_PARM="typeid";
	static final String ITEMID_PARM="itemid";
	static final String ATTRID_PARM="attrid";
	static final String ACTIONID_PARM="actionid";
	static final String SELECTEDIDS="selectedids";
	static final String FORMAT_PARM="format";
	static final String REPORT_PARM="report";
	static final String EXPAND_PARM="expand";
	static final String CONTENT_PARM="content";
	public static final String ACCOUNT_PARM="account";
	
	static final String SESSIONID="appsessionid";
	
	static final String[] CREATE_USER_FIELDS = {EMAIL_PARM, PASSWD_PARM, PASSWD2_PARM};
	static final String[] CREATE_ACCOUNT_FIELDS = {ACCOUNT_PARM, USERID_PARM, PASSWD_PARM, EMAIL_PARM};
	
	public static final int LOGON=0, LOGOFF=1, 
							GETTYPELIST=2, GETITEMDETAILS=3, 
							UPDATEITEM=4, UPDATEITEMREFRESH=5, UPDATEITEMSAVE=6, UPDATEITEMCANCEL=7,  
							CREATEITEM=8, CREATECHILDITEM=9,
							DELETEITEM=10, DELETESELECTEDITEMS=11, REMOVESELECTEDITEMS=12,  
							SELECTREF=13, SELECTREFOK=14, SELECTREFCANCEL=15,
							SELECTREFLIST=16, SELECTREFLISTOK=17, SELECTREFLISTCANCEL=18, RELOADREFTREE=19, 
							BACK=20, FORWARD=21,
							
							NEXTPAGE=22, PREVPAGE=23, 
							
							SEARCH=24, // 24 is hardcoded in sendReloadPost in utils.js to determine which form to process (serch or update item)
							
							POSTSEARCH=25, HELP=26, REMOVECONTEXT=27,  
							UPDATENAMESPACE=28, UPDATEDOMAIN=29, EXECUTEACTION=30, 
							CLOSECONTEXT=31, UPDATELIST=32, POSTUPDATELIST=33,
							SETTINGS=34, TODO=35, TOGGLETODO=36,
							
							SORTLIST=37, EDITSETTINGS=38, UPDATESETTINGS=39, UPDATESETTINGSCANCEL=40,
							
							IMPORT=41, EXECUTEACTIONUPLOAD=42,
							
							SHOWGRAPHVIEW=43,
							TYPESEARCH=44,
							REPORTS=45,
							EXPORTLIST=46,
							
							SPEAK=47,
							
							CREATE_ACCOUNT=48;
	
	private static final String LEXICON_FILE = "IDCGlobalLexicon.txt";
	
	/****************************************************************************
     *  init()                                                                  *
     ****************************************************************************/

	public void init(ServletConfig conf) {
		
		IDCUtils.debug("IDCWebAppController starting ...");
		
		dbManager = (IDCAdminDbManager) servletInit(conf, false);
		
		adminApp = dbManager.getAdminApplication();
		
	}
	
	/****************************************************************************
     *  destroy()                                                               *
     ****************************************************************************/

	public void destroy() { 
		dbManager.disconnect(); 
	}
	
	/****************************************************************************
     *  dispatch()                                                              *
     ****************************************************************************/

	public void dispatch(HttpServletRequest request, HttpServletResponse response, String input) throws ServletException, IOException {
		
		IDCUtils.debug("IDCWebAppController Request received: input=" + input);
		
		PrintWriter out = response.getWriter();
		    
        try {
        	
    		debugSession(request);

    		int query = getIntParam(request, ACTION_PARM);
        	if(query == NA) {
        		response.sendRedirect("error.html");
    		} else if(query == LOGON) {
        		login(request, out);
    		} else if(query == CREATE_ACCOUNT) {
        		createUser(request, out);
    		} else {
    			String contextId = getParam(request, CONTEXTID_PARM);
        		IDCWebAppContext context =  (IDCWebAppContext) request.getSession().getAttribute(SESSIONID+contextId);
        		if(context != null) {
                	if(query == LOGOFF) {
    		        	logoff(request, out, context, contextId);
    	        		response.sendRedirect("app.html");
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
	 *  createUser()                                                         *
	 ****************************************************************************/

	private void createUser(HttpServletRequest request, PrintWriter out) {

		String email = getParam(request, EMAIL_PARM);
		String password = getParam(request, PASSWD_PARM);
		String password2 = getParam(request, PASSWD2_PARM);

		IDCUtils.debug("IDCWebAppController.createUser: email = " + email);
		IDCUtils.debug("IDCWebAppController.createUser: password = " + password);
		IDCUtils.debug("IDCWebAppController.createUser: password2 = " + password2);

		if(checkMandatoryFields(request, CREATE_USER_FIELDS)) {
			
			if(password.equals(password2)) {
				
				if(adminApp != null) {
					IDCData user = adminApp.getUser(email);
					if(user != null) {
						sendHTML(out, IDCSuperAdminApplication.getNewAccountForm(email, password, password2, "<B>Sorry, this user name is already taken, please chose another one.</B>"));
					} else {
						user = adminApp.addUser(email, password);
						if(user != null) {
							sendHTML(out, getNewAccountPage());
						} else {
							sendHTML(out, IDCSuperAdminApplication.getNewAccountForm(email, password, password2, "<B>oops, there was a problem creating your account. Please try again later?</B>"));
						}
					}
				} else {
					sendHTML(out, IDCSuperAdminApplication.getNewAccountForm(email, password, password2, "<B>oops, there was a problem creating your account. Please try again later?</B>"));
				}

			} else {
				sendHTML(out, IDCSuperAdminApplication.getNewAccountForm(email, password, password2, "<B>Sorry, passwords don't match.</B>"));
			}
			
			
		} else {
			sendHTML(out, IDCSuperAdminApplication.getNewAccountForm(email, password, password2, "<B>Sorry, Missing mandatory fields.</B>"));
		}
		
	}

	/****************************************************************************
	 *  createAccount()                                                         *
	 ****************************************************************************/

	private void createAccount(HttpServletRequest request, PrintWriter out) {

		String accountName = getParam(request, ACCOUNT_PARM);
		String userName = getParam(request, USERID_PARM);
		String password = getParam(request, PASSWD_PARM);
		String email = getParam(request, EMAIL_PARM);

		IDCUtils.debug("IDCWebAppController.createAccount: accountName = " + accountName);
		IDCUtils.debug("IDCWebAppController.createAccount: userName = " + userName);
		IDCUtils.debug("IDCWebAppController.createAccount: password = " + password);
		IDCUtils.debug("IDCWebAppController.createAccount: email = " + email);

		if(checkMandatoryFields(request, CREATE_ACCOUNT_FIELDS)) {
			
			if(adminApp != null) {
				IDCData account = adminApp.createAccount(accountName, userName, password, email);
				if(account != null) {
					sendHTML(out, "<h1>Thank you!</h1><p>Your account has been created.</p>");
				} else {
					sendError(out, "oops, there was a problem creating your account. Please try again later?");
				}
			} else {
				sendError(out, "oops, there was a problem creating your account. Please try again later?");
			}
			
		} else {
			sendError(out, "Missing mandatory fields");
		}
	}

	/****************************************************************************
	 *  getNewAccountPage()                                                     *
	 ****************************************************************************/

	private String getNewAccountPage() {
		
		String ret = IDCSuperAdminApplication.HTML + "<h1 id=\"text\">Thank you!</h1><p id=\"text\">Your account has been created.</p><p id=\"text\">You can start modeling your data <a href=\"appeditor.html\">here</a>.</p><p id=\"text\">After you have deployed your model, you can login to your application <a href=\"app.html\">here</a>.</p><p id=\"text\"></p><br>";
		
		return ret;
		
	}

	/****************************************************************************
	 *  logon()                                                                 *
	 ****************************************************************************/

	private void login(HttpServletRequest request, PrintWriter out) {

		String appName = getParam(request, APPL_PARM);
		String userName = getParam(request, USERID_PARM);
		String password = getParam(request, PASSWD_PARM);
		int role = getIntParam(request, ROLE_PARM);

		IDCUtils.debug("IDCWebAppController.logon: appname = " + appName);
		IDCUtils.debug("IDCWebAppController.logon: username = " + userName);
		IDCUtils.debug("IDCWebAppController.logon: password = " + password);
		IDCUtils.debug("IDCWebAppController.logon: role = " + role);

		IDCWebApplication webApp = null;

		if(adminApp != null) {
			
			IDCSystemUser user = adminApp.applicationLogin(appName, userName, password);
			if(user != null) {
				
				IDCApplication app = user.getApplication();
				app.connect();
				
				webApp = new IDCWebApplication(app, "");

				IDCLanguageEngine nluEngine = new IDCLanguageEngine(app);
				app.setNLUEngine(nluEngine);

				IDCWebAppContext context = IDCWebAppContext.createFirstContext(webApp, user);
				context.stack.stackElement(new IDCURL("Home", IDCURL.HOME));
				webApp.setContext(request, context);
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

	private void logoff(HttpServletRequest request, PrintWriter out, IDCWebAppContext context, String contextId) {
		
		context.disconnect();
		request.getSession().setAttribute(SESSIONID + contextId, null);
		
	}

	/****************************************************************************/

	public static void debugSession(HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		Enumeration<String> attrs = session.getAttributeNames();
		
		String name = null;
		
		try {
			while((name = attrs.nextElement()) != null) {
				IDCUtils.debug("Session attr name = " + name);
			}
		} catch(Exception ex) {
		}
		
	}
		
}
