package com.indirectionsoftware.runtime.nlu;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCEnabled;
import com.indirectionsoftware.runtime.webapp.IDCWebAppContext;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCResults implements IDCEnabled {
	
	String html = "";
	String sentence;
	String query;
	String errorMsg;
	String log = "";
	IDCWebAppContext context;
	IDCData data;
	
	/*****************************************************************************/

	public IDCResults(IDCWebAppContext context, String sentence) {
		this.context = context;
		this.sentence = sentence;
	}

	/*****************************************************************************/

	public void setQuery(String query) {
		this.query = query;
	}

	/*****************************************************************************/

	public void setErrorMessage(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	/*****************************************************************************/

	public void setResultsHTML(String content) {

		html = context.webApp.getButtonsDivHTML(context);
		
		html += "<div id=\"_datapane\" class=\"datapane\"><h1 class=\"pagetitle\">NLU Query: " + sentence + "</h1>";

		html += content;
		
	}
	
	/*****************************************************************************/

	public String getFullHTML() {
		return html += "<h1 class=\"pagetitle\">___________________________________________________________________________</h1>" + "<p> </p><h1 class=\"pagetitle\">NLU Logs:</h1>" + log;

	}
	
	/*****************************************************************************/

	public void debugNLU(String s){
		
		log += "<p>" + s + "</p>";
		IDCUtils.debugNLU(s);
		
	}
	
	/*****************************************************************************/

	@Override
	public String getURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void exportXML(String fn, boolean isExpanded) {
	}

	@Override
	public void exportXML(File file, boolean isExpanded) {
	}

	@Override
	public String getXMLString(boolean isExpanded) {
		return "";
	}

	@Override
	public void writeXML(PrintWriter out, boolean isExpanded, Map<IDCType, Map<Long, Object>> refMap) {
	}

	@Override
	public void exportJSON(String fn, boolean isExpanded) {
	}

	@Override
	public void exportJSON(File file, boolean isExpanded) {
	}

	@Override
	public String getJSONString(boolean isExpanded, boolean isFirstChild) {
		return "";
	}

	@Override
	public void writeJSON(PrintWriter out, boolean isExpanded, Map<IDCType, Map<Long, Object>> refMap, boolean isFirstChild) {
	}

	@Override
	public boolean isData() {
		return false;
	}

	@Override
	public boolean isModelData() {
		return false;
	}

	@Override
	public boolean isType() {
		return false;
	}

	@Override
	public boolean isSearchList() {
		return false;
	}

	@Override
	public boolean isApplication() {
		return false;
	}

	@Override
	public boolean isNluResults() {
		return true;
	}

}
