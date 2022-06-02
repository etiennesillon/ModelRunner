package com.indirectionsoftware.metamodel;

public class IDCTypeRef {
	
	private IDCType type;
	private IDCAttribute attr;
	
    /************************************************************************************************/

	public IDCTypeRef(IDCType type, IDCAttribute attr) {
		this.type = type;
		this.attr = attr;
	}
	
    /************************************************************************************************/

	public IDCType getType() {
		return type;
	}

    /************************************************************************************************/

	public void setType(IDCType type) {
		this.type = type;
	}

    /************************************************************************************************/

	public IDCAttribute getAttr() {
		return attr;
	}

    /************************************************************************************************/

	public void setAttr(IDCAttribute typeId) {
		this.attr = attr;
	}

}