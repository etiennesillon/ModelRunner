package com.indirectionsoftware.runtime.nlu;

import java.io.Serializable;

public class IDCEntityRef implements Serializable {
		
	private IDCConcept concept = null;
	private IDCValue value = null;
	
	/*****************************************************************************/

	public IDCEntityRef(IDCConcept concept) {
		this.concept = concept;
	}

	/*****************************************************************************/

	public IDCEntityRef(IDCValue value) {
		this.value = value;
	}

	/*****************************************************************************/

	public boolean isConcept() {
		return (concept == null ? false : true);
	}

	/*****************************************************************************/

	public boolean isValue() {
		return (value == null ? false : true);
	}

	/*****************************************************************************/

	public String toString() {
		return "" + (concept == null ? value : concept);
	}

	/*****************************************************************************/

	public IDCConcept getConcept() {
		return concept;
	}
	
	/*****************************************************************************/

	public IDCValue getValue() {
		return value;
	}
	
}
