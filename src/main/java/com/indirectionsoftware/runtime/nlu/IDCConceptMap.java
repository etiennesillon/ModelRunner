package com.indirectionsoftware.runtime.nlu;

import com.indirectionsoftware.metamodel.IDCModelData;

public class IDCConceptMap {
	
	public String query;
	public IDCModelData entity;

	/*****************************************************************************/

	public IDCConceptMap(IDCModelData entity, String query) {
		this.entity = entity;
		this.query = query;
	}

	/*****************************************************************************/

	public String toString() {
		
		String ret = "IDCConceptMap: entity = " + entity + " / query= " + query;
		
		return ret;
		
	}

}
