package com.indirectionsoftware.metamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataParentRef;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.backend.database.IDCDatabaseConnection;
import com.indirectionsoftware.backend.database.IDCSystemUser;
import com.indirectionsoftware.runtime.IDCCSVImportParser;
import com.indirectionsoftware.runtime.IDCEvalData;
import com.indirectionsoftware.runtime.IDCFormula;
import com.indirectionsoftware.runtime.IDCModelDataPath;
import com.indirectionsoftware.runtime.IDCModelDataRef;
import com.indirectionsoftware.runtime.IDCXMLImportParser;
import com.indirectionsoftware.runtime.nlu.IDCAttributeValue;
import com.indirectionsoftware.runtime.nlu.IDCAttributeValueDataList;
import com.indirectionsoftware.runtime.nlu.IDCDataValue;
import com.indirectionsoftware.runtime.nlu.IDCLanguageEngine;
import com.indirectionsoftware.runtime.nlu.IDCOntology;
import com.indirectionsoftware.runtime.nlu.IDCValue;
import com.indirectionsoftware.runtime.nlu.IDCValueDataList;
import com.indirectionsoftware.utils.IDCCalendar;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCApplication  extends IDCModelData {
	
	public static final int PACKAGES=START_ATTR, GLOBALS=START_ATTR+1, VIEWFOLDERS=START_ATTR+2, REPORTFOLDERS=START_ATTR+3, 
							 DATABASEREF=START_ATTR+4, HOMEPAGE=START_ATTR+5, HELPPAGE=START_ATTR+6;

	private List<IDCType> 			types;
	private List<IDCPackage> 		packages, allPackages;		
	private List<IDCGlobal> 		globals;	
	private List<IDCViewFolder>	  	viewFolders;	
	private List<IDCReportFolder> 	reportFolders;	
	private IDCDatabaseRef 			databaseRef;

	private static final long NO_TRANSACTION=-1;
	private long transactionId = NO_TRANSACTION;
	
	private String homePage, helpPage;
	
	private IDCModelParser parser;

	private String modelXML;
	
	private List<IDCReference> unresolvedRefs = new ArrayList<IDCReference>();

	private IDCSystemUser user;
	
	IDCDatabaseConnection con;
	
	IDCOntology ontology = new IDCOntology(this);
	
	IDCLanguageEngine nluEngine = null;

	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCApplication(IDCModelData parent, long id, List<Object> values, IDCModelParser parser) {
		
		super(parent, IDCModelData.APPLICATION, id, values);
		this.parser = parser;
		
	}
	
	/**************************************************************************************************/

	public void addPackages(List<IDCPackage> packages, boolean isSystem) {
		
		for(IDCPackage pack : packages) {
			pack.setParent(this);
			pack.setIsSystem(isSystem);
			this.addChildToChildrenList(IDCApplication.PACKAGES, pack);
		}

	}
	
	/**************************************************************************************************/

	public void init() {
		init(null);
	}
	
	public void init(IDCData userData) {
		
		if(isInitRequired()) {
			
			super.init(userData);

			packages = (List<IDCPackage>) getList(PACKAGES);
			allPackages = new ArrayList<IDCPackage>();
			List<IDCPackage> temp = new ArrayList<IDCPackage>();
			for(IDCPackage pack : packages) {
				pack.init(userData);
				allPackages.add(pack);
				if(userData == null || userData.isEnabled(pack)) {
					temp.add(pack);
				}
			}
			packages = temp;
	
			globals = (List<IDCGlobal>) getList(GLOBALS);
			for(IDCGlobal glob: globals) {
				glob.init(userData);
			}

			viewFolders = (List<IDCViewFolder>) getList(VIEWFOLDERS);
			for(IDCViewFolder fold : viewFolders) {
				fold.init(userData);
			}

			reportFolders = (List<IDCReportFolder>) getList(REPORTFOLDERS);
			for(IDCReportFolder fold : reportFolders) {
				fold.init(userData);
			}
			
			List<IDCDatabaseRef> dbRefs = getList(DATABASEREF);
			if(dbRefs.size() == 1) {
				databaseRef = dbRefs.get(0);
				if(con != null) {
					databaseRef.setConnection(con);
				}
				databaseRef.init(userData);
			}
			
			types = new ArrayList<IDCType>();
			for(IDCPackage pack : packages) {
				types.addAll(pack.getTypes());
			}
			
			initIndexes();

			homePage = getString(HOMEPAGE, "Deploy\\html\\Home.html");
			helpPage = getString(HELPPAGE, "Deploy\\html\\Help.html");
			
			completeInit();
			
		}
		
	}

	/**************************************************************************************************/

	public void initIndexes() {
		
		int packId=0;
		for(IDCPackage pack : packages) {
			pack.setId(packId++);
		}

		int typeId=0;
		for(IDCType type : types) {
			type.setId(typeId++);
		}
		
	}

	/**************************************************************************************************/

	public IDCModelData getTopParent() {
		return this;
	}

	/**************************************************************************************************/

	public IDCCalendar getCalendar() {
		return IDCCalendar.getCalendar();
	}

	/**************************************************************************************************/
	// Types ...
	/**************************************************************************************************/
	
	public List<IDCType> getTypes() {
		return types;
	}
	
	/**************************************************************************************************/

	public IDCType getType(int nType) {
		
		IDCType ret = null;
		
		if(nType < types.size()) {
			ret = types.get(nType);
		}

		return ret;
		
	}

	/**************************************************************************************************/

	public IDCType getType(String typeName) {
		
		IDCType ret = null;
		
		for(IDCType type : types) {
			if(type.getName().equals(typeName)) {
				ret = type;
				break;
			}
		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public List<IDCType> initGetTypes() {

		List<IDCType> ret = types;
		
		if(ret == null) {
			ret = new ArrayList<IDCType>();
			for(IDCPackage pack : (List<IDCPackage>) getList(PACKAGES)) {
				ret.addAll(pack.initGetTypes());
			}
		}

		return ret;
		
	}

	public IDCType initGetType(String typeName) {
		
		IDCType ret = null;
		
		for(IDCType type : initGetTypes()) {
			if(type.initGetName().equals(typeName)) {
				ret = type;
			}
		}
		
		return ret;
		
	}

    /************************************************************************************************/

	public IDCData loadDataRef(IDCDataRef ref) {
		return loadDataRef(ref, true);
	}
	
	public IDCData loadDataRef(IDCDataRef ref, boolean getSuper) {
		
		IDCData ret = null;
		
		IDCType refType = getType(ref.getTypeId());
		ret = refType.loadDataObject(ref.getItemId(), getSuper); 
		
		return ret;
	}

	/**************************************************************************************************/
	// Pacakges ...
	/**************************************************************************************************/
	
	public List<IDCPackage> getPackages() {
		return getPackages(true);
	}

	public List<IDCPackage> getPackages(boolean includeSystemPackages) {
		
		List<IDCPackage> ret = new ArrayList<IDCPackage>();

		for(IDCPackage pack : packages) {
			if(!pack.isSystem() || includeSystemPackages) {
				ret.add(pack);
			}
		};

		return ret;
	}

	/**************************************************************************************************/

	public IDCPackage getPackage(int nPack) {
		return packages.get(nPack);
	}

	/**************************************************************************************************/

	public List<IDCPackage> initGetPackages() {

		List<IDCPackage> ret = packages;
		
		if(ret == null) {
			ret = getList(PACKAGES);
		}

		return ret;
		
	}
	
	/**************************************************************************************************/
	// Globals ...
	/**************************************************************************************************/

	public List<IDCGlobal> getGlobals() {
		return globals;
	}

	/**************************************************************************************************/

	public IDCGlobal getGlobal(int nGlob) {
		return globals.get(nGlob);
	}

	/**************************************************************************************************/

	public List<IDCGlobal> initGetGlobals() {

		List<IDCGlobal> ret = globals;
		
		if(ret == null) {
			ret = getList(GLOBALS);
		}

		return ret;
		
	}

	/**************************************************************************************************/
	// View Folders ...
	/**************************************************************************************************/

	public List<IDCViewFolder> getViewFolders() {
		return viewFolders;
	}

	/**************************************************************************************************/

	public IDCViewFolder getViewFolder(int nViewFold) {
		return viewFolders.get(nViewFold);
	}

	/**************************************************************************************************/

	public List<IDCViewFolder> initGetViewFolders() {

		List<IDCViewFolder> ret = viewFolders;
		
		if(ret == null) {
			ret = getList(VIEWFOLDERS);
		}

		return ret;
		
	}

	/**************************************************************************************************/
	// Report Folders ...
	/**************************************************************************************************/

	public List<IDCReportFolder> getReportFolders() {
		return reportFolders;
	}

	/**************************************************************************************************/

	public IDCReportFolder getReportFolder(int nRepFold) {
		return reportFolders.get(nRepFold);
	}

	/**************************************************************************************************/

	public List<IDCReportFolder> initGetReportFolders() {

		List<IDCReportFolder> ret = reportFolders;
		
		if(ret == null) {
			ret = getList(REPORTFOLDERS);
		}

		return ret;
		
	}

	/**************************************************************************************************/
	// Database References ...
	/**************************************************************************************************/

	public IDCDatabaseRef getDatabaseRef() {
		return databaseRef;
	}

	/**************************************************************************************************/

	public IDCDatabaseRef initGetDatabaseRef() {

		IDCDatabaseRef ret = databaseRef;
		
		if(ret == null) {
			List<IDCDatabaseRef> dbRefs = getList(DATABASEREF);
			if(dbRefs.size() == 1) {
				ret = dbRefs.get(0);
				if(con != null) {
					ret.setConnection(con);
				}

			}
		}

		return ret;
		
	}

	/**************************************************************************************************/
	// Database connections ...
	/**************************************************************************************************/
	
    public boolean connect() {
    	
    	boolean ret = databaseRef.getConnection().connect();
    	
		for(int nPack=0; ret && nPack < packages.size(); nPack++) {
			ret = packages.get(nPack).connect();
		}
		
		return ret;
		
    }

	/**************************************************************************************************/

    public void disconnect() throws Error {
		databaseRef.getConnection().disconnect();
    }

	/**************************************************************************************************/

    public long startTransaction() throws Error {

    	long ret = NO_TRANSACTION;
    	
		if(transactionId == NO_TRANSACTION) {
			transactionId = System.currentTimeMillis();
			ret = transactionId;
    		databaseRef.getConnection().startTransaction();
		}

    	return ret;

    }

	/**************************************************************************************************/

    public void endTransaction(long transactionId, boolean isCommit) throws Error {

		if(this.transactionId == transactionId) {
    		databaseRef.getConnection().endTransaction(isCommit);
			this.transactionId = NO_TRANSACTION;
		}

    }

	/**************************************************************************************************/

    public void createSchema(String schemaName) {
    	databaseRef.getConnection().createSchema(schemaName);
    }

	/**************************************************************************************************/

    public void generateSchema(boolean isSystemSchema) {
    	
    	for(IDCType type : types) {
    		if(isSystemSchema || !type.isSystem()) {
        		type.dropTable();
        		type.createTable();
    		}
    	}
    	
    }

	/**************************************************************************************************/
	// Children ...
	/**************************************************************************************************/
	
	public List getChildren(int mode) {

		 List ret = null; 
		
		 switch(mode) {
		
			case VIEW_MODE:
				ret = getViewFolders();
				break;
	
			case REPORT_MODE:
				ret = getReportFolders();
				break;
	
			case TODO_MODE:
				ret = getTodoNodes();
				break;
	
			default:
				ret = getPackages(false);
				break;

		 }
		
		 return ret;
		
	}

    /************************************************************************/

    public void display() {
    	
		IDCUtils.info("Application = " + getName());
		IDCUtils.info("Default db = " + getDatabaseRef().getName());
    	for(IDCPackage pack : getPackages()) {
    		IDCUtils.info(".Package = " + pack.getName());
    		IDCUtils.info(".Default db = " + pack.getDatabaseRef().getName());
        	for(IDCType type : pack.getTypes()) {
        		IDCUtils.info("..Type = " + type.getName());
        		IDCUtils.info(".db connection = " + type.getDatabaseRef().getName());
            	for(IDCPanel panel : type.getPanels()) {
            		IDCUtils.info("...Panel = " + panel.getName());
                	for(IDCAttribute attr : panel.getAttributes()) {
                		IDCUtils.info("....Attribute = " + attr.getName());
                	}
            			
            	}
        			
        	}
    			
    	}
    	
    }

    /************************************************************************/

	public IDCModelData getRefAnchor(int entityType, int entityId) {

		IDCModelData ret = null;
		
		switch(entityType) {
		
			case APPLICATION:
				ret = this;
				break;
				
			case TYPE:
				ret = getType(entityId);
				break;
				
			case PACKAGE:
				ret = getPackage(entityId);
				break;
				
			case VIEWFOLDER:
				ret = getViewFolder(entityId);
				break;
				
			case REPORTFOLDER:
				ret = getReportFolder(entityId);
				break;
				
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	// References ...
	/**************************************************************************************************/
	
	public IDCModelData getMetaModelData(String refStr) {
		
		IDCModelData ret = this; 
		
		IDCModelDataRef ref = IDCModelDataRef.getRef(refStr);
		if(ref != null) {
			
			int entityType = ref.getAnchorEntityType();
			int entityId = ref.getAnchorEntityId();
			
			ret = getRefAnchor(entityType, entityId);

			List<IDCModelDataPath> paths = ref.getPaths();
			if(paths != null) {
				for(IDCModelDataPath path : paths) {
					ret = ret.getRefChild(path.getEntityType(), path.getEntityId());
					if(ret == null) {
						break;
					}
				}
			}
			
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	
	public String getHomePage() {
		return homePage;
	}

	/**************************************************************************************************/
	
	public String getHelpPage() {
		return helpPage;
	}

	/************************************************************************************************/
	// Import ...
	/************************************************************************************************/

	public void importXML(String content) {
		IDCXMLImportParser importer = new IDCXMLImportParser(this);
		importer.importXML(content);
	}
	
	/*******************************************************************************************************/
	
	public void importCSV(String fileName) {
		
		IDCCSVImportParser parser = new IDCCSVImportParser(this);
		parser.importCSV(new File(fileName));
		
	}
	
	/************************************************************************************************/
	// Export ...
	/************************************************************************************************/

    public void writeXMLList(PrintWriter out, List<IDCDataRef> list, boolean isExpanded) {
    	
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		
		out.println("<ItemList>");
		
		for(IDCDataRef ref : list) {
			IDCData data = loadDataRef(ref);
			data.writeXML(out, isExpanded, null);
		}

		out.println("</ItemList>");

    }

	/************************************************************************************************/

    public void exportXMLList(String fn, List<IDCDataRef> list, boolean isExpanded) {
    	
    	try {
    		
    		PrintWriter out = new PrintWriter(new File(fn));
			
    		writeXMLList(out, list, isExpanded);
			
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
    	
    }

	/************************************************************************************************/

    public void writeJSONList(PrintWriter out, List<IDCDataRef> list, boolean isExpanded) {
    	
		out.println("{\"list\":[");
		
		HashMap<String, String> refMap = new HashMap<String, String>();
		
		boolean isFirstChild = true;
		for(IDCDataRef ref : list) {
			IDCData data = loadDataRef(ref);
			data.writeJSON(out, isExpanded, null, isFirstChild);
			isFirstChild = false;
		}

		out.println("]}");

    }

	/************************************************************************************************/

    public void exportJSONList(String fn, List<IDCDataRef> list, boolean isExpanded) {
    	
    	try {
    		
    		PrintWriter out = new PrintWriter(new File(fn));
			
    		writeJSONList(out, list, isExpanded);
			
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
    	
    }

	/************************************************************************************************/

	public boolean isApplication() {
		return true;
	}

	/************************************************************************************************/

	private IDCModelParser getParser() {
		return parser;
	}

	/************************************************************************************************/

	IDCModelData getRefData(int xmiId) {
		IDCUtils.debug("IDCApplicationg.getRefData() xmiId=" + xmiId);
		return (IDCModelData) parser.getReferencedData(this, xmiId);
	}

	/************************************************************************************************/

	public boolean checkIntegrity() {

		boolean ret = true;
		
		if(databaseRef == null) {
			IDCUtils.info("Application Integrity Error: no database connection defined ...");
			ret = false;
		}
		
		return ret;
		
	}

	/************************************************************************************************/

	public IDCReport getReport(String reportName) {
		return null;
	}

	/************************************************************************************************/

	public void getNewTestData() {
		
		List<IDCType> typeList = getTestDataTypeList();
		
		for(IDCType type : typeList) {
			if(type.isTopLevelCreatable() && !type.isSystem()) {
				type.getNewTestObjects();

			}
		}
		
	}

	/************************************************************************************************/

	public List<IDCType> getTestDataTypeList() {
		
		List<IDCType> ret = new ArrayList<IDCType>();
		
		for(IDCType type : types) {

			if(type.isTopLevelCreatable() && !type.isSystem()) {

				for(IDCAttribute attr : type.getAttributes()) {
		    		
		    		if(attr.hasFormula()) {
		    			String formStr = attr.getFormula();
		    			IDCFormula form = IDCFormula.getFormula(formStr);
		    			for(IDCType refType : form.getReferencedTypes(type)) {
		    				type.refTypes.add(refType);
		    			}
		    		}
		    	}
		    	
		    	for(IDCAction action : type.getCreateActions()) {
	    			String formStr = action.getFormula();
	    			IDCFormula form = IDCFormula.getFormula(formStr);
	    			for(IDCType refType : form.getReferencedTypes(type)) {
	    				type.refTypes.add(refType);
	    			}
		    	}

			}
	    	
		}

		boolean isDone = false;
		
		while(!isDone) {
			
			boolean isAllGood = true;
			
			for(IDCType type : types) {
				
				boolean isOk = true;
				
				for(IDCType refType : type.refTypes) {
					if(refType.isTestDataCreated == -1) {
						isOk = false;
					}
				}
				
				if(isOk) {
					ret.add(type);
				} else {
					isAllGood = false;
				}
				

			}
			
			if(isAllGood) {
				isDone = true;
			}
			
		}
		
		return ret;
		
	}
		
	/************************************************************************************************/

	public String getModelXML() {
		return modelXML;
	}

	public void setModelXML(String modelXML) {
		this.modelXML = modelXML;
	}

	/************************************************************************************************/

	public void addUnresolvedRef(IDCReference ref) {
		unresolvedRefs.add(ref);
	}

	/************************************************************************************************/

	public void resolveReferences(IDCApplication adminAppl) {
		for(IDCReference ref : unresolvedRefs) {
			ref.resolveRef(adminAppl);
		}
	}

	/************************************************************************************************/

	public void setUser(IDCSystemUser user) {
		this.user = user;
	}
	
	public IDCSystemUser getUser() {
		return user;
	}

	/************************************************************************************************/

	public void setConnection(IDCDatabaseConnection con) {
		this.con = con;
	}

	/************************************************************************************************/

	public IDCData createData(String typeName) {
		
		IDCData ret = null;

		IDCType type = getType(typeName);
		if(type != null) {
			ret = createData(type);
		}
		
		return ret;
		
	}
	
	/************************************************************************************************/

	public IDCData createData(IDCType type) {
		return createData(type, true);		
	}
	
	/**************************************************************************************************/

	public IDCData createData(IDCType type, boolean isGetSuperType) {

		IDCData ret = null; 

		if(isGetSuperType) {
			
			IDCType superType = type.getSuperType();
			IDCAttribute superAttr = type.getSuperAttribute();
			
			String superTypeName = type.getExtendsSystemType();
			if(superTypeName != null && superTypeName.length() > 0) {
				superType = getType(superTypeName);
				superAttr = superType.getAttribute(IDCModelData.APPLICATION_SYSTEM_EXTENSION_ATTR_NAME);
			} 
			
			ret = superType.getNewObject(true); 

			if(superAttr != null) {
				IDCDataRef ref = new IDCDataRef(type.getEntityId(), -1);
				ret.setValue(superAttr.getAttributeId(), ref);
			} 
			
		} else {
			ret = type.getNewObject(true); 
		}
		
		return ret;

	}

	/**************************************************************************************************/

	public IDCData createData(IDCType type, IDCDataParentRef parentRef, boolean isGetSuperType) {

		IDCData ret = createData(type, isGetSuperType); 
		ret.setNamespaceParentRef(parentRef);
		
		return ret;

	}

	/**************************************************************************************************/

	public IDCData createData(IDCType type, IDCData parent, int attrId, boolean isGetSuperType) {

		IDCData ret = createData(type, isGetSuperType); 
		ret.setNamespaceParentRef(parent, attrId);
		
		return ret;

	}
	
	/**************************************************************************************************/

	public IDCData createData(String typeName, IDCData parent, String attrName, boolean isGetSuperType) {
		
		IDCData ret = null;

		IDCType type = getType(typeName);
		if(type != null) {
			ret = createData(type, isGetSuperType);
			ret.setNamespaceParentRef(parent, parent.getDataType().getAttribute(attrName).getAttributeId());
		}
		
		return ret;

	}
	
	/**************************************************************************************************/

	public IDCData createData(String typeName, IDCData parent, String attrName) {
		return createData(typeName, parent, attrName, true);
	}
	
	/**************************************************************************************************/

	public IDCData requestSingleData(String typeName, String formula, IDCDataParentRef parentRef) {
		
		IDCData ret = null;
		
		IDCType type = getType(typeName);
		if(type != null) {
			ret = type.requestSingleData(formula, parentRef);
		}
		
		return ret;
		
		
	}
	
	/**************************************************************************************************/

	public List<IDCData> requestAllData(String typeName) {
		
		List<IDCData> ret = new ArrayList<IDCData>();
		
		IDCType type = getType(typeName);
		if(type != null) {
			ret = type.requestData(null, null);
		}
		
		return ret;
		
		
	}
	
	/**************************************************************************************************/

	public List<IDCData> requestData(String typeName, String formula, IDCDataParentRef parentRef) {
		
		List<IDCData> ret = new ArrayList<IDCData>();
		
		IDCType type = getType(typeName);
		if(type != null) {
			ret = type.requestData(formula, parentRef);
		}
		
		return ret;
		
		
	}
	
	/**************************************************************************************************/

	public IDCData requestDataByName(String typeName, String name) {
		
		IDCData ret = null;
		
		IDCType type = getType(typeName);
		if(type != null) {
			ret = type.requestDataByName(name);
		}
		
		return ret;
		
		
	}
	
	/**************************************************************************************************/

	public IDCData requestDataById(String typeName, long id) {
		
		IDCData ret = null;
		
		IDCType type = getType(typeName);
		if(type != null) {
			ret = type.loadDataObject(id);
		}
		
		return ret;
		
		
	}
	
	/**************************************************************************************************/

	public void resetType(IDCData data) {
		
		String typeName = data.getDataType().getName();
		
		IDCType type = getType(typeName);
		if(type != null) {
			data.setType(type);
		}

	}

	/**************************************************************************************************/
	// Todo Types ...
	/**************************************************************************************************/

	public List<IDCType> getTodoNodes() {
		
		List<IDCType> ret = new ArrayList<IDCType>();
		
		for(String typeName : TODOTYPENAMES) {
			ret.add(getType(typeName));
		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public Map<IDCType,List<IDCData>> getTodoTypeList() {
		return getTodoTypeList(false, false);
	}

	/**************************************************************************************************/

	public Map<IDCType, List<IDCData>> getTodoTypeList(boolean isActive, boolean isUser) {
		
		Map<IDCType,List<IDCData>> ret = new HashMap<IDCType,List<IDCData>>();
		
		String filter = "";
		if(isUser) {
			filter += "User == {User}";
		}
		
		int nType = 0;
		for(String typeName : TODOTYPENAMES) {

			IDCType type = getType(typeName);
			int attrId = TODOATTRNAMES[nType];
			int activeVal = TODOATTRVALUESACTIVE[nType];
			boolean activeValTest = TODOATTRVALUESTEST[nType];
			
			List<IDCData> dataList = new ArrayList<IDCData>();
			for(IDCData data : type.loadAllRootDataObjects()) {
				IDCEvalData evalData = data.getEvalData(filter);
				if(((Boolean)evalData.getValue())) {
					int val = data.getInt(attrId);
					if(!isActive || val == activeVal && activeValTest || val != activeVal && !activeValTest) {
						dataList.add(data);
					}
				}					
			}
			
			if(dataList.size() > 0) {
				ret.put(type,  dataList);
			}
			
			nType++;
			
		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public IDCOntology getOntology() {
		return ontology;
	}

	/**************************************************************************************************/

	public IDCLanguageEngine getNLUEngine() {
		return nluEngine;
	}

	/**************************************************************************************************/

	public void setNLUEngine(IDCLanguageEngine nluEngine) {
		this.nluEngine = nluEngine;
	}

	/*******************************************************************************************************/
	
	public void clearType(String typeName) {
		
		IDCType type = getType(typeName);
		
		if(type != null) {
			for(IDCData cat : type.loadAllDataObjects()) {
				cat.delete(true);
			}
		}
		
	}

	/*******************************************************************************************************/
	
	public Map<IDCType, Map<Long, Object>> getExportMap() {

		Map<IDCType, Map<Long, Object>> ret = new HashMap<IDCType, Map<Long, Object>>();
		
		for(IDCType type : getTypes()) {
			Map<Long, Object> map = new HashMap<Long, Object>();
			ret.put(type,  map);
		}
		
		return ret;
		
	}
	
	/*******************************************************************************************************/
	
	public List<IDCData> search(String keyword) {
		
		List<IDCData> ret = new ArrayList<IDCData>();
		
		for(IDCType type : types) {
			for(IDCData data : type.loadAllDataObjects()) {
				if(data.search(keyword)) {
					ret.add(data);
				}
			}
		}
		
		return ret;
		
	}

	/*******************************************************************************************************/
	
	public IDCValue searchValues(String keyword, int searchType) {
		
		IDCValue ret = new IDCValue(keyword);
		
		for(IDCType type : types) {
			
			if(!type.isSystem()) {
				
				IDCUtils.debugNLU("IDCApplication.searchValues(): type = " + type);

				List<IDCAttribute> attrs = type.getAttributes();
				List<IDCData> dataList = type.loadAllDataObjects();

				int nAttr=0;
				for(IDCAttribute attr : attrs) {
					
					Map<String, IDCValueDataList> attrValMap = new HashMap<String, IDCValueDataList>();

					IDCUtils.debugNLU("IDCApplication.searchValues(): attr = " + attr);

					for(IDCData data : dataList) {
						
						String val = data.getDisplayValue(nAttr).toLowerCase();
						
						IDCUtils.debugNLU("IDCApplication.searchValues(): data = " + data + " / val = " + val);

						boolean isFound = false;
						switch(searchType) {
						
							case IDCData.SEARCH_EQUALS:
								isFound = val.equals(keyword); 
								break;
								
							case IDCData.SEARCH_STARTS_WITH:
								isFound = val.startsWith(keyword);
								break;
								
							case IDCData.SEARCH_CONTAINS:
								isFound = val.indexOf(keyword) != -1;
								break;
								
						}
						
						if(isFound) {
							
							IDCValueDataList valDataList = attrValMap.get(val);
							if(valDataList == null) {
								IDCUtils.debugNLU("IDCApplication.searchValues(): found  type = " + type + " / data = " + data + " / keyword = " + keyword + " / attr = " + attr + " / val = " + val);
								valDataList = new IDCValueDataList(val);
								attrValMap.put(val,  valDataList);
							}
							valDataList.addData(data);
							
						}

					}
					
					if(attrValMap.values().size() > 0) {
						
						IDCAttributeValueDataList attrVal = new IDCAttributeValueDataList(attr);
						for(IDCValueDataList valDataList : attrValMap.values()) {
							attrVal.valueDataList.add(valDataList);
						}
						ret.attrVals.add(attrVal);
						
					}
					
					nAttr++;
					
				}
				
			}
			
		}
		
		IDCUtils.debugNLU("IDCApplication.searchValues(): ======================================================================");
		for(IDCAttributeValueDataList attrValDataList : ret.attrVals) {
			for(IDCValueDataList valData : attrValDataList.valueDataList) {
				IDCAttributeValue attrVal = new IDCAttributeValue(attrValDataList.attr, valData.value);
				IDCUtils.debugNLU("IDCApplication.searchValues(): attr = " + attrValDataList.attr + "/ value = " + valData.value);
			}
		}
		IDCUtils.debugNLU("IDCApplication.searchValues(): ======================================================================");

		
		return ret;
		
	}

	/*******************************************************************************************************/
	
	public List<IDCDataValue> searchDataValues(String keyword, int searchType) {
		
		List<IDCDataValue> ret = new ArrayList<IDCDataValue>();
		
		for(IDCType type : types) {
//			IDCUtils.debugNLU("searchDataValues(): type = " + type);
			if(!type.isSystem()) {
				for(IDCData data : type.loadAllDataObjects()) {
					ret.addAll(data.searchValues(keyword, searchType));
				}
			}
		}
		
		return ret;
		
	}

}