package com.indirectionsoftware.runtime.webeditor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDbManager;
import com.indirectionsoftware.backend.database.IDCSystemApplication;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCWorkflowInstanceData;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCWebEditor {
	
	private IDCApplication app;
	private String serverPath;
	
	private int editor;
	private final int APPLICATION_EDITOR=0, WORKFLOW_EDITOR=1;

	private IDCType workflowType = null;
	private IDCType applicationType = null;

//	static final String HTML_HEADER = "<head><title>Your Workflows</title><script src=\"utils.js\" type=\"text/javascript\"></script><script src=\"model.js\" type=\"text/javascript\"></script><script src=\"metamodel.js\" type=\"text/javascript\"></script><script src=\"application.js\" type=\"text/javascript\"></script><script src=\"workflow.js\" type=\"text/javascript\"></script><script src=\"editor.js\" type=\"text/javascript\"></script><link rel=\"stylesheet\" href=\"styles.css\"></head><body onload=\"init(";
	static final String HTML_HEADER = "<head><title>Your Models</title><script src=\"utils.js\" type=\"text/javascript\"></script><script src=\"application.js\" type=\"text/javascript\"></script><script src=\"workflow.js\" type=\"text/javascript\"></script><script src=\"editor.js\" type=\"text/javascript\"></script><link rel=\"stylesheet\" href=\"styles.css\"></head><body onload=\"init(";
	static final String HTML_HEADER2 = ");\" oncontextmenu=\"return false;\">";
	static final String HTML_FOOTER = "</body>";
	
	static final String TEST_JSON = "{\"values\":[\"Activate Item\"],\"steps\":[{\"values\":[\"step1\"],\"type\":0,\"x\":179,\"y\":69,\"preReqs\":[{\"values\":[\"cont1\",\"type33\",\"form33\"],\"type\":3}],\"actions\":[{\"values\":[\"Action11\",\"obj11\",\"act11\"],\"type\":1,\"x\":93,\"y\":298},{\"values\":[\"action12\",\"obj12\",\"act12\"],\"type\":1,\"x\":568,\"y\":459}]}],\"contexts\":[{\"values\":[\"cont1\",\"type1\",\"form1\"],\"type\":2,\"x\":755,\"y\":62}],\"preReqs\":[{\"values\":[\"key2\",\"type2\",\"form2\"],\"type\":4,\"x\":788,\"y\":173},{\"values\":[\"ke3\",\"dewdw\",\"wedwed\"],\"type\":4,\"x\":872,\"y\":321}]}";
	
	static final String EXPLORER_TITLE = "Palette Explorer";

	static final char SEP1 = ',', SEP2 = '.', SEP3 = ':';
	
	static List<Object> shapes = new ArrayList<Object>();
	
	IDCDbManager dbManager;
	
	/****************************************************************************/

	public IDCWebEditor(IDCDbManager dbManager, IDCApplication app, String serverPath, int editor) {
		
		IDCUtils.traceStart("IDCWebApplication() ...");
		
		this.dbManager = dbManager;
		this.app = app;
		this.serverPath = serverPath;
		this.editor = editor;
		this.applicationType = this.app.getType(IDCSystemApplication.APPLICATION_TYPE);
		this.workflowType = this.app.getType(IDCWorkflowInstanceData.WORKFLOW_TYPE);
		
		IDCUtils.traceEnd("IDCWebApplication()");
		
	}

	/****************************************************************************/

	public String process(HttpServletRequest request, IDCWebEditContext context, int action, IDCSystemApplication sysApp) {
		
		IDCUtils.traceStart("IDCWebApplication.process() ...");
		
		String ret = "";
		
		String errMsg = "";

		if(context != null) {
			
			context.message = "";

			switch(action) {
			
				case IDCWebEditController.OPEN:
					
					if(editor == APPLICATION_EDITOR) {
						for(IDCData app : sysApp.getAllApplications()) {
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
								ret += data.getString(IDCSystemApplication.APPLICATION_XML);
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
								if(this.dbManager.deployApplicationFomXML(itemData, action == IDCWebEditController.PUBLISH_CLEAN)) {
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

	public String getFullPage(IDCWebEditContext context) {
		
		String ret = HTML_HEADER + editor + HTML_HEADER2;
		
		ret += "<div id=\"editor\">";
		ret += "<div id=\"_content\"><p></p>";
		ret += getCanvasHTML(context);
		ret += "</div>";
		ret += "</div>";
		ret += HTML_FOOTER;
		
		return ret;
		
	}
	
	/****************************************************************************/

	public String getCanvasHTML(IDCWebEditContext context) {
		
		String ret = getButtonsDivHTML(context);
		
		ret += "<div id=\"canvasParent\"  class=\"canvasParent\"><canvas id=\"canvas\" class=\"canvas\">You're browser doesn't support HTML5 Canvas :(</canvas><ul id=\"popuppanel\" class=\"popuppanel\"></ul></div>";
		   
		
		return ret;
		
	}
	
	/****************************************************************************/

	public String getSettingsHTML(IDCWebEditContext context) {
		
		String ret = getButtonsDivHTML(context);
		
		ret += "<div class=\"datapane\"><h1 class=\"pagetitle\">" + getPageTitle(context) + "</h1>";

		if(context.message != null) {
			ret += "<p>" + context.message + "</p>";
		}
		
		ret += "</div>";

		return ret;
		
	}

	/****************************************************************************/

	public String getButtonsDivHTML(IDCWebEditContext context) {	
		
		String ret = "<ul class=\"menubar\">";

		ret += "<li>" + getButton("New", null, "newItem();") + "</li>";
		ret += "<li>" + getButton("Open", null, "openItem();") + "</li>";
		ret += "<li>" + getButton("Deploy", null, "publishItem(false);") + "</li>";
		ret += "<li>" + getButton("Clean Deploy", null, "publishItem(true);") + "</li>";
		ret += "<li>" + getButton("Upload", null, "uploadItem();") + "</li>";
		ret += "<li>" + getButton("Download", null, "downloadItem();") + "</li>";
		ret += "<li>" + getButton("Speak", null, "speak();") + "</li>";
				
		ret += "<ul class=\"menubar-right\">";
		ret += "<li>" + getURLLink(context, "Logout", IDCWebEditController.LOGOFF, IDCWebEditController.NA, IDCWebEditController.NA, IDCWebEditController.NA, "", true, false) + "</li>";
		ret += "</ul>";

		ret += "</ul>";

		return ret;

	}


    /****************************************************************************/
    
	String getURLLink(IDCWebEditContext context, String label, int action, long typeId, long itemId, long actionId, String attrIdStr, boolean isActive, boolean isCloseNav) {
		return "<a href=\"IDCWebEditController?" + IDCWebEditController.ACTION_PARM + "=" + action + "&" + IDCWebEditController.EDITOR_PARM + "=" + editor  + "&" + IDCWebEditController.TYPEID_PARM + "=" + typeId  + "&" + IDCWebEditController.ITEMID_PARM + "=" + itemId  + "&" + IDCWebEditController.ATTRID_PARM + "=" + attrIdStr + "&" + IDCWebEditController.ACTIONID_PARM + "=" + actionId+ "\">" + label + "</a>";
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

	public String getTitle() {
		
		String ret = "Model Data Manager";
		
		if(app != null) {
			ret = app.getName();
		}
		
		return ret;
	
	}

	/****************************************************************************/

	public String getPageTitle(IDCWebEditContext context) {
		
		String ret = "Model Data Manager";
		
		return ret;
	
	}

	/****************************************************************************/

	public List<IDCType> getTypes() {

		List<IDCType> ret = new ArrayList<IDCType>();
		
		for(IDCType type : app.getTypes()) {
			if(type.isTopLevelViewable()) {
				ret.add(type);
			}
		}
		
		return ret;
		
	}

	/****************************************************************************/

	public IDCApplication getApplication() {
		return app;
	}

	/****************************************************************************/

	public String getServerPath() {
		return serverPath;
	}

	/****************************************************************************/

	public void disconnect() {
		app.disconnect();
	}
	
	/****************************************************************************/

	public void setContext(HttpServletRequest request, IDCWebEditContext context) {
		request.getSession().setAttribute(IDCWebEditController.SESSIONID, context);
	}
	
}