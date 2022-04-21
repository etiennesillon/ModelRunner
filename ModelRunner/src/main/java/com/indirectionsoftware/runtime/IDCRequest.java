package com.indirectionsoftware.runtime;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataParentRef;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCType;

public class IDCRequest {

	private IDCType type;
	private String selectionFormula;
	private List<String> resultFormulas;
	private IDCDataParentRef parentRef = null;
	
    /************************************************************************/

	public IDCRequest(IDCType type) {
		this(type, null);
	}

	public IDCRequest(IDCType type, IDCDataParentRef parentRef) {
		this.type = type;
		this.parentRef = parentRef;
		selectionFormula = null;
		resultFormulas = new ArrayList<String>();
	}

    /************************************************************************/

	public void setSelectionFormula(String selectionFormula) {
		this.selectionFormula = selectionFormula;
	}

    /************************************************************************/

	public void addResultFormula(String formula) {
		resultFormulas.add(formula);
	}

    /************************************************************************/

    public List<List<Object>> execute() {
    	
    	List<List<Object>> ret = new ArrayList<List<Object>>();
    	 
    	for(IDCData data : type.loadAllDataObjects(selectionFormula, parentRef)) {
    		
    		List<Object> values = new ArrayList<Object>();
    		
        	for(String formula : resultFormulas) {
        		Object object = data.evaluate(formula);
    			values.add(object);
        	}

			ret.add(values);

    	}
    	
    	return ret;
    	
    }

    /************************************************************************

	public String buildWhereString() {
		
		String ret = "";
		boolean isAndNeeded = false;
		
		Set<IDCAttribute> attrs = keyValues.keySet();
		
		for(IDCAttribute attr : attrs) {
			IDCRequestOperValuePair operValPair = keyValues.get(attr);
			if(isAndNeeded) {
				ret += " AND ";
			}
			isAndNeeded = true;
			ret += attr.getColName() + operValPair;
		}
		
		return ret;
		
	}
	
    ************************************************************************/
}
