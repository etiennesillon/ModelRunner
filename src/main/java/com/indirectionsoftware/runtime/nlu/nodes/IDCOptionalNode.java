package com.indirectionsoftware.runtime.nlu.nodes;

import com.indirectionsoftware.utils.IDCUtils;

public class IDCOptionalNode extends IDCNode {
	
	public IDCOptionalNode(IDCNode parent, String schema) {
		super(parent, schema, OPTIONAL);
	}

	/***************************************************************************************/

	public String match(String part) {
		
		IDCUtils.debug("IDCOptionalNode.match() - start: this = " + this);
		IDCUtils.debug("IDCOptionalNode.match() - start: part = " + part);
		
		String ret = part;
		
		for(int nChild=0; nChild < children.size(); nChild++) {
			
			IDCNode node = children.get(nChild);
			IDCUtils.debug("IDCOptionalNode.match(): node = " + node);
			
			String newPart = node.match(ret);
			if(newPart != null) {
				ret = newPart;
			}
			
		}
		
		IDCUtils.debug("IDCOptionalNode.match() - end: this = " + this);
		IDCUtils.debug("IDCOptionalNode.match() - end: part = " + part);
		IDCUtils.debug("IDCOptionalNode.match() - end: ret = " + ret);

		return ret;
		
	}	

	/***************************************************************************************/

	public String matchParameter(String part) {
		
		IDCUtils.debug("matchParameter.match() - start: this = " + this);
		IDCUtils.debug("matchParameter.match() - start: part = " + part);
		
		String ret = null;
		
		for(int nChild=0; ret == null && nChild < children.size(); nChild++) {
			
			IDCNode node = children.get(nChild);
			IDCUtils.debug("matchParameter.match(): node = " + node);
			ret = node.match(part);			
		}
		
		IDCUtils.debug("matchParameter.match() - end: this = " + this);
		IDCUtils.debug("matchParameter.match() - end: part = " + part);
		IDCUtils.debug("matchParameter.match() - end: ret = " + ret);

		return ret;
		
	}	

}
