package com.indirectionsoftware.metamodel;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;

public class IDCDomain extends IDCModelData {
	
	private List<IDCDomainValue> values;
	private List<String> valueKeys;
	
	public static final int VALUES=START_ATTR;

	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCDomain(IDCPackage parent, long id, List<Object> values) {
		super(parent, IDCModelData.DOMAIN, id, values);
	}

	/**************************************************************************************************/

	public void init(IDCData userData) {
		
		if(isInitRequired()) {

			super.init(userData);

			values = (List <IDCDomainValue>) getList(VALUES);
			valueKeys = new ArrayList<String>();
			
			int nVal=0;
			for(IDCDomainValue val : values) {
				val.init(userData, nVal++);
				valueKeys.add(val.getKey());
			}
			
			completeInit();

		}
		
	}

	/**************************************************************************************************/
	// Domain methods ...
	/**************************************************************************************************/
	
    public List<IDCDomainValue> getDomainValues() {
    	return values;
    }

    /**************************************************************************************************/
	
    public IDCDomainValue getDomainValue(int nDomVal) {
    	
    	IDCDomainValue ret = null;
    	
    	if(nDomVal >=0 && nDomVal < values.size()) {
    		ret = values.get(nDomVal);
		}
    	
    	return ret;
    }

	/**************************************************************************************************/
	
    public List<String> getKeys() {
    	return valueKeys;
    }

	/**************************************************************************************************/
	
    public String getKey(int nKey) {
    	return valueKeys.get(nKey);
    }

	/**************************************************************************************************/
	
    public int getIndex(String key) {
    	
    	int ret = -1;
    	
		for(int nKey=0, maxKeys=valueKeys.size(); nKey < maxKeys && ret == -1; nKey++) {
			String k = valueKeys.get(nKey);
			if(k.equalsIgnoreCase(key)) {
				ret = nKey;
			}
		}

		return ret;
    	
    }

	/**************************************************************************************************/
	
    public IDCDomainValue getDomainValue(String key) {
    	
    	IDCDomainValue ret = null;
    	
    	int index = getIndex(key);
    	if(index != -1) {
    		ret = values.get(index);
    	}

		return ret;
    	
    }

}