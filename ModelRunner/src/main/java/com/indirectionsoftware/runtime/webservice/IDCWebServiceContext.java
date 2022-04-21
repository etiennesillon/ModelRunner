package com.indirectionsoftware.runtime.webservice;


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

public class IDCWebServiceContext {

	private IDCWebService service;
	
	public IDCSystemUser user;
	
	public int type;
	
	public IDCType selectedType;
	public IDCData selectedData, searchData;
	public IDCAttribute selectedAttr;
	public IDCWebServiceContext parentContext;
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
	public static int CONTEXTSELECTQUERY[] = {IDCWebServiceController.GETITEMDETAILS, IDCWebServiceController.SELECTREFOK,IDCWebServiceController.GETITEMDETAILS};
	
	public HashMap<String, IDCRefTree> refTrees = new HashMap<String, IDCRefTree>();
	
	public boolean isInsideTableExpanded = false;
	
	public IDCDatabaseTableBrowser browser;

	public ArrayList<String> searchVals;

	public int page;

	public String onKeyPress;

	public String prefix = "";
	
	public static int nextContextId=0;
	
	/************************************************************************************************/

    public IDCWebServiceContext(IDCWebService service, IDCSystemUser user, IDCWebServiceContext parentContext, int type) {
    	
    	this.id = nextContextId++;
    	this.type = type;
		this.service = service;
		this.parentContext = parentContext;
		
		stack = new IDCHistoryStack();
		
	}
    
	/**************************************************************************************************/

	public static IDCWebServiceContext createFirstContext(IDCWebService service, IDCSystemUser user) {
		return new IDCWebServiceContext(service, user, null, ROOT);
	}

	/**************************************************************************************************/

	public static IDCWebServiceContext createChildContext(IDCWebServiceContext parentContext, int type) {
		return new IDCWebServiceContext(parentContext.service, parentContext.user, parentContext, type);
	}

	/**************************************************************************************************/

	public String process(HttpServletRequest request, int query) {
		return service.process(request, this, query);
	}
	
	/**************************************************************************************************/

	public void disconnect() {
		service.disconnect();
	}

	/**************************************************************************************************/

	public boolean isChild() {
		return parentContext != null;
	}

}
    