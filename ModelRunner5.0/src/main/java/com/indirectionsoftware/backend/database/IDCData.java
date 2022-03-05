package com.indirectionsoftware.backend.database;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.indirectionsoftware.metamodel.IDCAction;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCDomain;
import com.indirectionsoftware.metamodel.IDCDomainValue;
import com.indirectionsoftware.metamodel.IDCModelData;
import com.indirectionsoftware.metamodel.IDCReference;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCEnabled;
import com.indirectionsoftware.runtime.IDCError;
import com.indirectionsoftware.runtime.IDCEvalData;
import com.indirectionsoftware.runtime.IDCFormula;
import com.indirectionsoftware.runtime.IDCFormulaContext;
import com.indirectionsoftware.runtime.IDCRequest;
import com.indirectionsoftware.runtime.IDCWorkflowInstanceData;
import com.indirectionsoftware.runtime.nlu.IDCDataValue;
import com.indirectionsoftware.runtime.webapp.IDCWebAppSettings;
import com.indirectionsoftware.utils.IDCCalendar;
import com.indirectionsoftware.utils.IDCTriplet;
import com.indirectionsoftware.utils.IDCUtils;
import com.indirectionsoftware.utils.IDCVector;

public class IDCData extends IDCVector implements IDCEnabled {
	
	private IDCType			 type;
	private List<Object> 	 cachedValues;
	private long 			 lastUpdateTime;
	private String 			 lastUpdateUser;
	
	private IDCDataParentRef namespaceParentRef, extentionParentRef, systemExtentionParentRef;
	private IDCData namespaceParentData, extentionParentData, systemExtentionParentData;
	
	private IDCData systemReferenceData = null;
	
	private boolean isCached = false, isNew=true;
	
	private CacheInitValue initVal = new CacheInitValue();
	
	public static final String URLPREFIX = "IDCDREF:";
	
	private long tempId=0;
	
	public long tempVersion = 0;
	
	IDCData context;
	
	IDCSystemUser user;
	
	boolean isSaving = false;
	
	/************************************************************************************************/

    public static final int  NA=-1;
	public static final String NOSELECTION = "(no selection)";
    
    public static final int  SEARCH_EQUALS=0, SEARCH_STARTS_WITH=1, SEARCH_CONTAINS=2;

    /************************************************************************************************/

    public IDCData(IDCType type, boolean isInit) {
    	super(type.getEntityId(), NA, null);
    	this.type = type;
    	setValues(initValues(isInit));
    	tempId = System.currentTimeMillis();
    	isNew=true;
    }	
    	
    /************************************************************************************************/

    public IDCSystemUser getUser() { 
    	
    	if(user == null) {
    		user = type.getApplication().getUser(); 
    	}
    	
    	return user;
    	
    }

    /************************************************************************************************/

    public long getId() { 
    	
    	long ret = id;
    
    	if(isNew) {
    		ret = tempId; 
		}
	
    	return ret;
    }

    /************************************************************************************************/

    public long getTempId() { 
    	return tempId;
    }

    /************************************************************************************************/

    public void resetId() { 
    	isNew=true;
    }

    /************************************************************************************************/

    public void setId(long id) { 
    	if(id != NA) {
        	isNew=false;
    	}
    	this.id = id;
    }

    /************************************************************************************************/

    public boolean isNew() { 
    	return isNew;
    }

    /************************************************************************************************/

    public void setCacheMode(boolean isCached) {
    	this.isCached = isCached;
    }	
    	
    /************************************************************************************************/

    public void copy(IDCData orig) {
    	
    	super.copy(orig);
    	setType(orig.type);
    	setNamespaceParentRef(orig.namespaceParentRef);
    	copyValues(orig.getValues());
    	
    }	
    	
    /************************************************************************************************/

    public IDCData clone() {
    	
    	IDCData ret = new IDCData(type, false);
    	
    	setNamespaceParentRef(namespaceParentRef);
    	ret.copyValues(getValues());
    	
    	return ret;
    	
    }	
    	
    /************************************************************************************************/

    public boolean equals(IDCData data) {
    	
    	boolean ret = false;
    	
    	if(data != null) {
    		if(this.type == data.getDataType() && this.getId() == data.getId()) {
        		ret = true;
    		}
    	}
    	
    	return ret;
    	
    }	
    	
    /************************************************************************************************/

    public boolean matches(IDCData data, String[] attrNames) {
    	
    	boolean ret = true;
    	
    	if(data != null) {
			IDCType type = data.getDataType();
    		for(String attrName : attrNames) {
    			IDCAttribute attr = data.getDataType().getAttribute(attrName);
    			if(attr.isDate()) {
        			long oldDate = getLong(attrName);
        			long newDate =data. getLong(attrName);
        			if(!type.getApplication().getCalendar().isSameDay(oldDate,  newDate)) {
        				ret = false;
        				break;
        			}
    			} else {
        			Object oldVal = getValue(attrName);
        			Object newVal = data.getValue(attrName);
        			if(oldVal == null && newVal != null || oldVal != null && newVal == null || !oldVal.equals(newVal)) {
        				ret = false;
        				break;
        			}
    			}
    		}
    	}
    	
    	return ret;
    	
    }	
    	
    /************************************************************************************************/

    public IDCData createData(String typeName) {return type.getApplication().createData(typeName);}
    
    public IDCApplication getApplication() {return type.getApplication();}
    
    public IDCType getDataType() {return type;}
    
    public IDCDataParentRef getNamespaceParentRef() { return namespaceParentRef;}
    
    public IDCDataParentRef getExtentionParentRef() { return extentionParentRef;}
    
    public IDCDataParentRef getSystemExtentionParentRef() { return systemExtentionParentRef;}
    
    public long getLastUpdateTime() { return lastUpdateTime;}
    
    public String getLastUpdateUser() { return lastUpdateUser;}
    
    /************************************************************************************************/

    public void setType(IDCType type) { 
    	this.type = type;
    	setType(type.getEntityId());
	}
    
    public void setNamespaceParentRef(IDCDataParentRef namespaceRef) { this.namespaceParentRef = namespaceRef;}
    
	public void setNamespaceParentRef(IDCData namespaceParentData, int nsAttrId) {
    	this.namespaceParentData = namespaceParentData;
    	this.namespaceParentRef = namespaceParentData.getAsParentRef(nsAttrId);
	}

    public void setExtensionParentRef(IDCDataParentRef superRef) { this.extentionParentRef = superRef;}
    
	public void setExtensionParentRef(IDCData extParentData, int extAttrId) {
    	this.extentionParentData = extParentData;
    	this.extentionParentRef = extParentData.getAsParentRef(extAttrId);
	}

    public void setSystemExtensionParentRef(IDCDataParentRef superRef) { this.systemExtentionParentRef = superRef;}
    
	public void setSystemExtensionParentRef(IDCData extParentData, int extAttrId) {
    	this.systemExtentionParentData = extParentData;
    	this.systemExtentionParentRef = extParentData.getAsParentRef(extAttrId);
	}

	public void setLastUpdateTime(long lastUpdateTime) {this.lastUpdateTime = lastUpdateTime;} 

	public void setLastUpdateUser(String lastUpdateUser) { this.lastUpdateUser = lastUpdateUser;}

    /************************************************************************************************/

    public boolean isRootNamespace() { return namespaceParentRef == null ? true : false;}
    
    /************************************************************************************************/

    private List<Object> initValues(boolean isInit) {
    	
    	List<Object> ret = new ArrayList<Object>();
    	cachedValues = new ArrayList<Object>();
    	
    	for(IDCAttribute attr : type.getAttributes()) {
    		Object value = attr.getDefaultValue();
    		ret.add(value);
    		cachedValues.add(initVal);
    	}
    	
    	if(isInit) {
        	for(IDCAttribute attr : type.getAttributes()) {
        		String initValue = attr.getInitValue();
        		if(initValue != null) {
        			Object value = evaluate(initValue, attr);
        			ret.set(attr.getAttributeId(),formatValue(attr,value));
            		cachedValues.set(attr.getAttributeId(),value);
        		}
        	}
    	}
    	
    	return ret;
    	
    }
    
    /************************************************************************************************/

    public void copyValues(List<Object> vals) {
    	
    	if(vals != null) {
    		int nVal=0;
    		for(Object val : vals) {
            	setValue(nVal++, val);
    		}
    	}
    
    }
    
    /************************************************************************************************/
    
    public void set(int ind, Object value) {
    	set(ind, value, true);
    }
    
    /************************************************************************************************/
    
    public void set(int ind, Object value, boolean reeval) {
    	
		IDCAttribute attr = type.getAttribute(ind);
		
		if(!attr.hasFormula()) {
			
			Object newVal = formatValue(attr, value);

	    	if(newVal == null) {
	    		newVal = attr.getDefaultValue();
	    	}
	    	
	    	setValue(ind, newVal);
	    	cachedValues.set(ind, newVal);
	    	
	    	if(reeval) {
	        	reealuateFormulas(ind);
	    	}
	    
		} else {
			if(reeval) {
				System.err.println("Ignoring set value for attribute with formula: data = " + this.getName() + " / attribute = " + attr.getName());
			}
		}
		
    }
    
    /************************************************************************************************/
    
    public Object formatValue(IDCAttribute attr, Object value) {
    	
    	Object ret = null;
    	
		if(value != null) {
			
			switch(attr.getAttributeType()) {
	    		
					case IDCAttribute.STRING:
					case IDCAttribute.EMAIL:
					case IDCAttribute.PHONE:
						ret = value.toString();
						if(((String)ret).length() > attr.getLength()) {
							ret = value.toString().substring(0, attr.getLength()-1);
						}
						break;
						
					case IDCAttribute.TEXT:
						ret = (String)value;
						break;
						
	    			case IDCAttribute.INTEGER:
	    				
	    				if(value.getClass() == Integer.class) {
	    					ret = value;
	    				} else if(value.getClass() == Long.class) {
	    					ret = ((Long)value).intValue();
	    				} else if(value.getClass() == String.class) {
	    					if(((String)value).length() > 0) {
		    					ret = Integer.parseInt((String)value);
	    					} else {
	    						ret = 0;
	    					}
	    				}
	    				break;
	    		
	    			case IDCAttribute.PRICE:
	    				
	    				if(value.getClass() == Long.class) {
	    					ret = value;
	    				} else if(value.getClass() == String.class) {
	    					if(((String)value).length() > 0) {
								ret = Math.round(Double.parseDouble((String)value) * 100);
	    					} else {
	    						ret = new Long(0);
	    					}
	    				}
	    				
	    				break;

	    			case IDCAttribute.BOOLEAN:
	    				
	    				if(value.getClass() == Boolean.class) {
	    					ret = value;
	    				} else if(value.getClass() == String.class) {
	    					ret = new Boolean((String)value);
	    				}
	    				break;
			
	    			case IDCAttribute.DATE:

	    				if(value.getClass() == Long.class) {
	    					ret = value;
	    				} else if(value.getClass() == Integer.class) {
	    					ret = ((Integer)value).longValue();
	    				} else if(value.getClass() == Double.class) {
	    					ret = ((Double)value).longValue();
	    				} else if(value.getClass() == String.class) {
	    					if(((String)value).length() > 0) {
	    						long date = type.getApplication().getCalendar().getDate((String)value);
	    						if(date == -1) {
		    						ret = Long.parseLong((String)value);
	    						} else {
	    							ret = date;
	    						}
	    					} else {
	    						ret = new Long(0);
	    					}
	    				} else if(value.getClass() == Date.class) {
	    					ret = ((Date)value).getTime();
	    				}
	    				break;

	    			case IDCAttribute.DURATION:
	    			case IDCAttribute.DATETIME:

	    				if(value.getClass() == Long.class) {
	    					ret = value;
	    				} else if(value.getClass() == Integer.class) {
	    					ret = ((Integer)value).longValue();
	    				} else if(value.getClass() == Double.class) {
	    					ret = ((Double)value).longValue();
	    				} else if(value.getClass() == String.class) {
	    					if(((String)value).length() > 0) {
	    						long date = type.getApplication().getCalendar().getDate((String)value);
	    						if(date == -1) {
		    						ret = Long.parseLong((String)value);
	    						} else {
	    							ret = date;
	    						}
	    					} else {
	    						ret = new Long(0);
	    					}
	    				} else if(value.getClass() == Date.class) {
	    					ret = ((Date)value).getTime();
	    				}
	    				break;

	    			case IDCAttribute.DOMAIN:
	    				
	    				if(value.getClass() == IDCDomainValue.class) {
	    					ret = value;
	    				} else {
		    				IDCDomain domain = attr.getRefDomain();
		    				int indVal = -1;
		    				if(value.getClass() == Integer.class) {
		    					indVal = (Integer) value;
		    				} else if(value.getClass() == Long.class) {
		    					indVal = ((Long)value).intValue();
		    				} else if(value.getClass() == Double.class) {
		    					indVal = ((Double)value).intValue();
		    				} else if(value.getClass() == String.class) {
		    					indVal = domain.getIndex((String) value);
		    				}
		    				ret = (IDCDomainValue) domain.getDomainValue(indVal);
	    				}
	    				break;
	    				
				
	    			case IDCAttribute.REF:
					case IDCAttribute.REFBOX:
					case IDCAttribute.REFTREE:
						
						if(value instanceof String) {
							
							IDCType refedType = attr.getReferences().get(0).getDataType();
							if(refedType != null) {
								IDCData refedData = refedType.requestDataByNameIgnoreCase((String) value);
								if(refedData != null) {
									value = refedData;
								}
							}
	    				}
	    				
    					ret = getDataRef(value);
	    				break;

	    			case IDCAttribute.EXTENSION:

	    				if(value instanceof IDCData) {
	    					if(((IDCData)value).isNew) {
	        					ret = value;
	    					} else {
	        					ret = getDataRef(value);
	    					}
	    				} else if(value instanceof IDCDataRef) {
	    					ret = value;
	    				}
	    				break;

	    			case IDCAttribute.LIST:
	    				clearListReference(attr);
	    				if(value instanceof IDCData) {
		    				insertListReference(attr, (IDCData)value);
	    				} else {
		    				insertAllListReferences(attr, IDCDataRef.getRefList((List<IDCData>)value));
	    				}
	    				break;

	    			case IDCAttribute.NAMESPACE:
	    				if(value instanceof IDCData) {
	    					updateNamespace(attr, (IDCData)value);
	    				} else {
		    				updateAllNamespaceReferences(attr, IDCDataRef.getRefList((List<IDCData>)value));
	    				}
	    				break;
	    				
    				default:
    					ret = value;
    					break;

			}
			
		}

    	if(ret == null) {
    		ret = attr.getDefaultValue();
    	}
    	
    	return ret;
    	
    }
    
	/************************************************************************************************/
    
    private void reealuateFormulas(int ind) {
    	
    	int i=0;
    	for(IDCAttribute attr : type.getAttributes()) {
    		
    		if(ind != i && attr.hasFormula()) {
        		String formula = attr.getFormula();
    			Object value = evaluate(formula, attr);
        		set(attr.getAttributeId(), value, false);
    		}
    		i++;
    		
    	}
    	
	}

	/************************************************************************************************/
    
    private static IDCDataRef getDataRef(Object value) {
    	
    	IDCDataRef ret = null;
    	
    	if(value instanceof List && ((List)value).size() == 1) {
			value = ((List)value).get(0);
		}
    	
		if(value instanceof IDCDataRef) {
			ret = (IDCDataRef)value;
		} else if(value instanceof IDCData) {
			ret = new IDCDataRef((IDCData) value);
		}  
    	
    	return ret;
    	
    }
    
    /************************************************************************************************/
    
	public void set(String attrName, Object value) {
		
		IDCAttribute attr = type.getAttribute(attrName);
		
		if(attr != null) {
			set(attr.getAttributeId(), value);
		}
		
	}
	
    /************************************************************************************************/
    
	public void set(IDCAttribute attr, Object value) {
		
		if(attr != null) {
			set(attr.getAttributeId(), value);
		}
		
	}
	
	/************************************************************************************************/

    private void updateNamespace(IDCAttribute attr, IDCData data) {
    	data.getDataType().updateNamespace(data.getId(), getAsParentRef(attr.getAttributeId()));
	}

	/************************************************************************************************/

    private void updateAllNamespaceReferences(IDCAttribute attr, List<IDCDataRef> refs) {
    	getDataType().updateAllNamespaceRefs(this, attr, refs);
	}

	/************************************************************************************************/

    private void insertAllListReferences(IDCAttribute attr, List<IDCDataRef> refs) {
    	getDataType().insertAllListReferences(this, attr, refs);
	}

	/************************************************************************************************/

    private void insertListReference(IDCAttribute attr, IDCData data) {
    	getDataType().insertListReference(this, attr, data);
	}

	/************************************************************************************************/

    private void clearListReference(IDCAttribute attr) {
    	getDataType().removeDataListFromParent(getId(), attr);
	}

	/************************************************************************************************/

	public void insertReference(IDCAttribute attr, IDCData value) {
		
		if(attr.getAttributeType() == IDCAttribute.NAMESPACE) {
			updateNamespace(attr, value);
		} else {
			insertListReference(attr, value);
		}
	
    }

	/************************************************************************************************/

	public void removeReference(IDCAttribute attr, IDCData value) {
		
		if(attr.getAttributeType() == IDCAttribute.NAMESPACE) {
			value.delete(true);
		} else {
			removeListReference(attr, value);
		}
	
    }

	/************************************************************************************************/

    private void removeListReference(IDCAttribute attr, IDCData data) {
    	getDataType().removeListReference(this, attr, data);
	}

	/************************************************************************************************/
    
    public Object getValue(int attrId) {
    	
    	IDCUtils.traceStart("getValue()");

    	Object ret = cachedValues.get(attrId);
		
		if(!isCached || ret == initVal) {

			IDCAttribute attr = type.getAttribute(attrId);
			
			if(attr.hasFormula() && !isEditable(attr)) {
				ret = evaluate(attr.getFormula());
			} else {

				Object value = getRawValue(attrId);
				
				ret = value;
		    	
				switch(attr.getAttributeType()) {
	    		
					case IDCAttribute.BACKREF:
						
	    				IDCReference ref = attr.getReferences().get(0);
						IDCType refType = ref.getDataType();
						IDCAttribute refAttr = ref.getAttribute();
						
						switch(refAttr.getAttributeType()) {
						
							case IDCAttribute.REF:
							case IDCAttribute.REFBOX:
							case IDCAttribute.REFTREE:
				    			ret = type.loadAllDataObjects((List<IDCDataRef>)ret, attr.getRefFormula());
								break;
						
							case IDCAttribute.LIST:
								ret = type.loadAllDataObjects((List<IDCDataRef>)ret, attr.getRefFormula());
								break;
						
							case IDCAttribute.NAMESPACE:
								if(ret != null) {
									ret = refType.loadDataObject(((IDCDataRef)ret).getItemId());
								}
								break;
						
						}
						
						break;
		

					/*********************************	
					case IDCAttribute.DOMAIN:
						
		    			if(value != null) {
		    				int nDomVal = ((Integer) value).intValue();
		    				if(nDomVal != -1) {
			    				ret = attr.getRefDomain().getDomainValue(nDomVal).getKey();
		    				} else {
		    					ret = null;
		    				}
		    			}
						break;
					 *************************************/					
				
					case IDCAttribute.REF:
					case IDCAttribute.REFBOX:
					case IDCAttribute.REFTREE:
						
						if(value != null) {
							if(value instanceof IDCData) {
								ret = value;
							} else {
								List<IDCReference> refs = attr.getReferences();
								if(refs.size() == 1) {
				    				ref = refs.get(0);
									refType = ref.getDataType();
									ret = refType.loadDataRef((IDCDataRef)value);
								} else {
									int typeId = ((IDCDataRef)value).getTypeId();
									refType= getApplication().getType(typeId);
									if(refType != null) {
										ret = refType.loadDataRef((IDCDataRef)value);
									}
								}
							}
						}
						break;
		
					case IDCAttribute.EXTENSION:
						
						if(value != null) {
							if(value instanceof IDCData) {
								ret = value;
							} else {
								ret = type.loadDataRef((IDCDataRef)value, false);
							}
						}
						break;
		
					case IDCAttribute.LIST:
					case IDCAttribute.NAMESPACE:
						
						if(!((List<IDCDataRef>) value).isEmpty() && ((List<IDCDataRef>) value) instanceof IDCData) {
							ret = value;
						} else {
							ret = type.loadAllDataObjects((List<IDCDataRef>) value);
						}

						break;
						
				}

			}
			
			cachedValues.set(attrId, ret);
		
		}
    	
    	IDCUtils.traceEnd("getValue()");

    	return ret;
    	
    }
    
	/************************************************************************************************/

    private Object getValue(String attrName) {
		
		Object ret = null;
		
		IDCAttribute attr = type.getAttribute(attrName);
		
		if(attr != null) {
			ret = getValue(attr.getAttributeId());
		}
		
		return ret;
		
	}

    /************************************************************************************************/

    public Object getValue(IDCAttribute attr) {
		
		Object ret = getValue(attr.getAttributeId());
		
		return ret;
		
	}

    /************************************************************************************************/

    public String getDisplayValue(IDCAttribute attr) {
		
    	String ret = getDisplayValue(attr.getAttributeId());
		
		return ret;
		
	}

    /************************************************************************************************/
    
    public String getDisplayValue(int attrId) {
		return getDisplayValue(attrId, false);
	}
    	
	/************************************************************************************************/

	public String getDisplayValue(int attrId, boolean isUpdate) {
	
    	IDCUtils.traceStart("getDisplayValue()");

		String ret = "";
    	
		IDCAttribute attr = type.getAttribute(attrId);
		
		int attrType = attr.getAttributeType();
		
		if(attrType == IDCAttribute.EXTENSION) {
			Object val = getRawValue(attrId);
			if(val != null) {
				if(val instanceof IDCDataRef) {
					IDCDataRef ref =  (IDCDataRef) val;
					if (ref != null && ref.getItemId() != -1) {
						ret = type.getApplication().getType(ref.getTypeId()).getDisplayName();
					}
				} else {
					ret = ((IDCData) val).getDataType().getDisplayName();
				}
			}

		} else {
			
			Object value = getValue(attrId);

			if (value == null) {
				ret = "";
			} else {
				if (value instanceof IDCData ) {
					ret = ((IDCData) value).getName();
				} else if (value instanceof List) {
					ret = IDCData.getNamesString((List) value);
				} else {
					
					switch(attrType) {
					
						case IDCAttribute.DOMAIN:
							
			    			if(value != null) {
			    				if(value instanceof Integer) {
				    				int nDomVal = ((Integer) value).intValue();
				    				if(nDomVal != -1) {
					    				ret = attr.getRefDomain().getDomainValue(nDomVal).getKey();
				    				} else {
				    					ret = null;
				    				}
			    				} else {
				    				ret = ((IDCDomainValue) value).getKey();
			    				}
			    			}
							break;

						case IDCAttribute.DATE:
							ret = type.getApplication().getCalendar().displayDateShort((Long)value);
							break;
							
						case IDCAttribute.DATETIME:
							ret = type.getApplication().getCalendar().displayTimeDateShort((Long)value);
							break;
							
						case IDCAttribute.DURATION:
							ret = IDCCalendar.getDaysHoursMinutesString((Long)value);
							break;
							
						case IDCAttribute.PRICE:
							ret = IDCUtils.getAmountString((Long)value, isUpdate);
							break;
							
						default:
							ret = value.toString();
							break;
						
					}

				}
			}
		}

    	IDCUtils.traceEnd("getDisplayValue()");

    	return ret;
    	
    }
    
    /************************************************************************************************/
    
    public String getDisplayValue(String attrName) {
		return getDisplayValue(attrName, false);
	}
    	
	/************************************************************************************************/

    public String getDisplayValue(String attrName, boolean isUpdate) {
		
		String ret = "";
		
		IDCAttribute attr = type.getAttribute(attrName);
		
		if(attr != null) {
			ret = getDisplayValue(attr.getAttributeId(), isUpdate);
		}
		
		return ret;
		
	}

	/************************************************************************************************/
    
    public Object getRawValue(int attrId) {
    	
    	IDCUtils.traceStart("getRawValue()");
    	
    	Object ret = super.getValue(attrId); 
    	
		IDCAttribute attr = type.getAttribute(attrId);
		switch(attr.getAttributeType()) {
		
			case IDCAttribute.LIST:
				ret = type.loadDataListReferences(getId(), attr);
				break;
	
			case IDCAttribute.NAMESPACE:
				ret = type.loadNamespaceReferences(getId(), attr);
				break;
				
			case IDCAttribute.BACKREF:
				
				IDCReference ref = attr.getReferences().get(0);
				IDCType refType = ref.getDataType();
				IDCAttribute refAttr = ref.getAttribute();
				
				switch(refAttr.getAttributeType()) {
				
					case IDCAttribute.REF:
					case IDCAttribute.REFBOX:
					case IDCAttribute.REFTREE:
		    			
		    			IDCDataRef parentRef = new IDCDataRef(type.getEntityId(), getId()); 
		    			if(extentionParentRef != null) {
		    				parentRef = new IDCDataRef(extentionParentRef.getTypeId(), extentionParentRef.getItemId());
		    			}
		    			
		    			// MIGHT NEED TO DO SOMETHING ABOUT SYSTEM EXTENTION !!!!!!

						String where = refAttr.getColName() + " = '" + parentRef + "'";
						ret = refType.loadAllDataReferences(null, where, null, IDCType.NO_MAX_ROWS);
						break;
				
					case IDCAttribute.LIST:
						
						ret = type.loadDataBackListReferences(getId(), attr);
						break;
				
					case IDCAttribute.NAMESPACE:
						if(namespaceParentRef != null && namespaceParentRef.isParentNamespace(refType, refAttr)) {
					    	ret = new IDCDataRef(refType.getEntityId(), namespaceParentRef.getItemId());
						}
						break;
				
				}
				
				break;




		}

    	IDCUtils.traceEnd("getRawValue()");
    	
		return ret;
		
    }
    
	/************************************************************************************************/

    public Object getRawValue(String attrName) {
		
		Object ret = null;
		
		IDCAttribute attr = type.getAttribute(attrName);
		
		if(attr != null) {
			ret = getRawValue(attr.getAttributeId());
		}
		
		return ret;
		
	}

    /************************************************************************************************/

    public Object getRawValue(IDCAttribute attr) {
		
		Object ret = getRawValue(attr.getAttributeId());
		
		return ret;
		
	}

    /************************************************************************************************/
	
    public static String getNamesString(List<IDCData> values) {
    	
    	String ret = "";
    	
       	for(int nValue=0, maxValues=values.size(); nValue<maxValues; nValue++) {
    		ret += values.get(nValue).getName() + " ";	
    	}
		
		return  ret;	
		
	}	
    
   /************************************************************************************************/

	public String getName() {

    	IDCUtils.traceStart("getName()");

		String ret = null; 

		String nameFormula = type.getNameFormula();
    	if(nameFormula != null) {
    		Object val = evaluate(nameFormula);
    		if(val != null) {
        		ret = "" + val;
    		}
    	} else {
    		ret = getString("Name");
    	}
		
		if(ret == null || ret.length() == 0) {
			ret = "(no name defined)";
		}

    	IDCUtils.traceEnd("getName()");

		return "" + ret;
		
	}

	/************************************************************************************************/

    public IDCEvalData getEvalData(String exprStr, IDCData ref) {

    	IDCEvalData ret = null;
    	
    	IDCFormulaContext context = new IDCFormulaContext(this, ref, getUser());
		IDCFormula form = IDCFormula.getFormula(exprStr);
		ret = form.evaluate(context);
    
		return ret;
    
    }
    
    /************************************************************************************************/

    public void applyUpdates(IDCWorkflowInstanceData workflowInstance, String exprStr, String fileContent, boolean isSave) {
        
    	IDCFormulaContext context = new IDCFormulaContext(workflowInstance, this, null, getUser(), isSave);
    	if(fileContent != null) {
    		context.setFileContent(fileContent);
    	}
		List<IDCFormula> formList = IDCFormula.getFormulas(exprStr);
		if(formList != null) {
			for(IDCFormula form : formList) {
				form.evaluate(context);
			}
		}

    }
    
    /************************************************************************************************/

    public IDCEvalData getEvalData(String expr) {
		return getEvalData(expr, null);
    }

    /************************************************************************************************/

    public Object evaluate(String expr, IDCData ref) {
    	
    	IDCUtils.traceStart("evaluate(expr=" + expr+")");
    	
    	Object ret = null;
    	IDCEvalData evalData = getEvalData(expr, ref);
		if(evalData != null) {
			ret = evalData.getValue();
		}
    	
    	IDCUtils.traceEnd("evaluate()");

    	return ret;
    }

    /************************************************************************************************/

    public Object evaluate(String expr) {
		return evaluate(expr, (IDCData) null);
    }

    /************************************************************************************************/

    public Object evaluate(String expr, IDCAttribute attr) {
    	return attr.getValue(evaluate(expr));
    }

    /************************************************************************************************/

    public boolean isTrue(String expr, IDCData ref) {

    	boolean ret = false;

		Object condValue = evaluate(expr, ref);
		if(condValue instanceof Boolean) {
			ret = ((Boolean) condValue).booleanValue();
		} else {
			Long longValue = (Long) condValue;
			if(longValue == 0) {
	            ret = false;
			} else {
				ret = true;
			}
		}
    
    	return ret;
    
    }

    public boolean isTrue(String expr) {
    	return isTrue(expr, null);
    }

    /************************************************************************************************/

    public String getSQLString(IDCAttribute attr) {
    	
    	String ret = "";
    	
    	int attrId = attr.getAttributeId();
    	
    	Object value = super.getValue(attrId);
    		
		switch(attr.getAttributeType()) {
		
    		case IDCAttribute.BOOLEAN:
    	    	if(value != null) {
	        		if(((Boolean)value).booleanValue()) {
	        			ret = "1";
	        		} else {
	        			ret = "0";
	        		}
    	    	}
    	    	break;
			
    		case IDCAttribute.DOMAIN:
    			if(value != null) {
        			ret += ((IDCDomainValue)value).getIndex();
    			} else {
    				ret += -1;
    			}
    			break;

    		default:
    	    	if(value != null) {
    	    		ret += value;
    	    	}
    			break;

		}

    	ret = IDCUtils.convert2SQL(ret);
    		
    	return ret;

    }
    
    /************************************************************************************************/

    public String getString(int attrId) {
    	
    	String ret = "";
    	
    	Object o = getValue(attrId);
    	if(o != null) {
    		ret += o;
		}
    		
    	return ret;

    }
    
    public String getString(String attrName) {
    	
    	String ret = "";
    	
    	Object o = getValue(attrName);
    	if(o != null) {
    		ret += o;
		}
    		
    	return ret;

    }
    
    /************************************************************************************************/

    public String getDateString(int attrId) {
    	
    	String ret = "";
    	
    	Object o = getValue(attrId);
    	if(o != null) {
    		if(o instanceof Long) {
    			ret = IDCCalendar.displayDateShortStatic((Long)o);
    		}
		}
    		
    	return ret;

    }
    
    public String getDateString(String attrName) {
    	
    	String ret = "";
    	
    	Object o = getValue(attrName);
    	if(o != null) {
			ret = IDCCalendar.displayDateShortStatic((Long)o);
		}
    		
    	return ret;

    }
    
    /************************************************************************************************/

    public byte getByte(int attrId) {
    	
    	byte ret = NA;
    	
    	Object o = getValue(attrId);
    	if(o != null) {
    		ret = ((Byte) o).byteValue();
		}
    		
    	return ret;

    }
    
    public byte getByte(String attrName) {
    	
    	byte ret = NA;
    	
    	Object o = getValue(attrName);
    	if(o != null) {
    		ret = ((Byte) o).byteValue();
		}
    		
    	return ret;

    }
    
    /************************************************************************************************/

    public int getInt(int attrId) {
    	
    	int ret = NA;
    	
    	Object o = getValue(attrId);
    	if(o != null) {
    		if(o instanceof IDCDomainValue) {
    			ret = ((IDCDomainValue) o).getIndex();
    		} else if(o instanceof Integer) {
    			ret = ((Integer) o).intValue();
    		}
		}
    		
    	return ret;

    }
    
    public int getInt(String attrName) {
    	
    	int ret = NA;
    	
    	Object o = getValue(attrName);
    	if(o != null) {
    		if(o instanceof IDCDomainValue) {
    			o = ((IDCDomainValue) o).getIndex();
    		} else if(o instanceof Integer) {
    			ret = ((Integer) o).intValue();
    		} else if(o instanceof Long) {
    			ret = ((Long) o).intValue();
    		}
    	}
    		
    	return ret;

    }
    
    /************************************************************************************************/

    public long getLong(int attrId) {
    	
    	long ret = NA;
    	
    	Object o = getValue(attrId);
    	if(o != null) {
    		ret = ((Long) o).longValue();
		}
    		
    	return ret;

    }
    
    public long getLong(String attrName) {
    	
    	long ret = NA;
    	
    	Object o = getValue(attrName);
    	if(o != null) {
    		ret = ((Long) o).longValue();
		}
    		
    	return ret;

    }
    
    /************************************************************************************************/

    public double getDouble(int attrId) {
    	
    	double ret = NA;
    	
    	Object o = getValue(attrId);
    	if(o != null) {
    		ret = ((Double) o).doubleValue();
		}
    		
    	return ret;

    }
    
    public double getDouble(String attrName) {
    	
    	double ret = NA;
    	
    	Object o = getValue(attrName);
    	if(o != null) {
    		ret = ((Double) o).doubleValue();
		}
    		
    	return ret;

    }
    
    /************************************************************************************************/

    public boolean getBoolean(int attrId) {
    	
    	boolean ret = false;
    	
    	Object o = getValue(attrId);
    	if(o != null) {
    		ret = ((Boolean) o).booleanValue();
		}
    		
    	return ret;

    }
    
    public boolean getBoolean(String attrName) {
    	
    	boolean ret = false;
    	
    	Object o = getValue(attrName);
    	if(o != null) {
    		ret = (Boolean) o;
		}
    		
    	return ret;

    }
    
    /************************************************************************************************/

    public List<IDCData> getList(int attrId) {
    	return getList(attrId, null);
	}
    
    public List<IDCData> getList(int attrId, String formula) {
    	
    	List<IDCData> ret = (List<IDCData>) getValue(attrId);
    	
    	return ret;

    }
    
    public List<IDCData> getList(String attrName) {
    	return getList(attrName, null);
	}
    
    public List<IDCData> getList(String attrName, String formula) {
    	
    	List<IDCData> ret = (List<IDCData>) getValue(attrName);
    	
    	return ret;

    }
    
    /************************************************************************************************/

    public List<IDCDataRef> getRefList(int attrId) {
    	return (List<IDCDataRef>) getRawValue(attrId);
    }
    
    public List<IDCDataRef> getRefList(String attrName) {
    	return (List<IDCDataRef>) getRawValue(attrName);
    }
    
    /************************************************************************************************/

	public IDCDataRef getDataRef(int attrId) {
		return (IDCDataRef) super.getValue(attrId);
	}
    
	public IDCDataRef getDataRef(String attrName) {
		
		IDCDataRef mref = null;
		
		String mrefStr = getString(attrName);

		if(mrefStr != null && mrefStr.length() > 0) {
			mref = IDCDataRef.getRef(mrefStr);
		}
		
		return mref;
		
	}
    
    /************************************************************************************************/

	public IDCData getData(int attrId) {
		
		IDCData ret = null;
		
    	Object o = getValue(attrId);
    	if(o != null) {
    		ret = (IDCData) o;
		}
    		
		return ret;
		
	}
    
	public IDCData getData(String attrName) {
		
		IDCData ret = null;
		
    	Object o = getValue(attrName);
    	if(o != null) {
    		ret = (IDCData) o;
		}
    		
		return ret;
		
	}
    
    /************************************************************************************************/

	public IDCDomainValue getDomainValue(int attrId) {
		
		IDCDomainValue ret = null;
		
    	Object o = getValue(attrId);
    	if(o != null) {
    		ret = (IDCDomainValue) o;
		}
    		
		return ret;
		
	}
    
	public IDCDomainValue getDomainValue(String attrName) {
		
		IDCDomainValue ret = null;
		
    	Object o = getValue(attrName);
    	if(o != null) {
    		ret = (IDCDomainValue) o;
		}
    		
		return ret;
		
	}
    
	/************************************************************************************************/

	public void appendText(String attrName, String text) {
		
		String val = getString(attrName);
		val += "\n" + text;
		set(attrName, val);
		
	}

    /************************************************************************************************/

    public String toString() {
        	
    	String ret = getName();
    	
    	return ret;

    }

	/************************************************************************************************/
    // Database access ...
    /************************************************************************************************/

    public List<IDCError> save() {
    	return save(true);
    }
    
    public List<IDCError> save(boolean isCheckWorkflows) {

    	List<IDCError> ret = checkData();
    	
		boolean isNewStatus = isNew;
    	
    	if(ret.size() == 0) {
    		
    		isSaving = true;
    		
        	executeSaveActions(true, isNew);
        	
        	if(isNew) {
        		IDCError addError = type.addObject(this);
        		if(addError == null) {
        			type.updateTempReferences(this);
        		} else {
        			ret.add(addError);
        		}
        	} else {
        		type.updateObject(this);
        	}
        	
        	if(systemReferenceData != null) {
        		saveSystemReference();
        	}
        	
        	executeSaveActions(false, isNewStatus);
        	
    		isSaving = false;
    		type.updateObjectSavingStatus(this);
    		
        	if(isCheckWorkflows) {
        		checkWorkflows();
        	}

    	}
    	
		return ret;
    	
    }
    
	/************************************************************************************************/

    public IDCError delete(boolean force) {
    	
    	IDCError ret = null;
    	
    	IDCApplication app = type.getApplication();
    	
    	boolean delOk = true;
            	
    	if(!force) { 
    		delOk = deleteParentRefs(true);
    	}

		if(delOk) {
    		
    		deleteParentRefs(false);

    		for(IDCAttribute attr : type.getAttributes()) {
        		
        		if(attr.isNameSpace()) {
        			
        			List<IDCDataRef> refs = type.loadNamespaceReferences(getId(), attr);
                	for(IDCDataRef ref : refs) {
                    	IDCType refType = app.getType(ref.getTypeId());
                    	IDCData refData = refType.loadDataObject(ref.getItemId());
                    	refData.delete(force);
                	}
        			
        		} else if(attr.isList()) {
        			type.removeDataListFromParent(getId(), attr);
        		}
        			
        	}
        	
    		type.deleteObject(this);
    		
    	} else {
    		ret = new IDCError(IDCError.CANT_DELETE_REFERENCED_DATA);
    	}
    	
    	return ret;
    	
    }
    
    public void clearNamespace(String attrName) {
    	
    	IDCAttribute attr = type.getAttribute(attrName);
    	if(attr != null) {
    		type.removeNamespaceRefs(id, attr);
    	}
    	
    }
    
    /************************************************************************************************/

    public boolean deleteParentRefs(boolean check) {
    	
    	boolean ret = true;
    	
    	IDCApplication app = type.getApplication();
    	
    	for(IDCType refType : app.getTypes()) {
    		
        	for(IDCAttribute refAttr : refType.getAttributes()) {
        		
        		List<IDCReference> refs = refAttr.getReferences();
        		
        		if(refs != null) {
        			
                	for(IDCReference ref : refs) {
                		
                		if(ref.getDataType() == type) {
                			
        					switch(refAttr.getAttributeType()) {
        					
        						case IDCAttribute.REF:
        						case IDCAttribute.REFBOX:
        						case IDCAttribute.REFTREE:
        						case IDCAttribute.EXTENSION:
        			    			
        			    			IDCDataRef thisRef = new IDCDataRef(type.getEntityId(), getId()); 

        							String where = refAttr.getColName() + " = '" + thisRef + "'";
        							List<IDCDataRef> list = refType.loadAllDataReferences(null, where, null, IDCType.NO_MAX_ROWS);
        							
        							if(list.size() > 0) {
        								if(check) {
        									ret = false;
        								} else {
        									for(IDCDataRef parentRef : list) {
        										refType.removeRef(thisRef, refAttr);
        									}
        									
        								}
        							}
        							break;
        					
        						case IDCAttribute.LIST:

        							if(check) {
        								if(refType.checkDataListFromChild(new IDCDataRef(type.getEntityId(),getId()), refAttr)) {
        									ret = false;
        								}
        							} else {
            							refType.removeDataListFromChild(new IDCDataRef(type.getEntityId(),getId()), refAttr);
        							}
        							break;
        					
        					}

                		}
                	}
        		}
        		
        	}
    	}

    	return ret;
    }
    
	/************************************************************************************************/
    // XML ...
    /************************************************************************************************/

    public void writeXML(PrintWriter out, boolean isExpanded, Map<IDCType, Map<Long, Object>> refMap) {
    	
    	//IDCUtils.debug("generateXMLData: data = " + this);
    	
    	Map<Long, Object> map = refMap.get(type);
    	if(!map.containsKey(id)) {

    		map.put(id,  null);

        	Vector<IDCData> refs = new Vector<IDCData>();
        	
    		out.print("<Object type=\"" + type.getName() + "\" typeId=\"" + type.getEntityId() + "\" id=\"" + getId());
    		
    		if(namespaceParentRef == null) {
    			out.println("\">");
    		} else {			
    			out.println("\" parentTypeId=\"" + namespaceParentRef.getTypeId() + "\" parentAttributeId=\"" + namespaceParentRef.getAttrId()  + "\" parentItemId=\"" + namespaceParentRef.getItemId()+ "\">");
    		}
    		
    		out.println("  <Attributes>");

    		for(IDCAttribute attr: type.getAttributes()) {

    	    	//IDCUtils.debug("generateXMLData: attr = " + attr);

    			int attrId = attr.getAttributeId();
    			int attrType = attr.getAttributeType();
    			
    			switch(attrType) {
    			
    				case IDCAttribute.REF:
    				case IDCAttribute.REFBOX:
    				case IDCAttribute.REFTREE:
    				case IDCAttribute.EXTENSION:
    				
    					IDCDataRef ref = (IDCDataRef) super.getValue(attrId);
    					if(ref != null) {
    						IDCData refData = type.loadDataRef(ref, attrType == IDCAttribute.EXTENSION ? false : true);
    						out.println("    <Attribute name=\"" + attr.getName() + "\" attributeId=\"" + attrId + "\"  attributeType=\"" + attr.getAttributeTypeName() + "\" >");
							refData.writeXMLRef(out, "       ");
							if(isExpanded && refMap.get(ref.toString()) == null) {
								refs.add(refData);
							}
    						out.println("    </Attribute>");
    					}

    					break;

    				case IDCAttribute.BACKREF:
    					break;

    				case IDCAttribute.LIST:
    				case IDCAttribute.NAMESPACE:
    					
    					List<IDCData> refList = (List<IDCData>) getValue(attrId);

    					if(refList != null && refList.size() > 0) {
    						out.println("    <Attribute name=\"" + attr.getName() + "\" attributeId=\"" + attrId + "\"  attributeType=\"" + attr.getAttributeTypeName() + "\" >");
    						for(IDCData refData : refList) {
								refData.writeXMLRef(out, "       ");
								IDCDataRef ref1 = new IDCDataRef(refData);
								if(attr.getAttributeType() == IDCAttribute.NAMESPACE || isExpanded && refMap.get(ref1.toString()) == null) {
									refs.add(refData);
								}
    						}
    						out.println("    </Attribute>");
    					}					
    					break;
    					
    				default:

    					out.print("    <Attribute name=\"" + attr.getName() + "\" attributeId=\"" + attrId + "\"  attributeType=\"" + attr.getAttributeTypeName() + "\" >");
    					out.print(IDCUtils.convert2XML(getDisplayValue(attrId).toString()));
    					out.println("</Attribute>");
    					break;

    				case IDCAttribute.DOMAIN:

    					out.print("    <Attribute name=\"" + attr.getName() + "\" attributeId=\"" + attrId + "\"  attributeType=\"" + attr.getAttributeTypeName() + "\" >");
    					IDCDomainValue domainValue = ((IDCDomainValue) getValue(attrId));
    					if(domainValue != null) {
    						out.print(""+domainValue.getIndex());
    					}
    					out.println("</Attribute>");
    					break;

    			}
    	
    		}
    			
    		out.println("  </Attributes>");

    		out.println("</Object>");

    		//IDCUtils.debug("IDCData.generateXMLData(): refs size = " + refs.size());

    		for(IDCData data : refs) {
    			//IDCUtils.debug("IDCData.generateXMLData(): ref = " + data);
    			data.writeXML(out, isExpanded, refMap);
    		}
    		
    	}

    }
    
    /************************************************************************************************/
    
    public void writeXMLRef(PrintWriter out, String pref) {
		out.println(pref + "<Ref type=\"" + type.getName() + "\" id=\"" + getId() + "\"/>");
    }
    
   /************************************************************************************************/
   
    public void writeXML(PrintWriter out, boolean isExpanded) {
    	
		IDCUtils.writeXMLHeader(out);
		writeXML(out, isExpanded, getApplication().getExportMap());
		IDCUtils.writeXMLTrailer(out);
    	
    }
    
    /************************************************************************************************/
    
   public String getXMLString(boolean isExpanded) {
    	
    	String ret = null;
    	
		ByteArrayOutputStream str = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(str);
		
		IDCUtils.writeXMLHeader(out);
		writeXML(out, isExpanded, getApplication().getExportMap());
		IDCUtils.writeXMLTrailer(out);
		
		out.close();

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
    // CSV ...
	/************************************************************************************************/

	public String getCSVString(boolean isExpanded) {
		
    	String ret = null;
    	
		ByteArrayOutputStream str = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(str);
		
		writeCSV(out);
		
		out.close();

		ret = str.toString();
			
    	return ret;
    	
	}

	/************************************************************************************************/

    public void writeCSV(PrintWriter out) {
    	
    	IDCUtils.debug("writeCSV: data = " + this);

    	Vector<IDCData> refs = new Vector<IDCData>();
    	
		out.print(type.getEntityId() + "," + getId() + ",");
		
		if(namespaceParentRef != null) {
			out.print(namespaceParentRef.getTypeId() + "/" + namespaceParentRef.getAttrId()  + "/" + namespaceParentRef.getItemId());
		}
    	
		for(IDCAttribute attr: type.getAttributes()) {

	    	IDCUtils.debug("writeJSON: attr = " + attr);

			int attrId = attr.getAttributeId();
			
        	switch(attr.getAttributeType()) {
			
				case IDCAttribute.REF:
				case IDCAttribute.REFBOX:
				case IDCAttribute.REFTREE:
				case IDCAttribute.EXTENSION:
				
					IDCData refData = getData(attrId);
					if(refData != null) {
						out.print(", " + "\"" + refData.getName() + "\"");
					}
					break;

				case IDCAttribute.BACKREF:
				case IDCAttribute.LIST:
				case IDCAttribute.NAMESPACE:
					break;
					
				case IDCAttribute.DATE:
				case IDCAttribute.PRICE:
					out.print(", " + IDCUtils.convert2CSV(getDisplayValue(attrId).toString()));
					break;
					
				default:
					out.print(", " + "\"" + IDCUtils.convert2CSV(getDisplayValue(attrId).toString())+ "\"");
					break;

			}
	
		}
		
		out.print("\n");
				
    }
    
	/************************************************************************************************/
    // JSON ...
    /************************************************************************************************/

    public void writeJSON(PrintWriter out, boolean isExpanded, Map<IDCType, Map<Long, Object>> refMap, boolean isFirstChild) {
    	
    	if(refMap != null) {
//        	refMap.put(new IDCDataRef(this).toString(),"");
    	}
    	
    	IDCUtils.debug("writeJSON: data = " + this);

    	Vector<IDCData> refs = new Vector<IDCData>();
    	
		out.print((isFirstChild ? " " : ",") + "{\"type\": \"" + type.getName() + "\", \"typeId\":\"" + type.getEntityId() + "\", \"id\":\"" + getId() + "\"");
		
		if(namespaceParentRef != null) {
			out.print("\"parentTypeId\":" + namespaceParentRef.getTypeId() + "\"parentAttributeId\":" + namespaceParentRef.getAttrId()  + "\"parentItemId\":" + namespaceParentRef.getItemId());
		}
		
		for(IDCAttribute attr: type.getAttributes()) {

	    	IDCUtils.debug("writeJSON: attr = " + attr);

			int attrId = attr.getAttributeId();
			
			out.print(", \"" + attr.getName() + "\": ");

			switch(attr.getAttributeType()) {
			
				case IDCAttribute.REF:
				case IDCAttribute.REFBOX:
				case IDCAttribute.REFTREE:
				case IDCAttribute.EXTENSION:
				
					IDCData refData = getData(attrId);
					if(refMap == null) {
						if(isExpanded) {
							refData.writeJSON(out, isExpanded, null, true);
						} else {
							out.print("\"" + refData.getDataRef() + "\"");
						}
					} else {
						refData.writeJSONRef(out, "       ");
						if(isExpanded && refMap.get(refData.getDataRef().toString()) == null) {
							refs.add(refData);
						}
					}

					break;

				case IDCAttribute.BACKREF:
					out.print("[]");
					break;

				case IDCAttribute.LIST:
				case IDCAttribute.NAMESPACE:
					
					List<IDCData> refList = (List<IDCData>) getValue(attrId);

					out.print("[");
					if(refList != null && refList.size() > 0) {
						isFirstChild = true;
						for(IDCData refData2 : refList) {
							if(refMap == null) {
								if(isExpanded) {
									refData2.writeJSON(out, isExpanded, null, isFirstChild);
								} else {
									out.print(refData2);
								}
							} else {
								refData2.writeJSONRef(out, (isFirstChild ? "       " : "      ,"));
								IDCDataRef ref1 = new IDCDataRef(refData2);
								if(attr.getAttributeType() == IDCAttribute.NAMESPACE || isExpanded && refMap.get(ref1.toString()) == null) {
									refs.add(refData2);
								}
							}
							isFirstChild = false;
						}
					}
					out.print("]");
					break;
					
				default:
					out.print("\"" + IDCUtils.convert2JSON(getDisplayValue(attrId).toString())+ "\"");
					break;

			}
	
		}

		IDCUtils.debug("IDCData.writeJSON(): refs size = " + refs.size());

		for(IDCData data : refs) {
			//IDCUtils.debug("IDCData.generateXMLData(): ref = " + data);
			data.writeJSON(out, isExpanded, refMap, false);
		}
		
		out.print("}");
				
    }
    
    /************************************************************************************************/
    
    public void writeJSONRef(PrintWriter out, String pref) {
		out.println(pref + "{\"type\" : \"" + type.getName() + "\", \"id\" :\"" + getId() + "\"}");
    }
    
   /************************************************************************************************/
   
    public void writeJSON(PrintWriter out, boolean isExpanded) {
    	
		IDCUtils.writeJSONHeader(out);
		writeJSON(out, isExpanded, getApplication().getExportMap(), true);
		IDCUtils.writeJSONTrailer(out);
    	
    }
    
    /************************************************************************************************/
    
   public String getJSONString(boolean isExpanded, boolean isFirstChild) {
    	
    	String ret = null;
    	
		ByteArrayOutputStream str = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(str);
		
		IDCUtils.writeJSONHeader(out);
		writeJSON(out, isExpanded, getApplication().getExportMap(), isFirstChild);
		IDCUtils.writeJSONTrailer(out);
		
		out.close();

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

    		IDCUtils.writeJSONHeader(out);
    		writeJSON(out, isExpanded, getApplication().getExportMap(), true);
    		IDCUtils.writeJSONTrailer(out);
			
    		out.close();
			
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
    	
    }
    
	/************************************************************************************************/

	public boolean isEditable() {
		return isEditable(type);
	}
    
	/************************************************************************************************/

	public boolean isEditable(IDCModelData element) {

		boolean ret = false;
		
		switch(element.getIsEditableStatus()) {

			case IDCModelData.ENABLED:
				ret = true;
				break;
				
			case IDCModelData.DISABLED:
				ret = false;
				break;
				
			case IDCModelData.EVALUATE:
				String formula = element.getIsEditableFormula();
				Object evalData = evaluate(formula); 
				if(evalData != null) {
					ret = ((Boolean) evalData).booleanValue();
				}
				break;
		}
		
		
		return ret;
		
	}

	/************************************************************************************************/

	public boolean isEnabled() {

		boolean ret = false;
		
		switch(type.getIsDataEnabledStatus()) {

			case IDCModelData.ENABLED:
				ret = true;
				break;
				
			case IDCModelData.DISABLED:
				ret = false;
				break;
				
			case IDCModelData.EVALUATE:
				String formula = type.getIsDataEnabledFormula();
				ret = ((Boolean) evaluate(formula)).booleanValue();
				break;
		}
		
		
		return ret;
		
	}
    
	/************************************************************************************************/

	public boolean isEnabled(IDCModelData element) {

		boolean ret = false;
		
		switch(element.getIsEnabledStatus()) {

			case IDCModelData.ENABLED:
				ret = true;
				break;
				
			case IDCModelData.DISABLED:
				ret = false;
				break;
				
			case IDCModelData.EVALUATE:
				String formula = element.getIsEnabledFormula();
				ret = ((Boolean) evaluate(formula)).booleanValue();
				break;
		}
		
		
		return ret;
		
	}

	/************************************************************************************************/

	public String getURLId() {
		return getURLId(true);
	}
	
	public String getURLId(boolean isDisplayType) {
		return "<a href=\"" + URLPREFIX +  new IDCDataRef(this) + "\" >" + (isDisplayType ? getDataType().getName() : "") + " <b>" + getName()+ "</b></a>";
	}
	
	/************************************************************************************************/

	private class CacheInitValue {
		
	}

	/************************************************************************************************/

	public boolean search(String txt) {

		boolean ret = false;
		
		for(int nAttr=0, maxAttr=getValuesCount(); nAttr < maxAttr && ret == false; nAttr++) {
			String val = getDisplayValue(nAttr).toLowerCase();
			if(val.indexOf(txt) != -1) {
				ret = true;
			}
		}
		
		return ret;

	}

	/************************************************************************************************/

	public List<IDCAttribute> searchAttributes(String txt) {

		List<IDCAttribute> ret = new ArrayList<IDCAttribute>();
		
		int nAttr=0;
		for(IDCAttribute attr : type.getAttributes()) {
			String val = getDisplayValue(nAttr++).toLowerCase();
			if(val.indexOf(txt) != -1) {
				ret.add(attr);
			}
		}
		
		return ret;

	}
	
	/************************************************************************************************/

	public List<IDCDataValue> searchValues(String keyword, int searchType) {

		List<IDCDataValue> ret = new ArrayList<IDCDataValue>();
				
		int nAttr=0;
		for(IDCAttribute attr : type.getAttributes()) {
			
			boolean found=false;
			
			String val = getDisplayValue(nAttr++).toLowerCase();
			switch(searchType) {
			
				case SEARCH_EQUALS:
					found = val.equals(keyword); 
					break;
					
				case SEARCH_STARTS_WITH:
					found = val.startsWith(keyword);
					break;
					
				case SEARCH_CONTAINS:
					found = val.indexOf(keyword) != -1;
					break;
					
			}
			
			if(found) {
				IDCUtils.debugNLU("searchValues(): found  type = " + this.getDataType() + " / data = " + this + " / keyword = " + keyword + " / attr = " + attr + " / val = " + val);
				ret.add(new IDCDataValue(attr, this, val));
			}
			
		}
		
		return ret;

	}

	/************************************************************************************************/

	public void reload() {
		IDCData newData = type.loadDataObject(getId());
		copy(newData);
	}

	/************************************************************************************************/

	public boolean isData() {
		return true;
	}

	/************************************************************************************************/

	public boolean isModelData() {
		return false;
	}

	/************************************************************************************************/

	public boolean isApplication() {
		return false;
	}

	public boolean isType() {
		return false;
	}

	/************************************************************************************************/

	public IDCDataParentRef getAsParentRef(String attrName) {
		
		return new IDCDataParentRef(getDataType().getEntityId(), getDataType().getAttribute(attrName).getId(), getId());
	}

	/************************************************************************************************/

	public IDCDataParentRef getAsParentRef(int attrId) {
		
		return new IDCDataParentRef(getDataType().getEntityId(), getDataType().getAttribute(attrId).getId(), getId());
	}

	/************************************************************************************************/
    /* API helper methods ... */
	/************************************************************************************************/

	public void insertReference(String attrName, IDCData value) {
		
		IDCAttribute attr = type.getAttribute(attrName);
		
		if(attr != null) {
			insertReference(attr, value);
		}
		
	}

	/************************************************************************************************/

	public void insertReference(int ind, IDCData value) {

    	IDCAttribute attr = type.getAttribute(ind);
		
		if(attr != null) {
			insertReference(attr, value);
		}
	}

	/************************************************************************************************/

	public void removeReference(String attrName, IDCData value) {
		
		IDCAttribute attr = type.getAttribute(attrName);
		
		if(attr != null) {
			removeReference(attr, value);
		}
		
	}

	/************************************************************************************************/

	public void removeReference(int ind, IDCData value) {

    	IDCAttribute attr = type.getAttribute(ind);
		
		if(attr != null) {
			removeReference(attr, value);
		}

	}

	/************************************************************************************************/

	public IDCData getNamespaceParent() {
		
		IDCData ret = null;
		
		if(namespaceParentData == null) {
			if(namespaceParentRef != null) {
				IDCType parentType = type.getApplication().getType(namespaceParentRef.getTypeId());
				ret = parentType.loadDataObject(namespaceParentRef.getItemId());
			}
		} else {
			ret = namespaceParentData;
		}
		
		return ret;
	
	}

	/************************************************************************************************/

	public IDCData getExtentionParent() {
		
		IDCData ret = this;
		
		if(extentionParentData == null) {
			if(extentionParentRef != null) {
				IDCType parentType = type.getApplication().getType(extentionParentRef.getTypeId());
				IDCData parentData = parentType.loadDataObject(extentionParentRef.getItemId());
				ret = parentData.getExtentionParent();
			}
		} else {
			ret = extentionParentData;
		}
		
		ret = ret.getSystemExtentionParent();
		
		return ret;
	
	}

	/************************************************************************************************/

	public IDCData getSystemExtentionParent() {
		
		IDCData ret = this;
		
		if(systemExtentionParentData == null) {
			if(systemExtentionParentRef != null) {
				IDCType parentType = type.getApplication().getType(systemExtentionParentRef.getTypeId());
				IDCData parentData = parentType.loadDataObject(systemExtentionParentRef.getItemId());
				ret = parentData.getSystemExtentionParent();
			}
		} else {
			ret = systemExtentionParentData;
		}
		
		return ret;
	
	}

	/************************************************************************************************/

	public void executeSaveActions(boolean isPreSave, boolean isNew) {
		
		for(IDCAction act : type.getSaveActions(isPreSave, isNew)) {
			act.executeAction(this, null, false);
		}
		
	}

	/************************************************************************************************/

	public void executeAction(String actionName, boolean isSave) {
		
		IDCAction act = type.getAction(actionName);
		if(act != null) {
			act.executeAction(this, null, isSave);
		}
		
	}

	/************************************************************************************************/

    private List<IDCError> checkData() {
    	
    	List<IDCError> ret = new ArrayList<IDCError>();
    	
    	for(IDCAttribute attr : getDataType().getAttributes()) {
    		IDCError err = checkData(attr, false);
    		if(err != null) {
    			ret.add(err);
    		}
    	}
    	
    	return ret;
	
    }

    /************************************************************************************************/

    public IDCError checkData(IDCAttribute attr, boolean isWarning) {
    	
    	IDCError ret = null;
    	
    	if(isEditable(attr)) {
        	Object value = getValue(attr);
        	String strValue = "" + value;
        	
        	ret = attr.checkData(strValue);
        	
        	if(ret == null) {
        		
        		String constraint = attr.getConstraintFormula();
        		
        		if(constraint != null && constraint.length() > 0) {
        			constraint = attr.getName() + " " + constraint;
        			boolean isConstraintOk = ((Boolean) evaluate(constraint)).booleanValue();
        			if(!isConstraintOk) {
        				ret = new IDCError(IDCError.CONSTRAINTSATISFACTIONERROR, attr.getConstraintMessage(), attr.getAttributeId());
        			}
        		}

        	}
    	}
    	
    	
        return ret;
    	
    }

    /************************************************************************************************/

    public IDCError checkDataOLD(IDCAttribute attr, boolean isWarning) {
    	
    	IDCError ret = null;
    	
    	Object value = getValue(attr);
    	String strValue = "" + value;
    	
    	int attrId = attr.getAttributeId();

    	if(getValue(attr) == null || strValue.length() == 0) {
        	
    		if(attr.isMandatory()) {
        		if(isWarning) {
                	ret = new IDCError(IDCError.MANDATORYATTRIBUTE, "*", attrId, IDCError.WARNING);
        		} else {
                	ret = new IDCError(IDCError.MANDATORYATTRIBUTE, attr.getName() + " is mandatory", attrId, IDCError.ERROR);
        		}
            }    	
    		
        } else {
            	
        	switch(attr.getAttributeType()) {
        	
				case IDCAttribute.INTEGER:
					try {
						Integer.parseInt(strValue);
					} catch(NumberFormatException ex) {
		            	ret = new IDCError(IDCError.INVALIDFORMAT, getName() + " must be an integer", attrId, IDCError.ERROR);
					}
					
					break;
			
				case IDCAttribute.PRICE:
					try {
						Double.parseDouble(strValue);
					} catch(NumberFormatException ex) {
		            	ret = new IDCError(IDCError.INVALIDFORMAT, getName() + " must be numeric", attrId, IDCError.ERROR);
					}
					break;
	
				case IDCAttribute.PHONE:
            		
					String num = IDCUtils.replaceAll(strValue, " ", "");
					if(num.length() != 12 || !strValue.startsWith("+")) { 
            			ret = new IDCError(IDCError.INVALIDFORMAT, attr.getDesc() + " must be +ccnnnnnnnnnn where cc is country code and n..n is a 9 digit phone number ...", attrId);
					}
            			
            		break;
            		
				case IDCAttribute.EMAIL:
            		
					if(strValue.indexOf('@') == -1) {
            			ret = new IDCError(IDCError.INVALIDFORMAT, attr.getDesc() + " must be a valid email address ...", attrId);
					}
            			
            		break;
            		
            	default:
        			break;

        	}
        	
        }
    	
    	if(ret == null) {
    		
    		String constraint = attr.getConstraintFormula();
    		
    		if(constraint != null && constraint.length() > 0) {
    			constraint = attr.getName() + " " + constraint;
    			boolean isConstraintOk = ((Boolean) evaluate(constraint)).booleanValue();
    			if(!isConstraintOk) {
    				ret = new IDCError(IDCError.CONSTRAINTSATISFACTIONERROR, attr.getConstraintMessage(), attrId);
    			}
    		}

    	}
    	
        return ret;
    	
    }

    /***************************************************/    
	
    public boolean isSearchList() {
		return false;
	}
    
	/**************************************************************************************************/
	
    /************************************************************************************************/
    
    public IDCData getContextData() {
    	return context;
    }

    /************************************************************************************************/
    
	public void setContextData(IDCData context) {
		this.context = context;
	}

    /************************************************************************************************/
    
	// this sets the system reference object in the application object
	// used at the end of save() to update and save system object
	// needed for new app objects where tempid causes error on sys object save if done before app object save (like part of an execute action on create) ;)
	
	public void setSystemReferenceData(IDCData systemReferenceData) {
		this.systemReferenceData = systemReferenceData;
	}

    /************************************************************************************************/
    
	// this updates the System Reference Attribute in the system object to the current application object being saved and saves the system object
	// why? see above :)
	
	public void saveSystemReference() {
		systemReferenceData.set(IDCModelData.APPLICATION_SYSTEM_REFERENCE_ATTR_NAME, this);
		systemReferenceData.save();
	}
    
    /************************************************************************************************/
    
	public IDCData getSystemReferenceData() {
		return systemReferenceData;
	}

    /************************************************************************************************/
    
	public IDCDataRef getDataRef() {
		return new IDCDataRef(this);
	}
	
	/**************************************************************************************************/
	// Request methods ...
	/**************************************************************************************************/

	public List<IDCData> requestChildren(String formula, int attrId) {
		
		List<IDCData> ret = new ArrayList<IDCData>();
		
		IDCDataParentRef parentRef = this.getAsParentRef(attrId);
		
		for(IDCReference ref : getDataType().getAttribute(attrId).getReferences()) {
			IDCType childType = ref.getDataType();
			ret.addAll(childType.requestData(formula, parentRef));
		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public IDCData requestChild(String formula, int attrId) {

		IDCData ret = null;
		
		List<IDCData> results = requestChildren(formula, attrId);
		
		if(results.size() == 1) {
			ret = results.get(0);
		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public List<IDCData> requestChildren(String formula, String attrName) {
		return requestChildren(formula, getDataType().getAttribute(attrName).getAttributeId());
	}

	/**************************************************************************************************/

	public IDCData requestChild(String formula, String attrName) {

		IDCData ret = null;
		
		List<IDCData> results = requestChildren(formula, attrName);
		
		if(results.size() == 1) {
			ret = results.get(0);
		}
		
		return ret;
		
	}

    /************************************************************************************************/

	public IDCData loadDataRef(IDCDataRef ref) {
		return loadDataRef(ref, true);
	}

	public IDCData loadDataRef(IDCDataRef ref, boolean getSuper) {
		return getApplication().loadDataRef(ref, getSuper);
	}
	
	/************************************************************************************************/

	private void checkWorkflows() {
		
		List<IDCWorkflowInstanceData> instances = IDCWorkflowInstanceData.checkWorkflows(this);
		for(IDCWorkflowInstanceData instance : instances) {
			instance.execute();
		}
		
	}

	/************************************************************************************************/

	public boolean isSystemApp() {
		
		boolean ret = false;
		
		if(type.getName().equals(IDCSystemApplication.APPLICATION_TYPE) && getName().equals(IDCSystemApplication.ADMIN_APPL)) {
			ret = true;
		}
		
		return ret;
		
	}

	/************************************************************************************************/

	public boolean isSaving() {
		return isSaving;
	}

	/************************************************************************************************/

	public void setIsSaving(boolean isSaving) {
		this.isSaving = isSaving;
	}

	/************************************************************************************************/

	public boolean isInSettingsDateRange() {
		
		boolean ret = true;
		
		if(type.dateAttributes != null && type.dateAttributes.size() > 0) {
			
			ret = false;
			
			for(IDCAttribute attr : type.dateAttributes) {
				long date = getLong(attr.getAttributeId());
				if((IDCWebAppSettings.startDate != -1 && date < IDCWebAppSettings.startDate) || (IDCWebAppSettings.endDate != -1 && date > IDCWebAppSettings.endDate)) {
				} else {
					ret = true;
					break;
				}
			}
		}

		return ret;
		
	}

	/************************************************************************************************/

	@Override
	public boolean isNluResults() {
		return false;
	}

}



