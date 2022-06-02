package com.indirectionsoftware.runtime.nlu;

import java.util.List;

public class IDCTermOLD {

	int id;
	public String name;
	public List<IDCConcept> concepts;
	
	/*****************************************************************************/

	public IDCTermOLD(int id, String name, List<IDCConcept> concepts) {
		this.id = id;
		this.name = name;
		this.concepts = concepts;
	}

	/*****************************************************************************/

	public String toString() {
		
		String ret = "Term Name = " + name + " / id = " + id + " concepts = {";
		
		for(IDCConcept concept : concepts) {
			ret += " " + concept + "";
		}
		
		ret += " }";
		
		return ret;
		
	}
	
}
