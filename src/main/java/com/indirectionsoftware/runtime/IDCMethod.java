package com.indirectionsoftware.runtime;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.metamodel.IDCApplication;

public abstract class IDCMethod {
	
	public IDCApplication appl;
	public IDCData data;
	
	/**************************************************************************************************/
	
	public void init(IDCApplication appl, IDCData data) {
		this.appl = appl;
		this.data = data;
	}

}
