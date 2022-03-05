package com.indirectionsoftware.runtime.nlu;

import java.util.HashMap;
import java.util.Map;

import com.indirectionsoftware.backend.database.IDCData;

public class IDCValueDataList {

	public String value;
	public Map<String, IDCData> dataMap;
	
	/*****************************************************************************/

	public IDCValueDataList(String value) {
		this.value = value;
		this.dataMap = new HashMap<String, IDCData>(); 
		
	}

	/*****************************************************************************/

	public void addData(IDCData data) {

		String name = data.getName();
		IDCData oldData = dataMap.get(name);
		if(oldData == null) {
			dataMap.put(name, data);
		}
		
	}

	/*****************************************************************************/

	public String toString() {

		String ret = "IDCValue: value = " + value + " -> {";
		
		for(IDCData  data : dataMap.values()) {
			ret += " " + data.toString() + "";
		}
		
		ret += " }";
		
		return ret;

	}
	
}
