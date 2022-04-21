package com.indirectionsoftware.metamodel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataParentRef;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.backend.database.IDCDatabaseConnection;
import com.indirectionsoftware.backend.database.IDCDbQueryResult;
import com.indirectionsoftware.runtime.IDCError;
import com.indirectionsoftware.runtime.IDCFormula;
import com.indirectionsoftware.runtime.IDCRequest;
import com.indirectionsoftware.runtime.nlu.IDCAttributeValue;
import com.indirectionsoftware.runtime.nlu.IDCAttributeValueDataList;
import com.indirectionsoftware.runtime.nlu.IDCConcept;
import com.indirectionsoftware.runtime.nlu.IDCDataValue;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCType extends IDCModelData {
	
	public final static int ISTOPLEVELVIEWABLE=START_ATTR, ISTOPLEVELCREATABLE=START_ATTR+1, EXPLORERSQLFILTER=START_ATTR+2,
							 EXPLORERSQLORDERBY=START_ATTR+3, PANELS=START_ATTR+4,  
							 DATABASEREF=START_ATTR+5, NAMEFORMULA=START_ATTR+6, ISUNIQUEKEY=START_ATTR+7,
							 DEFAULTLISTSTYLESHEET=START_ATTR+8, DEFAULTDETAILSSTYLESHEET=START_ATTR+9,
							 DEFAULTLISTPANEL=START_ATTR+10, DEFAULTDETAILSPANEL=START_ATTR+11, DEFAULTEDITDIALOG=START_ATTR+12, ISDATAENABLED=START_ATTR+13, EXTENDS_SYSTEM_TYPE=START_ATTR+14;
	
	private boolean 			  isTopLevelViewable, isTopLevelCreatable, isUniqueKey;
	private int	 			 	  isDataEnabledStatus;
	private List<IDCAttribute> 	  attributes, allAttributes;
	private List<IDCPanel> 		  panels, allPanels;
	private IDCPackage			  pack;
	
	private IDCDatabaseRef 		  databaseRef;
	
	private String	  			  tableName, tableIdColName, explorerSQLFilter, explorerSQLOrderBy, nameFormula, isDataEnabledFormula,
								  defaultListStylesheet, defaultDetailsStylesheet, 
								  defaultListPanel, defaultDetailsPanel, defaultEditDialog, extendsSystemType;
	
	IDCType superType;
	IDCAttribute superAttr;
	
	public List<IDCType> refTypes;
	
	/**************************************************************************************************/

	public static final String  JOINT_PARENT_TYPE_ATTR_COL = "_Parent_Type_Attr";
	public static final String  JOINT_PARENT_ID_COL = "_ParentId";
	public static final String  JOINT_CHILD_REF_COL = "_ChildId";

	public static final String  NAMESPACE_TYPE_ATTR_COL = "_NamespaceType",
								NAMESPACE_ID_COL = "_NamespaceId",
								SUPER_REF_COL = "_SuperRef",
								SYSTEM_SUPER_REF_COL = "_SystemSuperRef",
							 	IS_SAVING_COL = "_IsSaving", 
							 	LAST_UPDATE_TIME_COL = "_LastUpdateTime", 
							 	LAST_UPDATE_USER_COL = "_LastUpdateUser";
	
	public static final String REF_SQL_TYPE = "VARCHAR(50)";
	
	public static final int MAX_ROWS=500, NO_MAX_ROWS=99999999;
	
	public int isTestDataCreated = -1;

	public List<IDCAttribute> dateAttributes = null;

	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCType(IDCPackage parent, long id, List<Object> values) {
		super(parent, IDCModelData.TYPE, id, values);
	}

	/**************************************************************************************************/
	// Init processing ...
	/**************************************************************************************************/

	public void init(IDCData userData) {
		
		if(isInitRequired()) {

			super.init(userData);
			
			if(getName().equals("Program")) {
				System.out.println("debug!");
			}
			
			isTopLevelViewable = IDCUtils.translateBoolean(getString(ISTOPLEVELVIEWABLE));
			isTopLevelCreatable = IDCUtils.translateBoolean(getString(ISTOPLEVELCREATABLE));
			isUniqueKey = IDCUtils.translateBoolean(getString(ISUNIQUEKEY));
			explorerSQLFilter = getString(EXPLORERSQLFILTER);
			explorerSQLOrderBy = getString(EXPLORERSQLORDERBY);
			defaultListStylesheet = getString(DEFAULTLISTSTYLESHEET);
			defaultDetailsStylesheet = getString(DEFAULTDETAILSSTYLESHEET);
			defaultListPanel = getString(DEFAULTLISTPANEL);
			defaultDetailsPanel = getString(DEFAULTDETAILSPANEL);
			defaultEditDialog = getString(DEFAULTEDITDIALOG);
			extendsSystemType = getString(EXTENDS_SYSTEM_TYPE);

			nameFormula = getString(NAMEFORMULA, null);
			
			isDataEnabledFormula = getString(ISDATAENABLED);
			isDataEnabledStatus = ENABLED;
			if(isDataEnabledFormula != null && isDataEnabledFormula.length() > 0) {
				if(isDataEnabledFormula.equalsIgnoreCase("true")) {
					isDataEnabledStatus = ENABLED;
				} else if(isDataEnabledFormula.equalsIgnoreCase("false")) {
					isDataEnabledStatus = DISABLED;
				} else {
					isDataEnabledStatus = EVALUATE;
				}
			}

			panels = (List<IDCPanel>) getList(PANELS);
			allPanels  = new ArrayList<IDCPanel>();
			List<IDCPanel> temp = new ArrayList<IDCPanel>();
			for(IDCPanel panel : panels) {
				panel.init(userData);
				allPanels.add(panel);
				if(userData == null || userData.isEnabled(panel)) {
					temp.add(panel);
				}
			}
			panels = temp;
	
			allAttributes = new ArrayList<IDCAttribute>();
			for(IDCPanel panel : allPanels) {
				allAttributes.addAll(panel.getAttributes());
			}

			attributes = new ArrayList<IDCAttribute>();
			for(IDCPanel panel : panels) {
				attributes.addAll(panel.getAttributes());
			}

			int attrId=0;
			for(IDCAttribute attr : attributes) {
				attr.setAttributeId(attrId++);
				if(attr.getAttributeType() == IDCAttribute.DATE || attr.getAttributeType() == IDCAttribute.DATETIME) {
					if(dateAttributes == null) {
						dateAttributes = new ArrayList<IDCAttribute>();
					}
					dateAttributes.add(attr);
				}
			}

			for(IDCPackage pck : getApplication().initGetPackages()) {
				for(IDCType type : pck.initGetTypes()) {
					if(type == this) {
						pack = pck;
					}
				}
			}
	
			List<IDCDatabaseRef> dbRefs = getList(DATABASEREF);
			if(dbRefs.size() == 1) {
				databaseRef = dbRefs.get(0);
				databaseRef.init(userData);
			}
			
			if(databaseRef == null && pack != null) {
				databaseRef = pack.initGetDatabaseRef();
			}
				
	    	tableName = getSQLName(getName());
	    	tableIdColName = getSQLName(tableName+"Id");

			completeInit();
			
		}
		
	}
	
	public IDCType getSuperType() { return superType == null ? this : superType; }
	public IDCAttribute getSuperAttribute() { return superAttr; }

	public void setSuperType(IDCType superType, IDCAttribute superAttr) { this.superType = superType;  this.superAttr = superAttr; }
	
	/**************************************************************************************************/

    public int getIsDataEnabledStatus() {
    	return isDataEnabledStatus;
	}
    
    public String getIsDataEnabledFormula() {
    	return isDataEnabledFormula;
	}
    
	/**************************************************************************************************/

	public List<IDCAttribute> initGetAttributes() {
		
		List<IDCAttribute> ret = attributes;
		
		if(ret == null) {
			ret = new ArrayList<IDCAttribute>();
			for(IDCPanel panel : (List<IDCPanel>) getList(PANELS)) {
				ret.addAll(panel.initGetAttributes());
			}
		}

		return ret;
	
	}

	/**************************************************************************************************/
	// Type methods ...
	/**************************************************************************************************/
	
	public boolean isTopLevelViewable() {
    	return isTopLevelViewable;
	}

	public boolean isTopLevelCreatable() {
    	return isTopLevelCreatable;
	}

	public boolean isUniqueKey() {
    	return isUniqueKey;
	}

	/**************************************************************************************************/

	public String getExplorerSQLFilter() {
    	return explorerSQLFilter;
	}

	/**************************************************************************************************/

	public String getExplorerSQLOrderBy() {
    	return explorerSQLOrderBy;
	}

	/**************************************************************************************************/

	public String getDefaultListStylesheet() {
    	return defaultListStylesheet;
	}

	/**************************************************************************************************/

	public String getDefaultDetailsStylesheet() {
    	return defaultDetailsStylesheet;
	}

	/**************************************************************************************************/

	public String getDefaultListPanel() {
    	return defaultListPanel;
	}

	/**************************************************************************************************/

	public String getDefaultDetailsPanel() {
    	return defaultDetailsPanel;
	}

	/**************************************************************************************************/

	public String getDefaultEditDialog() {
    	return defaultEditDialog;
	}
	
	
	/**************************************************************************************************/

	public String getExtendsSystemType() {
    	return extendsSystemType;
	}
	
	/**************************************************************************************************/
	// Attributes methods ...
	/**************************************************************************************************/
	
	public List<IDCAttribute> getAttributes() {
		return attributes;
	}

	/**************************************************************************************************/
	
	public List<IDCAttribute> getListAttributes() {
		
		List<IDCAttribute> ret = new ArrayList<IDCAttribute>();
		
		for(IDCAttribute attr : getAttributes()) {
			if(attr.isDisplayList() && !attr.isTable()) {
				ret.add(attr);
			}
		}
		return ret;
	}

	/**************************************************************************************************/
	
	public List<IDCAttribute> getSearchAttributes() {
		
		List<IDCAttribute> ret = new ArrayList<IDCAttribute>();
		
		for(IDCAttribute attr : getAttributes()) {
			if(attr.isDisplaySearch()) {
				ret.add(attr);
			}
		}
		return ret;
	}

	/**************************************************************************************************/
	
	public IDCAttribute getAttribute(int nAttr) {
		
		IDCAttribute ret = null;
		
		if(attributes.size() > nAttr) {
			ret = attributes.get(nAttr);
		}
		
		return ret;
	}

	/**************************************************************************************************/

	public IDCAttribute getAttribute(String attrName) {
		
		IDCAttribute ret = null;
		
		for(IDCAttribute attr : attributes) {
			if(attr.getName().equals(attrName)) {
				ret = attr;
			}
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	// Panels methods ...
	/**************************************************************************************************/
	
	public List<IDCPanel> getPanels() {
		return panels;
	}

	/**************************************************************************************************/
	
	public IDCPanel getPanel(int nPanel) {
		return panels.get(nPanel);
	}

	/**************************************************************************************************/
	// Actions methods ...
	/**************************************************************************************************/

	public List<IDCAction> getSaveActions(boolean isPreSave, boolean isNew) {
		
		List<IDCAction> ret = new ArrayList<IDCAction>();
		
		for(IDCAction act : getActions()) {
			if(act.isPreSave() == isPreSave && (isNew && act.isCreateAction() || !isNew && act.isUpdateAction())) {
				ret.add(act);
			}
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	
	public List<IDCAction> getDeleteActions() {
		
		List<IDCAction> ret = new ArrayList<IDCAction>();
		
		for(IDCAction act : getDataActions()) {
			if(act.isDeleteAction()) {
				ret.add(act);
			}
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	
	public List<IDCAction> getCreateActions() {
		
		List<IDCAction> ret = new ArrayList<IDCAction>();
		
		for(IDCAction act : getDataActions()) {
			if(act.isCreateAction()) {
				ret.add(act);
			}
		}
		
		return ret;
		
	}

	/**************************************************************************************************/
	// Database Connection ...
	/**************************************************************************************************/
	
	public IDCDatabaseRef getDatabaseRef() {
		return databaseRef;
	}
	
	/**************************************************************************************************/
	
	public IDCDatabaseConnection getDatabaseConnection() {
		return databaseRef.getConnection();
	}
	
	/**************************************************************************************************/

    public void createTable() {
    	
    	IDCUtils.info("Creating table: " + tableName + " ...");

    	try {
    		
			String query = "CREATE TABLE " + tableName + " ( ";

			String cols = tableIdColName + " BIGINT " + getDatabaseConnection().getCreateTablePart1() + " , " + SUPER_REF_COL + "  " + REF_SQL_TYPE + " , " + SYSTEM_SUPER_REF_COL + "  " + REF_SQL_TYPE+ " , " + NAMESPACE_TYPE_ATTR_COL + "  " + REF_SQL_TYPE + ", " + NAMESPACE_ID_COL + "  BIGINT, ";

			for (IDCAttribute attr : attributes) {
				String colType = getDatabaseConnection().getColType(attr.getColType());
				if(colType != null) {
					cols += attr.getColName() + " " + colType + ", ";
				}
			}

			cols += IS_SAVING_COL + " TINYINT(1), " + LAST_UPDATE_TIME_COL + " BIGINT, " + LAST_UPDATE_USER_COL + "  " + REF_SQL_TYPE;

			query += cols + getDatabaseConnection().getCreateTablePart2(tableIdColName);
			
			getDatabaseConnection().executeUpdate(query, false);

			for (IDCAttribute attr : attributes) {

				if (attr.isList()) {

					String tableName = getJointTableName(attr);

					getDatabaseConnection().dropTable(tableName);

					query = "CREATE TABLE " + tableName + " (" + JOINT_PARENT_TYPE_ATTR_COL + " " + REF_SQL_TYPE + ", " + JOINT_PARENT_ID_COL + " BIGINT, " + JOINT_CHILD_REF_COL + "  " + REF_SQL_TYPE + getDatabaseConnection().getCreateTablePart2(null);

					getDatabaseConnection().executeUpdate(query, false);

				}

			}

    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
    	
    }
	
	/**************************************************************************************************/

    public void dropTable() {

    	try {
    		
			getDatabaseConnection().dropTable(tableName);
			
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
    	
    }
	
	/*******************************************************************************************************/
	
    public void insertAttribute(IDCAttribute attr) {
		
    	try {

    		String colType = getDatabaseConnection().getColType(attr.getColType());
    		
    		if(colType != null) {
    			String query = "ALTER TABLE " + tableName + " ADD " + attr.getColName() + " " + colType;
    			getDatabaseConnection().executeUpdate(query, false);
    		}
    		
			if (attr.isList()) {
				String tableName = getJointTableName(attr);
				getDatabaseConnection().dropTable(tableName);
				//query = "CREATE TABLE " + tableName + " (" + JOINT_PARENT_TYPE_ATTR_COL + " " + REF_SQL_TYPE + ", " + JOINT_PARENT_ID_COL + " BIGINT, " + JOINT_CHILD_REF_COL + "  " + REF_SQL_TYPE + ") TYPE=InnoDB";
				String query = "CREATE TABLE " + tableName + " (" + JOINT_PARENT_TYPE_ATTR_COL + " " + REF_SQL_TYPE + ", " + JOINT_PARENT_ID_COL + " BIGINT, " + JOINT_CHILD_REF_COL + "  " + REF_SQL_TYPE + ")";
				getDatabaseConnection().executeUpdate(query, false);

			}

    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
    	
	}

	/*******************************************************************************************************/
	
	public void dropAttribute(IDCAttribute attr) {
		
    	try {
    		
    		if(attr.getColType() != null) {
        		String query = "ALTER TABLE " + tableName + " DROP COLUMN " + attr.getColName();
    			getDatabaseConnection().executeUpdate(query, false);
    		}
    		
			if (attr.isList()) {
				getDatabaseConnection().dropTable(getJointTableName(attr));
			}

    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
    	
	}

	/**************************************************************************************************/
	// Create Object ...
	/**************************************************************************************************/

    public IDCData getNewObject(boolean isInit) {
    	return new IDCData(this, isInit);
    }

    public IDCData getNewObject() {
    	return getNewObject(false);
    }

	/**************************************************************************************************/
	
	public IDCData createData() {
		return getApplication().createData(this);
	}
    
	/**************************************************************************************************/
	
	public IDCData createData(boolean isGetSuperType) {
		return getApplication().createData(this, isGetSuperType);
	}
    
    /**************************************************************************************************/

    public void getNewTestObjects() {
    	
		isTestDataCreated = 1;

		int nObjects = getMaxTestObjects();
    	
		for(int nObj=0; nObj < nObjects; nObj++) {
			getNewTestObject();
		}
		
		isTestDataCreated = 2;
    	
    }
    
    /**************************************************************************************************/

    public IDCData getNewTestObject() {
    	
    	IDCData ret = createData();
    	
    	for(IDCAttribute attr : attributes) {
    		
    		if(!attr.hasFormula() && !attr.getName().equals(IDCModelData.APPLICATION_SYSTEM_REFERENCE_ATTR_NAME) && !attr.getName().equals(IDCModelData.APPLICATION_SYSTEM_EXTENSION_ATTR_NAME)) {
        		ret.set(attr.getAttributeId(), attr.getTestValue(ret));
    		}

    	}
    	
    	ret.save();
    	
    	return ret;
    	
    }
    
    /**************************************************************************************************/
	// Load Data Object ...
	/**************************************************************************************************/
    public IDCData loadDataObject(long id) throws Error {
    	return loadDataObject(id, true);
    }
    
    public IDCData loadDataObject(long id, boolean getSuper) throws Error {
    	
		IDCUtils.traceStart("loadDataObject() id = " + id);

        IDCData ret = null; 

        try {

            String query = "SELECT * FROM " + tableName + " WHERE " + tableIdColName + " = " + id;
            IDCDbQueryResult dbRes = getDatabaseConnection().executeQuery(query);
            ResultSet rs = dbRes.getResultSet();
            
            List<Object> values = new ArrayList<Object>();
        	
            if(rs.next()) {
            	
            	IDCDataParentRef superRef = IDCDataParentRef.getParentRef(rs.getString(SUPER_REF_COL));
            	IDCDataParentRef systemSuperRef = IDCDataParentRef.getParentRef(rs.getString(SYSTEM_SUPER_REF_COL));

            	if(getSuper) {
            		
    				if(superRef != null) {
            			ret = getApplication().getType(superRef.getTypeId()).loadDataObject(superRef.getItemId());
    				}            		

    				if(ret != null) {
        				systemSuperRef = ret.getSystemExtentionParentRef();
        				if(systemSuperRef != null) {
                			ret = getApplication().getType(systemSuperRef.getTypeId()).loadDataObject(systemSuperRef.getItemId());
        				}        
    				}            		
    				
            	}
            	
				if(ret == null) {
					
	            	ret = new IDCData(this, false);
	            	
	            	ret.setId(id);
	            	ret.setNamespaceParentRef(IDCDataParentRef.getParentRef(rs.getString(NAMESPACE_TYPE_ATTR_COL), rs.getLong(NAMESPACE_ID_COL)));
	            	ret.setExtensionParentRef(superRef);
	            	ret.setSystemExtensionParentRef(systemSuperRef);
	            	ret.setLastUpdateTime(rs.getLong(LAST_UPDATE_TIME_COL));
	            	ret.setLastUpdateUser(rs.getString(LAST_UPDATE_USER_COL));
	            	ret.setIsSaving(rs.getInt(IS_SAVING_COL) == 1 ? true : false);
	            	
	            	for(IDCAttribute attr : attributes) {
	            		
	            		Object value = null;
	            		
	            		String colName = attr.getColName();
	            		
	                    	switch (attr.getAttributeType()) {
	                        	
		    					case IDCAttribute.STRING:
		    					case IDCAttribute.TEXT:
		    					case IDCAttribute.EMAIL:
		    					case IDCAttribute.PHONE:

		    						values.add(rs.getString(colName));
		    						break;
		    						
		    	    			case IDCAttribute.INTEGER:
		    	    				
	                    			values.add(new Integer(rs.getInt(colName)));
		    	    				break;
		    	    		
		    	    			case IDCAttribute.DOMAIN:
		    	    				
	                    			values.add(attr.getRefDomain().getDomainValue(rs.getInt(colName)));
		    	    				break;
		    	    		
		    	    			case IDCAttribute.BOOLEAN:
		    	    				
		    	    				byte byteVal = rs.getByte(colName);
	                    			if(byteVal ==0) {
	                    				values.add(new Boolean(false));
	                    			} else {
	                    				values.add(new Boolean(true));
	                    			}
		    	    				break;
		    			
		    	    			case IDCAttribute.DATE:
		    	    			case IDCAttribute.DURATION:
		    	    			case IDCAttribute.DATETIME:
		    	    			case IDCAttribute.PRICE:
		
	                    			values.add(new Long(rs.getLong(colName)));
		    	    				break;
		    	    		
		    	    				
		    	    			case IDCAttribute.REF:
		    	    			case IDCAttribute.REFBOX:
								case IDCAttribute.REFTREE:
		    	    			case IDCAttribute.EXTENSION:
		    	    				
		    						values.add(IDCDataRef.getRef(rs.getString(colName)));
		    	    				break;
		
		    	    			default:
		    	    				
		                    		values.add(null);
		    	    				break;

	                    	}
	                	
	            	}

	            	ret.setValues(values); 

				}

            }

        	dbRes.close();

        } catch(SQLException e) {
             throw new Error("SQLException: " + e.getMessage());
        }

		IDCUtils.traceEnd("loadDataObject()");

        return ret;

    }

	/**************************************************************************************************/
	// Load List Attribute ...
	/**************************************************************************************************/

    public List<IDCDataRef> loadDataListReferences(long id, IDCAttribute attr) throws Error {
    	
    	IDCUtils.traceStart("loadDataListReferences()");

    	List<IDCDataRef>  ret = new ArrayList<IDCDataRef>();
    	
    	IDCDataParentRef parentRef = new IDCDataParentRef(getEntityId(), attr.getId(), id);

		try {
        	
			String jointTableName = getJointTableName(attr);

        	String query = "SELECT " +  JOINT_CHILD_REF_COL + " FROM " + jointTableName + " WHERE " + JOINT_PARENT_TYPE_ATTR_COL + " = '" + parentRef.getParentRef()
        					+ "' AND " + JOINT_PARENT_ID_COL + " = '" + parentRef.getItemId() + "'";
        	
            IDCDbQueryResult dbRes = getDatabaseConnection().executeQuery(query);
            ResultSet rs = dbRes.getResultSet();
            
        	while (rs.next()) {
        		ret.add(IDCDataRef.getRef(rs.getString(JOINT_CHILD_REF_COL)));
        	}

        	dbRes.close();

        } catch(SQLException e) {
        	throw new Error("SQLException: " + e.getMessage());
        }

        
    	IDCUtils.traceEnd("loadDataListReferences()");

    	return ret;

    }
    
	/**************************************************************************************************/
	// Load List Attribute ...
	/**************************************************************************************************/

    public List<IDCDataRef> loadDataBackListReferences(long id, IDCAttribute attr) throws Error {
    	
    	//IDCUtils.debug("reftype = " + refType);
    	
		IDCUtils.traceStart("loadDataBackListReferences()");

    	List<IDCDataRef>  ret = new ArrayList<IDCDataRef>();
    	
    	IDCDataRef childRef = new IDCDataRef(getEntityId(), id);

    	for(IDCReference ref : attr.getReferences()) {
    		
    		IDCType refType = ref.getDataType();
    		IDCAttribute refAttr = ref.getAttribute();
    		
    		ret.addAll(refType.loadDataBackListReferences2(refAttr, childRef));

        }
        
		IDCUtils.traceEnd("loadDataBackListReferences()");

        return ret;

    }
    
	/**************************************************************************************************/

    public List<IDCDataRef> loadDataBackListReferences2(IDCAttribute refAttr, IDCDataRef childRef) throws Error {
    	
    	List<IDCDataRef>  ret = new ArrayList<IDCDataRef>();
    	
		try {
        	
        	IDCDataParentRef parentRef = new IDCDataParentRef(getEntityId(), refAttr.getId());

			String jointTableName = getJointTableName(refAttr);

			String query = "SELECT " +  JOINT_PARENT_ID_COL + " FROM " + jointTableName + " WHERE " + JOINT_PARENT_TYPE_ATTR_COL + " = '" + parentRef.getParentRef()
        					+ "' AND " + JOINT_CHILD_REF_COL + " = '" + childRef + "'";
        	
            IDCDbQueryResult dbRes = getDatabaseConnection().executeQuery(query);
            ResultSet rs = dbRes.getResultSet();
            
        	while (rs.next()) {
        		ret.add(new IDCDataRef(getEntityId(), rs.getLong(JOINT_PARENT_ID_COL)));
        	}

        	dbRes.close();

        } catch(SQLException e) {
        	throw new Error("SQLException: " + e.getMessage());
        }

        
        return ret;

    }
    
	/**************************************************************************************************/
	// Load Namespace Attribute ...
	/**************************************************************************************************/

	public List<IDCDataRef> loadNamespaceReferences(long id, IDCAttribute attr) {

		IDCUtils.traceStart("loadNamespaceReferences()");
		
		IDCDataParentRef parentRef = new IDCDataParentRef(getEntityId(), attr.getId(), id);
		
    	List<IDCDataRef>  ret = new ArrayList<IDCDataRef>();
    	
    	for(IDCReference ref : attr.getReferences()) {
    		
    		IDCType refType = ref.getDataType();
    		ret.addAll(refType.loadAllDataReferences(parentRef, null, null, NO_MAX_ROWS));
    		
    	}
    	
		IDCUtils.traceEnd("loadNamespaceReferences()");
		
        return ret;

	}

	/**************************************************************************************************/
	// Load All Data References ...
	/**************************************************************************************************/
	
	public List<IDCDataRef>  loadAllDataReferences(IDCDataParentRef parentRef, String where, String orderby, int maxRows) {

		IDCUtils.traceStart("IDCType.loadAllDataReferences()");
		
    	List<IDCDataRef>  ret = new ArrayList<IDCDataRef>();
    	
		try {
        	
        	String query = getQueryString(parentRef, where, orderby);
        	
        	IDCUtils.debug("IDCType.loadAllDataReferences(): query = " + query);
//    		System.out.println("IDCType.loadAllDataReferences(): query = " + query);
    		
    		IDCDbQueryResult dbRes = getDatabaseConnection().executeQuery(query);
            ResultSet rs = dbRes.getResultSet();
            
            int nRow = 0;
        	while (rs.next() && nRow++ < maxRows) {
        		ret.add(new IDCDataRef(getEntityId(), rs.getLong(tableIdColName)));
        	}

        	dbRes.close();

        } catch(SQLException e) {
        	throw new Error("SQLException: " + e.getMessage());
        }

		IDCUtils.traceEnd("IDCType.loadAllDataReferences()");
		
		return ret;
	
	}

	public List<IDCDataRef>  loadAllDataReferences() {
		return loadAllDataReferences(false);
	}

	public List<IDCDataRef>  loadAllDataReferences(boolean isTopNSOnly) {
		
//		String where = IDCFormula.getSQLFilter(getExplorerSQLFilter(), this);
//		String order = IDCFormula.getSQLFilter(getExplorerSQLOrderBy(), this);

		IDCDataParentRef parentRef = null;
		if(isTopNSOnly) {
			parentRef = IDCDataParentRef.getRootParentRef();
		}
		
		return loadAllDataReferences(parentRef, null, null, NO_MAX_ROWS);
	
	}

    /************************************************************************************************/

	public List<IDCData> loadAllDataObjects(List<IDCDataRef> refs, String selectionFormula, IDCData refer, boolean getSuper) {

		IDCUtils.traceStart("loadAllDataObjects()");

		List<IDCData> ret = new ArrayList<IDCData>();
		
		for(IDCDataRef ref : refs) {
			
			IDCData data = loadDataRef(ref, getSuper);
			
			if(selectionFormula != null && selectionFormula.length() > 0) {
				if(data.isTrue(selectionFormula, refer) && data.isInSettingsDateRange()) {
					ret.add(data);
				}
			} else if(data.isInSettingsDateRange()) {
				ret.add(data);
			}
		}
		
		IDCUtils.traceEnd("loadAllDataObjects()");

		return ret;
		
	}

    /************************************************************************************************/

	public List<IDCData> loadAllDataObjects(List<IDCDataRef> refs, String selectionFormula) {
		return loadAllDataObjects(refs, selectionFormula, null, true);
	}

	public List<IDCData> loadAllDataObjects(List<IDCDataRef> refs, String selectionFormula, IDCData refer) {
		return loadAllDataObjects(refs, selectionFormula, refer, true);
	}

	/************************************************************************************************/

	public List<IDCData> loadAllDataObjects(List<IDCDataRef> refs) {

		return loadAllDataObjects(refs, null, null);
		
	}

    /************************************************************************************************/

	public List<IDCData> loadAllDataObjects(String selectionFormula, IDCData ref) {

//		String where = IDCExpressionEvaluator.getSQLFilter(selectionFormula, this);
		String where = IDCFormula.getSQLFilter(selectionFormula, this);
		return loadAllDataObjects(loadAllDataReferences(null, where, null, NO_MAX_ROWS), selectionFormula, ref);
		
	}

    /************************************************************************************************/

	public List<IDCData> loadAllDataObjects(String selectionFormula, IDCDataParentRef parentRef) {

//		String where = IDCExpressionEvaluator.getSQLFilter(selectionFormula, this);
		String where = IDCFormula.getSQLFilter(selectionFormula, this);
		return loadAllDataObjects(loadAllDataReferences(parentRef, where, null, NO_MAX_ROWS), selectionFormula, null);
		
	}

    /************************************************************************************************/

	public List<IDCData> loadAllDataObjects(String selectionFormula) {
		
		List<IDCData> ret = null;
		
//		String where = IDCExpressionEvaluator.getSQLFilter(selectionFormula, this);
		String where = IDCFormula.getSQLFilter(selectionFormula, this);
		ret = loadAllDataObjects(loadAllDataReferences(null, where, null, NO_MAX_ROWS), selectionFormula, null);

		return ret;
		
	}

    /************************************************************************************************/

	public List<IDCData> loadAllDataObjects() {
		return loadAllDataObjects(loadAllDataReferences(null, null, null, NO_MAX_ROWS), null, null);
	}

	public List<IDCData> loadAllDataObjects(int maxRows) {
		return loadAllDataObjects(loadAllDataReferences(null, null, null, maxRows), null, null);
	}

	public List<IDCData> loadAllRootDataObjects() {
		return loadAllDataObjects(loadAllDataReferences(IDCDataParentRef.getRootParentRef(), null, null, NO_MAX_ROWS), null, null);
	}

	public List<IDCData> loadAllRootDataObjects(String filter) {
		return loadAllDataObjects(loadAllDataReferences(IDCDataParentRef.getRootParentRef(), filter, null, NO_MAX_ROWS), null, null);
	}

	public List<IDCData> loadAllRootDataObjects(int maxRows) {
		return loadAllDataObjects(loadAllDataReferences(IDCDataParentRef.getRootParentRef(), null, null, maxRows), null, null);
	}

	public List<IDCData> loadAllDataRootObjects(int maxRows, boolean isGetRoot) {
		return loadAllDataObjects(loadAllDataReferences(IDCDataParentRef.getRootParentRef(), null, null, maxRows), null, null, isGetRoot);
	}

    /************************************************************************************************/

	public IDCData loadDataRef(IDCDataRef ref) {
		return loadDataRef(ref, true);
	}

	public IDCData loadDataRef(IDCDataRef ref, boolean getSuper) {
		return getApplication().loadDataRef(ref, getSuper);
	}

	/**************************************************************************************************/
	// Update Object ...
	/**************************************************************************************************/

    public void updateObject(IDCData data) throws Error {
    	
        String query = "UPDATE " + tableName + " SET ";

        String set = IS_SAVING_COL + " = " + (data.isSaving() ? 1 : 0) + ", " + LAST_UPDATE_TIME_COL + " = " + System.currentTimeMillis() + ", " + LAST_UPDATE_USER_COL + " = '(no user)'";
        
        int nAttr = 0;
        
    	for(IDCAttribute attr : attributes) {
    		
    		String colName = attr.getColName();
        	if(colName != null) {
        		set += ", " + colName + " = " + attr.getColQuote() + data.getSQLString(attr) + attr.getColQuote();
        	} 
        	
        	nAttr++;

    	}
    	
    	query += set + " WHERE " + tableIdColName + " = " + data.getId();

        getDatabaseConnection().executeUpdate(query, false);

    }

	/**************************************************************************************************/
	// Update Object ...
	/**************************************************************************************************/

    public void updateObjectSavingStatus(IDCData data) throws Error {
    	
        String query = "UPDATE " + tableName + " SET " + IS_SAVING_COL + " = " + (data.isSaving() ? 1 : 0) + " WHERE " + tableIdColName + " = " + data.getId();

        getDatabaseConnection().executeUpdate(query, false);

    }

	/**************************************************************************************************/
	// Add Object ...
	/**************************************************************************************************/

    public IDCError addObject(IDCData data) {
    	
    	IDCError ret = null;

    	if(isUniqueKey) {
        	ret = checkDataKeyExists(data);
    	}
    	
    	if(ret == null) {

            String query = "INSERT INTO " + tableName + " (" + SUPER_REF_COL + ", " + SYSTEM_SUPER_REF_COL + ", " + NAMESPACE_TYPE_ATTR_COL + ", " + NAMESPACE_ID_COL + ", ";
            
            List<String> namesAndValues = getInsertNamesAndValues(data);

            String names = namesAndValues.get(0);
            String values = namesAndValues.get(1);
            
            String parentTypeAttr = "";
            long parentId = 0;
            IDCDataParentRef parentRef = data.getNamespaceParentRef();
            if(parentRef != null) {
            	parentTypeAttr = parentRef.getParentRef();
        		parentId = 	parentRef.getItemId();
            }
            
            IDCDataParentRef superRef = data.getExtentionParentRef();
            
            IDCDataParentRef systemSuperRef = data.getSystemExtentionParentRef();
            
        	query += names + ") VALUES ('" + (superRef == null ? "" : superRef) + "' , '" + (systemSuperRef == null ? "" : systemSuperRef) + "' , '" + parentTypeAttr + "' , " + parentId + ", " + values + ")";
        	
            getDatabaseConnection().executeUpdate(query, false);
            
            data.setId(getMaxRow());

    	}
        
        return ret;

    }


	/**************************************************************************************************/
	
    public List<IDCData> loadSimilarData(IDCData data, List<IDCAttribute> attrs) {

    	List<IDCData> ret = null;
    	
		String where = getWhereString(data, attrs);
		
		ret = loadAllDataObjects(loadAllDataReferences(null, where, null, NO_MAX_ROWS));
		
        return ret;

	}

	/**************************************************************************************************/
	
    public List<IDCData> loadSimilarData(IDCData data) {

    	List<IDCData> ret = null;
    	
    	List<IDCAttribute> attrs = new ArrayList<IDCAttribute>();
		for(IDCAttribute attr : attributes) {
			Object val = data.getValue(attr);
			if(val != null) {
				attrs.add(attr);
			}
		}

		String where = getWhereString(data, attrs);
		
		ret = loadAllDataObjects(loadAllDataReferences(null, where, null, NO_MAX_ROWS));
		
        return ret;

	}

	/**************************************************************************************************/
	
    public IDCData loadDataByName(String name) {

		IDCData ret = requestSingleData("Name == '" + name + "'");
		
		if(ret == null) {
//			ret = getNewObject();
			ret = createData();
			ret.set("Name",  name);
			ret.save();
		}
		
		return ret;


	}

	/**************************************************************************************************/
	
    private IDCError checkDataKeyExists(IDCData data) {

		IDCError ret = null;
		
		List<IDCAttribute> attrs = new ArrayList<IDCAttribute>();
		for(IDCAttribute attr : attributes) {
			if(attr.isKey()) {
				attrs.add(attr);
			}
		}

		String where = getWhereString(data, attrs);
		
		ret = checkDataKeyExists(where, data.getNamespaceParentRef());
		
        return ret;

	}

	/**************************************************************************************************/
	
    private IDCError checkDataKeyExists(String where, IDCDataParentRef parentRef) {

		IDCError ret = null;
		
		List<IDCDataRef> refs = loadAllDataReferences(parentRef, where, null, NO_MAX_ROWS);
		
		if(refs.size() > 0) {
			ret = new IDCError(IDCError.DUPLICATE_KEY);
		}
    	
        return ret;

	}

	/**************************************************************************************************/
	// Update Object ...
	/**************************************************************************************************/

    public void updateTempReferences(IDCData data) throws Error {
    	
    	for(IDCAttribute attr : attributes) {
    		
    		int attrType = attr.getAttributeType();
    		if( attrType== IDCAttribute.LIST) {
    			List<IDCDataRef> list = data.getDataType().loadDataListReferences(data.getTempId(), attr);
    			removeDataListFromParent(data.getTempId(), attr);
    			insertAllListReferences(data, attr, list);
    		} else if( attrType== IDCAttribute.NAMESPACE) {
    			updateAllNamespaceRefs(data, attr, data.getDataType().loadNamespaceReferences(data.getTempId(), attr));
    		} else if( attrType== IDCAttribute.EXTENSION) {
    			IDCDataRef extRef = (IDCDataRef) data.getRawValue(attr.getAttributeId());
    			if(extRef != null) {
    				if(attr.getName().equals(IDCModelData.APPLICATION_SYSTEM_EXTENSION_ATTR_NAME)) {
            			getApplication().getType(extRef.getTypeId()).updateSystemSuperRef(extRef.getItemId(), data.getAsParentRef(attr.getAttributeId()));
		//				ALSO NEED TO UPDATE SUPER REF !!!
    				} else {
            			getApplication().getType(extRef.getTypeId()).updateSuperRef(extRef.getItemId(), data.getAsParentRef(attr.getAttributeId()));
    				}
    			}
    		} 
    		
    	}
    	
    }

    /************************************************************************************************/

    public void insertListReference(String jointTableName, IDCDataParentRef parentRef, IDCDataRef childRef) throws Error {
			
        	String query = "INSERT INTO " + jointTableName + " (" + JOINT_PARENT_TYPE_ATTR_COL + ", " + JOINT_PARENT_ID_COL + ", " + JOINT_CHILD_REF_COL + 
        	") VALUES ('" + parentRef.getParentRef() + "', '" + parentRef.getItemId() + "', '" + childRef + "')";

            getDatabaseConnection().executeUpdate(query, false);

    }

    /************************************************************************************************/

    public void insertListReference(IDCData parent, IDCAttribute attr, IDCData child) throws Error {

		String jointTableName = getJointTableName(attr);

		int attrId = attr.getAttributeId();
		
		IDCDataParentRef parentRef = parent.getAsParentRef(attrId);
		
		insertListReference(jointTableName, parentRef, new IDCDataRef(child));
		
    }

    /************************************************************************************************/

    public void insertAllListReferences(IDCData parent, IDCAttribute attr, List<IDCDataRef> children) throws Error {

		String jointTableName = getJointTableName(attr);

		int attrId = attr.getAttributeId();
		
		IDCDataParentRef parentRef = parent.getAsParentRef(attrId);
		
		for(IDCDataRef childRef : children) {
			insertListReference(jointTableName, parentRef, childRef);
		}
		
    }

	/**************************************************************************************************/

    public long getMaxRow() throws Error {

    	long ret = -1;
    	
    	try {
        	
            String query = "SELECT MAX(" + tableIdColName + ") AS MAXID FROM " + tableName;
            
            IDCDbQueryResult dbRes = getDatabaseConnection().executeQuery(query);
            ResultSet rs = dbRes.getResultSet();
        	while (rs.next()) {
              ret = rs.getLong("MAXID");
        	}

        	dbRes.close();

        } catch(SQLException e) {
            throw new Error("SQLException: " + e.getMessage());
        }
        
        return ret;

    }

    /************************************************************************************************/

    public List<String> getInsertNamesAndValues(IDCData data) {
    	
    	List<String> ret = new ArrayList<String>();

        String names = LAST_UPDATE_TIME_COL + ", " + LAST_UPDATE_USER_COL;
        String values = "" + System.currentTimeMillis() + ", '(no user)'";
        
    	for(IDCAttribute attr : attributes) {
    		
    		String colName = attr.getColName();
    		int attrId = attr.getAttributeId();
        	if(colName != null) {
        		names += ", " + colName;
            	values += ", " + attr.getColQuote() + data.getSQLString(attr) + attr.getColQuote();
        	} 
        	
    	}
    	
    	ret.add(names);
    	ret.add(values);
    	
    	return ret;
    	
    }

	/**************************************************************************************************/
	// Delete Object ...
	/**************************************************************************************************/

    public void deleteObject(IDCData data) throws Error {
    	deleteObject(data.getId());
    }

	/**************************************************************************************************/

    public void deleteObject(long id) throws Error {
    	
    	if(id != -1) {
            String query = "DELETE FROM " + tableName + " WHERE " + tableIdColName + " = " + id;
            getDatabaseConnection().executeUpdate(query, false);
    	}
    	
    }

	/**************************************************************************************************/
	// Remove List Attribute ...
	/**************************************************************************************************/

    public void removeDataListFromParent(long id, IDCAttribute attr) throws Error {
    	
    	//IDCUtils.debug("reftype = " + refType);

    	IDCDataParentRef parentRef = new IDCDataParentRef(getEntityId(), attr.getId(), id);

		String jointTableName = getJointTableName(attr);

    	String query = "DELETE FROM " + jointTableName + " WHERE " + JOINT_PARENT_TYPE_ATTR_COL + " = '" + parentRef.getParentRef()
    					+ "' AND " + JOINT_PARENT_ID_COL + " = '" + parentRef.getItemId() + "'";
    	
    	getDatabaseConnection().executeUpdate(query, false);

    }
    
	/**************************************************************************************************/

    public void removeListReference(IDCData parent, IDCAttribute attr, IDCData child) throws Error {
    	
    	//IDCUtils.debug("reftype = " + refType);

    	IDCDataParentRef parentRef = parent.getAsParentRef(attr.getAttributeId());
    	IDCDataRef childRef = new IDCDataRef(child);

		String jointTableName = getJointTableName(attr);

    	String query = "DELETE FROM " + jointTableName + " WHERE " + JOINT_PARENT_TYPE_ATTR_COL + " = '" + parentRef.getParentRef()
    					+ "' AND " + JOINT_PARENT_ID_COL + " = '" + parentRef.getItemId()
    					+ "' AND " + JOINT_CHILD_REF_COL + " = '" + childRef + "'";
    	
    	getDatabaseConnection().executeUpdate(query, false);

    }
    
	/**************************************************************************************************/

    public void removeDataListFromChild(IDCDataRef childRef, IDCAttribute attr) throws Error {
    	
    	//IDCUtils.debug("reftype = " + refType);

    	IDCDataParentRef parentRef = new IDCDataParentRef(getEntityId(), attr.getId());

		String jointTableName = getJointTableName(attr);

    	String query = "DELETE FROM " + jointTableName + " WHERE " + JOINT_PARENT_TYPE_ATTR_COL + " = '" + parentRef.getParentRef()
    					+ "' AND " + JOINT_CHILD_REF_COL + " = '" + childRef + "'";
    	
    	getDatabaseConnection().executeUpdate(query, false);

    }
    
	/**************************************************************************************************/

    public void removeNamespaceRefs(long id, IDCAttribute attr) throws Error {
    	
		IDCDataParentRef parentRef = new IDCDataParentRef(getEntityId(), attr.getId(), id);
		
		for(IDCReference ref : attr.getReferences()) {
			IDCType refType = ref.getDataType();
			refType.removeNamespaceRefs2(parentRef.getParentRef(), parentRef.getItemId());
		}

    }
    
	/**************************************************************************************************/

    public void removeNamespaceRefs2(String parentRef, long itemId) throws Error {
    	
    	String query = "DELETE FROM " + tableName + " WHERE " + NAMESPACE_TYPE_ATTR_COL + " = '" + parentRef + "' AND " + NAMESPACE_ID_COL + " = " + itemId;
    	
    	getDatabaseConnection().executeUpdate(query, false);

    }
    
	/**************************************************************************************************/
	// check List Attribute ...
	/**************************************************************************************************/

    public boolean checkDataListFromChild(IDCDataRef childRef, IDCAttribute attr) throws Error {
    	
    	//IDCUtils.debug("reftype = " + refType);

    	boolean ret = false;
    	
		try {
        	
	    	IDCDataParentRef parentRef = new IDCDataParentRef(getEntityId(), attr.getId());

			String jointTableName = getJointTableName(attr);

	    	String query = "SELECT * FROM " + jointTableName + " WHERE " + JOINT_PARENT_TYPE_ATTR_COL + " = '" + parentRef.getParentRef()
	    					+ "' AND " + JOINT_CHILD_REF_COL + " = '" + childRef + "'";

            IDCDbQueryResult dbRes = getDatabaseConnection().executeQuery(query);
            ResultSet rs = dbRes.getResultSet();
	    	
	    	ret = rs.next();
			
        	dbRes.close();

        } catch(SQLException e) {
        	throw new Error("SQLException: " + e.getMessage());
        }
        
        return ret;

    }
    
	/**************************************************************************************************/

	public void removeRef(IDCDataRef ref, IDCAttribute attr) {

        String query = "UPDATE " + tableName + " SET " + attr.getColName() + " = '' WHERE " + attr.getColName() + " = " + "'" + ref + "'";
        getDatabaseConnection().executeUpdate(query, false);

	}
    
    /************************************************************************************************/

    public void updateAllNamespaceRefs(IDCData parent, IDCAttribute attr, List<IDCDataRef> children) throws Error {

		int attrId = attr.getAttributeId();
		
		IDCDataParentRef parentRef = parent.getAsParentRef(attrId);
		
		for(IDCDataRef childRef : children) {
			getApplication().getType(childRef.getTypeId()).updateNamespace(childRef.getItemId(), parentRef);
		}
		
    }

	/**************************************************************************************************/

	public void updateNamespace(long id, IDCDataParentRef ref) {
		
        String query = "UPDATE " + tableName + " SET " + LAST_UPDATE_TIME_COL + " = " + System.currentTimeMillis() + ", " + LAST_UPDATE_USER_COL + " = '(no user)'" +
        				", " + NAMESPACE_TYPE_ATTR_COL + " = '" + ref.getParentRef() + "', " + NAMESPACE_ID_COL + " = " + ref.getItemId()
        				+ " WHERE " + tableIdColName + " = " + id;

        getDatabaseConnection().executeUpdate(query, false);
        
	}

	/**************************************************************************************************/

	public void updateSuperRef(long id, IDCDataParentRef ref) {
		
        String query = "UPDATE " + tableName + " SET " + LAST_UPDATE_TIME_COL + " = " + System.currentTimeMillis() + ", " + LAST_UPDATE_USER_COL + " = '(no user)'" +
        				", " + SUPER_REF_COL + " = '" + (ref == null ? "" : ref) + "' WHERE " + tableIdColName + " = " + id;

        getDatabaseConnection().executeUpdate(query, false);
        
	}

	/**************************************************************************************************/

	public void updateSystemSuperRef(long id, IDCDataParentRef ref) {
		
        String query = "UPDATE " + tableName + " SET " + LAST_UPDATE_TIME_COL + " = " + System.currentTimeMillis() + ", " + LAST_UPDATE_USER_COL + " = '(no user)'" +
        				", " + SYSTEM_SUPER_REF_COL + " = '" + (ref == null ? "" : ref) + "' WHERE " + tableIdColName + " = " + id;

        getDatabaseConnection().executeUpdate(query, false);
        
	}

	/**************************************************************************************************/
	// Static methods ...
	/**************************************************************************************************/
	
	public static String getSQLName(String name) {
		
		String ret = "_" + name.replaceAll("\\s", "_");
		ret = ret.replaceAll("\\.", "_");
		ret = ret.replaceAll("\\?", "_");
		
		return ret;
		
	}
    
    /**********************************************************************/

    public String getJointTableName(IDCAttribute attr) {
    	return tableName +  getSQLName(attr.getName()) + "_Join";
    }

	/**************************************************************************************************/

    public String getQueryString(IDCDataParentRef parentRef, String where, String orderby) {
    	
        String ret = "SELECT " + tableIdColName + " FROM " + tableName;
        
        boolean isWhereNeeded = false;
        
        if(where == null || where.length() == 0) {
    		where = IDCFormula.getSQLFilter(getExplorerSQLFilter(), this);
        }
        
        if(where != null && where.length() > 0) {
        	isWhereNeeded = true;
        }
        
    	if(parentRef != null) {
    		if(parentRef.isRootRef()) {
        		ret += " WHERE " + NAMESPACE_TYPE_ATTR_COL + " = ''";
    		} else {
        		ret += " WHERE " + NAMESPACE_TYPE_ATTR_COL + " = '" + parentRef.getParentRef() + "' AND " + NAMESPACE_ID_COL + " = " + parentRef.getItemId();
    		}
    		if(isWhereNeeded) {
            	ret += " AND ";
    		}
    	//} else if(!isTopLevelViewable) {
    	//	ret += " WHERE " + NAMESPACE_ID_COL + " = 0";
    	//	if(isWhereNeeded) {
        //    	ret += " AND ";
    	//	}
    	} else if(isWhereNeeded){
    		ret += " WHERE ";
    	}
        
        if(isWhereNeeded) {
        	ret += where;
        }

        if(orderby == null || orderby.length() == 0) {
    		orderby = IDCFormula.getSQLFilter(getExplorerSQLOrderBy(), this);
        }
        
        if(orderby != null && orderby.length() > 0) {
        	ret += " ORDER BY " + orderby;
        }
            
        return ret;

    }

	/**************************************************************************************************/

    public String getWhereString(IDCData data, List<IDCAttribute> attrs) {
    	
        String ret = "";
        
        boolean isFirst = true;
        
        for(IDCAttribute attr : attrs) {
        	
    		String colName = attr.getColName();
        	if(colName != null) {
            	String value = data.getSQLString(attr);
            	
            	if(!isFirst) {
                	ret += " AND ";
            	}
        		ret +=  colName + " = " + attr.getColQuote() + value + attr.getColQuote();
        		isFirst = false;
        	} 
        	
        }
            
        return ret;

    }

	/**************************************************************************************************/

    public String getWhereString(List<IDCAttributeValue> attrVals) {
    	
        String ret = "";
        
        Map<IDCAttribute, List<String>> attrMap = new HashMap<IDCAttribute, List<String>>();
        
        for(IDCAttributeValue  attrVal : attrVals) {
        	List<String> list = attrMap.get(attrVal.attr);
        	if(list == null) {
        		list = new ArrayList<String>();
        		attrMap.put(attrVal.attr, list);
        	}
    		list.add(attrVal.value);
        }
        
        boolean isFirstAND = true;
        
        for(IDCAttribute attr : attrMap.keySet()) {

        	if(attr.getColName() != null) {
        		
        		List<String> list = attrMap.get(attr);
        		
            	if(list.size() == 1) {
                	if(!isFirstAND) {
                    	ret += " AND ";
                	}
                	String value = list.get(0);
                	ret += attr.getWhereString(value);
            		isFirstAND = false;
            	} else {
                	if(!isFirstAND) {
                    	ret += " AND ";
                	}
                	ret += " ( ";
                    boolean isFirstOR = true;
                    for(String value : list) {
                    	if(!isFirstOR) {
                        	ret += " OR ";
                    	}
                    	ret += attr.getWhereString(value);
                    	isFirstOR = false;
                    }
                	ret += " ) ";
            		isFirstAND = false;
            	}
        	}
        }

        return ret;

    }

	/**************************************************************************************************/

    public String getWhereStringOLD(List<IDCAttributeValue> valAttrs) {
    	
        String ret = "";
        
        Map<Long, List<IDCAttributeValue>> attrMap = new HashMap<Long, List<IDCAttributeValue>>();
        for(IDCAttributeValue  entry : valAttrs) {
        	List<IDCAttributeValue> list = attrMap.get(entry.attr.getId());
        	if(list == null) {
        		list = new ArrayList<IDCAttributeValue>();
        		attrMap.put(entry.attr.getId(), list);
        	}
    		list.add(entry);
        }
        
        boolean isFirstAND = true;
        
        for(List<IDCAttributeValue> list : attrMap.values()) {
        	if(list.size() == 1) {
            	if(!isFirstAND) {
                	ret += " AND ";
            	}
            	IDCAttributeValue attrVal = list.get(0);
            	ret += attrVal.attr.getWhereString(attrVal.value);
        		isFirstAND = false;
        	} else {
            	if(!isFirstAND) {
                	ret += " AND ";
            	}
            	ret += " ( ";
                boolean isFirstOR = true;
                for(IDCAttributeValue  attrVal : list) {
                	if(!isFirstOR) {
                    	ret += " OR ";
                	}
                	ret += attrVal.attr.getWhereString(attrVal.value);
                	isFirstOR = false;
                }
            	ret += " ) ";
        		isFirstAND = false;
        	}
        }

        return ret;

    }

	/**************************************************************************************************/
	// Request methods ...
	/**************************************************************************************************/

	public List<IDCData> requestData(String formula) {
		return requestData(formula, null);
	}
	
	public List<IDCData> requestData(String formula, IDCDataParentRef parentRef) {
		
		List<IDCData> ret = new ArrayList<IDCData>();
		
		IDCRequest req = new IDCRequest(this, parentRef);
		req.setSelectionFormula(formula);
		req.addResultFormula("{This}");
		
		for(List<Object> result : req.execute()) {
			ret.add((IDCData) result.get(0));
			
		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public IDCData requestDataByName(String name) {
		return requestSingleData("Name == '" + name + "'", null);
	}

	/**************************************************************************************************/

	public IDCData requestDataByNameIgnoreCase(String name) {
		return requestSingleData("Name ~= '" + name + "'", null);
	}

	/**************************************************************************************************/

	public IDCData requestDataByNameStartsWith(String name) {
		return requestSingleData("Name =- '" + name + "'", null);
	}

	/**************************************************************************************************/

	public IDCData requestSingleData(String formula) {
		return requestSingleData(formula, null);
	}

	public IDCData requestSingleData(String formula, IDCDataParentRef parentRef) {

		IDCData ret = null;
		
		List<IDCData> results = requestData(formula, parentRef);
		
		if(results.size() == 1) {
			ret = results.get(0);
		}
		
		return ret;
		
	}
	
	/*******************************************************************************************************/
	// Search ...
	/**************************************************************************************************/
	
	public List<IDCData> search(List<IDCAttributeValue> attrValues) {
		
		String where = getWhereString(attrValues);
		IDCUtils.debug("IDCType.search(): where = " + where);
		return loadAllDataObjects(loadAllDataReferences(null, where, null, NO_MAX_ROWS));
	}

	/**************************************************************************************************/
	// Children ...
	/**************************************************************************************************/
	
	public List getChildren(int mode) {
		return getChildren(mode, MAX_ROWS);
	}

	public List getChildren(int mode, int maxChildren) {

		 List ret = new ArrayList<Object>(); 
		
		if(maxChildren == -1) {
			maxChildren = NO_MAX_ROWS;
		}

		switch(mode) {
		
			case DATA_MODE:
			case TODO_MODE:
				ret = loadAllRootDataObjects(maxChildren);
				break;
	
			case EXPORT_MODE:
				ret = loadAllDataRootObjects(maxChildren, false);
				break;
	
			case EDITOR_MODE:
				ret = getPanels();
				break;
	
		 }
		
		 return ret;
		
	}

	/************************************************************************************************/

	public boolean isType() {
		return true;
	}

    /************************************************************************************************/
    
	public String getNameFormula() {
		return nameFormula;
	}
	
    /************************************************************************************************/

    public String getHTMLActions() {
    	
    	String ret = "";

        for(IDCAction act : getGUIActions(true)) {
        	ret += "<a href='list.jsp?typename=" + getName() + "&action=" + act.getName()+ "'>" + act.getName()  + "</a>";
        }

    	return ret;
    	
    }
    
	/**************************************************************************************************/
	
    public boolean  connect() {
		return databaseRef.getConnection().connect();
    }

	/**************************************************************************************************/
	
	public String getCSVHeader() {
		
        String ret = "typeId, Id, parentNamceSpaceRef";
        
        for(IDCAttribute attr : attributes) {
        	ret +=  ", " + attr.getName();
        }
        
		ret += "\n";
		
        return ret;
        
	}

}