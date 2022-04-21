package com.indirectionsoftware.backend.database;

import java.util.ArrayList;
import java.util.List;

public class IDCDataRef {
	
	private int typeId;
	private long itemId;
	
    /************************************************************************************************/

	public IDCDataRef(int typeId, long itemId) {
		this.typeId = typeId;
		this.itemId = itemId;
	}
	
    /************************************************************************************************/

	public IDCDataRef(IDCData data) {
		this.typeId = data.getDataType().getEntityId();
		this.itemId = data.getId();
	}
	
    /************************************************************************************************/

	public IDCDataRef(IDCDataParentRef ref) {
		this.typeId = ref.getTypeId();
		this.itemId = ref.getItemId();
	}
	
    /************************************************************************************************/

	public static IDCDataRef getRef(String ref) {
		
		IDCDataRef ret = null;
		
		if(ref != null) {
			int startInd = ref.indexOf('(');
			int endInd = ref.indexOf(')');
			int midInd = ref.indexOf('/');
			
			if(startInd != -1 && endInd != -1 && midInd != -1) {
				int typeId = Integer.parseInt(ref.substring(startInd+1, midInd));
				int itemId = Integer.parseInt(ref.substring(midInd+1, endInd));
				ret = new IDCDataRef(typeId, itemId);
			}
		}

		return ret;
		
	}

    /************************************************************************************************/

	public String toString() {
		
		String ret = "(" + typeId + "/" + itemId + ")";
		
		return ret;
		
	}

    /************************************************************************************************/

	public int getTypeId() {
		return typeId;
	}

    /************************************************************************************************/

	public long getItemId() {
		return itemId;
	}

    /************************************************************************************************/

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

    /************************************************************************************************/

	public static List<IDCDataRef> getRefList(List<IDCData> list) {
		
		List<IDCDataRef> ret = new ArrayList<IDCDataRef>();
		
		for(IDCData data : list) {
			ret.add(new IDCDataRef(data));
		}
		
		return ret;
		
	}

}