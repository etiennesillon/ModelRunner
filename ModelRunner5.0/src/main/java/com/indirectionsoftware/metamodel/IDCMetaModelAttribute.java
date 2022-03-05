package com.indirectionsoftware.metamodel;

public class IDCMetaModelAttribute {
	
	private int id;
	private int type;
	private String name;
	private String childType;
	private String defaultVal;

	public static final int ATTRIBUTE=0, CHILD=1, NA=-1; 
	
	public static final String ATTRIBUTE_STR="Attribute", CHILD_STR="Child"; 

	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/

	public IDCMetaModelAttribute(int id, String name, String typeName, String childType, String defaultVal) {

		this(id, name, -1, childType, defaultVal);
		
		if(typeName.equalsIgnoreCase(ATTRIBUTE_STR)) {
			type = ATTRIBUTE;
		} else if(typeName.equalsIgnoreCase(CHILD_STR)) {
			type = CHILD;
		} 
		

	}

	public IDCMetaModelAttribute(int id, String name, int type, String childType, String defaultVal) {
		
		this.id = id;
		this.name = name;
		this.type = type;
		this.childType = childType;
		this.defaultVal = defaultVal;

	}

	/**************************************************************************************************/
	// Getters ...
	/**************************************************************************************************/

	public int getId() {
		return id;
	}

	/**************************************************************************************************/

	public String getName() {
		return name;
	}

	/**************************************************************************************************/

	public int getType() {
		return type;
	}

	/**************************************************************************************************/

	public String getDefaultValue() {
		return defaultVal;
	}

	/**************************************************************************************************/

	public String getChildType() {
		return childType;
	}

	/**************************************************************************************************/
	// Misc ...
	/**************************************************************************************************/

	public boolean isAttribute() {
		return type == ATTRIBUTE;
	}

	/**************************************************************************************************/

	public boolean isChild() {
		return type == CHILD;
	}

	/**************************************************************************************************/

	public String toString() {
		return "IDCMetaModelAttribute: name=" + name + " / type=" + type + " / childtype = " + childType+ " / defaultVal = " + defaultVal;
	}

}
