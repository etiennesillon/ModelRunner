package com.indirectionsoftware.metamodel;

import java.io.PrintWriter;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;

public class IDCReference extends IDCModelData {
	
	public static final int REFID=START_ATTR;
	
	IDCModelData refData;
	
	IDCType refType = null;
	IDCAttribute refAttr = null;
	IDCDomain refDom = null;
//	IDCDatabaseRef refDBRef = null;
	
	String systemReference = null;
	
	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCReference(IDCModelData parent, long id, List<Object> values) {
		super(parent, IDCModelData.REFERENCE, id, values);
	}
	
	/**************************************************************************************************/
	
	public void init(IDCData userData) {
		
		if(isInitRequired()) {

			super.init(userData);
			
			int refId = getInt(REFID);
			refData = getApplication().getRefData(refId);
			
			if(systemReference == null) {
				
				if(refData != null) {

					refData.init(userData);
					
					switch(refData.getType()) {
					
						case IDCModelData.TYPE:
							refType = (IDCType)refData;  
							break;
							
						case IDCModelData.ATTRIBUTE:
							refAttr = (IDCAttribute)refData;  
							refType = (IDCType)refAttr.getParent().getParent();  
							break;
							
						case IDCModelData.DOMAIN:
							refDom = (IDCDomain)refData;  
							break;
							
//						case IDCModelData.DATABASEREF:
//							refDBRef = (IDCDatabaseRef)refData;  
//							break;
							
					}
					
				} else {
					getApplication().addUnresolvedRef(this);
				}

			} else {
				refType = getApplication().initGetType(systemReference);
				System.out.println("systemReference="+systemReference + " / refType = " + refType);
			}
	
			completeInit();
			
		}
		
	}
	
	/**************************************************************************************************/

	public void setSystemReference(String systemReference) {
		this.systemReference = systemReference;
	}
	
	/**************************************************************************************************/

	public void resolveRef(IDCApplication adminAppl) {		
	}
	
	/**************************************************************************************************/
	// Extension methods ...
	/**************************************************************************************************/
	
	public IDCType getDataType() {
		return refType;
	}

	public IDCAttribute getAttribute() {
		return refAttr;
	}

	public IDCDomain getDomain() {
		return refDom;
	}

}