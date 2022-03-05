package com.indirectionsoftware.runtime.training;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.backend.database.IDCDatabaseTableBrowser;
import com.indirectionsoftware.metamodel.IDCAction;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCDomainValue;
import com.indirectionsoftware.metamodel.IDCModelData;
import com.indirectionsoftware.metamodel.IDCPackage;
import com.indirectionsoftware.metamodel.IDCPanel;
import com.indirectionsoftware.metamodel.IDCRefTree;
import com.indirectionsoftware.metamodel.IDCReference;
import com.indirectionsoftware.metamodel.IDCReport;
import com.indirectionsoftware.metamodel.IDCReportFolder;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCEnabled;
import com.indirectionsoftware.runtime.IDCError;
import com.indirectionsoftware.runtime.IDCURL;
import com.indirectionsoftware.runtime.webapp.IDCWebAppContext;
import com.indirectionsoftware.runtime.webapp.IDCWebAppController;
import com.indirectionsoftware.utils.IDCItemId;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCTrainingApp {
	
	private IDCApplication app;
	private String serverPath;
	
	static final String HTML_HEADER = "<!DOCTYPE html><html><head><title>Your Training</title><script src=\"training.js\" type=\"text/javascript\"></script><link rel=\"stylesheet\" href=\"training.css\"></head><body>";
	static final String HTML_FOOTER = "</body></html>";
	
	static final String EXPLORER_TITLE = "Data Explorer";

	static final char SEP1 = ',', SEP2 = '.', SEP3 = ':';
	
	/****************************************************************************/

	public IDCTrainingApp(IDCApplication app, String serverPath) {
		
		IDCUtils.traceStart("IDCWeblication() ...");
		
		this.app = app;
		this.serverPath = serverPath;
		
		IDCUtils.traceEnd("IDCWeblication()");
		
	}

	/****************************************************************************/

	public String process(HttpServletRequest request, IDCTrainingContext context, int action) {
		
		IDCUtils.traceStart("IDCWeblication.process() ...");
		
		String ret = "";
		
		if(context != null) {
			
			context.message = "";
			
			ret = getHeader(context.user);

			switch(action) {
			
				case IDCTrainingController.HOME:
					ret += getHomePage(context.user);
					break;
					
				case IDCTrainingController.PROGRAMDETAILS:
					setSelectedData(context, request);
					ret += getProgramDetails(context.user, context.selectedData);
					break;
					
				case IDCTrainingController.WEEKDETAILS:
					break;
					
				case IDCTrainingController.DAYDETAILS:
					break;
					
				case IDCTrainingController.UPDATEACTIVITY:
					IDCData prog = context.selectedData;
					setSelectedData(context, request);
					updateActivity(context.selectedData);
					context.selectedData = prog;
					ret += getProgramDetails(context.user, context.selectedData);
					break;
					

			}
			
			ret += getFooter(context.user);

		} else {
			IDCUtils.debug("Context not found ...");
		}
		
		IDCUtils.traceEnd("IDCWeblication.process()");
		
		return ret;
		
	}
	
	/****************************************************************************/

	private void updateActivity(IDCData act) {

		boolean completed = act.getBoolean("Completed");
		act.set("Completed", !completed);
		act.save();
	
	}

	/****************************************************************************/

	public String getHeader(IDCData user) {
		
		String ret = HTML_HEADER;
		
		ret += "<div class=\"header\">";
		ret += "<div class=\"menu\">";
		
		ret += "<ul>";
		ret += "<li>" + getURLLink("Logout", IDCTrainingController.LOGOFF, -1, -1) + "</li>";
		ret += "</ul>";

		ret += "</div>";
		ret += "</div>";

		ret += "<div id=\"main\" class=\"main\">";

		return ret;
		
	}
	
	/****************************************************************************/

	public String getFooter(IDCData user) {
		
		String ret = "</div>";
		
		ret += "</div><div class=\"menu\">";
		
		ret += "<ul>";
		ret += "<li>" + getURLLink("Home", IDCTrainingController.HOME, -1, -1) + "</li>";
		ret += "<li>" + getURLLink("Logout", IDCTrainingController.LOGOFF, -1, -1) + "</li>";
		ret += "</ul>";

		ret += "</div>";

		ret += HTML_FOOTER;
		
		return ret;
		
	}
	
	/****************************************************************************/

	public String getHomePage(IDCData user) {
		
		String ret = "<h1>" + user.getString("FirstName") + "'s training</h1>";

		List<IDCData> programs = user.getList("Programs");
		
		for(IDCData prog : programs) {
			ret += getProgramSummary(prog);
		}
		
		return ret;
		
	}
	
	/****************************************************************************/

	private String getProgramSummary(IDCData prog) {
		
		String ret = "";
		
		ret += "<p>" + prog.getName() + ": Points = " + prog.evaluate("Points") + " / " + prog.evaluate("Program.Points") + "</p>";
		
		ret += getURLLink("Details", IDCTrainingController.PROGRAMDETAILS, prog.getDataType().getId(), prog.getId());
		 
		return ret;
	}

	/****************************************************************************/

	private String getProgramDetails(IDCData user, IDCData prog) {
		
		String ret = "<h1>" + user.getString("FirstName") + "'s " + prog.getName() + " program</h1>";
		
		List<IDCData> weeks = prog.getList("Weeks");
		for(IDCData week : weeks) {
			ret += getWeekDetails(week);
		}
		
		return ret;
	}

	/****************************************************************************/

	private String getWeekDetails(IDCData week) {
		
		String ret = "<div class = \"week\">";
		
		ret += "<h2>" + week.getName() + "</h2>";
		ret += "<p>" + week.getName() + ": Points = " + week.evaluate("Points") + " / " + week.evaluate("Week.Points") + "</p>";
		
		List<IDCData> days = week.getList("Days");
		for(IDCData day : days) {
			ret += getDayDetails(day);
		}
		
		ret += "</div>";
		
		return ret;
	}

	/****************************************************************************/

	private String getDayDetails(IDCData day) {
		
		String ret = "<div class = \"day\">";
		
		ret += "<h3>" + day.getName() + "</h3>";
		
		ret += "<ul>";
		
		List<IDCData> activities = day.getList("Activities");
		for(IDCData activity : activities) {
			
			ret += "<li>";
			ret += "<div class=\"actleft\"><img src=\"images/" + activity.evaluate("Activity.ImageName") + "\" /><h3>" + activity.getName() + "</h3><p>" + activity.evaluate("Activity.Description") + "</p></div>";
			ret += "<div class=\"actright\">" + getURLImageLink(( activity.getBoolean("Completed") ? "on.png" : "off.png"), IDCTrainingController.UPDATEACTIVITY, activity.getDataType().getId(), activity.getId()) + "</div>";
			ret += "</li>";
			
		}

		ret += "<ul>";
		ret += "</div>";

		return ret;
	}
	
	/****************************************************************************/

	public void executeAction(IDCTrainingContext context, String actionName) {
		
		IDCAction act = context.selectedType.getAction(actionName);
		if(act != null) {
			act.execute(context.selectedData);
		}
		
	}
	
	/****************************************************************************/
	
	public IDCReport getReport(IDCTrainingContext context, HttpServletRequest request) {
		
		IDCReport ret = null;

		String reportName = request.getParameter("reportname");
		if(reportName != null) {
			for(IDCReportFolder folder : app.getReportFolders()) {
				for(IDCReport report : folder.getReports()) {
					if(report.getReportType() == context.selectedType && report.getName().equals(reportName)) {
						report.setDirectoryName(getServerPath());
						ret = report;
						break;
					}
				}
			}
		}

		return ret;

	}

	/**************************************************************************************************/

   	public void print(IDCTrainingContext context, PrintWriter out) {
		
   		String styleSheet;
   		
   		if(context.selectedData == null) {
   			styleSheet = context.selectedType.getDefaultListStylesheet();
   		} else {
   			styleSheet = context.selectedType.getDefaultDetailsStylesheet();
   		}

   		if(styleSheet.length() > 0) {
   	   		
			String printFileName = generatePrintFile(context);
	        
			File xmlFile = new File(printFileName);
	        File xsltFile = new File(styleSheet);
	 
	        Source xmlSource = new StreamSource(xmlFile);
	        Source xsltSource = new StreamSource(xsltFile);
	        Result result = new StreamResult(out);
	 
	        TransformerFactory transFact = TransformerFactory.newInstance(  );
	 
	        Transformer trans;
			try {
				trans = transFact.newTransformer(xsltSource);
		        trans.transform(xmlSource, result);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			xmlFile.delete();
			
   		} else {
   			
   	   		if(context.selectedData == null) {
   	   			app.writeXMLList(out, context.browser.getAllRefs(), true);
   	   		} else {
   	   			context.selectedData.writeXML(out, true);
   	   		}
   		}

	}

	/**************************************************************************************************/

	private String generatePrintFile(IDCTrainingContext context) {

		String ret = "Print" + System.currentTimeMillis() + ".xml";
		
		try {
			File tempFile = new File(getServerPath() + ret);
			PrintWriter tempWriter = new PrintWriter(tempFile);
   	   		if(context.selectedData == null) {
   	   			app.writeXMLList(tempWriter, context.browser.getAllRefs(), true);
   	   		} else {
   	   		context.selectedData.writeXML(tempWriter, true);
   	   		}
			tempWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return ret;
		
	}
	
	/****************************************************************************/

	public void startTransaction(IDCTrainingContext context) {
		context.transId = app.startTransaction();
	}

	/****************************************************************************/

	public void endTransaction(IDCTrainingContext context, boolean isCommit) {
		if(context.transId != -1) {
			app.endTransaction(context.transId, isCommit);
			context.transId = -1;
		}
	}
	
	/************************************************************************************************/

	public void setBrowser(IDCTrainingContext context, List<IDCDataRef> list) {
		context.browser = new IDCDatabaseTableBrowser(context.selectedType, list);
	}

	/****************************************************************************/
	
	public IDCType getType(HttpServletRequest request) {
		
		IDCType ret = null;
		
		int typeId = IDCTrainingController.getIntParam(request, IDCTrainingController.TYPEID_PARM);
		if(typeId != IDCTrainingController.NA) {
			ret = app.getType(typeId);
		}
			
		return ret;

	}

	/****************************************************************************/
	
	public void setSelectedType(IDCTrainingContext context, HttpServletRequest request) {
		
		int typeId = IDCTrainingController.getIntParam(request, IDCTrainingController.TYPEID_PARM);
		if(typeId != IDCTrainingController.NA) {
			IDCType type = app.getType(typeId);
			if(type != null) {
				context.selectedType = app.getType(typeId);
			} else {
				context.message = "Invalid type specified: typeId = " + typeId;
			}
		}
		
	}

	/****************************************************************************/
	
	public void setSelectedData(IDCTrainingContext context, HttpServletRequest request) {
		
		setSelectedType(context, request);
		
		long itemId = IDCUtils.getJSPLongParam(request, IDCTrainingController.ITEMID_PARM);
		if(itemId != IDCTrainingController.NA) {
			IDCData data = context.selectedType.loadDataObject(itemId);
			if(data != null) {
				if(context.selectedData == null || data.getDataType().getEntityId() != context.selectedData.getDataType().getEntityId() || itemId != context.selectedData.getId()) {
					context.selectedData = data; 
				}
			} else {
				context.message = "Invalid data: type = " + context.selectedType.getName() + " /  itemId = " + itemId;
			}
		}
		
	}

	/****************************************************************************/

    private IDCItemId getItemId(String itemStr) {
    	
    	IDCItemId ret = null;
    	
		int indx = itemStr.indexOf(SEP2);
		if(indx != -1) {
			String attrStr = itemStr.substring(0, indx);
			String dataIdStr = itemStr.substring(indx+1, itemStr.length());
			indx = dataIdStr.indexOf(SEP3);
			if(indx != -1) {
				String typeIdStr = dataIdStr.substring(0, indx);
				String itemIdStr = dataIdStr.substring(indx+1, dataIdStr.length());
				int typeId = Integer.parseInt(typeIdStr);
				long itemId = Long.parseLong(itemIdStr);
				ret = new IDCItemId(attrStr, typeId, itemId);
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

	public void setContext(HttpServletRequest request, IDCTrainingContext context) {
		request.getSession().setAttribute(IDCTrainingController.SESSIONID, context);
	}
	
    /****************************************************************************/
    
	static String getURLLink(String label, int action, long typeId, long itemId) {
		return "<a href=\"IDCTrainingController?" + IDCTrainingController.ACTION_PARM + "=" + action + "&" + IDCTrainingController.TYPEID_PARM + "=" + typeId  + "&" + IDCTrainingController.ITEMID_PARM + "=" + itemId + "\">" + label + "</a>";
	}
	
    /****************************************************************************/
    
	static String getURLImageLink(String imageName, int action, long typeId, long itemId) {
		return "<a href=\"IDCTrainingController?" + IDCTrainingController.ACTION_PARM + "=" + action + "&" + IDCTrainingController.TYPEID_PARM + "=" + typeId  + "&" + IDCTrainingController.ITEMID_PARM + "=" + itemId + "\"><img src=\"images/" + imageName + "\" /> </a>";
	}
	
}