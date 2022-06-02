package com.indirectionsoftware.runtime;

import java.util.List;

import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCDomainValue;
import com.indirectionsoftware.utils.IDCCalendar;

public class IDCEvalData {
	
	private Object value;
	private IDCAttribute attr;
	private Object parentData;
	private int type;
	
	public static int UNDEF=-1, LIST=-2;
	
	/************************************************************************************************/

	public IDCEvalData(Object value) {
		this(value, null, null, UNDEF);
	}

	/************************************************************************************************/

	public IDCEvalData(Object value, int attrType) {
		this(value, null, null, attrType);
	}

	/************************************************************************************************/

	public IDCEvalData(Object value, Object data, IDCAttribute attr) {
		this(value, data, attr, attr.getAttributeType());
	}

	/************************************************************************************************/

	public IDCEvalData(Object value, Object parentData, IDCAttribute attr, int type) {
		this.value = value;
		this.attr = attr;
		this.parentData = parentData;
		this.type = type;
	}

	/************************************************************************************************/

	public Object getValue() {
		
		Object ret = value;
		
		if(value instanceof IDCDomainValue) {
			ret = ((IDCDomainValue)value).getIndex();
		}
		return ret; 
		
	}

	/************************************************************************************************/

	public boolean isList() {
		return type == LIST;
	}

	/************************************************************************************************/

	public String getStringValue() {
		
		String ret = "";
		
		if(value != null) {
			if(value instanceof IDCDomainValue) {
				ret = ((IDCDomainValue)value).getKey();
			} else if(attr != null) {
				ret += attr.getValue(value);
			} else {
				ret += value;
			}
		}
		
		
		return ret;
		
	}

	/************************************************************************************************/

	public String getDisplayValue() {
		
		String ret = null;
		
		int attrType = type;
		
		if(attr != null) {
			type = attr.getAttributeType();
		}
		
		if(attrType == UNDEF) {
			ret = getStringValue();
		} else {
			
	    	switch(attrType) {
			
				case IDCAttribute.DATE:
					ret = IDCCalendar.getCalendar().displayDateShort((Long)value);
					break;
					
				case IDCAttribute.DATETIME:
					ret = IDCCalendar.getCalendar().displayTimeDateShort((Long)value);
					break;
					
				case IDCAttribute.DURATION:
					ret = IDCCalendar.getDaysHoursMinutesString((Long)value);
					break;
													
				default:
					ret = "" + value;
					break;
					
			}

		}				
		return ret;
		
	}

    /************************************************************************************************/

	public Double getDoubleValue() {
		
		Double ret = null;
		
		if(value instanceof IDCDomainValue) {
			ret = new Double(((IDCDomainValue)value).getIndex());
		} else if(value != null) {
			try {
				ret = Double.parseDouble(""+value);
			} catch (Exception e) {
				ret = new Double(0);
			}
		} else {
			ret = new Double(0);
		}
		
		return ret;
		
	}

	/************************************************************************************************/

	public Class getValueClass() {
		
		Class ret = null;
		if(value != null) {
			if(value instanceof List) {
				ret = List.class;
			} else {
				ret = value.getClass();
			}
		}
		
		return ret;
		
	}

	/************************************************************************************************/

	public int getType() {
		
		int ret = type;
	
		if(attr != null) {
			ret = attr.getAttributeType();
		}
		
		return ret;

	}

	/************************************************************************************************/

	public IDCAttribute getAttribute() {
		return attr;
	}

	/************************************************************************************************/

	public Object getData() {
		return parentData;
	}

	/************************************************************************************************/

	public void setValue(Object value) {
		this.value = value;
	}

	/************************************************************************************************/

	public String toString() {
		return "value=" + value + " / parentData=" + parentData + " / attr=" + attr + " / attrType=" + type;
	}

}
