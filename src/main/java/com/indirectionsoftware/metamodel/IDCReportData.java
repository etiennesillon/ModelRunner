package com.indirectionsoftware.metamodel;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.runtime.IDCEvalData;
import com.indirectionsoftware.utils.IDCCalendar;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCReportData implements Comparable<IDCReportData> {

	private IDCReport report;
	private IDCData data;
	private List<Object> values;
	private List<Object> displayValues;

	
    /************************************************************************/

	public IDCReportData(IDCReport report, IDCData data) {
		
		this.report = report;
		this.data = data;
		
		values = new ArrayList<Object>();
		displayValues = new ArrayList<Object>();

		for(IDCReportField field : report.getReportFields()) {

			Object value = "-= Formula Error =-";
			Object displayValue = value;

    		IDCEvalData evalData = data.getEvalData(field.getFormula());
    		if(evalData != null) {
        		value = evalData.getValue();
        		displayValue = value;
        		IDCAttribute attr = evalData.getAttribute();
        		
        		int type = field.getFieldType();
        		if(type == -1) {
        			type = evalData.getType();
        		}
        		
        		switch(type) {
        		
    				case IDCAttribute.DOMAIN:
    					
    	    			if(value != null) {
    	    				int nDomVal = ((Integer) value).intValue();
    	    				if(nDomVal != -1) {
    	    					displayValue = attr.getRefDomain().getDomainValue(nDomVal).getKey();
    	    				} else {
    	    					displayValue =  "&nbsp;";
    	    				}
    	    			}
    					break;

    				case IDCAttribute.REF:
    				case IDCAttribute.REFBOX:
					case IDCAttribute.REFTREE:
    					if(value instanceof IDCData) {
    						displayValue = ((IDCData) value).getURLId(false);
    					} else {
    						displayValue = "&nbsp;";
    					}
    					break;
    					
    				case IDCAttribute.DATE:
    					displayValue = report.getApplication().getCalendar().displayDateShort((Long)value);
    					break;
    					
    				case IDCAttribute.DATETIME:
    					displayValue = report.getApplication().getCalendar().displayTimeDateShort((Long)value);
    					break;
    					
    				case IDCAttribute.DURATION:
    					displayValue = IDCCalendar.getDaysHoursMinutesString((Long)value);
    					break;
    					
    			}
    			
    		}
    		
			values.add(value);
    		displayValues.add(displayValue);
    	}
		
	}
	
    /************************************************************************/

	public Object get(int nVal) {
		return displayValues.get(nVal);
	}
	
    /************************************************************************/

	public List<Object> getValues() {
		return displayValues;
	}
	
    /************************************************************************/

	public int compareTo(IDCReportData otherData) {
		
		int ret = 0;
		
		//IDCUtils.debug("Comparing " + this.values.get(2) + " to " + ((IDCReportData)comp).values.get(2) );
		//IDCUtils.debug(" >> sortSequence length = " + sortSequence.length);
		
		for(int nCol : report.getSortSequence()) {

			IDCReportField field = report.getReportFields().get(nCol);
			
			Object val1 = values.get(nCol);
			Object val2 = otherData.values.get(nCol);
		
			int test = IDCUtils.compare(val1, val2);

			if(test != -2) {
				if(field.isDescending()) {
					ret = test * (-1);
				} else {
					ret = test;
				}
			}
			
			if(ret != 0) {
				break;
			}

		}
		
		if(ret == 0) {
			ret = -1;
		}

		return ret;

	}

    /************************************************************************/

	public void writeXML(PrintWriter out) {
		
		out.println("<ReportLine>"); 
		
		int nField=0;
		for(IDCReportField field : report.getReportFields()) {

			out.println("<" + field.getHeader() + ">");

			Object value = values.get(nField);
			Object displayValue = displayValues.get(nField);

			if(value instanceof List) {
				writeXMLForList(out, (List<IDCData>) value);
			} else if(value instanceof IDCData) {
				writeXMLForDataObject(out, (IDCData)value);
			} else {
				out.println(value == null ? "(NA)" : value);
			}

			out.println("</" + field.getHeader() + ">");
			
			nField++;

    	}
    	
		out.println("</ReportLine>");

    }

    /************************************************************************/

    public void writeXMLForList(PrintWriter out, List<IDCData> list) {
    	
    	for(IDCData data : list) {
			writeXMLForDataObject(out, data);
    	}
    	
    }

    /************************************************************************/

    public void writeXMLForDataObject(PrintWriter out, IDCData data) {
		data.writeXML(out, true, null);
    }

}
