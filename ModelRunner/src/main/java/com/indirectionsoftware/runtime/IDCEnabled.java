package com.indirectionsoftware.runtime;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.indirectionsoftware.metamodel.IDCType;

public interface IDCEnabled {

	String getURLId();

	public void exportXML(String fn, boolean isExpanded);
	public void exportXML(File file, boolean isExpanded);
	public String getXMLString(boolean isExpanded);
	public void writeXML(PrintWriter out, boolean isExpanded, Map<IDCType, Map<Long, Object>> refMap);

	public void exportJSON(String fn, boolean isExpanded);
	public void exportJSON(File file, boolean isExpanded);
	public String getJSONString(boolean isExpanded, boolean isFirstChild);
	public void writeJSON(PrintWriter out, boolean isExpanded, Map<IDCType, Map<Long, Object>> refMap, boolean isFirstChild);
	
	public boolean isData();
	public boolean isModelData();
	public boolean isType();
	public boolean isSearchList();

	boolean isApplication();
	
	boolean isNluResults();
	
}
