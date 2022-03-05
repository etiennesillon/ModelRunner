package com.indirectionsoftware.metamodel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.runtime.IDCError;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCAttribute extends IDCModelData {
	
	private String 	desc, initValue, formula, colName, colType, colQuote, refFormula, refListFormula, systemReference, constraintFormula, constraintMessage;
	private int 	attrType, len;
	private boolean isMandatory, isKey, hasFormula, isPrimitiveType, needsTable, isNumericType, isDisplayEdit, isDisplayList, isDisplaySearch;
	
	private IDCDomain refDom;
	private List<IDCReference> references;
	private Color labelColor;
	private Object defaultValue;
	
	private Class attributeClass;
	private int attrId;
	
	private int MAX_TEST_VAL = 999999;
	
	/**************************************************************************************************/
	// Constants ...
	/**************************************************************************************************/
	
	public static final int STRING=0, INTEGER=1, BOOLEAN=2, DATE=3, DATETIME=4, DURATION=5, PHONE=6, EMAIL=7, PRICE=8, TEXT=9,  
						    BACKREF=10, DOMAIN=11, REF=12, REFBOX=13, REFTREE=14, LIST=15, NAMESPACE=16, EXTENSION=17;

	public static final String ATTRIBUTE_TYPES[] = {"STRING", "INT", "BOOLEAN", "DATE", "DATETIME", "DURATION", "PHONE", "EMAIL", "PRICE", "TEXT", 
		                                             "BACKREF", "DOMAIN", "REF", "REFBOX", "REFTREE", "AGGREGATION", "COMPOSITION", "EXTENSION"};

	public  static final String DISPLAY_TYPES[] = {"String", "Integer", "Boolean", "Date", "DateTime", "Duration", "Phone", "Email", "Price", "Text",  
		                                           "Back Ref", "Domain", "Ref", "RefBox", "RefTree", "Aggregation", "Composition", "Extension"};

	public static final int DESC=START_ATTR, TYPE=START_ATTR+1, LENGTH=START_ATTR+2, ISMANDATORY=START_ATTR+3, 
			ISKEY=START_ATTR+4, ISDISPLAYLIST=START_ATTR+5, ISDISPLAYEDIT=START_ATTR+6, ISDISPLAYSEARCH=START_ATTR+7,
			INITVALUE=START_ATTR+8, FORMULA=START_ATTR+9, REFFORMULA=START_ATTR+10, REFLISTFORMULA=START_ATTR+11, CONSTRAINTFORMULA=START_ATTR+12, CONSTRAINTMESSAGE=START_ATTR+13, SYSREF=START_ATTR+14, REFERENCES=START_ATTR+15;

	private static final int STRING_DEFAULT_LEN = 300; 
	
	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCAttribute(IDCPanel parent, long id, List<Object> values) {
		super(parent, IDCModelData.ATTRIBUTE, id, values);
	}

	/**************************************************************************************************/

	public void init(IDCData userData) {
		
		if(isInitRequired()) {
			
			super.init(userData);
			
			IDCUtils.debug(">>>> IDCAttribute.Init(): processing type = " + getParent().getParent().getName() + " attr = " + getName());
			
			initValue = getFormula(INITVALUE);
			formula = getFormula(FORMULA);
			constraintFormula = getFormula(CONSTRAINTFORMULA);
			constraintMessage = getString(CONSTRAINTMESSAGE);
			formula = getFormula(FORMULA);
			desc = getString(DESC);
			refFormula = getFormula(REFFORMULA);
			refListFormula = getFormula(REFLISTFORMULA);
			systemReference = getFormula(SYSREF);
			
			len = IDCUtils.translateInteger(getString(LENGTH));
			
			isMandatory = IDCUtils.translateBoolean(getString(ISMANDATORY));
			isDisplayEdit = IDCUtils.translateBoolean(getString(ISDISPLAYEDIT));
			isDisplayList = IDCUtils.translateBoolean(getString(ISDISPLAYLIST));
			isDisplaySearch = IDCUtils.translateBoolean(getString(ISDISPLAYSEARCH));
			isKey = IDCUtils.translateBoolean(getString(ISKEY));
			hasFormula= false;
			if(formula != null) {
				hasFormula = true;
			}

			attrType = getAttributeType(getString(TYPE));
			
			List<IDCReference> refs = (List<IDCReference>) getList(REFERENCES);
			if(isDomain()) {
				IDCReference ref = refs.get(0);
				ref.init(userData);
				refDom = ref.getDomain();
				refDom.init(userData);
			} else {
				references = new ArrayList<IDCReference>();
				for(IDCReference ref : refs) {
					ref.init(userData);
					references.add(ref);
				}
			}
			
			if(isExtension()) { //attrType == EXTENSION 
				IDCType superType = (IDCType) getParent().getParent();
				for(IDCReference ref : refs) {
					IDCType childType = ref.getDataType();
					if(childType == null) {
						int i=0;
					} else {
						childType.setSuperType(superType, this);
					}
				}
			}
			
			labelColor = (isMandatory ? Color.BLACK : Color.GRAY);

    		colName = IDCType.getSQLName(getName());
    		colType = null;
    		colQuote = "";
    		
			isPrimitiveType = false;
			isNumericType = false;
			needsTable=false;
			defaultValue = null;
			
			int defaultLen = 0;
    		
	    	switch(attrType) {
	    		
	    		case STRING:
	    		
					isPrimitiveType = true;
	    			defaultValue = "";
	    			defaultLen = STRING_DEFAULT_LEN;
	    			colType = "VARCHAR";
	    			attributeClass = String.class;
	    			break;
    			
	    		case TEXT:
		    		
					isPrimitiveType = true;
	    			defaultValue = "";
	    			defaultLen = 50;
	    			colType = "LONGTEXT";
	    			attributeClass = String.class;
	    			break;
    			
	    		case INTEGER:
	    			
					isPrimitiveType = true;
	    			isNumericType = true;
	    			defaultValue = new Integer(0);
	    			defaultLen = 10;
	    			colType = "INTEGER";
	    			attributeClass = Integer.class;
	    			break;

	    		case BOOLEAN:
	    			
					isPrimitiveType = true;
	    			isNumericType = true;
	    			defaultValue = new Boolean(false);
	    			defaultLen = 1;
	    			colType = "TINYINT";
	    			attributeClass = Boolean.class;
	    			break;

	    		case DATE:
	    		case DURATION:
	    		case DATETIME:

					isPrimitiveType = true;
	    			isNumericType = true;
	    			defaultValue = new Long(0);
	    			defaultLen = 20;
	    			colType = "BIGINT";
	    			attributeClass = Long.class;
	    			break;
	    			
	    		case PHONE:
				
					isPrimitiveType = true;
	    			defaultValue = "";
	    			defaultLen = 20;
	    			colType = "VARCHAR";
	    			attributeClass = String.class;
					break;
	    			
	    		case EMAIL:
		    		
					isPrimitiveType = true;
	    			defaultValue = "";
	    			defaultLen = 50;
	    			colType = "VARCHAR";
	    			attributeClass = String.class;
	    			break;
	    			
	    		case PRICE:
	    			
					isPrimitiveType = true;
	    			isNumericType = true;
	    			defaultValue = new Long(0);
	    			defaultLen = 10;
	    			colType = "BIGINT";
	    			attributeClass = Long.class;
	    			break;
	    			
				case BACKREF:

					colName = null;
					defaultLen = 50;
					needsTable = true;
					break;
	    		
				case DOMAIN:
	    			
					defaultValue = null;
	    			defaultLen = 30;
	    			colType = "INTEGER";
	    			attributeClass = Integer.class;
	    			break;
	    			
				case REF:
				case REFBOX:
				case REFTREE:
				case EXTENSION:
	    			
					defaultLen = 50;
	    			colType = "VARCHAR";
	    			attributeClass = String.class;
	    			break;
	    			
				case LIST:
				case NAMESPACE:

					colName = null;
					needsTable = true;
					defaultValue = new ArrayList<IDCDataRef>();
	    			break;
	    			
	    	}
	    	
	    	if(len == -1) {
	    		len = defaultLen;
	    	}
	    	
	    	if(colType != null) {
		    	if(colType.equals("VARCHAR")) {
		    		colQuote = "'";
		    		colType+="(" + len + ")";
		    	} else if(colType.equals("LONGTEXT")) {
		    		colQuote = "'";
		    	}
	    	}
	    	
			completeInit();

		}
		
	}

	/**************************************************************************************************/
	// Attribute methods ...
	/**************************************************************************************************/
	
	public String getColName() {
		return colName;
	}
	
	/************************************************************************************************/

	public String getColType() {
		return colType;
	}
	
	/************************************************************************************************/

	public String getColQuote() {
		return colQuote;
	}
	
	/************************************************************************************************/

	public String getWhereString(String val) {

		String ret = null;
		
		if(colName != null) {
			
			if(isDomain()) {
				IDCDomain domain = getRefDomain();
				val = "" + domain.getIndex(val);
			} else if(isRef()) {
				for(IDCReference ref : references) {
					IDCData data = ref.getDataType().requestDataByNameIgnoreCase(val);
					if(data != null ) {
						val = data.getDataRef().toString();
						break;
					}
				}
			}
			
			val = IDCUtils.convert2SQL(val);
			ret =  colName + " = " + colQuote + val + colQuote;

		}

		return ret;
		
	}
	
	/**************************************************************************************************/
	
	public boolean isNumericType() {
 
		boolean ret = false;
    	
    	switch(attrType) {
    		
    		case INTEGER:
    		case BOOLEAN:
    		case PRICE:
    			ret = true;
    			break;


    	}
    	
    	return ret;
    	
	}

	/**************************************************************************************************/

	public static boolean isTable(int attrType) {
		
		boolean ret=false;
		
		switch(attrType) {
		
			case LIST:
			case NAMESPACE:
			case BACKREF:
				ret=true;

		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public boolean isTable() {
		return isTable(attrType);
	}

	/************************************************************************************************/

	public boolean isList() {
		return attrType == LIST;
	}

	/************************************************************************************************/

	public boolean isTextType() {
		return attrType == STRING || attrType == TEXT;
	}

	/************************************************************************************************/

	public boolean isText() {
		return attrType == TEXT;
	}

	/************************************************************************************************/

	public boolean isString() {
		return attrType == STRING;
	}

	/**************************************************************************************************/

	public boolean isExtension() {
		return attrType == EXTENSION;
	}

	/**************************************************************************************************/

	public boolean isDate() {
		return attrType == DATE;
	}

	/**************************************************************************************************/

	public boolean isDomain() {
		return attrType == DOMAIN;
	}

	/**************************************************************************************************/

	public boolean isBackRef() {
		return attrType == BACKREF;
	}

	/**************************************************************************************************/

	public boolean isNameSpace() {
		return attrType == NAMESPACE;
	}

	/**************************************************************************************************/

	public boolean isLinkedForward() {
		return attrType > DOMAIN;
	}

	/**************************************************************************************************/

	public boolean isRef() {
		return attrType == REF || attrType == REFBOX || attrType == REFTREE;
	}

    /************************************************************************************************/

    public boolean isPrimitiveType() { 
    	return isPrimitiveType;
    }
    
	/**************************************************************************************************/
	
	public boolean isMandatory() {
    	return isMandatory;
	}
	
	/**************************************************************************************************/
	
	public boolean isKey() {
    	return isKey;
	}
	public boolean isDisplayEdit() {
    	return isDisplayEdit;
	}

	/**************************************************************************************************/

	public boolean isDisplayList() {
    	return isDisplayList;
	}

	/**************************************************************************************************/

	public boolean isDisplaySearch() {
    	return isDisplaySearch;
	}

	/**************************************************************************************************/

	public String getDesc() {
    	return desc;
	}

	/**************************************************************************************************/

	public String getInitValue() {
    	return initValue;
	}

	/**************************************************************************************************/
	
	public int getLength() {
    	return len;
	}

	/**************************************************************************************************/

	public int getAttributeType() {
		return attrType;
	}

	public void setAttributeType(int attrType) {
		this.attrType = attrType;
	}

	/**************************************************************************************************/

	public String getAttributeTypeName() {
		return ATTRIBUTE_TYPES[attrType];
	}
	
	/**************************************************************************************************/

	public static String getAttributeTypeName(int attrType) {
		return ATTRIBUTE_TYPES[attrType];
	}
	
	/**************************************************************************************************/

    public IDCDomain getRefDomain() {
    	return refDom;
    }
    
	public void setRefDomain(IDCDomain refDom) {
		this.refDom = refDom;
	}
	
	/**************************************************************************************************/

	public List<String> getRefDomainKeys() {
		return refDom.getKeys();
	}

	/**************************************************************************************************/

	public boolean hasFormula() {
		return hasFormula;
	}
 
	/**************************************************************************************************/

	public String getFormula() {
		return formula;
	}

	/**************************************************************************************************/

	public String getRefFormula() {
		return refFormula;
	}

	/**************************************************************************************************/

	public String getRefListFormula() {
		return refListFormula;
	}

	/**************************************************************************************************/

	public String getSystemReference() {
		return systemReference;
	}

	/**************************************************************************************************/

	public String getConstraintFormula() {
		return constraintFormula;
	}

	/**************************************************************************************************/

	public String getConstraintMessage() {
		return constraintMessage;
	}

	/**************************************************************************************************/

	public List<IDCReference> getReferences() {
		return references;
	}

    /************************************************************************************************/
	public Color getLabelColor() {
		return labelColor;
	}

	/**************************************************************************************************/
    public Object getDefaultValue() {
    	return defaultValue;
    }

	/**************************************************************************************************/

    public Object getTestValue(IDCData data) {

    	Object ret = null;
    	
    	switch(attrType) {
		
			case STRING:
			case TEXT:
				ret = getName() + new Random().nextInt(MAX_TEST_VAL);
				break;
			
			case INTEGER:
				ret = new Random().nextInt(999);
				break;
	
			case BOOLEAN:
				int val = new Random().nextInt(MAX_TEST_VAL);
				if(val == (val / 2) * 2) {
					ret = true;
				} else {
					ret = false;
				}
				break;
	
			case DATE:
			case DURATION:
			case DATETIME:
				ret = System.currentTimeMillis();
				break;
				
			case PHONE:
				ret = "+61";
				for(int i=0; i < 9; i++) {
					ret += "" + new Random().nextInt(9);
				}
				break;
				
			case EMAIL:
				ret = "email" + new Random().nextInt() + "@email.com";
				break;
				
			case PRICE:
				ret = new Random().nextInt(100000);
				break;
				
			case BACKREF:
				break;
			
			case DOMAIN:
				IDCDomain dom = getRefDomain();
				int max = dom.getDomainValues().size();
				ret = new Random().nextInt(max);
				break;
				
			case REF:
			case REFBOX:
				IDCType refType = references.get(0).refType;
				if(refType.isTestDataCreated == 0) {
					refType.getNewTestObjects();
				}
				List<IDCData> vals = refType.loadAllDataObjects();
				IDCData newData = null;
				int index = new Random().nextInt(vals.size());
				newData = vals.get(index);
				ret = new IDCDataRef(newData);
				break;
				
			case LIST:
				ret = new ArrayList<IDCData>();
				int maxObjects = getMaxTestObjects();
				if(maxObjects > 0) {
					refType = references.get(0).refType;
					if(refType.isTestDataCreated == 0) {
						refType.getNewTestObjects();
					}
					vals = refType.loadAllDataObjects();
					for(int nList=0; nList < maxObjects; nList++) {
						index = new Random().nextInt(vals.size());
						((List<IDCData>) ret).add(vals.get(index));
					}
				}				
				break;
				
			case NAMESPACE:
				ret = new ArrayList<IDCData>();
				maxObjects = getMaxTestObjects();
				if(maxObjects > 0) {
					refType = references.get(0).refType;
					if(refType.isTestDataCreated == 0) {
						refType.getNewTestObjects();
					}
					for(int nList=0; nList < maxObjects; nList++) {
						newData = refType.getNewTestObject();
						((List<IDCData>) ret).add(newData);
					}
				}
				break;
				
		}

    	return ret;
    	
    }

    /************************************************************************************************/
    
    public Class getAttributeClass() {
    	return attributeClass;
    }
    
    /************************************************************************************************/
    
    public Object getValue(Object value) {
    	return getValue(attrType, value);
    }
    
    /************************************************************************************************/
        
    public static Object getValue(int attrType, Object value) {
        	
		Object ret = null;
		
		if(isTable(attrType)) {
			ret = value;
		} else {
			
			if(value != null) {
				
				int intVal = -1;
				long longVal = -1;
				double doubleVal = -1;
				if(value.getClass() == Integer.class) {
					intVal = ((Integer) value).intValue();
					longVal = ((Integer) value).longValue();
					doubleVal = ((Integer) value).doubleValue();
				} else if(value.getClass() == Long.class) {
					intVal = ((Long) value).intValue();
					longVal = ((Long) value).longValue();
					doubleVal = ((Long) value).doubleValue();
				} else if(value.getClass() == Double.class) {
					intVal = ((Double) value).intValue();
					longVal = ((Double) value).longValue();
					doubleVal = ((Double) value).doubleValue();
				}
				
				switch(attrType) {
				
					case BOOLEAN:

						if(intVal != -1) {
							ret = new Boolean(intVal == 0 ? false : true);
						} else if(value instanceof String) {
							ret = IDCUtils.translateBoolean((String)value);
						} else if(value instanceof Boolean) {
							ret = (Boolean) value;
						} else {
							ret = false;
						}
						break;
			
					case DOMAIN:
					case INTEGER:
						ret = new Integer(intVal);
						break;
				
					case PRICE:
						ret = new Long(longVal);
						break;
				
				
					case REF:
					case REFBOX:
					case REFTREE:
						
						if(value.getClass() == IDCData.class) {
							ret = value;
						} 
						break;

					case DATE:
					case DURATION:
					case DATETIME:
						ret = new Long(longVal);
						break;

					default:
						ret = "" + value;
						break;
				
				}
			
			}

		}
		
    	
		return ret;
    	
    }

    /************************************************************************************************/
    
	public static int getAttributeType(String attrType) {
		
		int ret = -1;
		
		try {
			ret = Integer.parseInt(attrType);
		} catch(NumberFormatException ex) {
			ret = decodeAttributeType(attrType);
		}
		
		if(ret == -1) {
			ret = STRING;
		}

		return ret;

	}

    /************************************************************************************************/
    
	public static int decodeAttributeType(String attrTypeName) {
		
		int ret = -1;

		if(attrTypeName != null) {
        	for(int nType=0, maxTypes = ATTRIBUTE_TYPES.length; ret == -1 && nType < maxTypes; nType++) {
        		if(attrTypeName.equalsIgnoreCase(ATTRIBUTE_TYPES[nType])) {
        			ret = nType;
        		}
        	}
    	}
		
		return ret;

	}

    /************************************************************************************************/
    
	public void setAttributeId(int attrId) {
		this.attrId = attrId;
	}
	
    /************************************************************************************************/
    
	public int getAttributeId() {
		return attrId;
	}
	
    /************************************************************************************************/

    public List<IDCAttribute> getNamespaceTree() {
    	
    	List<IDCAttribute> ret = new ArrayList<IDCAttribute>();
    	
    	IDCApplication app = getApplication();

    	List<IDCReference> refs = getReferences();
		if(refs != null && refs.size() > 0) {
		
	    	IDCType type = refs.get(0).getDataType();
	    	
	    	boolean ok = true;
	    	while(ok) {
    		
	        	IDCAttribute attr = null;
	        	
	        	for(IDCType refType : app.getTypes()) {
	        		
	            	for(IDCAttribute refAttr : refType.getAttributes()) {
	            		
	            		if(refAttr.getAttributeType() == NAMESPACE) {
	                    	
	            			for(IDCReference ref : refAttr.getReferences()) {
	                    		
	                    		if(ref.getDataType() == type) {
	                    			attr = refAttr;
	                    			type = refType;
	                    			break;
	                    		}	                    			
	                    	}
	            		}
	            		
	            		if(attr != null) {
	            			break;
	            		}
	
	            	}
	        	
	        	}
	        	
	        	if(attr != null) {
	        		ret.add(attr);
	        	} else {
	        		ok = false;
	        	}
	        	
	    	}
		
		}
    	
		return ret;
		
    }
    
    /************************************************************************************************/

    public IDCType getDataType() {
    	return (IDCType) getParent().getParent();
    }
    
    /************************************************************************************************/

    public String getHTMLWidth() {
    	
    	int width = 0;    	
    	
		switch(getAttributeType()) {
		
			case IDCAttribute.TEXT:
				width = 300;
				break;
	
			case IDCAttribute.BACKREF:
			case IDCAttribute.LIST:
			case IDCAttribute.NAMESPACE:
				width = 800;
				break;
	
			default:
				width = 100;
				break;
	
		}
		
		if(getName().equals("Name")) {
			width = 200;
		}
		
		return "<col width='" + width + "' />";		

    }
    
    /************************************************************************************************/

    public String getSearchHTML() {
    	
    	String ret = "(not available)";
    	
		switch(getAttributeType()) {
		
			case IDCAttribute.DOMAIN:
				ret = "<select class='dropdown' name='" + getName() + "'>";
				for(String val : getRefDomainKeys()) {
		            ret += "<option value='" + val + "' >" + val + "</option>";
				}
	            ret += "</select>";
				break;

			case IDCAttribute.REFBOX:
			case IDCAttribute.REF:
       			IDCType refType = getReferences().get(0).getDataType();
				List<IDCData> refs = refType.loadAllDataObjects(refType.loadAllDataReferences(null, refType.getExplorerSQLFilter(), refType.getExplorerSQLOrderBy(), IDCType.NO_MAX_ROWS));
				
				if(getAttributeType() == IDCAttribute.REFBOX) {
					ret = "<select class='dropdown' name='" + getName() + "'>";
					for(IDCData refData : refs) {
			            ret += "<option value='" + refData.getName() + "'>" + refData.getName() + "</option>";
					}
		            ret += "</select>";
				} else if(getAttributeType() == IDCAttribute.REF) {
					String displayValue = "" + getDefaultValue(); 
					if(displayValue.length() == 0) {
						displayValue = "(no data)";
					}
					ret = "<a href='#' name='" + getName() + "' onClick='return popup(\"select.jsp?typename=" + getDataType().getName() + "&itemid=" + getId() + "&attrid=" + getAttributeId() + "\", \"Select_" +  getDataType().getName() + "\")'>" + displayValue + "</a>";
				}
				break;
	
			case IDCAttribute.REFTREE:
			case IDCAttribute.EXTENSION:
				break;
	
			case IDCAttribute.BACKREF:
			case IDCAttribute.LIST:
			case IDCAttribute.NAMESPACE:
				break;
	
			default:
				ret = "<input name='" + getName() + "' class='text' type='text' value='" + getDefaultValue() + "' maxlength='20' size='15' />";
				break;

		}
    	
    	return ret;

    }
    
    /************************************************************************************************/

    public IDCError checkData(String strValue) {
    	
    	IDCError ret = null;
    	
    	if(strValue.length() == 0) {
        	
    		if(isMandatory()) {
            	ret = new IDCError(IDCError.MANDATORYATTRIBUTE, getName() + " is mandatory", attrId, IDCError.ERROR);
            }    	
    		
        } else {
            	
        	switch(getAttributeType()) {
        	
				case INTEGER:
					try {
						Integer.parseInt(strValue);
					} catch(NumberFormatException ex) {
		            	ret = new IDCError(IDCError.INVALIDFORMAT, getName() + " must be an integer", attrId, IDCError.ERROR);
					}
					
					break;
			
				case PRICE:
					try {
    					if(strValue.length() > 0) {
    						Double.parseDouble(strValue);
    					}
					} catch(NumberFormatException ex) {
		            	ret = new IDCError(IDCError.INVALIDFORMAT, getName() + " must be numeric", attrId, IDCError.ERROR);
					}
					break;

				case IDCAttribute.PHONE:
            		
					String num = IDCUtils.replaceAll(strValue, " ", "");
					if(num.length() != 12 || !strValue.startsWith("+")) { 
            			ret = new IDCError(IDCError.INVALIDFORMAT, getDesc() + " must be +ccnnnnnnnnnn where cc is country code and n..n is a 9 digit phone number ...", attrId);
					}
            			
            		break;
            		
				case IDCAttribute.EMAIL:
            		
					if(strValue.indexOf('@') == -1) {
            			ret = new IDCError(IDCError.INVALIDFORMAT, getDesc() + " must be a valid email address ...", attrId);
					}
            			
            		break;
            		
            	default:
        			break;

        	}
        	
        }
    	
        return ret;
    	
    }
    
	/************************************************************************************************/

	public boolean isAttribute() {
		return true;
	}

}