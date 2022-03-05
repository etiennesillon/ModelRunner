package com.indirectionsoftware.runtime;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCType;

public class IDCTaskData extends IDCData {

	// Request
	public final static String TASK_TYPE = "$$Task"; 
	public final static int TASK_REF=0, TASK_TITLE=1, TASK_TEXT=2, TASK_USER=3, TASK_STATUS=4;
	public final static int NEW=0, ASSIGNED=1, ACCEPTED=2, COMPLETED=3;

	
	/**************************************************************************************************/

	public static IDCTaskData getNewTask(IDCApplication appl, IDCData user, String title, String text) {
		
		IDCTaskData ret = null;
		
		IDCType taskType = appl.getType(TASK_TYPE);
		ret = new IDCTaskData(taskType);
		if(user != null) {
			ret.set(TASK_USER, user);
			ret.set(TASK_STATUS, 1);
		}
		ret.set(TASK_TITLE, title);
		ret.set(TASK_TEXT, text);
		ret.save();

		
		return ret;
		
	}
	
	/**************************************************************************************************/

	public IDCTaskData(IDCType taskType) {
		super(taskType, true);
	}
	
	/**************************************************************************************************/

	public static boolean isActive(IDCData data) {
		return data.getInt(TASK_STATUS) != COMPLETED;
	}
	
}
