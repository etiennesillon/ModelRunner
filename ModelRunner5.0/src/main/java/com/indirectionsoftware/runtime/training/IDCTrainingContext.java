package com.indirectionsoftware.runtime.training;


import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDatabaseTableBrowser;
import com.indirectionsoftware.backend.database.IDCSystemUser;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCRefTree;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCHistoryStack;

public class IDCTrainingContext {

	private IDCTrainingApp app;
	
	public IDCData user;
	
	public int type;
	
	public IDCType selectedType;
	public IDCData selectedData, searchData;
	public IDCAttribute selectedAttr;
	public IDCTrainingContext parentContext;
	public int id;
	
	public int action;
	public String message = "";
	public boolean isUpdate = false;
	public boolean isDetails = true;
	public boolean isTodoActive = true;
	
	public int fieldId;
	
	public IDCHistoryStack stack;
	public long transId = -1;
	
	
	public static final int ROOT=0, SELECT=1, CREATECHILD=2;
	
	public HashMap<String, IDCRefTree> refTrees = new HashMap<String, IDCRefTree>();
	
	public boolean isInsideTableExpanded = false;
	
	public IDCDatabaseTableBrowser browser;

	public ArrayList<String> searchVals;

	public int page;

	public String onKeyPress;

	public String prefix = "";
	
	public static int nextContextId=0;
	
	/************************************************************************************************/

    public IDCTrainingContext(IDCTrainingApp app, IDCData user, IDCTrainingContext parentContext, int type) {
    	
    	this.id = nextContextId++;
    	this.type = type;
		this.app = app;
		this.user = user;
		this.parentContext = parentContext;
		
		stack = new IDCHistoryStack();
		
	}
    
	/**************************************************************************************************/

	public static IDCTrainingContext createFirstContext(IDCTrainingApp app, IDCData user) {
		return new IDCTrainingContext(app, user, null, ROOT);
	}

	/**************************************************************************************************/

	public static IDCTrainingContext createChildContext(IDCTrainingContext parentContext, int type) {
		return new IDCTrainingContext(parentContext.app, parentContext.user, parentContext, type);
	}

	/**************************************************************************************************/

	public String process(HttpServletRequest request, int query) {
		return app.process(request, this, query);
	}
	
	/**************************************************************************************************/

	public void disconnect() {
		app.disconnect();
	}

	/**************************************************************************************************/

	public boolean isChild() {
		return parentContext != null;
	}

}
    