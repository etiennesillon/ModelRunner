package com.indirectionsoftware.metamodel; 


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.indirectionsoftware.utils.IDCUtils;

public class IDCMetaModelParser {

	Document document;
	
	IDCMetaModel metaModel;
	
	IDCMetaModelEntity metaModelEntity; 
	
	IDCMetaModelAttribute metaModelAttribute; 
	
	List<IDCMetaModelAttribute> commonAttrs = new ArrayList<IDCMetaModelAttribute>(); 
	
	public static final String METAMODEL="MetaModel", COMMON_ATTR="Common_Attribute", ENTITY="Entity", ATTRIBUTE="Attribute", CHILD="Child"; 
	public static final String NAME="name", ATTR_TYPE="type", ATTR_CHILDTYPE = "childType", ATTR_DEFAULT="default"; 

	/************************************************************************************************/

    public IDCMetaModel loadMetaModel(InputStream stream) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //factory.setValidating(true);   
        //factory.setNamespaceAware(true);
        
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(stream);
        
        processNode(document.getFirstChild());
        
        return metaModel;
            
    }
    
    /************************************************************************************************/

    private void processNode(Node node) throws Exception {

        String nodeType = node.getNodeName();    
    	
        NamedNodeMap attrs = node.getAttributes();
    	
    	if(nodeType.equals(COMMON_ATTR)) {
            processCommonAttributeNode(node, attrs);
    	} else if(nodeType.equals(METAMODEL)) {
            processMetaModelNode(node, attrs);
    	} else if(nodeType.equals(ENTITY)) {
            processEntityNode(node, attrs);
    	} else if(nodeType.equals(ATTRIBUTE)) {
            processAttributeNode(node, attrs);
    	} 
    	
        Node childNode = node.getFirstChild();
        if(childNode != null) {
            processNode(childNode);
            while((childNode = childNode.getNextSibling()) != null) {
                processNode(childNode);
            }
        }
        
   } 

	/************************************************************************************************/

    private void processCommonAttributeNode(Node node, NamedNodeMap attrs) {

    	String nodeName = IDCUtils.getXMLAttrValue(attrs, NAME);  
    	String type = IDCUtils.getXMLAttrValue(attrs, ATTR_TYPE);  
    	String refType = IDCUtils.getXMLAttrValue(attrs, ATTR_CHILDTYPE);  
    	String defaultVal = IDCUtils.getXMLAttrValue(attrs, ATTR_DEFAULT);  
    	
    	IDCUtils.debug("Processing Common Attribute node " + nodeName + " / defaultVal=" + defaultVal);
    	
    	commonAttrs.add(new IDCMetaModelAttribute(-1, nodeName, type, refType, defaultVal));
    	
    }

	/************************************************************************************************/

	private void processMetaModelNode(Node node, NamedNodeMap attrs) {
		
    	String nodeName = IDCUtils.getXMLAttrValue(attrs, NAME);  
    	
    	IDCUtils.debug("Processing MetaModel node " + nodeName);
    	
    	metaModel = new IDCMetaModel(nodeName); 
    	
	}

	/************************************************************************************************/

	private void processEntityNode(Node node, NamedNodeMap attrs) {

        String nodeName = IDCUtils.getXMLAttrValue(attrs, NAME);  
        
        IDCUtils.debug("Processing MetaModelEntity node " + nodeName);
    	
    	metaModelEntity = metaModel.addEntity(nodeName); 

    	for(IDCMetaModelAttribute attr : commonAttrs) {
        	metaModelEntity.addCommonAttribute(attr); 
    	}
	
	}

	/************************************************************************************************/

    private void processAttributeNode(Node node, NamedNodeMap attrs) {

    	String nodeName = IDCUtils.getXMLAttrValue(attrs, NAME);  
    	String type = IDCUtils.getXMLAttrValue(attrs, ATTR_TYPE);  
    	String refType = IDCUtils.getXMLAttrValue(attrs, ATTR_CHILDTYPE);  
    	String defaultVal = IDCUtils.getXMLAttrValue(attrs, ATTR_DEFAULT);  

    	IDCUtils.debug("Processing MetaModelAttribute node " + nodeName + " / defaultVal=" + defaultVal);
    	
    	metaModelAttribute = metaModelEntity.addAttribute(nodeName, type, refType, defaultVal); 
    	
    }

}