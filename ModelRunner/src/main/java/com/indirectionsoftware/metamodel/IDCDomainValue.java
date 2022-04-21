package com.indirectionsoftware.metamodel;

import java.io.PrintWriter;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;

public class IDCDomainValue extends IDCModelData {
	
	private String key;
	private int index=-1;
	
	public static int KEY=START_ATTR;

	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCDomainValue(IDCDomain parent, long id, List<Object> values) {
		super(parent, IDCModelData.DOMAINVALUE, id, values);
	}

	/**************************************************************************************************/

	public void init(IDCData userData, int index) {
		
		if(isInitRequired()) {

			super.init(userData);
			
			key = getString(KEY);
			this.index = index;
			
			completeInit();

		}
		
	}

	/**************************************************************************************************/
	// Domain Values methods ...
	/**************************************************************************************************/
	
    public String getKey() {
    	return key;
    }

    public int getIndex() {
    	return index;
    }

}