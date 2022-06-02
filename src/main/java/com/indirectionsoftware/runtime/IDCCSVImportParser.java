package com.indirectionsoftware.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCReference;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCCSVImportParser  {
	
	IDCApplication appl;
	HashMap<String, String> objectMap;
	
	private IDCData data;
	private IDCType type;
	private IDCAttribute attr;
	private List<IDCDataRef> refs;
	HashMap<String, IDCImportData> linkedData = new HashMap<String, IDCImportData>();
	List<IDCImportDataAttr> linkedDataAttrs;

	private List<String> headers;
	
	private static final int TYPEID=0, ID=1, PARENTNS=2;
	
	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCCSVImportParser(IDCApplication appl) {
		this.appl = appl;
	}
	
	/************************************************************************************************/

	public IDCError importCSV(File file) {
		
		IDCError ret = null;
		
    	try {
    		List<List<String>> lines = IDCUtils.parseCSVFile(file);
    		headers = lines.get(0);
    		for(int maxLines = lines.size(), nLine=1; nLine < maxLines; nLine++) {
    			importCSVLine(lines.get(nLine));
    		}
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		ret = new IDCError(IDCError.IMPORT_ERROR, "Import error ...");
    	}

    	return ret;
    	
	}

	/************************************************************************************************/

	private void importCSVLine(List<String> cols) {
		
		type = null;

		int  typeId = IDCUtils.translateInteger(cols.get(TYPEID)); 
		type = appl.getType(typeId);
		
		if(type != null) {
			
			int  id = IDCUtils.translateInteger(cols.get(ID)); 
			
			IDCUtils.debug("\nimportCSVLine(): " + type + " id=" + id);
			
			linkedDataAttrs = new ArrayList<IDCImportDataAttr>();

			if(id == -1) {
				data = type.getNewObject();
			} else {
				data = type.loadDataObject(id);
			}

			for(int nCol=3; nCol < headers.size(); nCol++) {
		    	String attrName = headers.get(nCol);
		    	String value = cols.get(nCol);
		    	importAttribute(attrName, value);
			}

	        data.save();
	        
    		IDCImportData importData = new IDCImportData();
    		IDCDataRef dataRef = new IDCDataRef(type.getEntityId(), id);
    		importData.parentRef = dataRef;
    		importData.newId = data.getId();
    		
    		if(linkedDataAttrs.size() > 0) {
	    		importData.attrs = linkedDataAttrs;
	        }
    	
    		linkedData.put(""+dataRef,importData);

    		IDCUtils.debug("Linked data = " + importData);

	        
		} else {
			IDCUtils.error("\nIMPORT ERROR: could not import Object ... unknown type: typeId=" + typeId);
		}

    
	}

	/************************************************************************************************/

	private void importAttribute(String attrName, String value) {
		
		attr = type.getAttribute(attrName);

		IDCUtils.debug("Importing Attribute " + attrName + "=" + value);
    	
		if(attr != null) {

			if(attr.isLinkedForward()) {
				
				List<IDCReference> refdTypes = attr.getReferences();
				if(refdTypes.size() == 1) {
					IDCType refdType = refdTypes.get(0).getDataType();
					IDCData refdData = refdType.loadDataByName(value, true);
		    		data.set(attr.getAttributeId(), refdData);
				} else {
					IDCUtils.debug("Ignoring complex linked attributes (more than 1 reference)");
				}

			} else {
				
		        if(value != null) {
		    		if(attr.isDomain()) {
			    		int domainValue = Integer.parseInt(value);
		    			data.set(attr.getAttributeId(), domainValue);
		    		} else {
			    		data.set(attr.getAttributeId(), value);
		    		}
		        }

			}
			
		} else {
			IDCUtils.error("\nIMPORT ERROR: could not import Atribute ... unknown Attribute : attribute = " + attrName);
		}
    	
	}

	/************************************************************************************************/

	private void updateLinks() {
		
		for(IDCImportData importData : linkedData.values()) {
			
			IDCType type = appl.getType(importData.parentRef.getTypeId());
			IDCData data = type.loadDataObject(importData.newId);
			
			if(importData.attrs != null) {
				
				for(IDCImportDataAttr importDataAttr : importData.attrs) {
					
					List<IDCData> dataRefs = new ArrayList<IDCData>();
					
					for(IDCDataRef child : importDataAttr.children) {
						IDCDataRef ref = null;
						IDCImportData refedImportData = linkedData.get(""+child);
						if(refedImportData != null) {
							IDCUtils.debug("Found refed data in import = " + refedImportData.parentRef + " / newid= " + refedImportData.newId);
							ref = new IDCDataRef(refedImportData.parentRef.getTypeId(), refedImportData.newId);
						} else {
							IDCUtils.debug("refed data not found in import, using existing ref = " + child);
							ref = child;
						}
						if(ref != null) {
							IDCData refedData = appl.loadDataRef(ref); 
							if(refedData != null) {
								dataRefs.add(refedData);
							} else {
								IDCUtils.debug("\nIMPORT ERROR: imported data references unimported data that does not exist already in the database: ref " + ref);
							}
						}
					}
					
					if(dataRefs.size() > 0) {
						data.set(importDataAttr.attrId, dataRefs);
					}

				}

			}
			
			data.save();
		
		}
		
	}

	/************************************************************************************************/

	public class IDCImportData {
	
		IDCDataRef parentRef;
		long newId;
		List<IDCImportDataAttr> attrs;
		
		/************************************************************************************************/

		public String toString() {

			String ret = "IDCImportData: " + parentRef + " newId = " + newId + "\n";
			
			if(attrs != null) {
				for(IDCImportDataAttr attr : attrs) {
		    		ret += attr + "\n";
	    		}
			}
			
			return ret;

		}

	}
	
	/************************************************************************************************/

	public class IDCImportDataAttr {
		
		int attrId;
		List<IDCDataRef> children;
		
		/************************************************************************************************/

		public String toString() {

			String ret = "IDCImportDataAttr: attrId = " + attrId;
			
			for(IDCDataRef child : children) {
	    		ret += child + " ";
    		}
			
			return ret;

		}

	}
	
}