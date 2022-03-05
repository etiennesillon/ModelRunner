package com.indirectionsoftware.runtime.webapp;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.backend.database.IDCDatabaseTableBrowser;
import com.indirectionsoftware.backend.database.IDCSystemUser;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCRefTree;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCHistoryStack;

public class IDCWebAppContext {

	public IDCWebApplication webApp;
	
	public IDCSystemUser user;
	
	public int type;
	
	public IDCType selectedType;
	public IDCData selectedData, searchData;
	public IDCAttribute selectedAttr;
	public IDCWebAppContext parentContext;
	public int id, sortAttributeId;
	
	public int action;
	public String message = "";
	public boolean isUpdate = false;
	public boolean isDetails = true;
	public boolean isTodoActive = true;
	
	public int fieldId;
	
	public IDCHistoryStack stack;
	public long transId = -1;
	
	
	public static final int ROOT=0, SELECT=1, CREATECHILD=2;
	public static int CONTEXTSELECTQUERY[] = {IDCWebAppController.GETITEMDETAILS, IDCWebAppController.SELECTREFOK,IDCWebAppController.GETITEMDETAILS};
	
	public Map<String, IDCRefTree> refTrees = new HashMap<String, IDCRefTree>();
	
	public boolean isInsideTableExpanded = false;
	
	public IDCDatabaseTableBrowser browser;

	public ArrayList<String> searchVals;

	public int page;

	public String onKeyPress;

	public String prefix = "";
	
	public static int nextContextId=0;
	
	private Map<IDCAttribute, IDCDatabaseTableBrowser> attrBrowserMap;

	public String searchVal = "";

	public String nluQuery;
	
	/************************************************************************************************/

    public IDCWebAppContext(IDCWebApplication webApp, IDCSystemUser user, IDCWebAppContext parentContext, int type) {
    	
    	this.id = nextContextId++;
    	this.type = type;
		this.webApp = webApp;
		this.parentContext = parentContext;
		
		stack = new IDCHistoryStack();
		
		resetBrowserMap();
		
	}
    
	/**************************************************************************************************/

	public static IDCWebAppContext createFirstContext(IDCWebApplication webApp, IDCSystemUser user) {
		
		IDCWebAppContext ret = new IDCWebAppContext(webApp, user, null, ROOT);
		
//		ret.selectedType = webApp.getApplication().getType(0);
//		ret.browser = new IDCDatabaseTableBrowser(ret.selectedType, ret.selectedType.loadAllDataReferences());
//		ret.stack.stackElement(ret.selectedType);

		return ret;
		
	}

	/**************************************************************************************************/

	public static IDCWebAppContext createChildContext(IDCWebAppContext parentContext, int type) {
		return new IDCWebAppContext(parentContext.webApp, parentContext.user, parentContext, type);
	}

	/**************************************************************************************************/

	public String process(HttpServletRequest request, int query) {
		return webApp.process(request, this, query);
	}
	
	/**************************************************************************************************/

	public void disconnect() {
		webApp.disconnect();
	}

	/**************************************************************************************************/

	public boolean isChild() {
		return parentContext != null;
	}

	/**************************************************************************************************/

	public void resetBrowserMap() {
		attrBrowserMap = new HashMap<IDCAttribute, IDCDatabaseTableBrowser>();
	}
	
	/**************************************************************************************************/

	public IDCDatabaseTableBrowser getBrowser(IDCAttribute attr) {
		return getBrowser(attr, null, null);
	}
	
	/**************************************************************************************************/

	public IDCDatabaseTableBrowser getBrowser(IDCAttribute attr, IDCType type, List<IDCDataRef> list) {
		
		IDCDatabaseTableBrowser ret = attrBrowserMap.get(attr);
		if(ret == null) {
			if(list != null) {
				ret = new IDCDatabaseTableBrowser(type, list);
				attrBrowserMap.put(attr,  ret);
			}
		} else {
			if(list != null) {
				ret.setList(list);
			}
		}
		
		if(ret != null) {
			ret.setPageNumber(stack.getAttributePageNumber(attr));
		}
		
		return ret;
		
	}
	
	/**************************************************************************************************/

	public IDCDatabaseTableBrowser getNextBrowserPage(IDCAttribute attr) {
		
		IDCDatabaseTableBrowser ret = getBrowser(attr);
		if(ret != null) {
			ret.setNextPage();
			stack.updateAttributePageMap(attr, ret.getPageNumber());
		}
		
		return ret;
	}
	
	/**************************************************************************************************/

	public IDCDatabaseTableBrowser getPrevBrowserPage(IDCAttribute attr) {
		
		IDCDatabaseTableBrowser ret = getBrowser(attr);
		if(ret != null) {
			ret.setPrevPage();
			stack.updateAttributePageMap(attr, ret.getPageNumber());
		}
		
		return ret;
	}
	
	/****************************************************************************/

	public void initSearchVals(String searchVal) {		
		searchVals = new ArrayList<String>();
		searchVals.add(searchVal);
	}
	

}
    