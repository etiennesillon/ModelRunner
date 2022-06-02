package com.indirectionsoftware.metamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;

public class IDCReport extends IDCModelData {
	
	/**************************************************************************************************/
	// Constants ...
	/**************************************************************************************************/
	
	public final static int TYPE=START_ATTR, SOURCE=START_ATTR+1, CLASS=START_ATTR+2, STYLESHEET=START_ATTR+3,
							 SELECTION_FORMULA=START_ATTR+4, FIELDS=START_ATTR+5;
	
	private IDCType type;
	private String source, reportClass, stylesheet, tempFileName, dirName;
	private List<IDCReportField> fields;

	private String selectionFormula;

	private int[] sortSequence;
	
	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCReport(IDCReportFolder parent, long id, List<Object> values) {
		super(parent, IDCModelData.REPORT, id, values);
	}
	
	/**************************************************************************************************/
	// Init processing ...
	/**************************************************************************************************/

	public void init(IDCData userData) {
		
		if(isInitRequired()) {

			super.init(userData);
			
			List<IDCReference> refs = (List<IDCReference>) getList(TYPE);
			for(IDCReference ref : refs) {
				ref.init(userData);
			}
			
			if(refs.size() > 0) {
				type = refs.get(0).getDataType();
			}
			
			source = getString(SOURCE);
			reportClass = getString(CLASS);
			stylesheet = getString(STYLESHEET);
			selectionFormula = getString(SELECTION_FORMULA);
			
	    	int nCol = 0, maxSeq = 0;
	    	fields = (List<IDCReportField>) getList(FIELDS);
	    	int[] tempSeqs = new int[fields.size()+1];
	    	for(IDCReportField field : fields) {
				field.init(userData);
	    		int seq = field.getSortSequence();
	    		if(seq != -1) {
	    			tempSeqs[seq] = nCol;
	    			if(seq > maxSeq) {
	    				maxSeq = seq;
	    			}
	    		}
	    		nCol++;
			}
	
			sortSequence = new int[maxSeq];
	    	for(int i=0; i<maxSeq; i++) {
	    		sortSequence[i] = tempSeqs[i+1];
	    	}
	    	
			
			completeInit();
			
		}
	
	}
	
	/**************************************************************************************************/
	// Report methods ...
	/**************************************************************************************************/
	
	public String getReportClass() {
		return reportClass;
	}
	
	/**************************************************************************************************/
	
	public String getSource() {
		return source;
	}
	
	/**************************************************************************************************/
	
	public String getStylesheet() {
		return stylesheet;
	}
	
	/**************************************************************************************************/
	
	public String getSelectionFormula() {
		return selectionFormula;
	}
	
	/**************************************************************************************************/
	
	public IDCType getReportType() {
		return type;
	}
	
	/**************************************************************************************************/
	
	public List<IDCReportField> getReportFields() {
		return fields;
	}
	
	/**************************************************************************************************/
	
	public List<String> getColumnHeaders() {
    	
		List<String> ret = new ArrayList<String>();
    	
    	for(IDCReportField field : getReportFields()) {
    		ret.add(field.getHeader());
    	}
    	
    	return ret;
    	
	}
	
    /************************************************************************/

	public int[] getSortSequence() {
		return sortSequence;
	}

    /************************************************************************/

    public List<IDCReportData> execute() {
    	
    	List<IDCReportData> ret = new ArrayList<IDCReportData>();
    	
    	TreeSet<IDCReportData> rows = new TreeSet<IDCReportData>();
    	
    	for(IDCData data : type.loadAllDataObjects(selectionFormula)) {
    		rows.add(new IDCReportData(this, data));
    	}
    	
    	for(IDCReportData reportData : rows) {
    		ret.add(reportData);
    	}
    	
    	return ret;
    	
    }

	/**************************************************************************************************/

   	public void write(PrintWriter out) {
		
   		if(getStylesheet().length() > 0) {
   	   		
			generateXMLFile();
	        
			File xmlFile = new File(getXMLFileName());
	        File xsltFile = new File(getDirectoryName() + getStylesheet());
	 
	        Source xmlSource = new StreamSource(xmlFile);
	        Source xsltSource = new StreamSource(xsltFile);
	        Result result = new StreamResult(out);
	 
	        TransformerFactory transFact = TransformerFactory.newInstance(  );
	 
	        Transformer trans;
			try {
				trans = transFact.newTransformer(xsltSource);
		        trans.transform(xmlSource, result);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				deleteXMLFile();
			}
			
   		} else {
   			writeDefaultHTML(out);
   		}

	}

	/**************************************************************************************************/
	
	public void generateXMLFile() {
		
		try {
			tempFileName = getDirectoryName() + getName() + System.currentTimeMillis() + ".xml";
			File tempFile = new File(tempFileName);
			PrintWriter tempWriter = new PrintWriter(tempFile);
			writeXML(tempWriter);
			tempWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	/**************************************************************************************************/
	
	public void deleteXMLFile() {
		
		File tempFile = new File(tempFileName);
		tempFile.delete();

	}

	/**************************************************************************************************/
	
	public String getXMLFileName() {
		return tempFileName;
	}

	/**************************************************************************************************/
	
	public void setDirectoryName(String dirName) {
		this.dirName = dirName;
	}

	/**************************************************************************************************/
	
	public String getDirectoryName() {
		return dirName;
	}

	/**************************************************************************************************/
	
	public void writeXML(PrintWriter out) {
		
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		if(stylesheet.length() > 0) {
			out.print("<?xml-stylesheet type=\"text/xsl\" href=\"" + stylesheet + "\"?>");
		}
		
		out.println("<Report>");
		
		for(IDCReportData reportData : execute()) {
			reportData.writeXML(out);
		}

		out.println("</Report>");
		
	}

    /************************************************************************/

   	public void writeDefaultHTML(PrintWriter out) {    	

   		boolean isGrey=false;

   		out.println("<table><thead><tr>");
    	
    	for(String header : getColumnHeaders()) {
    		out.println("<th>" + header + "</th>");
		}
    	
    	out.println("</thead></tr><tbody>");
    	
    	for(IDCReportData reportData : execute()) {
    		
    		out.println("<tr " + (isGrey ? "bgcolor=\"#ececec\"" : "") + ">");
        	isGrey = !isGrey;
        	
        	for(Object value : reportData.getValues()) {
        		writeDefaultHTMLForAttributeValue(out, value);
    		}

        	out.println("</tr>");
    		
    	}
    	
    	out.println("</tbody></table>");
    	
    }

    /************************************************************************/

    public void writeDefaultHTMLForAttributeValue(PrintWriter out, Object value) {
    	
    	out.println("<td>");

		if(value instanceof List) {
			writeDefaultHTMLForList(out, (List<IDCData>) value);
		} else if(value instanceof IDCData) {
			writeDefaultHTMLForDataObject(out, (IDCData)value);
		} else {
			out.println(value == null ? "(NA)" : value);
		}
    	
    	out.println("</td>");
    	
    }

    /************************************************************************/

    public void writeDefaultHTMLForList(PrintWriter out, List<IDCData> list) {
    	
    	out.println("<table border=1>");
    	
    	for(IDCData data : list) {
        	out.println("<tr>");
        	writeDefaultHTMLForDataObject(out, data);
        	out.println("<tr>");
        }
    	
    	out.println("</table>");
    	
    }

    /************************************************************************/

    public void writeDefaultHTMLForDataObject(PrintWriter out, IDCData data) {
    	out.println(data.getURLId(false));
    }

}