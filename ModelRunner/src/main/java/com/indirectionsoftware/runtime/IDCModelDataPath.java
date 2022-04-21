package com.indirectionsoftware.runtime;


public class IDCModelDataPath {
	
	private int entityType;
	private int entityId;
	
    /************************************************************************************************/

	public IDCModelDataPath(int entityType, int entityId) {
		this.entityType = entityType;
		this.entityId = entityId;
	}
	
    /************************************************************************************************/

	public static IDCModelDataPath getPath(String path) {
		
		IDCModelDataPath ret = null;

		int startAnchorInd = path.indexOf('<');
		int endAnchorInd = path.indexOf('>');
		int midAnchorInd = path.indexOf('/');
		
		if(startAnchorInd != -1 && endAnchorInd != -1 && midAnchorInd != -1) {

			int entityType = Integer.parseInt(path.substring(startAnchorInd+1, midAnchorInd));
			int entityId = Integer.parseInt(path.substring(midAnchorInd+1, endAnchorInd));
			
			ret = new IDCModelDataPath(entityType, entityId);
			
		}
		
		return ret;
		
	}

    /************************************************************************************************/

	public String toString() {
		
		String ret = "<" + entityType + "/" + entityId + ">";
		
		return ret;
		
	}

    /************************************************************************************************/

	public int getEntityType() {
		return entityType;
	}

    /************************************************************************************************/

	public int getEntityId() {
		return entityId;
	}

}