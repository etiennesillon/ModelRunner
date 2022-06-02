package com.indirectionsoftware.runtime;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;

import com.indirectionsoftware.metamodel.IDCType;

public class IDCURL implements IDCEnabled {

	private static final String URLPREFIX = "IDCURL:";
	
	public static final int URL=0, TEXT=1, HIST=2, FAV=3, SETTINGS=4, HOME=5, HELP=6, ABOUT=7, WELCOME=8, TODO=9;

	private int type;
	private String text, label;
	private URL url;
	
	/************************************************************************/

	public IDCURL(String label, URL url) {
    	this(label, URL, url); 
	}
 	
	/************************************************************************/

	public IDCURL(String label, int type) {
    	this(label, type, ""); 
	}
 	
    public IDCURL(String label, int type, URL url) {
    	this.label = label; 
    	this.type = type; 
    	this.url = url;
	}

	/************************************************************************/

	public IDCURL(String label, String text) {
    	this(label, TEXT, text); 
	}
 	
	public IDCURL(String label, int type, String text) {
    	this.label = label; 
    	this.type = type; 
    	this.text = text;
	}
 	
	/************************************************************************/

	public static IDCURL getFileURL(String label, int type, String fn) {

		IDCURL urlData = null;
		
		try {

			URL url = new URL("file:///" + fn);
			urlData = new IDCURL(label, type, url);
		
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return urlData;
		
	}

	public static IDCURL getFileURL(String label, String fn) {
		return getFileURL(label, URL, fn);
	}	
	
    /************************************************************************/

	public String getURL() {
		
		String ret = null;
		
		switch(type) {
		
			case URL:
				ret = "<a href=\"" + URLPREFIX + "(" + type + ")" + url + "\" >" + " <b>" + label+ "</b></a>";
				break;
				
		}
		
		return ret;
	
	}

    /************************************************************************/

	public int getType() {
		return type;
	}

    /************************************************************************/

	public void setHTML(JEditorPane htmlPanel) {
		
		try {
			
			switch(type) {
			
				case URL:
				case HOME:
				case HELP:
					htmlPanel.getEditorKit().createDefaultDocument();
					htmlPanel.setPage(url);
					break;
						
				case TEXT:
				case HIST:
				case FAV:
				case ABOUT:
				case WELCOME:
				case SETTINGS:
					htmlPanel.getEditorKit().createDefaultDocument();
					htmlPanel.setText(text);
					break;
					
			}

		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

    /************************************************************************/

	public static IDCURL getHyperlinkURL(HyperlinkEvent e) {
		return null;
	}

    /************************************************************************/

	public void exportXML(String fn, boolean isExpanded) {}
	public void exportXML(File file, boolean isExpanded) {}
	public String getXMLString(boolean isExpanded) { return null; }
	public void writeXML(PrintWriter out, boolean isExpanded, HashMap<String, String> refMap) {}

	/************************************************************************************************/

	public boolean isData() {
		return false;
	}

	/************************************************************************************************/

	public boolean isModelData() {
		return false;
	}

	/************************************************************************************************/

	public boolean isApplication() {
		return false;
	}

	/************************************************************************************************/

	public boolean isType() {
		return false;
	}

    /***************************************************/    
	
    public boolean isSearchList() {
		return false;
	}

	@Override
	public void exportJSON(String fn, boolean isExpanded) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exportJSON(File file, boolean isExpanded) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getJSONString(boolean isExpanded, boolean isFirstChild) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeXML(PrintWriter out, boolean isExpanded, Map<IDCType, Map<Long, Object>> refMap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeJSON(PrintWriter out, boolean isExpanded, Map<IDCType, Map<Long, Object>> refMap,
			boolean isFirstChild) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isNluResults() {
		return false;
	}
    
}
