package com.indirectionsoftware.utils;

public class IDCItemId {
	
	public String attrKey;
	public int typeId;
	public long itemId;

	/************************************************************************************************/

    public IDCItemId(String attrKey, int typeId, long itemId) {
    	this.attrKey = attrKey;
    	this.typeId = typeId;
    	this.itemId = itemId;
    }	

}