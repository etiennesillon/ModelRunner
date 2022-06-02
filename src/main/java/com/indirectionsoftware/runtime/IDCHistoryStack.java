package com.indirectionsoftware.runtime;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCHistoryStack {
	
	/**************************************************************************************************/

	protected IDCHistoryStackEntry stackRoot, stackCurrent;
	
	/************************************************************************************************/

	public void stackElement(IDCEnabled element) {
		
		if(stackRoot == null || stackCurrent == null) {
			stackRoot = stackCurrent = new IDCHistoryStackEntry(element, null, null);
		} else {
			stackCurrent.next = new IDCHistoryStackEntry(element, stackCurrent, null);
			stackCurrent = stackCurrent.next;
		}
		
		debugStack();

	}

	/**************************************************************************************************/

	public IDCEnabled moveBackStack() {
		
		IDCEnabled ret = null;
		
		stackCurrent = stackCurrent.prev;
		
		debugStack();

		if(stackCurrent != null) {
			ret = stackCurrent.element;
		}

		return ret;
		
	}

	/**************************************************************************************************/

	public IDCEnabled moveForwardStack() {
		
		stackCurrent = stackCurrent.next;
		
		debugStack();

		return stackCurrent.element;

	}

	/**************************************************************************************************/

	public void updateAttributePageMap(IDCAttribute attr, int nPage) {
		stackCurrent.updateAttributePageMap(attr, nPage);
	}

	/**************************************************************************************************/

	public int getAttributePageNumber(IDCAttribute attr) {
		
		int ret = 0; 
		
		Integer nPage = stackCurrent.attributePageMap.get(attr);
		if(nPage != null) {
			ret = nPage;
		}
		
		return ret;
		
	}

	/**************************************************************************************************/

	public void debugStack() {
		
		IDCUtils.debug("=================================");
		IDCHistoryStackEntry root = stackRoot;
		if(root !=null) {
			do {
				String curr = (root == stackCurrent ? "(CURRENT)" : "");
				IDCUtils.debug("stack entry = " + root.element + " " + curr);
				root = root.next;
			}
			while(root != null);
		}
		IDCUtils.debug("=================================");
		
	}

	/**************************************************************************************************/

	public boolean isBackOk(boolean isRootContext) {
		
		boolean ret = true;

		if(stackCurrent == null || isRootContext && stackCurrent.prev == null) {
			ret = false;
		}
		
		return ret;

	}

	/**************************************************************************************************/

	public boolean isForwardOk() {
		
		boolean ret = false;

		if(stackCurrent != null && stackCurrent.next != null) {
			ret = true;
		}
		return ret;

	}

}