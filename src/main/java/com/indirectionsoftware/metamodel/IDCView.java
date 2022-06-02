package com.indirectionsoftware.metamodel;

import java.io.PrintWriter;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;

public class IDCView extends IDCModelData {
	
	/**************************************************************************************************/
	// Constants ...
	/**************************************************************************************************/
	
	private final static int SOURCE=START_ATTR, CLASS=START_ATTR+1;
	
	String source, viewClass;
	
	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCView(IDCViewFolder parent, long id, List<Object> values) {
		super(parent, IDCModelData.VIEW, id, values);
	}
	
	/**************************************************************************************************/
	// Init processing ...
	/**************************************************************************************************/

	public void init(IDCData userData) {
		
		if(isInitRequired()) {

			super.init(userData);
			
			source = getString(SOURCE);
			viewClass = getString(CLASS);
		
			completeInit();
			
		}
	
	}
	
	/**************************************************************************************************/
	// View methods ...
	/**************************************************************************************************/
	
	public String getViewClass() {
		return viewClass;
	}
	
	/**************************************************************************************************/
	
	public String getSource() {
		return source;
	}
	
}