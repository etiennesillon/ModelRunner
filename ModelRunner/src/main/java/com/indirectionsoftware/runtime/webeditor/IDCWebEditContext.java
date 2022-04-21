package com.indirectionsoftware.runtime.webeditor;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.internal.compiler.ast.ThisReference;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataParentRef;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.backend.database.IDCDatabaseTableBrowser;
import com.indirectionsoftware.backend.database.IDCSystemApplication;
import com.indirectionsoftware.backend.database.IDCSystemUser;
import com.indirectionsoftware.backend.database.IDCUser;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCDomainValue;
import com.indirectionsoftware.metamodel.IDCRefTree;
import com.indirectionsoftware.metamodel.IDCAction;
import com.indirectionsoftware.metamodel.IDCReference;
import com.indirectionsoftware.metamodel.IDCReport;
import com.indirectionsoftware.metamodel.IDCReportFolder;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCEnabled;
import com.indirectionsoftware.runtime.IDCError;
import com.indirectionsoftware.runtime.IDCHistoryStack;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCWebEditContext {

	private IDCWebEditor webApp;
	
	public IDCSystemUser user;
	
	public int type;
	
	public IDCType selectedType;
	public IDCData selectedData, searchData;
	public IDCAttribute selectedAttr;
	public IDCWebEditContext parentContext;
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

    public IDCWebEditContext(IDCWebEditor webApp, IDCSystemUser user, IDCWebEditContext parentContext, int type) {
    	
    	this.id = nextContextId++;
    	this.type = type;
		this.webApp = webApp;
		this.parentContext = parentContext;
		
		stack = new IDCHistoryStack();
		
	}
    
	/**************************************************************************************************/

	public static IDCWebEditContext createFirstContext(IDCWebEditor webApp, IDCSystemUser user) {
		
		IDCWebEditContext ret = new IDCWebEditContext(webApp, user, null, ROOT);
		
//		ret.selectedType = webApp.getApplication().getType(0);
//		ret.browser = new IDCDatabaseTableBrowser(ret.selectedType, ret.selectedType.loadAllDataReferences());
//		ret.stack.stackElement(ret.selectedType);

		return ret;
		
	}

	/**************************************************************************************************/

	public static IDCWebEditContext createChildContext(IDCWebEditContext parentContext, int type) {
		return new IDCWebEditContext(parentContext.webApp, parentContext.user, parentContext, type);
	}

	/**************************************************************************************************/

	public String process(HttpServletRequest request, int query, IDCSystemApplication sysApp) {
		return webApp.process(request, this, query, sysApp);
	}
	
	/**************************************************************************************************/

	public void disconnect() {
		webApp.disconnect();
	}

	/**************************************************************************************************/

	public boolean isChild() {
		return parentContext != null;
	}

}
    