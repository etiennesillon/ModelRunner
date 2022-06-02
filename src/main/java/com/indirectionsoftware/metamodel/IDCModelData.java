package com.indirectionsoftware.metamodel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDatabaseConnection;
import com.indirectionsoftware.runtime.IDCApprovalRequestData;
import com.indirectionsoftware.runtime.IDCEnabled;
import com.indirectionsoftware.runtime.IDCEvalData;
import com.indirectionsoftware.runtime.IDCModelDataRef;
import com.indirectionsoftware.runtime.IDCNotificationData;
import com.indirectionsoftware.runtime.IDCTaskData;
import com.indirectionsoftware.runtime.nlu.IDCConcept;
import com.indirectionsoftware.runtime.nlu.IDCOntology;
import com.indirectionsoftware.utils.IDCUtils;
import com.indirectionsoftware.utils.IDCVector;

public class IDCModelData extends IDCVector implements IDCEnabled {
	
	private IDCModelData parent;
	
	boolean isInitRequired = true;
	
	public static final String URLPREFIX = "IDCMDREF:";
	
	public static int searchCount = 0; 
	private static IDCEnabled firstFoundData;
	
	private Object extension = null;
	
	private List<IDCConcept> concepts = new ArrayList<IDCConcept>();
	
	private IDCDatabaseRef databaseRef;
	
	private IDCDatabaseConnection databaseConnection;
	
	/**************************************************************************************************/
	// Common data ...
	/**************************************************************************************************/
	
	private String		name, displayName, isEditableFormula, isEnabledFormula, treeIconName, panelIconName, descr, contactDetails;
	private int			isEditableStatus, isEnabledStatus;

	private List<IDCAction> actions;
	protected boolean isSystem;

	private long xmiId;

	private int maxTestObjects;
	
	/**************************************************************************************************/
	// Constants ...
	/**************************************************************************************************/
	
	public static final int DATA_MODE = 0, MODEL_MODE=1, EXPORT_MODE=2, EDITOR_MODE=3, VIEW_MODE=4, REPORT_MODE=5, TODO_MODE=6;
	
	public static final int NAME=0, DISPLAY_NAME=1, IS_EDITABLE=2, IS_ENABLED=3, TREE_ICON=4, PANEL_ICON=5, DESCR=6, CONTACT_DETAILS=7, MAX_TEST_OBJECTS=8, CONCEPTS=9, ACTIONS=10;

	protected static final int START_ATTR=11;

	/**************************************************************************************************/

	public static final int APPLICATION=0, PACKAGE=1, TYPE=2, PANEL=3, ATTRIBUTE=4, REFERENCE=5, DOMAIN=6, DOMAINVALUE=7, 
							GLOBAL=8, ACTION=9, 
							VIEWFOLDER=10, VIEW=11, REPORTFOLDER=12, REPORT=13, REPORTFIELD=14, 
							DATABASEREF=15;

	public static final String[] TYPES = {"Application", "Package", "Type", "Panel", "Attribute", "Reference", "Domain", "DomainValue", 
										  "GlobalVariable", "Action",  
										  "ViewFolder", "View", "ReportFolder", "Report", "ReportField", 
										  "DatabaseRef"};

	public static final int ENABLED = 1, DISABLED = 0, EVALUATE = 2;
	
	/**************************************************************************************************/
	
	static final String[]  TODOTYPENAMES        = {IDCTaskData.TASK_TYPE, IDCApprovalRequestData.REQUEST_TYPE, IDCNotificationData.NOTIFICATION_TYPE};
	static final int[]     TODOATTRNAMES        = {IDCTaskData.TASK_STATUS, IDCApprovalRequestData.REQUEST_STATUS, IDCNotificationData.NOTIFICATION_STATUS};
	static final int[]     TODOATTRVALUESACTIVE = {IDCTaskData.COMPLETED, IDCApprovalRequestData.PENDING, IDCNotificationData.UNREAD};
	static final boolean[] TODOATTRVALUESTEST   = {false, true, true};


	// special attribute in IDCReference to store the name of the referenced system type 
	public static final String REFERENCED_SYSTEM_TYPE_ATTR_NAME = "__Referenced_System_Type__"; // NEEDS TO MATCH ATTRIBUTE NAME IN METAMODEL // used to be __systemReference__
	
	// special attribute in IDCReference to store the name of the referenced system type 
	public static final String APPLICATION_SYSTEM_EXTENSION_ATTR_NAME = "__System_Extension__";	// NEEDS TO MATCH EXTENDABLE SYSTEM TYPE ATTRIBUTE NAMES IN MODELADMIN MODEL
		
	// special attribute in IDCReference to store the name of the referenced system type 
	public static final String APPLICATION_SYSTEM_REFERENCE_ATTR_NAME = "__System_Reference__";	// NEEDS TO MATCH EXTENDABLE SYSTEM TYPE ATTRIBUTE NAMES IN MODELADMIN MODEL

	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCModelData(IDCModelData parent, int entityType, long id, List<Object> values) {
		
		super(entityType, id, values);
		this.parent = parent;
		this.xmiId = id;
		
	}

	/**************************************************************************************************/

	public void init(IDCData userData) {

		isInitRequired = false;
		
		name = getString(NAME);
		displayName = getString(DISPLAY_NAME);
		if(displayName == null || displayName.length() == 0) {
			displayName = name;
		}
		treeIconName = getString(TREE_ICON);
		panelIconName = getString(PANEL_ICON);
		descr = getString(DESCR);
		contactDetails = getString(CONTACT_DETAILS);

		isEditableFormula = getString(IS_EDITABLE);
    	isEditableStatus = ENABLED;
		if(isEditableFormula != null && isEditableFormula.length() > 0) {
			if(isEditableFormula.equalsIgnoreCase("true")) {
				isEditableStatus = ENABLED;
			} else if(isEditableFormula.equalsIgnoreCase("false")) {
				isEditableStatus = DISABLED;
			} else {
				isEditableStatus = EVALUATE;
			}
		}
		
		isEnabledFormula = getString(IS_ENABLED);
		isEnabledStatus = ENABLED;
		if(isEnabledFormula != null && isEnabledFormula.length() > 0) {
			if(isEnabledFormula.equalsIgnoreCase("true")) {
				isEnabledStatus = ENABLED;
			} else if(isEnabledFormula.equalsIgnoreCase("false")) {
				isEnabledStatus = DISABLED;
			} else {
				isEnabledStatus = EVALUATE;
			}
		}
		
		maxTestObjects = getInt(MAX_TEST_OBJECTS);
		
		if(!isSystem()) {
			
			switch(getEntityType()) {
			
				case TYPE:
				case ATTRIBUTE:
	
					IDCOntology ontology = getApplication().getOntology();
					ontology.addModelEntity(this);
					String conceptNames = getString(CONCEPTS);
			    	if(conceptNames.length() > 0) {
			    		for(String conceptName : conceptNames.split(" ")) {
			    			IDCConcept concept = ontology.addModelEntity(conceptName, this);
			    			concepts.add(concept);
			    		}
			    	}
			    	break;
			}
			
		} else {
			IDCUtils.debug("IDCModelData.init(): name = " + name + " => IS SYSTEM!");
		}

		actions = getList(ACTIONS); 
		for(Object action : actions) {
			if(action instanceof IDCModelData) {
				((IDCModelData) action).init(userData);				
			} else if(action instanceof IDCAction) {
				((IDCAction) action).init(userData);				
			}
		}

	}

	/**************************************************************************************************/

	public static IDCModelData getInstance(IDCModelData parent, int entityType, long id, List<Object> values, IDCModelParser parser) {
		
		IDCModelData ret = null;
		
		switch(entityType) {
		
			case APPLICATION:
				ret = new IDCApplication(parent, id, values, parser);
				break;
				
			case PACKAGE:
				ret = new IDCPackage((IDCApplication) parent, id, values);
				break;
				
			case TYPE:
				ret = new IDCType((IDCPackage) parent, id, values);
				break;
				
			case PANEL:
				ret = new IDCPanel((IDCType) parent, id, values);
				break;
				
			case ATTRIBUTE:
				ret = new IDCAttribute((IDCPanel) parent, id, values);
				break;
				
			case REFERENCE:
				ret = new IDCReference(parent, id, values);
				break;
				
			case DOMAIN:
				ret = new IDCDomain((IDCPackage) parent, id, values);
				break;
				
			case DOMAINVALUE:
				ret = new IDCDomainValue((IDCDomain) parent, id, values);
				break;
				
			case GLOBAL:
				ret = new IDCGlobal((IDCApplication) parent, id, values);
				break;
				
			case ACTION:
				ret = new IDCAction(parent, id, values);
				break;
				
//			case ACTIONSTEP:
//				ret = new IDCActionStep(parent, id, values);
//				break;
//				
//			case ACTIONPARAM:
//				ret = new IDCActionParam(parent, id, values);
//				break;
//				
			case VIEWFOLDER:
				ret = new IDCViewFolder((IDCModelData) parent, id, values);
				break;
				
			case VIEW:
				ret = new IDCView((IDCViewFolder) parent, id, values);
				break;
				
			case REPORTFOLDER:
				ret = new IDCReportFolder((IDCModelData) parent, id, values);
				break;
				
			case REPORT:
				ret = new IDCReport((IDCReportFolder) parent, id, values);
				break;
				
			case REPORTFIELD:
				ret = new IDCReportField((IDCReport)parent, id, values);
				break;
				
			case DATABASEREF:
				ret = new IDCDatabaseRef(parent, id, values);
				break;
				
			default:
				int i=0;
				break;
				
		}
		
		return ret;
		
	}
	
	/**************************************************************************************************/

	public void completeInit() {
		//setValues(null);
	}


	/**************************************************************************************************/
	// Database Connection ...
	/**************************************************************************************************/
	
	public IDCDatabaseRef getDatabaseRef() {
		
		IDCDatabaseRef ret = databaseRef;
		
		if(ret == null && parent != null) {
			ret = parent.getDatabaseRef();
		}
		
		return ret;
		
	}
	
	/**************************************************************************************************/
	
	public void setDatabaseRef(IDCDatabaseRef ref) {
		databaseRef = ref;
	}
	
	/**************************************************************************************************/
	
	public IDCDatabaseConnection getDatabaseConnection() {

		IDCDatabaseConnection ret = databaseConnection;
		
		if(ret == null && parent != null) {
			ret = parent.getDatabaseConnection();
		}
		
		return ret;
		
	}
	
	/**************************************************************************************************/
	
	public void setDatabaseConnection(IDCDatabaseConnection con) {
		databaseConnection = con;
	}
	
	/**************************************************************************************************/
	
    public boolean  connect() {
		return getDatabaseConnection().connect();
    }

	/**************************************************************************************************/
	// Getters ...
	/**************************************************************************************************/
	
	public int getEntityType() {
		return getType();
	}

	/**************************************************************************************************/
	
	public int getEntityId() {
		return (int) getId();
	}

	/**************************************************************************************************/
	
	public long getXmiId() {
		return xmiId;
	}

	public void setXmiId(long xmiId) {
		this.xmiId = xmiId;
	}

	/**************************************************************************************************/
	
	public IDCModelData getParent() {
		return parent;
	}

	/**************************************************************************************************/
	
	public IDCModelData getTopParent() {
		return parent.getTopParent();
	}

	public IDCApplication getApplication() {
		return (IDCApplication) getTopParent();
	}

	/**************************************************************************************************/

    public boolean isInitRequired() {
    	return isInitRequired;
	}
    
	/**************************************************************************************************/

    public int getMaxTestObjects() {
    	return maxTestObjects;
	}
    
	/**************************************************************************************************/
	// Setters ...
	/**************************************************************************************************/
	
	public void setParent(IDCModelData parent) {
		this.parent = parent;
	}

	/**************************************************************************************************/
	// Common methods to all Model elements ...
	/**************************************************************************************************/
	
    public String getEntityName() {
    	int type = getEntityType();
    	return TYPES[type];
	}
    
	/**************************************************************************************************/

    public String getDescription() {
    	return descr;
	}
    
	/**************************************************************************************************/

    public String getContactDetails() {
    	return contactDetails;
	}
    
	/**************************************************************************************************/

    public String getName() {
    	return name;
	}
    
	/**************************************************************************************************/

    public String getDisplayName() {
    	return displayName;
	}
    
	/**************************************************************************************************/

    public void setName(String name) {
    	this.name = name;
	}
    
	/**************************************************************************************************/

    public String initGetName() {
    	
    	String ret = getName();
    	if(ret == null) {
    		ret = getString(NAME);
    	}
    	return ret;
	}
    
	/**************************************************************************************************/

    public String getNameEditor() {
    	return getString(NAME);
	}
    
	/**************************************************************************************************/

    public boolean isSystem() {
    	return isSystem;
	}
    
	public void setIsSystem(boolean isSystem) {
		this.isSystem = isSystem;
	}

	/**************************************************************************************************/

    public int getIsEditableStatus() {
    	return isEditableStatus;
	}
    
    public void setIsEditableStatus(int isEnabledStatus) {
    	this.isEditableStatus = isEnabledStatus;
	}
    
    public String getIsEditableFormula() {
    	return isEditableFormula;
	}
    
	/**************************************************************************************************/

    public int getIsEnabledStatus() {
    	return isEnabledStatus;
	}
    
    public void setIsEnabledStatus(int isEnabledStatus) {
    	this.isEnabledStatus = isEnabledStatus;
	}
    
    public String getIsEnabledFormula() {
    	return isEnabledFormula;
	}
    
	/**************************************************************************************************/

	public List<IDCAction> getActions() {
		return actions;
	}

	/**************************************************************************************************/

	public List<IDCAction> getGUIActions(boolean isMetaActions) {
		
		List<IDCAction> ret = new ArrayList<IDCAction>();
		
		for(IDCAction act : actions) {
			if(act.isGUIAction() && ((isMetaActions && act.isMetaAction()) || (!isMetaActions && !act.isMetaAction()))) {
				ret.add(act);
			}
		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public List<IDCAction> getDataActions() {
		
		List<IDCAction> ret = new ArrayList<IDCAction>();
		
		for(IDCAction act : actions) {
			if(!act.isGUIAction()) {
				ret.add(act);
			}
		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public IDCAction getAction(String actionName) {
		
		IDCAction ret = null;
		
		for(IDCAction act : actions) {
			IDCUtils.debug("Action name = |" + act.getName() + "|");
			if(act.getName().equals(actionName)) {
				ret = act;
			}
		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public IDCAction getAction(long actionId) {
		
		IDCAction ret = null;
		
		for(IDCAction act : actions) {
			IDCUtils.debug("Action name = |" + act.getName() + "|");
			if(act.getId() == actionId) {
				ret = act;
			}
		}
		
		return ret;
		
	}

	/**************************************************************************************************/

    public String getTreeIconName() {
    	return treeIconName;
	}
    
    public String getPanelIconName() {
    	return panelIconName;
	}
    
	/**************************************************************************************************/
	// Debug ...
	/**************************************************************************************************/
	
    public String toString() {
    	
    	String ret = name;
    	
    	if(getType() == ATTRIBUTE) {
    		ret = getParent().getParent().getName() + "." + name;
    	}
    	return ret;
    }
    
	/**************************************************************************************************/
	// Ancestors ...
	/**************************************************************************************************/
	
	public List<IDCModelData> getAncestors() {
		
		List<IDCModelData> ret = new ArrayList<IDCModelData> (); 
		
		IDCModelData elem = this; 
		
		IDCModelData parent = null;
		while((parent = elem.getParent()) != null) {
			ret.add(parent);
			elem = parent;
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	// Children ...
	/**************************************************************************************************/
	
	public List<IDCEnabled> getChildren(int mode) {
		return new ArrayList<IDCEnabled> (); 
	}

	public List<IDCEnabled> getChildren(int mode, int maxChildren) {
		return getChildren(mode); 
	}

	/**************************************************************************************************/
	// References ...
	/**************************************************************************************************/
	
	public String getURL() {
		return "<a href=\"" + URLPREFIX + getModelDataRef() + "\" >" + getEntityName()+ " <b>" + getName()+ "</b></a>";
	}

	/**************************************************************************************************/
	
	public IDCModelData getRefChild(int entityType, int anchorEntityId) {
		return null;
	}
	
	/**************************************************************************************************/
	
	public IDCModelDataRef getModelDataRef() {
		
		return new IDCModelDataRef(this);
		
	}

	/************************************************************************************************/
	// Export ...
	/************************************************************************************************/

    public String getXMLString(boolean isExpanded) {
    	
    	String ret = null;
    	
		ByteArrayOutputStream str = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(str);
		
		IDCUtils.writeXMLHeader(out);
		writeXML(out, isExpanded, getApplication().getExportMap());
		IDCUtils.writeXMLTrailer(out);
		
		ret = str.toString();
			
    	return ret;
    	
    }

	/************************************************************************************************/

    public void exportXML(String fn, boolean isExpanded) {
    	exportXML(new File(fn), isExpanded);
    }
    
    public void exportXML(File file, boolean isExpanded) {
    	
    	try {
    		
    		PrintWriter out = new PrintWriter(file);
			
    		IDCUtils.writeXMLHeader(out);
    		writeXML(out, isExpanded, getApplication().getExportMap());
    		IDCUtils.writeXMLTrailer(out);
    		
    		out.close();
			
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
    	
    }

	/************************************************************************************************/

    public void writeXML(PrintWriter out, boolean isExpanded, Map<IDCType, Map<Long, Object>> refMap) {
    	
    	for(IDCEnabled child : getChildren(EXPORT_MODE)) {
    		child.writeXML(out, isExpanded, refMap);
    	}

    }

	/************************************************************************************************/

    public String getJSONString(boolean isExpanded, boolean isFirstChild) {
    	
    	String ret = null;
    	
		ByteArrayOutputStream str = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(str);
		
		out.append('{');
		writeJSON(out, isExpanded, getApplication().getExportMap(), isFirstChild);
		out.append('}');
		
		ret = str.toString();
			
    	return ret;
    	
    }
    
	/************************************************************************************************/

    public void exportJSON(String fn, boolean isExpanded) {
    	exportJSON(new File(fn), isExpanded);
    }
    
    public void exportJSON(File file, boolean isExpanded) {
    	
    	try {
    		
    		PrintWriter out = new PrintWriter(file);
			
    		out.append('{');
    		writeJSON(out, isExpanded, getApplication().getExportMap(), true);
    		out.append('}');
    		
    		out.close();
			
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
    	
    }

	/************************************************************************************************/

    public void writeJSON(PrintWriter out, boolean isExpanded, Map<IDCType, Map<Long, Object>> refMap, boolean isFirstChild) {
    	
    	for(IDCEnabled child : getChildren(DATA_MODE)) {
    		child.writeJSON(out, isExpanded, refMap, isFirstChild);
    	}

    }

	/************************************************************************************************/

    public static int getSearchCount() {
    	return searchCount;
    }

	/************************************************************************************************/

    public static void resetSearch() {
    	
    	searchCount = 0;
    	firstFoundData = null;
    }

	/************************************************************************************************/

	public static IDCEnabled getFoundData() {
		return firstFoundData;
	}

	/************************************************************************************************/

	public void exportXMLModel(PrintWriter out) {
		
		if(actions.size() > 0) {
			out.println("       <MetaActions>");
			for(IDCAction act : actions) {
				act.exportXMLModel(out);
			}
			out.println("       </MetaActions>");
		}

	}

	/************************************************************************************************/

	public boolean isData() {
		return false;
	}

	/************************************************************************************************/

	public boolean isModelData() {
		return true;
	}

	/************************************************************************************************/

	public boolean isType() {
		return false;
	}

	/************************************************************************************************/

	public boolean isAttribute() {
		return false;
	}

	/************************************************************************************************/

	public boolean isApplication() {
		return false;
	}

	/**************************************************************************************************/

	public boolean contains(IDCModelData child) {
		
		boolean ret = false;
		
		List<IDCEnabled> children = getChildren(EDITOR_MODE);

		ret = children.contains(child);
		
		return ret;
		
	}

	/**************************************************************************************************/

	public boolean isModelAnchor() {
		
		boolean ret = false;
		
		switch(getEntityType()) {
		
			case APPLICATION:
			case PACKAGE:
			case TYPE:
			case VIEWFOLDER:
			case REPORTFOLDER:
				ret = true;
				break;

		}
		
		return ret;
		
	}
	
    /***************************************************/    
	
    public boolean isSearchList() {
		return false;
	}
    
    /***************************************************/    
	
    public void setExtension(Object extension) {
		this.extension = extension;
	}
    
    /***************************************************/    
	
    public Object getExtension() {
		return extension;
	}
    
    /***************************************************/    
	
    public void setValue(int ind, Object value) {
    	
    	values.set(ind, value);
    	
		if(ind == IDCModelData.NAME) {
			setName(""+value);
		}

    }
    
    /***************************************************/    
	
    public void setSingleChildrenList(int ind, IDCModelData child) {
    	
    	List<IDCModelData> list = new ArrayList<IDCModelData>();
    	list.add(child);
    	values.set(ind, list);

    }
    
    /***************************************************/    
	
    public void addChildToChildrenList(int ind, IDCModelData child) {
    	
    	List<IDCModelData> list = getList(ind);
    	list.add(child);

    }
    
    /***************************************************/    
	
    public void removeChildFromChildrenList(int ind, IDCModelData child) {
    	
    	List<IDCModelData> list = getList(ind);
    	list.remove(child);

    }

	/**************************************************************************************************/
	
	public Object getSystemRef() {
		return null;
	}
	
	/**************************************************************************************************/
	
	public List<IDCConcept> getConcepts() {
		return concepts;
	}

	/**************************************************************************************************/
	
	@Override
	public boolean isNluResults() {
		return false;
	}

}