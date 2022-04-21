package com.indirectionsoftware.metamodel;

import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;

public class IDCGlobal  extends IDCModelData {
	
	/**************************************************************************************************/
	// Constants ...
	/**************************************************************************************************/
	
	public static final int VALUE=START_ATTR;

	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCGlobal(IDCApplication parent, long id, List<Object> values) {
		super(parent, IDCModelData.GLOBAL, id, values);
	}

	/**************************************************************************************************/

	public void init(IDCData userData) {
		
		if(isInitRequired()) {

			super.init(userData);
			
			completeInit();

		}
		
	}

	/**************************************************************************************************/
	// Global Variables methods ...
	/**************************************************************************************************/
	
}