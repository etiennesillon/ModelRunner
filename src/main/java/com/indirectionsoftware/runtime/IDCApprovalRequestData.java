package com.indirectionsoftware.runtime;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCType;

public class IDCApprovalRequestData extends IDCData {

	// Request
	public final static String REQUEST_TYPE = "$$ApprovalRequest"; 
	public final static int REQUEST_REF=0, REQUEST_TEXT=1, REQUEST_ASSIGNEDTO=2, REQUEST_STATUS=3;
	public final static int PENDING=0, APPROVED=1, REJECTED=2;

	/**************************************************************************************************/

	public static IDCApprovalRequestData getNewRequest(IDCApplication appl, IDCData user, String text) {
		
		IDCApprovalRequestData ret = null;
		
		IDCType requestType = appl.getType(REQUEST_TYPE);
		ret = new IDCApprovalRequestData(requestType);
		ret.set(REQUEST_ASSIGNEDTO, new IDCDataRef(user));
		ret.set(REQUEST_TEXT, text);
		ret.save();

		return ret;
		
	}
	
	/**************************************************************************************************/

	public IDCApprovalRequestData(IDCType requestType) {
		super(requestType, true);
	}
	
	/**************************************************************************************************/

	public static boolean isActive(IDCData data) {
		return data.getInt(REQUEST_STATUS) != PENDING;
	}
	
}
