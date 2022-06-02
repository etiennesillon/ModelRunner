package com.indirectionsoftware.runtime.nlu.nodes;

import java.util.List;

import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCParameterNode extends IDCNode {
	
	/***************************************************************************************/

	public IDCParameterNode(IDCNode parent, String schema) {
		super(parent, schema, PARAMETER);
	}

	/***************************************************************************************/

	public String match(String part) {
		
		IDCUtils.debug("IDCParameterNode.match() - start: this = " + this);
		IDCUtils.debug("IDCParameterNode.match() - start: part = " + part);

		String ret = null;

		if(schema.startsWith("#")) {
			ret = matchTerm(part);
		} else {
			ret = parent.matchParameter(this, part);
			if(ret == null) {
				value = part;
				ret = "";
			}
		}
				
		IDCUtils.debug("IDCParameterNode.match() - end: this = " + this);
		IDCUtils.debug("IDCParameterNode.match() - end: part = " + part);
		IDCUtils.debug("IDCParameterNode.match() - end: ret = " + ret);

		return ret;
		
	}	

	/***************************************************************************************/

	public String matchTerm(String part) {
		
		IDCUtils.debug("IDCParameterNode.matchList() - start: this = " + this);
		IDCUtils.debug("IDCParameterNode.matchList() - start: part = " + part);
		
		String ret = matchModelTypes(part);
				
		if(ret == null) {
			ret = matchModelData(part);
		}
		
		IDCUtils.debug("IDCParameterNode.matchList() - end: this = " + this);
		IDCUtils.debug("IDCParameterNode.matchList() - end: part = " + part);
		IDCUtils.debug("IDCParameterNode.matchList() - end: ret = " + ret);

		return ret;

	}

	/***************************************************************************************/

	private String matchModelTypes(String part) {

		IDCUtils.debug("IDCParameterNode.matchModelTypes() - start: this = " + this);
		IDCUtils.debug("IDCParameterNode.matchModelTypes() - start: part = " + part);
		
		String ret = null;
				
		List<IDCType> types = app.getTypes();
		
		for(IDCType type : types) {
			String word = type.getName().toLowerCase();
			if(part.startsWith(word)) {
				value = word;
				ret = part.substring(word.length()).trim();
				break;
			}
		}

		IDCUtils.debug("IDCParameterNode.matchModelTypes() - end: this = " + this);
		IDCUtils.debug("IDCParameterNode.matchModelTypes() - end: part = " + part);
		IDCUtils.debug("IDCParameterNode.matchModelTypes() - end: ret = " + ret);

		return ret;

	}

	/***************************************************************************************/

	private String matchModelData(String part) {

		IDCUtils.debug("IDCParameterNode.matchModelData() - start: this = " + this);
		IDCUtils.debug("IDCParameterNode.matchModelData() - start: part = " + part);
		
		String ret = null;
				

		IDCUtils.debug("IDCParameterNode.matchModelData() - end: this = " + this);
		IDCUtils.debug("IDCParameterNode.matchModelData() - end: part = " + part);
		IDCUtils.debug("IDCParameterNode.matchModelData() - end: ret = " + ret);

		return ret;

	}

	/***************************************************************************************/

	public String matchTermList(String part) {
		
		IDCUtils.debug("IDCParameterNode.matchList() - start: this = " + this);
		IDCUtils.debug("IDCParameterNode.matchList() - start: part = " + part);
		
		String ret = part;
		
		String[] list = {"Gadgets", "Widgets"};
		
		for(String word : list) {
			if(part.startsWith(word)) {
				value = word;
				ret = part.substring(word.length()).trim();
				break;
			}
		}

		IDCUtils.debug("IDCParameterNode.matchList() - end: this = " + this);
		IDCUtils.debug("IDCParameterNode.matchList() - end: part = " + part);
		IDCUtils.debug("IDCParameterNode.matchList() - end: ret = " + ret);

		return ret;
		
	}	

}
