package com.indirectionsoftware.utils;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.metamodel.IDCModelData;

public class IDCVector {
	
	private int  			 type;
	protected long 			 id;
	protected List<Object> 	 values;

	/************************************************************************************************/

    public IDCVector(int type, long id, List<Object> values) {
    	
    	setId(id);
    	setType(type);
    	initValues(values);
    
    }	
    	
    /************************************************************************************************/

    public void copy(IDCVector orig) {
    	
    	setType(orig.type);
    	setId(orig.getId());
    	initValues(orig.values);
    	
    }	
    	
    /************************************************************************************************/

    public IDCVector clone() {
    	return new IDCVector(type, id, values);
    }	
    	
    /************************************************************************************************/

    private void initValues(List<Object> vals) {
    	
    	values = new ArrayList<Object>();
    	if(vals != null) {
        	values.addAll(vals);
    	}
    	
    }
    
    /************************************************************************************************/

    public int getType() {return type; }
    
    public long getId() { return id; }
    
    public int getValuesCount() { return values.size(); }
    
    public List<Object> getValues() { return values; }

    public Object getValue(int ind) { 
    	
    	Object ret = null;
    	if(ind < values.size()) {
    		ret = values.get(ind);	
    	}
    	
    	return ret; 
    	
    }

    /************************************************************************************************/

    public void setType(int type) { this.type = type;}
    
    public void setId(long id) { this.id = id;}
    
    public void setValues(List<Object> values) { this.values = values;}
    
    public void setValue(int ind, Object value) {
    	values.set(ind, value);
    }
    
    /************************************************************************************************/

    public boolean equals(IDCVector data) {
    	
    	boolean ret = false;
    	
    	if(data != null) {
    		if(this.type == data.getType() && this.id == data.getId()) {
        		ret = true;
    		}
    	}
    	
    	return ret;
    	
    }	
    	
	/**************************************************************************************************/
	
	public String getFormula(int attrId) {
    	return getString(attrId, null);
	}

	/**************************************************************************************************/
	
	public String getString(int attrId) {
    	return getString(attrId, "");
	}

	public String getString(int attrId, String defValue) {
    	
    	String ret = "";
    	
    	Object o = values.get(attrId);
    	if(o != null) {
    		ret += o;
		}
    	
    	if(ret.length() == 0) {
    		ret = defValue;
    	}
    		
    	return ret;

	}

	/**************************************************************************************************/
	
	public int getInt(int attrId, int defValue) {
	    	
		int ret = defValue;
		
		String s = getString(attrId);
		if(s != null && s.length()>0) {
			ret = Integer.parseInt(s);
		}
	
		return ret;
		
	}

	public int getInt(int attrId) {
		return getInt(attrId, -1);
	}
	
	/**************************************************************************************************/
	
	public boolean getBoolean(int attrId) {

		boolean ret = false;
		
		String s = getString(attrId);
		if(s != null && s.length()>0) {
			if(s.equalsIgnoreCase("true")) {
				ret = true;
			}
		}
	
		return ret;
	
	}



	/**************************************************************************************************/
	
    public List getList(int attrId) {
    	
    	List ret = new ArrayList();
    	
    	if(values != null) {
    		ret = (List) values.get(attrId);
    	}
    	
    	return ret;

    }
	/**************************************************************************************************/
	
    public String getEntityName() {
    	int type = getType();
    	return IDCModelData.TYPES[type];
	}
    
}