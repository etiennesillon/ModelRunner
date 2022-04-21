package com.indirectionsoftware.runtime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCXMLImportParser  {
	
	IDCApplication appl;
	HashMap<String, String> objectMap;
	
	private IDCData data;
	private IDCType type;
	private IDCAttribute attr;
	private List<IDCDataRef> refs;
	HashMap<String, IDCImportData> linkedData = new HashMap<String, IDCImportData>();
	List<IDCImportDataAttr> linkedDataAttrs;
	
	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCXMLImportParser(IDCApplication appl) {
		this.appl = appl;
	}
	
	/************************************************************************************************/

	public IDCError importXML(String xml) {

		IDCError ret = null;
		
    	try {
    		InputStream stream = new ByteArrayInputStream(xml.getBytes());
    		ret = importXML(stream);
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		ret = new IDCError(IDCError.IMPORT_ERROR, "Import error ...");
    	}

    	return ret;
    	
}
	
	/************************************************************************************************/

	public IDCError importXML(File file) {
		
		IDCError ret = null;
		
    	try {
    		InputStream stream = new FileInputStream(file);
    		ret = importXML(stream);
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		ret = new IDCError(IDCError.IMPORT_ERROR, "Import error ...");
    	}

    	return ret;
    	
	}

	/************************************************************************************************/

	public IDCError importXML(InputStream stream) {
		
		IDCError ret = null;
		
    	try {

    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //factory.setValidating(true);   
            //factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(stream);
            
            importAllObjects(document.getFirstChild());
            
            updateLinks();
    	
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		ret = new IDCError(IDCError.IMPORT_ERROR, "Import error ...");
    	}

    	return ret;
    	
	}

	/************************************************************************************************/

	private void importAllObjects(Node node) {
		
    	Node childNode = node.getFirstChild();
        if(childNode != null) {
            if(!childNode.getNodeName().equals("#text")) {
            	importObject(childNode);
            }
            while((childNode = childNode.getNextSibling()) != null) {
                if(!childNode.getNodeName().equals("#text")) {
                	importObject(childNode);                    
            	}
            }
        }
	}

	/************************************************************************************************/

	private void importObject(Node node) {

		String nodeType = node.getNodeName();
        NamedNodeMap nodeAttributes = node.getAttributes();
		
		type = null;

		String typeName = IDCUtils.getXMLAttrValue(nodeAttributes, "type");  
		int  typeId = IDCUtils.translateInteger(IDCUtils.getXMLAttrValue(nodeAttributes, "typeId")); 
		type = appl.getType(typeName);
		
		if(type != null) {
			
			int  oldId = IDCUtils.translateInteger(IDCUtils.getXMLAttrValue(nodeAttributes, "id")); 
			
			IDCUtils.debugNow("\nImporting Object " + type + " oldIdId=" + oldId);

			data = type.getNewObject();
			linkedDataAttrs = new ArrayList<IDCImportDataAttr>();
			
	    	Node childNode = node.getFirstChild();
	        while(childNode != null && childNode.getNodeName().equals("#text")) {
	        	childNode = childNode.getNextSibling();
	        }
	        if(childNode != null) {
	            childNode = childNode.getFirstChild();
	        }
	        if(childNode != null) {
	            if(!childNode.getNodeName().equals("#text")) {
	            	importAttribute(childNode);
	            }
	            while((childNode = childNode.getNextSibling()) != null) {
	                if(!childNode.getNodeName().equals("#text")) {
	                	importAttribute(childNode);
	                }
	            }
	        }
	        
	        data.save();
	        
    		IDCImportData importData = new IDCImportData();
    		IDCDataRef dataRef = new IDCDataRef(type.getEntityId(), oldId);
    		importData.parentRef = dataRef;
    		importData.newId = data.getId();
    		
    		if(linkedDataAttrs.size() > 0) {
	    		importData.attrs = linkedDataAttrs;
	        }
    	
    		linkedData.put(""+dataRef,importData);

    		IDCUtils.debugNow("Linked data = " + importData);

		} else {
			IDCUtils.error("\nIMPORT ERROR: could not import Object ... unknown type: type = " + typeName + " typeId=" + typeId);
		}

    
	}

	/************************************************************************************************/

	private void importAttribute(Node node) {
		
		String nodeType = node.getNodeName();
        NamedNodeMap nodeAttributes = node.getAttributes();

        attr = null;
        
    	String attrName = IDCUtils.getXMLAttrValue(nodeAttributes, "name");
		String attrIdStr = IDCUtils.getXMLAttrValue(nodeAttributes, "attributeId"); 
		attr = type.getAttribute(attrName);
    	
		if(attr != null) {

			if(attr.isLinkedForward()) {

	        	refs = new ArrayList<IDCDataRef>();
		    	
		    	Node childNode = node.getFirstChild();
		        while(childNode != null && childNode.getNodeName().equals("#text")) {
		        	childNode = childNode.getNextSibling();
		        }
		        if(childNode != null) {
		            if(!childNode.getNodeName().equals("#text")) {
		            	importAttributeRef(childNode);
		            }
		            while((childNode = childNode.getNextSibling()) != null) {
		                if(!childNode.getNodeName().equals("#text")) {
		                	importAttributeRef(childNode);
		                }
		            }

		        }

		        if(refs.size() > 0) {
		    		IDCUtils.debugNow("Importing Attribute " + attrName + ":");
		    		IDCImportDataAttr attrData = new IDCImportDataAttr();
		    		attrData.attrId = attr.getAttributeId();
		    		attrData.children = refs;
		    		linkedDataAttrs.add(attrData);
		    		for(IDCDataRef ref : refs) {
			    		IDCUtils.debugNow("  ref = " + ref);
		    		}
		        }

				
			} else {
				
				String value = null;
		    	
		    	Node childNode = node.getFirstChild();
		        if(childNode != null && childNode.getNodeName().equals("#text")) {
		        	value = childNode.getNodeValue();
		        }

		        if(value != null) {
		    		IDCUtils.debugNow("Importing Attribute " + attrName + "=" + value);
		    		if(attr.isDomain()) {
			    		int domainValue = Integer.parseInt(value);
		    			data.set(attr.getAttributeId(), domainValue);
		    		} else {
			    		data.set(attr.getAttributeId(), value);
		    		}
		        }

			}
			
		} else {
			IDCUtils.error("\nIMPORT ERROR: could not import Atribute ... unknown Attribute : attribute = " + attrName + " attributeId=" + attrIdStr);
		}
    	
	}

	/************************************************************************************************/

	private void importAttributeRef(Node node) {
		
		String nodeType = node.getNodeName();
        NamedNodeMap nodeAttributes = node.getAttributes();

		IDCType refType = appl.getType(IDCUtils.getXMLAttrValue(nodeAttributes, "type"));
		long id = IDCUtils.translateLong(IDCUtils.getXMLAttrValue(nodeAttributes, "id"));

		if(refType != null && id != -1) {
			refs.add(new IDCDataRef(refType.getEntityId(), id));
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