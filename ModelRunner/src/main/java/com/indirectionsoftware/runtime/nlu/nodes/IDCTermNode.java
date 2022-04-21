package com.indirectionsoftware.runtime.nlu.nodes;

import com.indirectionsoftware.utils.IDCUtils;

public class IDCTermNode extends IDCNode {
	
	public IDCTermNode(IDCNode parent, String schema) {
		super(parent, schema, TERM);
	}

	/***************************************************************************************/

	public String match(String part) {
		
		IDCUtils.debug("IDCTermNode.match() - start: this = " + this);
		IDCUtils.debug("IDCTermNode.match() - start: part = " + part);
		
		String ret = null;
		
		if(part.startsWith(schema)) {
			value = schema;
			ret = part.substring(schema.length()).trim();
		}
		
		IDCUtils.debug("IDCTermNode.match() - end: this = " + this);
		IDCUtils.debug("IDCTermNode.match() - end: part = " + part);
		IDCUtils.debug("IDCTermNode.match() - end: ret = " + ret);
		
		return ret;
		
	}	

}
