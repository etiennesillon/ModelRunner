package com.indirectionsoftware.metamodel;

import java.io.PrintWriter;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCReportField extends IDCModelData {
	
	/**************************************************************************************************/
	// Constants ...
	/**************************************************************************************************/
	
	public static final int FORMULA=START_ATTR, HEADER=START_ATTR+1, SORTSEQUENCE=START_ATTR+2, ISDESCENDING=START_ATTR+3, ISHIDDEN=START_ATTR+4,
	                        TYPE=START_ATTR+5, WIDTH=START_ATTR+6;

	private static final int DEFAULT_WIDTH = 10;
	
	String formula, header;
	int sortSeq, fieldType, width;
	boolean isHidden, isDescending;

	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCReportField(IDCReport parent, long id, List<Object> values) {
		super(parent, IDCModelData.REPORTFIELD, id, values);
	}
	
	/**************************************************************************************************/
	// Init processing ...
	/**************************************************************************************************/

	public void init(IDCData userData) {
		
		if(isInitRequired()) {

			super.init(userData);
			
			formula = getString(FORMULA);
			header = getString(HEADER);
			
			if(header.length() == 0) {
				header = getName();
			}
			
			sortSeq = -1;
			String seq = getString(SORTSEQUENCE);
			if(seq != null && seq.length()>0) {
				sortSeq = Integer.parseInt(seq);
			}
			
			width = getInt(WIDTH, DEFAULT_WIDTH);

			isHidden = IDCUtils.translateBoolean(getString(ISHIDDEN));
			isDescending = IDCUtils.translateBoolean(getString(ISDESCENDING));
			
			fieldType = IDCAttribute.decodeAttributeType(getString(TYPE));

		}
	
	}
	
	/**************************************************************************************************/
	// Report Field methods ...
	/**************************************************************************************************/
	
	public String getFormula() {
		return formula;
	}
	
	public String getHeader() {
		
		String ret = header;
		
		if(sortSeq != -1) {
			ret += isDescending ? "&darr;" : "&uarr;"; 
		}
		return ret;
	}

	public int getSortSequence() {
		return sortSeq;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public boolean isDescending() {
		return isDescending;
	}

	public int getFieldType() {
		return fieldType;
	}

	public int getWidth() {
		return width;
	}

}