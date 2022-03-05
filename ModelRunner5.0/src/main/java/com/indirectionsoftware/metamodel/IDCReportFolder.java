package com.indirectionsoftware.metamodel;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;

public class IDCReportFolder  extends IDCModelData {
	
	public static final int SUBFOLDERS=START_ATTR, REPORTS=START_ATTR+1;

	private List<IDCReportFolder> subFolders;
	private List<IDCReport> reports;

	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCReportFolder(IDCModelData parent, long id, List<Object> values) {
		super(parent, IDCModelData.REPORTFOLDER, id, values);
	}

	/**************************************************************************************************/
	// Init processing ...
	/**************************************************************************************************/

	public void init(IDCData userData) {
		
		if(isInitRequired()) {

			super.init(userData);
			
			subFolders = (List<IDCReportFolder>) getList(SUBFOLDERS);
			for(IDCReportFolder folder : subFolders) {
				folder.init(userData);
			}
	
			reports = (List<IDCReport>) getList(REPORTS);
			for(IDCReport report : reports) {
				report.init(userData);
			}
				
			completeInit();

		}
		
	}

	/**************************************************************************************************/
	// View Folder methods ...
	/**************************************************************************************************/
	
	public List<IDCReportFolder> getSubFolders() {
		return subFolders;
	}

	/**************************************************************************************************/

	public List<IDCReport> getReports() {
		return reports;
	}

	/**************************************************************************************************/
	// Children ...
	/**************************************************************************************************/
	
	public List getChildren(int mode) {

		 List ret = new ArrayList<IDCType>(); 
		
		 ret.addAll(subFolders);
		 ret.addAll(reports);
		
		 return ret;
		
	}

	/**************************************************************************************************/
	
	public IDCModelData getRefChild(int entityType, int entityId) {

		IDCModelData ret = null;
		
		switch(entityType) {
		
			case REPORTFOLDER:
				ret = subFolders.get(entityId);
				break;
				
			case REPORT:
				ret = reports.get(entityId);
				break;
				
		}
		
		return ret;
		
	}
	
}