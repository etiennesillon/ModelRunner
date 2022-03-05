package com.indirectionsoftware.runtime.nlu.nodes;

import com.indirectionsoftware.utils.IDCUtils;

public class IDCChoiceValNode extends IDCNode {
	
	/***************************************************************************************/

	public IDCChoiceValNode(IDCNode parent, String schema) {
		super(parent, schema, CHOICEVAL);
	}

	/***************************************************************************************/

	public String match(String part) {
		
		IDCUtils.debug("IDCChoiceValNode.match() - start: this = " + this);
		IDCUtils.debug("IDCChoiceValNode.match() - start: part = " + part);
				
		String ret = part;
		
		for(int nChild=0; ret != null && part.length() > 0 && nChild < children.size(); nChild++) {
			IDCNode node = children.get(nChild);
			IDCUtils.debug("IDCChoiceValNode.match(): node = " + node);
			ret = node.match(ret);
		}
		
		IDCUtils.debug("IDCChoiceNode.match() - end: this = " + this);
		IDCUtils.debug("IDCChoiceNode.match() - end: part = " + part);
		IDCUtils.debug("IDCChoiceNode.match() - end: ret = " + ret);

		return ret;
		
	}	

}
