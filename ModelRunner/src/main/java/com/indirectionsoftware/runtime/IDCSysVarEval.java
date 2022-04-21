package com.indirectionsoftware.runtime;

import java.util.List;

public class IDCSysVarEval {
	
	int kwType=-1;
	int kwIndex=-1;
	List<String> funcParam=null;

    /************************************************************************************************/

	public IDCSysVarEval(int kwType, int kwIndex, List<String> funcParam) {
		this.kwType = kwType;
		this.kwIndex = kwIndex;
		this.funcParam = funcParam;
	}

}
	
