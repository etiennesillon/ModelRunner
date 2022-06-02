package com.indirectionsoftware.runtime.webeditor;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.indirectionsoftware.backend.database.IDCAdminApplication;
import com.indirectionsoftware.backend.database.IDCAdminDbManager;
import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCSystemUser;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCWorkflowInstanceData;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCWebEditor {
	
	private IDCSystemUser user = null;
	private IDCApplication app = null;
	private IDCType workflowType = null;
	
	private int editor;
	public static final int APPLICATION_EDITOR=0, WORKFLOW_EDITOR=1;


	static final String HTML_HEADER = "<head><title>Your Models</title><script src=\"utils.js\" type=\"text/javascript\"></script><script src=\"application.js\" type=\"text/javascript\"></script><script src=\"workflow.js\" type=\"text/javascript\"></script><script src=\"editor.js\" type=\"text/javascript\"></script><link rel=\"stylesheet\" href=\"styles.css\"></head><body onload=\"init(";
	static final String HTML_HEADER2 = ");\" oncontextmenu=\"return false;\">";
	static final String HTML_FOOTER = "</body>";
	
	static final String EXPLORER_TITLE = "Palette Explorer";

	static final char SEP1 = ',', SEP2 = '.', SEP3 = ':';
	
	static List<Object> shapes = new ArrayList<Object>();
	
	IDCAdminDbManager dbManager;
	
	/****************************************************************************/

	public IDCWebEditor(IDCAdminDbManager dbManager, IDCSystemUser user, int editor) {
		
		IDCUtils.traceStart("IDCWebApplication() ...");
		
		this.dbManager = dbManager;
		this.editor = editor;
		this.user = user;
		this.app = user.getApplication();
		if(this.app != null) {
			this.workflowType = this.app.getType(IDCWorkflowInstanceData.WORKFLOW_TYPE);
		}
		
		IDCUtils.traceEnd("IDCWebApplication()");
		
	}

	/****************************************************************************/

	public String process(HttpServletRequest request, IDCWebEditContext context, int action, IDCAdminApplication sysApp) {
		
		IDCUtils.traceStart("IDCWebApplication.process() ...");
		
		String ret = "";
		
		String errMsg = "";

		if(context != null) {
			
			context.message = "";

			switch(action) {
			
				case IDCWebEditController.OPEN:
					
					if(editor == APPLICATION_EDITOR) {
						for(IDCData app : sysApp.getUserApplications(user.getUserData())) {
							if(!app.isSystemApp()) {
								ret += "<li>" + getButton(app.getName(),  null,  "getItem(" + app.getId() + ");") + "</li>";
							}
						}
					} else {
						for(IDCData workflow : workflowType.loadAllDataObjects()) {
							ret += "<li>" + getButton(workflow.getName(),  null,  "getItem(" + workflow.getId() + ");") + "</li>";
							
						}
					}
					
					if(ret.length() == 0) {
						ret += "<li>No Application Models found</li>";
					}
					
					break;
					
				case IDCWebEditController.GET:
					
					long itemId = IDCUtils.getJSPLongParam(request, IDCWebEditController.ITEMID_PARM);
					if(itemId != IDCWebEditController.NA) {
						
						if(editor == APPLICATION_EDITOR) {
							IDCData data = sysApp.getApplication(itemId);
							if(data != null) {
								ret += data.getString(IDCAdminApplication.APPLICATION_XML);
							} else {
								context.message = "Invalid data: type = " + context.selectedType.getName() + " /  itemId = " + itemId;
							}
						} else {
							IDCData data = workflowType.loadDataObject(itemId);
							if(data != null) {
								ret += IDCWorkflowInstanceData.getWorkflowJSON(data);
//								ret += TEST_JSON;
							} else {
								context.message = "Invalid data: type = " + context.selectedType.getName() + " /  itemId = " + itemId;
							}
						}
						
					}
					break;

				case IDCWebEditController.PUBLISH:
				case IDCWebEditController.PUBLISH_CLEAN:
					
					try {

						String itemData = (String) request.getParameter(IDCWebEditController.ITEMDATA_PARM);
						if(itemData != null && itemData.length() > 0) {
							itemData = URLDecoder.decode(itemData,"UTF-8");
							if(editor == APPLICATION_EDITOR) {
								if(this.dbManager.deployUserApplicationFomXML(context.user, itemData, action == IDCWebEditController.PUBLISH_CLEAN)) {
									ret = "ok";
								} else {
									ret = "Error deploying Application ...";
								}
							} else {
								IDCData data = null;
								itemId = IDCUtils.getJSPLongParam(request, IDCWebEditController.ITEMID_PARM);
								ret = IDCWorkflowInstanceData.updateWorkflowFromJSON(app, itemId, itemData);
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
					
					break;

				case IDCWebEditController.HELP:
					
					try {

						String itemData = (String) request.getParameter(IDCWebEditController.ITEMDATA_PARM);
						if(itemData != null && itemData.length() > 0) {
							itemData = URLDecoder.decode(itemData,"UTF-8");
							if(editor == APPLICATION_EDITOR) {
								if(this.dbManager.deployUserApplicationFomXML(context.user, itemData, action == IDCWebEditController.PUBLISH_CLEAN)) {
									ret = "ok";
								} else {
									ret = "Error deploying Application ...";
								}
							} else {
								IDCData data = null;
								itemId = IDCUtils.getJSPLongParam(request, IDCWebEditController.ITEMID_PARM);
								ret = IDCWorkflowInstanceData.updateWorkflowFromJSON(app, itemId, itemData);
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
					
					break;

				default:
					
					IDCUtils.debug("Processing default ... no context.action found");
					context.message = "No context.action selected";
					ret += "<p>No context.action selected</p>";
					Enumeration paramNames = request.getParameterNames();
					while(paramNames.hasMoreElements()) {
						String paramName = (String)paramNames.nextElement();
						String[] values = request.getParameterValues(paramName);
						if (values.length == 1) {
				            String paramValue = values[0];
							ret += "<p>Name = " + paramName + " / Value = " + paramValue + "</p>";
				         } else {
				        	ret += "<p>Name = " + paramName + "</p>";
				            ret += "<ul>";
				            for(int i = 0; i < values.length; i++) {
								ret += "<li>" + values[i] + "</li>";
				            }
				            ret += "</ul>";
				         }
				    }
					break;
					
			}

		} else {
			IDCUtils.debug("Context not found ...");
		}
		
		IDCUtils.traceEnd("IDCWebApplication.process()");
		
		return ret;
		
	}
	
	/****************************************************************************/

	public String getFullPage() {
		
		String ret = HTML_HEADER + editor + HTML_HEADER2;
		
		ret += "<div id=\"editor\">";
		ret += "<div id=\"_content\"><p></p>";
		ret += getCanvasHTML();
		ret += "</div>";
		ret += "</div>";
		ret += HTML_FOOTER;
		
		return ret;
		
	}
	
	/****************************************************************************/

	public String getCanvasHTML() {
		
		String ret = getButtonsDivHTML();
		
		ret += "<div id=\"canvasParent\"  class=\"canvasParent\"><canvas id=\"canvas\" class=\"canvas\">You're browser doesn't support HTML5 Canvas :(</canvas><ul id=\"popuppanel\" class=\"popuppanel\"></ul></div>";
		   
		
		return ret;
		
	}
	
	/****************************************************************************/

	public String getButtonsDivHTML() {	
		
		String ret = "<ul class=\"menubar\">";

		ret += "<li>" + getButton("New", null, "newItem();") + "</li>";
		ret += "<li>" + getButton("Open", null, "openItem();") + "</li>";
		ret += "<li>" + getButton("Deploy", null, "publishItem(false);") + "</li>";
		ret += "<li>" + getButton("Clean Deploy", null, "publishItem(true);") + "</li>";
		ret += "<li>" + getButton("Upload", null, "uploadItem();") + "</li>";
		ret += "<li>" + getButton("Download", null, "downloadItem();") + "</li>";
		ret += "<li>" + getButton("Speak", null, "speak();") + "</li>";
				
		ret += "<ul class=\"menubar-right\">";
		ret += "<li>" + getButton("Help", null, "help();") + "</li>";
		ret += "<li>" + getURLLink("Logout", IDCWebEditController.LOGOFF, IDCWebEditController.NA, IDCWebEditController.NA, IDCWebEditController.NA, "", true, false) + "</li>";
		ret += "</ul>";

		ret += "</ul>";

		return ret;

	}


    /****************************************************************************/
    
	String getURLLink(String label, int action, long typeId, long itemId, long actionId, String attrIdStr, boolean isActive, boolean isCloseNav) {
		return "<a href=\"ModelEditor?" + IDCWebEditController.ACTION_PARM + "=" + action + "&" + IDCWebEditController.EDITOR_PARM + "=" + editor  + "&" + IDCWebEditController.TYPEID_PARM + "=" + typeId  + "&" + IDCWebEditController.ITEMID_PARM + "=" + itemId  + "&" + IDCWebEditController.ATTRID_PARM + "=" + attrIdStr + "&" + IDCWebEditController.ACTIONID_PARM + "=" + actionId+ "\">" + label + "</a>";
	}
		
    /****************************************************************************/
	 
	static String getButton(String label, String className, String func) {
		return "<button" + (className == null ? "" : " class=\"" + className + "\"") + " onclick=\"event.preventDefault(); " + func + "\">" + label + "</button>";
	}

    /****************************************************************************/
	 
	static String getClickUpdateFunction(int action, long typeId, long itemId, boolean isActive) throws Error {
		return "event.preventDefault(); reloadPost('IDCWebAppController'," + action + "," + typeId + "," + itemId + "," + IDCWebEditController.NA + "); return false;";
	}

    /************************************************************************************************/

    public String getHelp() {

    	String ret = "<P>This is your help page ...</P>";
    	
    	return ret;
    	
    }
    
	/****************************************************************************/

	public void disconnect() {
		if(app != null) {
			app.disconnect();
		}
	}
	
	/****************************************************************************/

	public void setContext(HttpServletRequest request, IDCWebEditContext context) {
		request.getSession().setAttribute(IDCWebEditController.SESSIONID, context);
	}
	
}