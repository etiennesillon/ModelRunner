package com.indirectionsoftware.runtime.nlu;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCEvalData;

public class IDCLanguageQuestion {

	public IDCType	type;
	public List<IDCAttributeValueDataList> selectionValues = new ArrayList<IDCAttributeValueDataList>();
	public List<IDCAttribute> requestedAttrs = new ArrayList<IDCAttribute>();
	
	/*****************************************************************************/

	public String toString() {
		
		String ret = "Question: type = " + type + " {\n";
		
		ret += " selectionValues {\n";
		for(IDCAttributeValueDataList value : selectionValues) {
			ret += "  " + value + "\n";
		}
		ret += " }\n";

		ret += " requestedAttrs {\n";
		for(IDCAttribute attr : requestedAttrs) {
			ret += "  " + attr + "\n";
		}
		ret += " }\n";

		ret += "}";
		
		return ret;
	}
	
	/*****************************************************************************/

	public List<List<IDCEvalData>> answer() {
		
		List<List<IDCEvalData>> ret = new ArrayList<List<IDCEvalData>>();
		
		for(IDCData data: type.search(selectionValues)) {
			List<Object> res = new ArrayList<Object>();
			for(IDCAttribute attr : requestedAttrs) {
				Object value = data.getValue(attr);
				res.add(new IDCEvalData(value, data, attr));
				System.out.println("answer = " + value);
			}
		}

		return ret;

	}
	
}
