package com.indirectionsoftware.metamodel;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;

public class IDCViewFolder  extends IDCModelData {
	
	public static final int SUBFOLDERS=START_ATTR, VIEWS=START_ATTR+1;

	private List<IDCViewFolder> subFolders;
	private List<IDCView> views;

	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCViewFolder(IDCModelData parent, long id, List<Object> values) {
		super(parent, IDCModelData.VIEWFOLDER, id, values);
	}

	/**************************************************************************************************/
	// Init processing ...
	/**************************************************************************************************/

	public void init(IDCData userData) {
		
		if(isInitRequired()) {

			super.init(userData);
			
			subFolders = (List<IDCViewFolder>) getList(SUBFOLDERS);
			for(IDCViewFolder folder : subFolders) {
				folder.init(userData);
			}
	
			views = (List<IDCView>) getList(VIEWS);
			for(IDCView view : views) {
				view.init(userData);
			}
				
			completeInit();

		}
		
	}

	/**************************************************************************************************/
	// View Folder methods ...
	/**************************************************************************************************/
	
	public List<IDCViewFolder> getSubFolders() {
		return subFolders;
	}

	/**************************************************************************************************/

	public List<IDCView> getViews() {
		return views;
	}

	/**************************************************************************************************/
	// Children ...
	/**************************************************************************************************/
	
	public List getChildren(int mode) {

		 List ret = new ArrayList<IDCType>(); 
		
		 ret.addAll(subFolders);
		 ret.addAll(views);
		
		 return ret;
		
	}

	/**************************************************************************************************/
	
	public IDCModelData getRefChild(int entityType, int entityId) {

		IDCModelData ret = null;
		
		switch(entityType) {
		
			case VIEWFOLDER:
				ret = subFolders.get(entityId);
				break;
				
			case VIEW:
				ret = views.get(entityId);
				break;
				
		}
		
		return ret;
		
	}
	
}