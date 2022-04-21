package com.indirectionsoftware.runtime.nlu;

import com.indirectionsoftware.metamodel.IDCAttribute;

public class IDCAttributeValue {

	public IDCAttribute attr;
	public String value;
	
	/*****************************************************************************/

	public IDCAttributeValue(IDCAttribute attr, String value) {
		this.attr = attr;
		this.value = value;
	}

	/*****************************************************************************/

	public String toString() {
		return "IDCValue: attr = " + attr + " / value = " + value;
	}
}
