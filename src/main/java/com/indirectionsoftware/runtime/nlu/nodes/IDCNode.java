package com.indirectionsoftware.runtime.nlu.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCNode {
	
	private static final int DEBUG_LEVEL = 1;
	
	static final int SENTENCE=0, CHOICE=1, OPTIONAL=2, PARAMETER=3, TERM=4, CHOICEVAL=5;
	static final String TYPES[] = {"Sentence", "Choice", "Optional", "Parameter", "Term", "Choice value"};
	
	static final int NORMAL=0, IGNORE_OPTIONAL=1, PROCESS_OPTIONAL_CHILDREN=2;
	static final String OPTIONS[] = {"Normal", "Ignore Optional", "Process Optional Children"};

	int type, id;
	String schema;
	String name;
	
	IDCNode parent;
	List<IDCNode> children = new ArrayList<IDCNode>();
	String value;
	
	boolean isQuestion = false;

//	static HashMap<String, String> terms = new HashMap<String,String>();

	static final String[][] QUESTIONS = {
		{"when","time"},
		{"where","place"},
		{"who","people"},
		{"how many","number"},
	};
	
	static final String[][] KEYWORDS_SEMANTICS = {
			{"in","time", "place"},
			{"everybody","people"},
		};
		
	static final String[] KEYWORDS_IGNORE = {"the", "of", "all", "every"};
		
	static final String[] KEYWORDS_LOGIC = {"and", "same", "'s"};
	
	static final String[] VERBS = {"was", "were", "having", "is"};
	
	static final String QUERY_KEY = "query";
	
	/****************************************************************************************/

	public static IDCNode getNode(IDCNode parent, String schema, int type) {
		
		IDCNode ret = null;
		
		switch(type) {
			
			case SENTENCE:
				ret = new IDCSentenceNode(schema);
				break;
				
			case CHOICE:
				ret = new IDCChoiceNode(parent, schema);
				break;
				
			case CHOICEVAL:
				ret = new IDCChoiceValNode(parent, schema);
				break;
				
			case OPTIONAL:
				ret = new IDCOptionalNode(parent, schema);
				break;
				
			case PARAMETER:
				ret = new IDCParameterNode(parent, schema);
				break;
				
			case TERM:
				ret = new IDCTermNode(parent, schema);
				break;
				
		}
		
		ret.init();
		
		return ret;
		
	}
	
	/****************************************************************************************/

	public static IDCNode getSentenceNode(String schema) {
			return getNode(null, schema, SENTENCE);
	}
		
	/****************************************************************************************/

	public IDCNode(IDCNode parent, String schema, int type) {

		this.type = type;
		this.parent = parent;
		this.schema = schema;

	}
	
	/****************************************************************************************/

	public void init() {
		
		id = (type == SENTENCE ? 0 : parent.children.size());
		
		getNLUNodes(schema);
		
		if(type == PARAMETER && this.children.size() == 0) {
			name = (schema.startsWith("#") ? schema.substring(1, schema.length()) : schema);
		} else if(type != TERM && type != PARAMETER && this.children.size() == 0) {
			String newText = this.schema;
			int newType = TERM;
			if(newText.charAt(0) == '[') {
				newText = newText.substring(1, newText.length()-1);
				newType = CHOICE;
			} else if(newText.charAt(0) == '(') {
				newText = newText.substring(1, newText.length()-1);
				newType = OPTIONAL;
			} else if(newText.charAt(0) == '{') {
				newText = newText.substring(1, newText.length()-1);
				newType = PARAMETER;
			} 
			addChild(newText,newType);
		}  
		
	}
	
	/***************************************************************************************/
	
	private void addChild(String text, int type) {
		children.add(getNode(this, text, type));
	}

	/***************************************************************************************/
	
	private void getNLUNodes(String schema) {
		
		IDCUtils.debug("getNLUNodes: schema = " + schema);
		
 	    String text = "";
		
		int nOption = 0;
		int nChoice = 0;
		
	    for(int nChar = 0; nChar < schema.length(); nChar++) {
	    	
	 	    char c = schema.charAt(nChar);

		    switch(c) {
	 	   
	 	   		case '[':
		 	   		if(type == CHOICE) {
		   				text += c;
		 	   			nChoice++;
		 			} else {
		 	   			if(nOption == 0) {
			 	   			if(nChoice == 0) {
			 	   				text = text.trim();
			 	   				if(text.length() > 0) {
			 	   					addChild(text, TERM);
					 	   	    	text = "";
					 	   	    }
			 	   			} else {
				   				text += c;
			 	   			}
			 	   			nChoice++;
		 	   			} else {
			   				text += c;
		 	   			}
		 			}
	 	   			break;
	 	   			
	 	   		case ']':
		 	   		if(type == CHOICE) {
		   				text += c;
		 	   			nChoice--;
		 			} else {
			 	   		if(nOption == 0) {
			 	   			nChoice--;
			 	   			if(nChoice == 0) {
			 	   				text = text.trim();
			 	   				if(text.length() > 0) {
			 	   					addChild(text, CHOICE);
					 	   	    	text = "";
					 	   	    }
			 	   			} else {
				   				text += c;
			 	   			}
		 	   			} else {
			   				text += c;
		 	   			}
		 			}
	 	   			break;
	 	   			
	 	   		case '(':
		 	   		if(type == CHOICE) {
		   				text += c;
		 	   			nOption++;
		 			} else {
		 	   			if(nChoice == 0) {
			 	   			if(nOption == 0) {
			 	   				text = text.trim();
			 	   				if(text.length() > 0) {
			 	   					addChild(text, TERM);
					 	   	    	text = "";
					 	   	    }
			 	   			} else {
				   				text += c;
			 	   			}
			 	   			nOption++;
		 	   			} else {
			   				text += c;
		 	   			}
		 			}
	 	   			
	 	   			break;
	 	   			
	 	   		case ')':
		 	   		if(type == CHOICE) {
		   				text += c;
	 	   				nOption--;
		 			} else {
		 	   			if(nChoice == 0) {
		 	   				nOption--;
			 	   			if(nOption == 0) {
			 	   				text = text.trim();
			 	   				if(text.length() > 0) {
			 	   					addChild(text, OPTIONAL);
					 	   	    	text = "";
					 	   	    }
			 	   			} else {
				   				text += c;
			 	   			}
		 	   			} else {
			   				text += c;
		 	   			}
		 			}
	 	   			break;
	 	   			
	 	   		case '{':
		 	   		if(type == CHOICE) {
		   				text += c;
		 			} else {
		 	   			if(nChoice == 0 && nOption == 0) {
		 	   				text = text.trim();
		 	   				if(text.length() > 0) {
		 	   					addChild(text, TERM);
				 	   	    	text = "";
				 	   	    }
		 	   			} else {
			   				text += c;
		 	   			}
		 			}

	 	   			break;
	 	   			
	 	   		case '}':
		 	   		if(type == CHOICE) {
		   				text += c;
		 			} else {
		 	   			if(nChoice == 0 && nOption == 0) {
		 	   				text = text.trim();
		 	   				if(text.length() > 0) {
		 	   					addChild(text, PARAMETER);
				 	   	    	text = "";
				 	   	    }
		 	   			} else {
			   				text += c;
		 	   			}
		 			}
	 	   			break;
	 	   			
	 	   		case '|':
		 	   		if(type == CHOICE && nChoice == 0) {
	 	   				text = text.trim();
	 	   				if(text.length() > 0) {
	 	   					addChild(text, CHOICEVAL);
			 	   	    	text = "";
			 	   	    }
		 			} else {
		   				text += c;
		 			}
	   				break;
		 			
	 	   		case '=':
		 	   		if(type == CHOICE && nChoice == 0) {
	 	   				text = text.trim();
	 	   				if(text.length() > 0) {
	 	   					name = text;
			 	   	    	text = "";
			 	   	    }
		 			} else {
		   				text += c;
		 			}
	   				break;
		 					 			
	 			default:
	   				text += c;
	   				break;
 	   			
	 	   }

	    }
	    
		text = text.trim();
	    if(text.length() > 0 && children.size() > 0) {
			addChild(text, (type == CHOICE ? CHOICEVAL : TERM));
	    }
	    	    
	}

	/***************************************************************************************/

	public List<IDCNode> getNextChildren(IDCNode child) {
		
		List<IDCNode> ret = new ArrayList<IDCNode>();
		
		for(IDCNode node : children) {
			if(node.id > child.id) {
				ret.add(node);
			}
		}
		
		return ret;
	}

	/***************************************************************************************/

	public String toString() {
		return TYPES[type] + " -> " + schema + " = " + value;
	}	

	/***************************************************************************************/

	public void display(int level) {
		
		String prefix = "";
		for(int i = 0; i < level; i++) {
			prefix += "  ";
		}

		IDCUtils.debug(prefix + level + ": " + this);
		
		if(children.size() > 0) {
			for(IDCNode child : children) {
				child.display(level+1);
			}
		}
	}	

	/***************************************************************************************/

	static void displayParameters(String prefix, Map<String,String> map) {
		
		IDCUtils.debug(prefix + "Parameters:");
		for(String key : map.keySet()) {
			IDCUtils.debug(prefix + "  " + key + " = " + map.get(key));
		}
		
	}

	/***************************************************************************************/

	public String match(String inputStr) {
		return null;
	}
	
	/***************************************************************************************/

	public String matchParameter(IDCNode child, String part) {
		return null;
	}

	/***************************************************************************************/
	
	public String matchParameter(String part) {
		return match(part);
	}

	/***************************************************************************************/

	void updateParameterMap(Map<String,String> map) {
		
		/////////////////////////////////////////////////////////////////////////////////	
		//TODO: if choice, need to get value from selected child, not choice Node
		/////////////////////////////////////////////////////////////////////////////////	
		
		IDCUtils.debug("updateParameterMap: " + this);
		
		if((type == PARAMETER || type == CHOICE) && value != null) {
			if(name != null && value != null) {
				String oldValue = map.get(name);
				IDCUtils.debug("updateParameterMap: name=" + name + " / oldValue = " + oldValue + " / value = " + value);
				if(oldValue == null) {
					map.put(name, value);
				}
			}
		}

		for(IDCNode child : children) {
			child.updateParameterMap(map);
		}

	}	

	
}
