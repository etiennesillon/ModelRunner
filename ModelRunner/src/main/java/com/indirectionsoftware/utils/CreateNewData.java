package com.indirectionsoftware.utils;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCMethod;

public class CreateNewData extends IDCMethod {
	
	public void run(String typeName, Long num) {
		
		IDCUtils.debug("CreateNewItems() ! typeName = " + typeName + " / num = " + num);
		
		IDCType type = appl.getType(typeName);
		
		for(int i=0; i<num; i++) {
			IDCData testData = type.getNewTestObject();
		}
		
	}

}
