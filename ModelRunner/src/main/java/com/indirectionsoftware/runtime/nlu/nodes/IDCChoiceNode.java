package com.indirectionsoftware.runtime.nlu.nodes;

import com.indirectionsoftware.utils.IDCUtils;

public class IDCChoiceNode extends IDCNode {
	
	/***************************************************************************************/

	public IDCChoiceNode(IDCNode parent, String schema) {
		super(parent, schema, CHOICE);
	}

	/***************************************************************************************/

	public String match(String part) {
		
		IDCUtils.debug("IDCChoiceNode.match() - start: this = " + this);
		IDCUtils.debug("IDCChoiceNode.match() - start: part = " + part);
				
		String ret = null;
		
		if(name != null) {
			String val = "";
			String retVal = null;
			for(IDCNode child : children) {
				ret = child.match(part);
				if(ret != null) {
					if(child.schema.length() > val.length()) {
						IDCUtils.debug("Match: value = " + child.schema);
						retVal = ret;
						val = child.schema;
					}
				}
			}
			ret = retVal;
			if(val.length() > 0) {
				value = val;
			}
		} else {
			for(IDCNode node : children) {
				IDCUtils.debug("IDCChoiceNode.match(): node = " + node);
				ret = node.match(part);
				if(ret != null) {
					IDCUtils.debug("Match: value = " + node.schema);
					break;
				}
			}
		}
				
		IDCUtils.debug("IDCChoiceNode.match() - end: this = " + this);
		IDCUtils.debug("IDCChoiceNode.match() - end: part = " + part);
		IDCUtils.debug("IDCChoiceNode.match() - end: ret = " + ret);

		return ret;
		
	}	

}
