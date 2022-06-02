package com.indirectionsoftware.metamodel;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;

public class IDCPackage extends IDCModelData {
	
	public static final int TYPES=START_ATTR, DOMAINS=START_ATTR+1, DATABASEREF=START_ATTR+2;

	List<IDCType> 	types, allTypes;
	
	private List<IDCDomain> 		domains;	

	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCPackage(IDCApplication parent, long id, List<Object> values) {
		super(parent, IDCModelData.PACKAGE, id, values);
	}
	
	/**************************************************************************************************/

	public void init(IDCData userData) {
		
		if(isInitRequired()) {

			super.init(userData);
			
			types = (List<IDCType>) getList(TYPES);
			allTypes = new ArrayList<IDCType>();
			List<IDCType> temp = new ArrayList<IDCType>();
			for(IDCType type : types) {
				type.init(userData);
				allTypes.add(type);
				if(userData == null || userData.isEnabled(type)) {
					temp.add(type);
				}
			}
			types = temp;

			List<IDCDatabaseRef> dbRefs = getList(DATABASEREF);
			if(dbRefs.size() == 1) {
				IDCDatabaseRef ref = dbRefs.get(0); 
				setDatabaseRef(ref);
				ref.init(userData);
				setDatabaseConnection(ref.getDatabaseConnection());
			}

			domains = (List<IDCDomain>) getList(DOMAINS);
			for(IDCDomain dom: domains) {
				dom.init(userData);
			}

			completeInit();
	
		}
		
	}

//	public void init(IDCApplication editorAppl) {
//	}

	/**************************************************************************************************/
	// Package methods ...
	/**************************************************************************************************/
	
	public List<IDCType> getTypes() {
		return types;
	}

	public IDCType getType(int nType) {
		return types.get(nType);
	}

	public List<IDCType> initGetTypes() {

		List<IDCType> ret = types;
		
		if(ret == null) {
			ret = (List<IDCType>) getList(TYPES);
		}

		return ret;
		
	}
	
	/**************************************************************************************************/

	public void setIsSystem(boolean isSystem) {
		this.isSystem = isSystem;
		for(IDCType type : types) {
			type.setIsSystem(isSystem);
		}
	}

	/**************************************************************************************************/
	// Domains ...
	/**************************************************************************************************/

	public List<IDCDomain> getDomains() {
		return domains;
	}

	/**************************************************************************************************/

	public IDCDomain getDomain(int nDom) {
		return domains.get(nDom);
	}

	/**************************************************************************************************/

	public List<IDCDomain> initGetDomains() {

		List<IDCDomain> ret = domains;
		
		if(ret == null) {
			ret = getList(DOMAINS);
		}

		return ret;
		
	}



	/**************************************************************************************************/

	public IDCDatabaseRef initGetDatabaseRef() {

		IDCDatabaseRef ret = getDatabaseRef();
		
		if(ret == null) {
			List<IDCDatabaseRef> dbRefs = getList(DATABASEREF);
			if(dbRefs.size() == 1) {
				ret = dbRefs.get(0);
				setDatabaseRef(ret);
				setDatabaseConnection(ret.getDatabaseConnection());
			}
		}

		return ret;
		
	}

	/**************************************************************************************************/
	// Children ...
	/**************************************************************************************************/
	
	public List getChildren(int mode) {

		 List ret = new ArrayList<IDCType>(); 
		 
		 switch(mode) {
		 
		 	case EDITOR_MODE:
		 	case EXPORT_MODE:
		 		ret = getTypes();
		 		break;

		 	default:
				 for(IDCType type : getTypes()) {
					 if(type.isTopLevelViewable()) {
						 ret.add(type);
					 }
				 }
				 break;
			 
		 }
		
		 return ret;
		
	}
	
	/**************************************************************************************************/
	
    public boolean connect() {
		
    	boolean ret = getDatabaseConnection().connect();
    	
		for(int nType=0; ret && nType < types.size(); nType++) {
			ret = types.get(nType).connect();
		}
		
		return ret;

    }

//	/**************************************************************************************************/
//	
//    public void setIsSystem(boolean isSystem) {
//		this.isSystem = isSystem;
//    }
//    
//	/**************************************************************************************************/
//	
//    public boolean isSystem() {
//		return isSystem;
//    }
//  
}