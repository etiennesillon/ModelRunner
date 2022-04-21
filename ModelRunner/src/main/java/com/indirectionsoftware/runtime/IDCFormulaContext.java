package com.indirectionsoftware.runtime;

import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.backend.database.IDCSystemUser;

/************************************************************************************************/

public class IDCFormulaContext {
	
	private IDCWorkflowInstanceData workflowInstance;
	IDCData data;
	IDCData ref;
	IDCSystemUser user;
	boolean isSave = true;
	private String fileContent;
	
	/************************************************************************************************/

	public IDCFormulaContext(IDCData data) {
		this(null, data, null, data.getApplication().getUser(), true);
	}
	
	public IDCFormulaContext(IDCData data, boolean isSave) {
		this(null, data, null, data.getApplication().getUser(), isSave);
	}
	
	/************************************************************************************************/

	public IDCFormulaContext(IDCData data, IDCData ref, IDCSystemUser user) {
		this(null, data, ref, user, true);
	}
	
	/************************************************************************************************/

	public IDCFormulaContext(IDCData data, IDCData ref, IDCSystemUser user, boolean isSave) {
		this(null, data, ref, user, isSave);
	}
	
	/************************************************************************************************/

	public IDCFormulaContext(IDCWorkflowInstanceData workflowInstance, IDCData data, IDCData ref, IDCSystemUser user, boolean isSave) {
		if(workflowInstance == null && data instanceof IDCWorkflowInstanceData) {
			this.workflowInstance = (IDCWorkflowInstanceData) data;
		} else {
			this.workflowInstance = workflowInstance;
		}
		this.data = data;
		this.ref = ref;
		this.user = user;
		this.isSave = isSave;
	}
	
	/************************************************************************************************/

	public IDCFormulaContext copy() {
		return new IDCFormulaContext(workflowInstance, data, ref, user, isSave);
	}
	
	/************************************************************************************************/

	public IDCFormulaContext getChildContext(IDCData data) {
		return new IDCFormulaContext(workflowInstance, data, ref, user, isSave);
	}

	public IDCWorkflowInstanceData getWorkflowInstance() {
		return workflowInstance;
	}

	/************************************************************************************************/

	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}
	
	/************************************************************************************************/

	public String getFileContent() {
		return fileContent;
	}
	
}
