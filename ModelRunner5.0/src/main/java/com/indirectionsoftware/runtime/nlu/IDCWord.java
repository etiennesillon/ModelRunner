package com.indirectionsoftware.runtime.nlu;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.runtime.IDCError;

public class IDCWord {

	public String stem;
	public int type;

	public boolean isPossesive = false;
	public boolean isPlural = false;
	
	public List<IDCEntityRef> refs = null;
	
	/*******************************************************************************************************/
	
	static final String[][] SINGPLUR_SUFX = {
    	{"ss", "ss"},
    	{"us", "us"},
    	{"ies", "y"},
    	{"ses", "s"},
    	{"sses", "ss"},
    	{"shes", "sh"},
    	{"ches", "ch"},
    	{"xes", "x"},
    	{"zzes", "zz"},
    	{"zes", "z"},
    	{"s", ""}
    };

    static final String[][] SINGPLUR_EXCEPT = {
    	{"men", "man"},
    	{"sizes", "size"},
    	{"women", "woman"},
    	{"sheep", "sheep"},
    	{"mice", "mouse"},
    	{"series", "series"},
    	{"species", "species"},
    	{"deer", "deer"},
    	{"feet", "foot"},
    	{"teeth", "tooth"},
    	{"roofs", "roof"},
    	{"beliefs", "belief"},
    	{"chefs", "chef"},
    	{"chiefs", "chief"},
    	{"gasses", "gas"},
    	{"geese", "goose"},
    	{"children", "child"},
    	{"matrices", "matrix"},
    	{"movies", "movie"},
    };

	static final String[][] VERB_SUFX = {
	    	{"ing", ""},
	    	{"ed", "e"},
	    };

	    static final String[][] VERB_EXCEPT = {
        	{"is", "be"},
        	{"was", "be"},
        	{"red", "red"},
	    };

	/*****************************************************************************/

	public IDCWord (String stem, int type, List<IDCEntityRef> refs) {
		
		this.stem = stem;
		this.type = type;
		this.refs = refs;
		
	}

	/*****************************************************************************/

	public IDCWord (String token) {
		this(token, IDCOntology.UNKNOWN, new ArrayList<IDCEntityRef>());
	}

	/*******************************************************************************************************/
	
	public void addConcept(IDCConcept concept) {
		
		boolean isFound = false;
		
		for(IDCEntityRef ref : refs) {
			if(ref.getConcept().name.equals(concept.name)) {
				isFound = true;
			}
		}
		
		if(!isFound) {
			refs.add(new IDCEntityRef(concept));
		}
		
	}

	/*******************************************************************************************************/
	
	public void addValue(IDCValue value) {
		
		boolean isFound = false;
		
		for(IDCEntityRef ref : refs) {
			if(ref.getValue().value.equals(value.value)) {
				isFound = true;
			}
		}
		
		if(!isFound) {
			refs.add(new IDCEntityRef(value));
		}
		
	}

	/*****************************************************************************/

	public String toString() {
		
		String ret = " Stem = " + stem + (isPossesive ? " (possesive)" : "") + (isPlural ? " (plural)" : "");
		
		for(IDCEntityRef ref : refs) {
			ret +=  " / " + ref;
		}
		
		return ret;
		
	}	

	/*******************************************************************************************************/
	
	public static String getStem(String token) {
		
		String ret = token;
		
		if(token.endsWith("'s")) {
			int i = ret.lastIndexOf("'s");
			ret = ret.substring(0, i);
		} else {
			ret = getStem(token, VERB_EXCEPT, VERB_SUFX);
			if(ret.equals(token) ) {
				ret = getStem(token, SINGPLUR_EXCEPT, SINGPLUR_SUFX);
			}
		}
		
		return ret;
		
	}
	
	/*******************************************************************************************************/
	
	public static String getStem(String s, String[][] excepts, String[][] suffixes) {
		
		String ret = s;

		boolean found = false;
		
		for(String[] exps : excepts) {
			if(ret.equalsIgnoreCase(exps[0])) {
				ret = exps[1];
				found = true;
				break;
			}
		}
		
		if(!found) {
			
			for(String[] sufs : suffixes) {
				if(ret.endsWith(sufs[0])) {
					int i = ret.lastIndexOf(sufs[0]);
					ret = ret.substring(0,  i) + sufs[1];
					break;
				}
			}

		}
		
		return ret;
				
	}

	/*******************************************************************************************************/
	
	public boolean isNumber() {
		
		boolean ret = true;
		
		try {
			Double.parseDouble(stem);
		} catch(NumberFormatException ex) {
        	ret = false;
		}

		return ret;
		
	}

	/*******************************************************************************************************/
	
	public boolean isCanIgnore() {
		return type == IDCOntology.MISC;
	}

}
