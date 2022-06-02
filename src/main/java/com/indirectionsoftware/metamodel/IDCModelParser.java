package com.indirectionsoftware.metamodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.indirectionsoftware.backend.database.IDCDbManager;
import com.indirectionsoftware.utils.IDCUtils;
import com.indirectionsoftware.utils.IDCVector;

public class IDCModelParser {

	Document document;
	
	private IDCMetaModel metaModel;

	long entityIds[];

	IDCDbManager dbManager;
	
	private int id=-1;
	
	/************************************************************************************************/

    public IDCModelParser(String xml, IDCDbManager dbManager) {
    	metaModel = IDCMetaModel.loadMetaModel(xml);
    	this.dbManager = dbManager;
	}

	/************************************************************************************************/

    public IDCModelParser(File metaModelFile,IDCDbManager dbManager) {
    	metaModel = IDCMetaModel.loadMetaModel(metaModelFile);
    	this.dbManager = dbManager;
	}
    
	/************************************************************************************************/

    public IDCVector loadModel(InputStream stream) throws Exception {
    	
    	entityIds = new long[getMetaModel().getEntityCount()];
    	for(int nId=0, maxIds=entityIds.length; nId < maxIds; nId++) {
    		entityIds[nId]=0;
    	}

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //factory.setValidating(true);   
        //factory.setNamespaceAware(true);
        
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(stream);
        
        IDCVector ret = processNodeUsingMetaModel(document.getFirstChild(), null, -1);

        return ret;
        
    }
    
    /************************************************************************************************/

    public IDCApplication loadApplication(String modelXML) {
    	
		IDCApplication ret = null;
		
		if(modelXML != null && modelXML.length() > 0) {
	    	try {
				ret = (IDCApplication) loadModel(new ByteArrayInputStream(modelXML.getBytes()));
				if(ret != null) {
	        		ret.setModelXML(modelXML);
				}

	    	} catch(Exception ex) {
	    		ex.printStackTrace();
	    	}
		}
		
    	return ret;
    	
    }

    /************************************************************************************************/

    public IDCMetaModel getMetaModel() {
    	return metaModel;
    }
    
    /************************************************************************************************/

    public IDCVector processNodeUsingMetaModel(Node node, IDCVector parent, int isDebug) throws Exception {

    	IDCVector modelData = null;
    	
    	IDCReference systemRef = null;

        String nodeType = node.getNodeName();    
    	
        IDCUtils.debug("\nProcessing entity node " + nodeType);
        
        if(nodeType.equalsIgnoreCase("DatabaseRef")) {
            IDCUtils.debug("Debug here !!!!");  // set breakpoint here ;)
        }
        
        IDCMetaModelEntity entity = getMetaModelEntity(nodeType);
    
        List<Object> entityAttributes = new ArrayList<Object>();
        
        if(entity != null) {
        	
            NamedNodeMap nodeAttributes = node.getAttributes();
            
            for(int i=0; i < nodeAttributes.getLength(); i++) {
            	Node n = nodeAttributes.item(i);
                IDCUtils.debug("node attribute = " + n);
            }
            
            // debug Attribute with name = LinkSystemUser for node = Action in node = Type
            
            // Identify processing Type node
            if(nodeType.equals("Type")) {
            	isDebug = 1;// identify original node (to stop after processing children)
            }
            
            if(isDebug == 1 && nodeType.equals("Action")) {
            	isDebug = 2;
                IDCUtils.debug("Debug here !!!!");  // set breakpoint here ;)
            }
            
            String xmiIdStr = IDCUtils.getXMLAttrValue(nodeAttributes, "xmi.id");
    		int xmiId = (int)(xmiIdStr == null || xmiIdStr.length() == 0 ? -1 : Integer.parseInt(xmiIdStr));
            
        	modelData = IDCModelData.getInstance((IDCModelData) parent, entity.getId(), xmiId, null, this);
        	
            for(IDCMetaModelAttribute metaModelAttribute : entity.getAttributes()) {
            	
            	String metaModelAttributeName = metaModelAttribute.getName();
            	
            	if( metaModelAttributeName.equals("DatabaseRef")) {
                    IDCUtils.debug("Processing entity attribute node " + metaModelAttributeName);
            	}
                
                IDCUtils.debug("Processing entity attribute node " + metaModelAttributeName);
                
            	if(metaModelAttribute.isAttribute()) {
                    
            		String value = IDCUtils.getXMLAttrValue(nodeAttributes, metaModelAttributeName);  
                    entityAttributes.add(value);

                    // Identify processing Action named LinkSystemUser for Type node
                    if(isDebug == 2 && nodeType.equals("Action") && metaModelAttributeName.equals("name") && value.contentEquals("LinkSystemUser")) {
                    	isDebug = 3;
                        IDCUtils.debug("Debug here !!!!");  // set breakpoint here ;)
                    }

                    if(metaModelAttributeName.equals(IDCModelData.REFERENCED_SYSTEM_TYPE_ATTR_NAME)) {
                    	
                    	if(value != null && value.length() > 0) {
                    		
                            IDCUtils.debug("FOUND SYSTEM REFERENCE value = " + value);                            
                            List<Object> values = getEmptyDataValues(IDCModelData.REFERENCE);
                           	systemRef = new IDCReference((IDCModelData) parent, -1234, values);
                           	systemRef.setSystemReference(value);
                    	}
                    	
                    }
                    
            	} else if(metaModelAttribute.isChild()) {

                    //IDCUtils.debug("");
                	List<IDCVector> children = new ArrayList<IDCVector>(); 
                    
                	Node childNode = node.getFirstChild();
                    while(childNode != null && !childNode.getNodeName().equalsIgnoreCase(metaModelAttributeName)) {
                    	childNode = childNode.getNextSibling();
                    }
                    if(childNode != null) {
                        Node grandChildNode = childNode.getFirstChild();
                        if(grandChildNode != null) {
                            if(!grandChildNode.getNodeName().equals("#text")) {
                            	IDCVector child = processNodeUsingMetaModel(grandChildNode, modelData, isDebug); 
                               	children.add(child);
                            }
                            while((grandChildNode = grandChildNode.getNextSibling()) != null) {
                                if(!grandChildNode.getNodeName().equals("#text")) {
                                	IDCVector child = processNodeUsingMetaModel(grandChildNode, modelData, isDebug); 
                                   	children.add(child);
                                }
                            }
                        }
                    }
                    
                    entityAttributes.add(children);

                } else {
                    entityAttributes.add(null);
                }
            
            }
            
            modelData.setValues(entityAttributes);
            
            if(systemRef != null) {
               	((IDCAttribute) modelData).getList(IDCAttribute.REFERENCES).add(systemRef);
            }
 
            IDCUtils.debug("End Processing node " + nodeType);
            
        }
        
        if(isDebug == 3) { /// end processing action
            IDCUtils.debug("Debug here !!!!");  // set breakpoint here ;)
        } else if(isDebug == 1) { /// end processing Type
            IDCUtils.debug("Debug here !!!!");  // set breakpoint here ;)
        }  
        
    	return modelData;
    	
   } 

	/*****************************************************************************/

	public List<Object> getEmptyDataValues(int entityType) {
		
		List<Object> ret = new ArrayList<Object>();	
		
		IDCMetaModelEntity entity = getMetaModelEntity(entityType);

        for(IDCMetaModelAttribute metaModelAttribute : entity.getAttributes()) {
        	
        	String metaModelAttributeName = metaModelAttribute.getName();
            
            IDCUtils.debug("Processing entity attribute node " + metaModelAttributeName);
            
        	if(metaModelAttribute.isAttribute()) {
                
        		String value = "";  
                ret.add(value);
                
        	} else if(metaModelAttribute.isChild()) {

            	List<IDCVector> children = new ArrayList<IDCVector>(); 
                ret.add(children);

            } else {
                ret.add(null);
            }
        
        }

        return ret;

	}



    /************************************************************************************************/

	public IDCVector getReferencedData(IDCVector data, long xmiId) {
		
		IDCVector ret = null;
		
		if(data.getId() == xmiId) {
			
			ret = data;
		
		} else {
			
	        String entityName = data.getEntityName();
//	        IDCUtils.debug("getReferencedData entityName=" + entityName);
	        IDCMetaModelEntity entity = getMetaModelEntity(entityName);
	        
//	        IDCUtils.debug("getReferencedData entity=" + entity.getName() + " id=" + data.getId());
	        
	        int nAttr = 0;
	        for(IDCMetaModelAttribute metaModelAttribute : entity.getAttributes()) {
	        	
//		        IDCUtils.debug("getReferencedData metaModelAttribute=" + metaModelAttribute.getName());
		        
	        	if(metaModelAttribute.isChild()) {

	            	for(IDCVector child : (List<IDCVector>) data.getList(nAttr)) {
	            		ret = getReferencedData(child,xmiId);
	            		if(ret != null) {
	            			break;
	            		}
	            	}
	            	
	        	}

	        	if(ret != null) {
        			break;
        		}
	        	
	        	nAttr++;
	        		
            }
	            
		}
		
		return ret;
		
	}

	/************************************************************************************************/

	public IDCMetaModelEntity getMetaModelEntity(String entityName) {
		return getMetaModel().getEntity(entityName);
	}

	/************************************************************************************************/

	public IDCMetaModelEntity getMetaModelEntity(int type) {
		return getMetaModel().getEntity(type);
	}
	
    /************************************************************************************************/

	public String generateModelDataInJSON(IDCModelData data) {
		
    	String ret = null;
    	
		ByteArrayOutputStream str = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(str);
		
		generateModelDataInJSON(data, out, "");
		
		ret = str.toString();
			
    	return ret;

    }
    

	public void generateModelDataInJSON(IDCModelData data, PrintWriter out, String prefix) {
    	generateModelDataInJSON(data, out, prefix, false, true);
    }
    
	public void generateModelDataInJSON(IDCModelData data, PrintWriter out, String prefix, boolean isSystemEntity, boolean isFirstChild) {
    	
    	int type = data.getType();
    	
    	IDCMetaModelEntity entity = getMetaModelEntity(type);
    	
		IDCUtils.debug("----------------------------------------------------------------------------");
		IDCUtils.debug("generateModelDataInJSON(): entity = " + entity.getName());

		String s = prefix + (isFirstChild ? " " : ",") + "{\"entityType\":\"" + entity.getName() + "\", \"id\":\"" + data.getXmiId() + "\"";
		
		int nAttr =0;
		for(IDCMetaModelAttribute attr : entity.getAttributes()) {
			
			if(attr.isAttribute()) {
				
				if(type != IDCModelData.REFERENCE || nAttr != IDCModelData.NAME) {
					
					String value = data.getString(nAttr);
					if(value == null || value.length() == 0) {
						value = attr.getDefaultValue();
					}
					IDCUtils.debug("generateModelDataInJSON(): attr = " + attr.getName() + " / value = " + value);
					if(value != null && value.length() > 0) {
						s += ", \"" + attr.getName() + "\": \"" + IDCUtils.convert2JSON(value) + "\"";
					}
					
				}
				
			}
			
			nAttr++;
			
		}

		out.println(s);

		nAttr =0;
		for(IDCMetaModelAttribute attr : entity.getAttributes()) {
			
			IDCUtils.debug("generateModelDataInJSON(): attr = " + attr.getName());
			
			if(attr.isChild()) {
				
				boolean writeAttributeHeader = true;
				
//				if(type == IDCModelData.APPLICATION && nAttr == IDCApplication.PACKAGES) {					
//					out.println(prefix + "  ,\"" + attr.getName() + "\": ");
//					writeAttributeHeader = false;
//					isFirstChild = true;
//					for(IDCPackage systemPackage : dbManager.getSystemApplication().getSystemApplication().getPackages()) {
//						generateModelDataInJSON(systemPackage, out, prefix + "    ", true, isFirstChild);
//						isFirstChild = false;
//					}
//					
//				}
//				
				List children = data.getList(nAttr);
				if(children.size() > 0) {
					
					if(writeAttributeHeader) {
						out.println(prefix + "  ,\"" + attr.getName() + "\": [");
					}
					
					isFirstChild = true;
					if(type == IDCModelData.ATTRIBUTE && nAttr == IDCAttribute.REFERENCES) {
						for(Object child : children) {
							if(((IDCModelData)child).getType() != IDCModelData.REFERENCE) {
								IDCModelData ref = getNewReference(data, (IDCModelData) child);
								generateModelDataInJSON(ref, out, prefix + "    ", isSystemEntity, isFirstChild);
							} else {
								generateModelDataInJSON((IDCModelData)child, out, prefix + "    ", isSystemEntity, isFirstChild);
							}
							isFirstChild = false;
						}						
					} else {
						for(Object child : children) {
							generateModelDataInJSON((IDCModelData)child, out, prefix + "    ", isSystemEntity, isFirstChild);
							isFirstChild = false;
						}
					}
					out.println(prefix + "  ]");
				}
			}

			nAttr++;
			
		}
    	
		out.println(prefix + "}");
    
    }

    /************************************************************************************************/

	private IDCModelData getNewReference(IDCModelData parent, IDCModelData child) {
		return getNewReference(parent, child.getId());
	}

    /************************************************************************************************/

	private IDCModelData getNewReference(IDCModelData parent, long childId) {

		IDCModelData ret = getNewModelData(parent, IDCModelData.REFERENCE);
		ret.setValue(IDCReference.REFID, childId);
		
		return ret;
		
	}

    /************************************************************************************************/

	public IDCModelData getNewModelData(IDCModelData parent, int type) {
		return getNewModelData(parent, type, getNextId());
	}
	
	public IDCModelData getNewModelData(IDCModelData parent, int type, long id) {

		IDCModelData ret = new IDCModelData(parent, type, id, null);
		
		IDCMetaModelEntity entity = getMetaModelEntity(IDCModelData.TYPES[type]);
		
		List<Object> attributes = new ArrayList<Object>();
        
        for(IDCMetaModelAttribute metaModelAttribute : entity.getAttributes()) {
        	
        	if(metaModelAttribute.isAttribute()) {
                attributes.add("");
        	} else if(metaModelAttribute.isChild()) {
                attributes.add(new ArrayList<Object>());
            } else {
                attributes.add(null);
            }
            
        }

        ret.setValues(attributes);
	
		return ret;
		
	}

    /************************************************************************************************/

	public int getNextId() {
    	return ++id;
	}
	

}