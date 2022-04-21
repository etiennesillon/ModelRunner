package com.indirectionsoftware.metamodel;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;

public class IDCPanel extends IDCModelData {
	
	List<IDCAttribute> attributes;
	
	public static final int ATTRS=START_ATTR;
	
	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCPanel(IDCType parent, long id, List<Object> values) {
		super(parent, IDCModelData.PANEL, id, values);
	}
	
	/**************************************************************************************************/

	public void init(IDCData userData) {
		
		if(isInitRequired()) {

			super.init(userData);
			
			attributes = (List<IDCAttribute>) getList(ATTRS);
			List<IDCAttribute> temp = new ArrayList<IDCAttribute>(); 
			for(IDCAttribute attr : attributes) {
				attr.init(userData);
				if(userData == null || userData.isEnabled(attr)) {
					temp.add(attr);
				}
			}
			attributes  = temp;
			
			completeInit();
		
		}
	
	}

	/**************************************************************************************************/
	// Panel methods ...
	/**************************************************************************************************/
	
	public List<IDCAttribute> getAttributes() {
		return attributes;
	}

	/**************************************************************************************************/
	
	public List<IDCAttribute> initGetAttributes() {
		return null;
	}

	/**************************************************************************************************/
	// Children ...
	/**************************************************************************************************/
	
	public List getChildren(int mode) {

		List ret = null; 
		
		if(mode == EDITOR_MODE){
			ret = getAttributes();
	 	} else {
	 		ret = new ArrayList<Object> (); 
		}
			
		return ret;
		
	}

}