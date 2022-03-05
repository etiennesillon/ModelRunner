package com.indirectionsoftware.backend.database;

import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCType;

public class IDCDataParentRef {
	
	private int typeId;
	private long attrId, itemId;
	
    /************************************************************************************************/

	public IDCDataParentRef(int typeId, long attrId, long itemId) {
		this.typeId = typeId;
		this.attrId = attrId;
		this.itemId = itemId;
	}
	
    /************************************************************************************************/

	public IDCDataParentRef(int typeId, long attrId) {
		this(typeId, attrId, 0);
	}

    /************************************************************************************************/

	public static IDCDataParentRef getRootParentRef() {
		return new IDCDataParentRef(-1,-1,-1);
	}
	
	public boolean isRootRef() {
		return typeId == -1 ? true : false;
	}
	
    /************************************************************************************************/

	public static IDCDataParentRef getParentRef(String s) {
		
		IDCDataParentRef ret = null;
		
		if(s != null) {
			
			int i = s.indexOf("/");
			
			if(i!=-1) {

				int typeId = Integer.parseInt(s.substring(0, i));

				int j = s.indexOf("/", i + 1);
				if(j!=-1) {
					long attrId = Long.parseLong(s.substring(i+1, j));
					long itemId = Long.parseLong(s.substring(j+1, s.length()));
					ret = new IDCDataParentRef(typeId, attrId, itemId);
				}
			}
			
		}
		
		return ret;
		
	}

    /************************************************************************************************/

	public static IDCDataParentRef getParentRef(String s, long itemId) {
		
		IDCDataParentRef ret = null;
		
		int i = s.indexOf("/");
		
		if(i!=-1) {
			int typeId = Integer.parseInt(s.substring(0, i));
			long attrId = Long.parseLong(s.substring(i+1, s.length()));
			ret = new IDCDataParentRef(typeId, attrId, itemId);
		}
		
		return ret;
		
	}

    /************************************************************************************************/

	public String toString() {
		return "" + typeId + "/" + attrId + "/" + itemId;
	}

    /************************************************************************************************/

	public int getTypeId() {
		return typeId;
	}

    /************************************************************************************************/

	public long getAttrId() {
		return attrId;
	}

    /************************************************************************************************/

	public String getParentRef() {
		return "" + typeId + "/" + attrId;
	}

    /************************************************************************************************/

	public long getItemId() {
		return itemId;
	}

    /************************************************************************************************/

	public boolean isParentNamespace(IDCType refType, IDCAttribute refAttr) {

		boolean ret = false;
		
		if(refType.getEntityId() == typeId && refAttr.getId() == attrId) {
			ret = true;
		}
		
		return ret;
	
	}
	
	

}