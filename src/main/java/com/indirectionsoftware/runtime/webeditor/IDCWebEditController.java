package com.indirectionsoftware.runtime.webeditor;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indirectionsoftware.backend.database.IDCAdminApplication;
import com.indirectionsoftware.backend.database.IDCAdminDbManager;
import com.indirectionsoftware.backend.database.IDCSystemUser;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.runtime.IDCController;
import com.indirectionsoftware.runtime.IDCURL;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCWebEditController extends IDCController {
	
	IDCAdminDbManager dbManager; 
	IDCAdminApplication adminApp;
	
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
							SETTINGS=5, PUBLISH_CLEAN=6, HELP=7;
	
	private static final String APP_EDITOR = "0";
	
	String editor;

	/****************************************************************************
     *  init()                                                                  *
     ****************************************************************************/

	public void init(ServletConfig conf) {
		
		IDCUtils.debug("IDCWebEditController starting ...");
		
		dbManager = (IDCAdminDbManager) servletInit(conf, false);
		
		adminApp = dbManager.getAdminApplication();
		
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
		
		IDCUtils.debug("IDCWebEditController Request received: input=" + input);
		
    	PrintWriter out = response.getWriter();
		    
        try {
        	
        	int query = getIntParam(request, ACTION_PARM);
    		IDCUtils.debug("IDCWebEditController.dispatch: query = " + query);
        	if(query == NA) {
        		response.sendRedirect("error.html");
    		} else {
    			
            	editor = getParam(request, EDITOR_PARM);
        		IDCUtils.debug("IDCWebEditController.dispatch: editor = " + editor);
            	
            	if(query == LOGON) {
            		logon(request, out, editor);
            	} else {
            		IDCWebEditContext context =  (IDCWebEditContext) request.getSession().getAttribute(SESSIONID+editor);
            		if(context != null) {
                    	if(query == LOGOFF) {
        		        	logoff(request, out, context);
        		    		if(editor.equals(APP_EDITOR)) {
            	        		response.sendRedirect("appeditor.html");
        		    		} else {
            	        		response.sendRedirect("workfloweditor.html");
        		    		}

                		} else {
                        	sendHTML(out, context.process(request, query, adminApp));
                		}
            		} else {
                    	sendText(out, "Please login first !");
            		}
            	}

        	}

    		IDCUtils.debug("IDCWebEditController All done ...");

        } catch(java.lang.NumberFormatException ex) {
        } catch(Error er) {
        	sendError(out, er.getMessage());
        }

	}

	/****************************************************************************
	 *  logon()                                                                 
	 * @param editor *
	 ****************************************************************************/

	private void logon(HttpServletRequest request, PrintWriter out, String editor) {

		String userName = getParam(request, USERID_PARM);
		String password = getParam(request, PASSWD_PARM);

		IDCUtils.debug("IDCWebEditController.logon: username = " + userName);
		IDCUtils.debug("IDCWebEditController.logon: password = " + password);

		IDCSystemUser user = null;
		IDCApplication app = null;

		if(editor.equals(APP_EDITOR)) {
			
			user = adminApp.userLogin(userName, password);
			
		} else {
			
			String appName = getParam(request, APPL_PARM);
			IDCUtils.debug("IDCWebEditController.logon: appname = " + appName);
			
			user = adminApp.applicationLogin(appName, userName, password);
			
			app = user.getApplication();

		}

		if(user != null) {
			
			if(app != null) {
				app.connect();
			}
			
			IDCWebEditor webApp = new IDCWebEditor(dbManager, user, IDCWebEditor.APPLICATION_EDITOR);

			IDCWebEditContext context = IDCWebEditContext.createFirstContext(webApp, user);
			context.stack.stackElement(new IDCURL("Home", IDCURL.HOME));
			request.getSession().setAttribute(SESSIONID + editor, context);
			sendHTML(out, webApp.getFullPage());

		} else {
			sendError(out, "Error: could not log in to Model Editor application with provided credentials.");
		}
		
	}
       
	/****************************************************************************
	 *  logoff()                                                                 *
	 ****************************************************************************/

	private void logoff(HttpServletRequest request, PrintWriter out, IDCWebEditContext context) {
		
		context.disconnect();
		request.getSession().setAttribute(SESSIONID, null);
		
	}

}
