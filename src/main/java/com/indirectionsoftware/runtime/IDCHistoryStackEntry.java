package com.indirectionsoftware.runtime;

import java.util.HashMap;
import java.util.Map;

import com.indirectionsoftware.metamodel.IDCAttribute;

public class IDCHistoryStackEntry {
		
	public IDCEnabled element;
	public IDCHistoryStackEntry prev, next;
	public Map<IDCAttribute, Integer> attributePageMap = new HashMap<IDCAttribute, Integer>();
		
	/************************************************************************************************/

	public IDCHistoryStackEntry(IDCEnabled element, IDCHistoryStackEntry prev, IDCHistoryStackEntry next) {
		this.element = element;
		this.prev = prev;
		this.next = next;
	}
	
	/************************************************************************************************/

	public void updateAttributePageMap(IDCAttribute attr, int nPage) {
		attributePageMap.put(attr, nPage);
	}

	/************************************************************************************************/

	public String toString () {
		return "element = " + element;
	}

}