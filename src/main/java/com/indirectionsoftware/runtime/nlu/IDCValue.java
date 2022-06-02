package com.indirectionsoftware.runtime.nlu;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.metamodel.IDCAttribute;

public class IDCValue {

	public String value;	
	public List<IDCAttributeValueDataList> attrVals = new ArrayList<IDCAttributeValueDataList>();
	
	/*****************************************************************************/

	public IDCValue(String value) {
		this.value = value;
		attrVals = new ArrayList<IDCAttributeValueDataList>();
	}

	/*****************************************************************************/

	public String toString() {
		
		String ret = "IDCValue: " + value + " -> {";
		
		for(IDCAttributeValueDataList  ent : attrVals) {
			ret += " " + ent.toString() + "";
		}
		
		ret += " }";

		return ret;
		
	}
	
}
