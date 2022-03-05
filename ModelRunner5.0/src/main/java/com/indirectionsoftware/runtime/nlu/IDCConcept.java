package com.indirectionsoftware.runtime.nlu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.metamodel.IDCModelData;

public class IDCConcept implements Serializable {
	
	public String name;

	public List<IDCModelData> modelEntities = new ArrayList<IDCModelData>();
	public List<IDCConceptMap> maps = new ArrayList<IDCConceptMap>();

	/*****************************************************************************/

	public IDCConcept(String name) {
		this.name = name;
	}

	/*****************************************************************************/

	public void addModelEntity(IDCModelData modelEntity) {
		
		boolean isFound = false;
		
		for(IDCModelData entity : modelEntities) {
			if(entity.equals(modelEntity)) {
				isFound = true;
			}
		}
		
		if(!isFound) {
			modelEntities.add(modelEntity);
		}
	}
		
	/*****************************************************************************/

	public void addModelEntityMap(IDCModelData modelEntity, String query) {
		
		boolean isFound = false;
		
		for(IDCConceptMap map : maps) {
			if(map.entity.equals(modelEntity)) {
				isFound = true;
			}
		}
		
		if(!isFound) {
			maps.add(new IDCConceptMap(modelEntity, query));
		}
		
	}
		
	/*****************************************************************************/

	public String toString() {
		
		String ret = "IDCConcept: " + name + " -> {";
		
		for(IDCModelData  ent : modelEntities) {
			ret += " " + ent.toString() + "";
		}
		
		ret += " } -> {";
		
		for(IDCModelData  ent : modelEntities) {
			ret += " " + ent.toString() + "";
		}
		
		ret += " }";
		
		return ret;
		
	}

}
