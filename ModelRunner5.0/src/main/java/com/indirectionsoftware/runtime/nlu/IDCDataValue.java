package com.indirectionsoftware.runtime.nlu;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCType;

public class IDCDataValue {

	public IDCData data;
	public IDCAttribute attr;
	public String value;	
	
	/*****************************************************************************/

	public IDCDataValue(IDCAttribute attr, IDCData data, String value) {
		this.attr = attr;
		this.data = data;
		this.value = value;
	}

	/*****************************************************************************/

	public String toString() {
		return "IDCValue: attr = " + attr + " / data = " + data  + " / value = " + value;
	}
	

	
}
