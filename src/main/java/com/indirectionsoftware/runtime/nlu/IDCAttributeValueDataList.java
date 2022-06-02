package com.indirectionsoftware.runtime.nlu;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.metamodel.IDCAttribute;

public class IDCAttributeValueDataList {

	public IDCAttribute attr;
	public List<IDCValueDataList> valueDataList;
	
	/*****************************************************************************/

	public IDCAttributeValueDataList(IDCAttribute attr) {
		this.attr = attr;
		this.valueDataList = new ArrayList<IDCValueDataList>(); 
		
	}

	/*****************************************************************************/

	public String toString() {

		String ret = "IDCValue: attr = " + attr + " / inputValue = " + valueDataList + " -> {";
		
		for(IDCValueDataList  dataList : valueDataList) {
			ret += " " + dataList.toString() + "";
		}
		
		ret += " }";
		
		return ret;

	}
}
