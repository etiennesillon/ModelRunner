package com.indirectionsoftware.runtime;

import com.indirectionsoftware.backend.database.IDCDataParentRef;

public class IDCError {
	
	public int id=-1;
	public String msg;
	private int attrId; 
	private int type;
	
	public static final int ERROR=0, WARNING=1;
	
	public static final int MODEL_LOADING_ERROR=0, APPLICATION_LOADING_ERROR=1, USER_NOT_LOGGED_IN=2, CANT_DELETE_REFERENCED_DATA=3,
							DUPLICATE_KEY=4, IMPORT_ERROR=5, MULTIPLE_RESULTS=6, CONSTRAINTSATISFACTIONERROR=7, 
							MANDATORYATTRIBUTE=8, INVALIDFORMAT=9,
							WORKFLOW_REQUS_NOT_MET=10;
	
	public static final String[] ERRORTYPE = {"Model Loading Error ...", "Application Loading Error ...", " User Not Logged In ...", "Can't Delete Referenced Data ...",
											  "Duplicate Key: Data already exists with the same key ...", "Import Error ...", "Multiple Results ...", "Constraint Satisfaction Error ...",
											  "Mandatory field ...", "Invalid format ...",
											  "Workflow Requirement not met ..."
	};
	
	public IDCError(int errorId, String errorMsg, int attrId, int type) {
		this.id = errorId;
		this.msg = errorMsg;
		this.attrId = attrId;
		this.type = type;
	}

	public IDCError(int errorId, String errorMsg, int attrId) {
		this(errorId, errorMsg, attrId, ERROR);
	}

	public IDCError(int errorId, String errorMsg) {
		this(errorId, errorMsg, -1);
	}

	public IDCError(int errorId) {
		this(errorId, getDefaultMessage(errorId));
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public String getMessage() {
		return msg;
	}
	
	public int getAttributeId() {
		return attrId;
	}

	public static String getDefaultMessage(int errorId) {
		return ERRORTYPE[errorId];
	}

}
