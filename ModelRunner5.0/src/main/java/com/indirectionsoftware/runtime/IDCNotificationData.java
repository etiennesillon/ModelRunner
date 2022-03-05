package com.indirectionsoftware.runtime;

import java.util.ArrayList;
import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCType;

public class IDCNotificationData extends IDCData {

	// Request
	public final static String NOTIFICATION_TYPE = "$$Notification"; 
	public final static int NOTIFICATION_REF=0, NOTIFICATION_STATUS=1, NOTIFICATION_USER=2, NOTIFICATION_TITLE=3, NOTIFICATION_TEXT=4;
	public final static int UNREAD=0, READ=1;
	/**************************************************************************************************/

	public static IDCNotificationData getNewNotification(IDCApplication appl, IDCData user, String title, String text) {
		
		IDCNotificationData ret = null;
		
		IDCType requestType = appl.getType(NOTIFICATION_TYPE);
		ret = new IDCNotificationData(requestType);
		ret.set(NOTIFICATION_USER, user);
		ret.set(NOTIFICATION_TITLE, title);
		ret.set(NOTIFICATION_TEXT, text);
		ret.save();
		
		return ret;
		
	}
	
	/**************************************************************************************************/

	public IDCNotificationData(IDCType notificationType) {
		super(notificationType, true);
		
	}
	
	/**************************************************************************************************/

	public static boolean isActive(IDCData data) {
		return data.getInt(NOTIFICATION_STATUS) == UNREAD;
	}
	
}
