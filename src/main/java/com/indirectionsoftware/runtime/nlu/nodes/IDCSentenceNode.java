package com.indirectionsoftware.runtime.nlu.nodes;

import java.util.HashMap;
import java.util.Map;

import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCSentenceNode extends IDCNode {
	
	/****************************************************************************************/

	public IDCSentenceNode(String schema) {
		super(null, normaliseString(schema), SENTENCE);
	}
	
	/***************************************************************************************/

	public static String normaliseString(String s) {
		
		String ret = s.trim();
		
//		ret = IDCUtils.replaceAll(ret, ",", "");
		ret = IDCUtils.replaceAll(ret, ", ", " ");
		ret = IDCUtils.replaceAll(ret, ",", " ");
		
		ret = ret.toLowerCase();
		if(ret.endsWith("?")) {
			ret = ret.substring(0, ret.length()-1);
		}

		return ret;
		
	}	

	/*************************************************************/
	
	public static Map<String,String> processSentence(String[][] schemaList, Map<String,String> map) {
		
		Map<String,String> ret = new HashMap<String,String>();
		
		boolean looping = true;
		while(looping) {

			IDCUtils.debug("IDCSentenceNode.processSentence(): looping ... adding map entries");
			for(String key : map.keySet()) {
				String phrase = map.get(key);
				ret.put(key, phrase);
				IDCUtils.debug("IDCSentenceNode.processSentence(): saving key = " + key + " / phrase = " + phrase);
			}

			Map<String,String> newMap = new HashMap<String, String>();
			
			for(String key : map.keySet()) {
				
				String phrase = map.get(key);

				boolean processedKey = false;
				for(String[] schema : schemaList) {

					if(schema[0].equals(key) && !processedKey) {

						IDCUtils.debug("IDCSentenceNode.processSentence(): processing key = " + key + " / phrase = " + phrase);
						IDCNode node = IDCSentenceNode.parseChunk(phrase, schema[1], newMap);
						if(node != null) {
							processedKey = true;
						}

					}
					
				}
		
			}
			
			if(newMap.isEmpty()) {
				looping = false;
			} else {
				map = newMap;
			}
			
		}
		
		return ret;

	}
	
	/***************************************************************************************/

	public static IDCNode parseChunk(String part, String schema, Map<String,String> map) {
		
		IDCUtils.debug("IDCSentenceNode.parseChunk(): phrase = " + part);
		IDCUtils.debug("IDCSentenceNode.parseChunk(): schema = " + schema);

		IDCNode ret = IDCNode.getSentenceNode(schema);

		ret.display(1);

		String s = ret.match(part);
		if(s != null) {
			ret.updateParameterMap(map);
		} else {
			ret = null;
		}
		
		return ret;
		
	}
	
	/***************************************************************************************/

	public String match(String part) {
		
		IDCUtils.debug("IDCSentenceNode.match() - start: this = " + this);
		IDCUtils.debug("IDCSentenceNode.match() - start: part = " + part);
		
		String ret = part;
		
		for(int nChild=0; ret != null && ret.length() > 0 && nChild < children.size(); nChild++) {
			IDCNode node = children.get(nChild);
			IDCUtils.debug("IDCSentenceNode.match(): node = " + node);
			String newPart = node.match(ret); 
			if(newPart != null || node.type != OPTIONAL) {
				ret = newPart;
			}
		}
		
		IDCUtils.debug("IDCSentenceNode.match() - end: this = " + this);
		IDCUtils.debug("IDCSentenceNode.match() - end: part = " + part);
		IDCUtils.debug("IDCSentenceNode.match() - end: ret = " + ret);
		
		return ret;
		
	}

	/***************************************************************************************/

	public String matchParameter(IDCNode child, String part) {
		
		String ret = null;
		
		IDCUtils.debug("IDCSentenceNode.matchParameter() - start: this = " + this);
		IDCUtils.debug("IDCSentenceNode.matchParameter() - start: part = " + part);
		
		String[] words = part.split(" ");
		
		for(IDCNode node : getNextChildren(child)) {
			
			IDCUtils.debug("IDCSentenceNode.matchParameter(): node = " + node);

			boolean looping = true;
			for(int nWord = 0; looping && nWord < words.length; nWord++) {

				String newPart = "";
				for(int n = nWord; n < words.length; n++) {
					newPart += words[n] + " ";
				}
				newPart = newPart.trim();
				
				IDCUtils.debug("IDCSentenceNode.matchParameter(): newPart = " + newPart);
				
				ret = node.matchParameter(newPart);
				if(ret != null) {
					child.value = "";
					for(int n = 0; n < nWord; n++) {
						child.value += words[n] + " ";
					}
					child.value = child.value.trim();
					ret = newPart;
					looping = false;
				}
				
			}
			
			if(looping == false) {
				break;
			}
			
		}
		
		
//		if(curNode != null) {
//			IDCNode node = nextNode;
//			while(node != curNode) {
//				node.isToSkip = true;
//				node = node.nextNode;
//			}
//			
//		}
		
		IDCUtils.debug("IDCSentenceNode.matchParameter() - end: this = " + this);
		IDCUtils.debug("IDCSentenceNode.matchParameter() - end: part = " + part);
		IDCUtils.debug("IDCSentenceNode.matchParameter() - end: ret = " + ret);
		
		return ret;
		
	}

}
