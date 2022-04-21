package com.indirectionsoftware.runtime.webapp;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCError;
import com.indirectionsoftware.utils.IDCCalendar;

public class IDCWebAppSettings {

	private static final int DEFAULT_PAGE_SIZE = 50;
	public static int pageSize = DEFAULT_PAGE_SIZE;

	private static final boolean DEFAULT_DATE_SORT_ASCENDING = false;
	public static boolean dateSortAscending = DEFAULT_DATE_SORT_ASCENDING;

	public static long startDate = -1, endDate = -1;
	
	
	/************************************************************************************/

	public static Map<String, IDCError> updateFromHTML(IDCWebApplication app, HttpServletRequest request) {
		
		Map<String, IDCError> ret = new HashMap<String, IDCError>();
		
		String newVal = (String) request.getParameter("startDate");
        if(newVal != null) {
	        if(newVal.length() == 0) {
				startDate = -1;
	        } else {
				try {
					newVal = URLDecoder.decode(newVal,"UTF-8");
					startDate = IDCCalendar.getCalendar().getDate(newVal);
				} catch (Exception e) {
				}
				
	        }
        }

		newVal = (String) request.getParameter("endDate");
        if(newVal != null) {
	        if(newVal.length() == 0) {
	        	endDate = -1;
	        } else {
				try {
					newVal = URLDecoder.decode(newVal,"UTF-8");
					endDate = IDCCalendar.getCalendar().getDate(newVal);
				} catch (Exception e) {
				}
	        }
        }
	
		newVal = (String) request.getParameter("pageSize");
        if(newVal != null) {
	        if(newVal.length() == 0) {
	        	pageSize = DEFAULT_PAGE_SIZE;
	        } else {
				try {
					newVal = URLDecoder.decode(newVal,"UTF-8");
					pageSize = Integer.parseInt(newVal);
				} catch (Exception e) {
				}
	        }
        }

    	return ret;
		
	}
	
	/**************************************************************************************************/

	public List<IDCDataRef> filterDateList(IDCType type, List<IDCDataRef> refList) {
		
		List<IDCDataRef> ret = new ArrayList<IDCDataRef>();
		
		if(type.dateAttributes == null || (IDCWebAppSettings.startDate == -1 && IDCWebAppSettings.endDate == -1)) {
			ret = refList;
		} else {
			ret = new ArrayList<IDCDataRef>();
			for(IDCDataRef ref : refList) {
				IDCData data = type.loadDataObject(ref.getItemId());
				if(data != null) {
					boolean ok = true;
					for(IDCAttribute attr : type.dateAttributes) {
						long date = data.getLong(attr.getAttributeId());
						if((IDCWebAppSettings.startDate != -1 && date < IDCWebAppSettings.startDate) || (IDCWebAppSettings.endDate != -1 && date > IDCWebAppSettings.endDate)) {
							ok = false;
							break;
						}
					}
					if(ok) {
						ret.add(ref);
					}
				}
			}
		}
		
		return ret;
		
	}

	/************************************************************************************************/

	public static boolean isWithinDateRange(long date) {
		
		boolean ret = true;
		
		if((IDCWebAppSettings.startDate != -1 && date < IDCWebAppSettings.startDate) || (IDCWebAppSettings.endDate != -1 && date > IDCWebAppSettings.endDate)) {
			ret = false;
		}

		return ret;
		
	}


	
}
    