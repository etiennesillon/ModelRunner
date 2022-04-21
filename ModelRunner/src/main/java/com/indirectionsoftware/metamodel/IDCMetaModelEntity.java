package com.indirectionsoftware.metamodel;

import java.util.List;


public class IDCMetaModelEntity {
	
	private int id;

	private String name;
	
	private List<IDCMetaModelAttribute> attributes;
	
	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/

	public IDCMetaModelEntity(int id, String name, List<IDCMetaModelAttribute> attributes) {
		
		this.id = id;
		this.name = name;
		this.attributes = attributes;
		
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

	public List<IDCMetaModelAttribute> getAttributes() {
		return attributes;
	}
	
	/**************************************************************************************************/
	// Adders (!) ...
	/**************************************************************************************************/

	public IDCMetaModelAttribute addCommonAttribute(IDCMetaModelAttribute attr) {

		int nextAttributeId = attributes.size();

		IDCMetaModelAttribute ret = new IDCMetaModelAttribute(nextAttributeId, attr.getName(), attr.getType(), attr.getChildType(), attr.getDefaultValue());

		attributes.add(ret);

		return ret;

	}

	public IDCMetaModelAttribute addAttribute(String name, String type, String refType, String defaultVal) {

		int nextAttributeId = attributes.size();

		IDCMetaModelAttribute ret = new IDCMetaModelAttribute(nextAttributeId, name, type, refType, defaultVal);

		attributes.add(ret);

		return ret;

	}

	/**************************************************************************************************/
	// Misc ...
	/**************************************************************************************************/

	public String toString() {
		
		String ret = "IDCMetaModelEntity: name=" + name + " / attributes:\n";
		
		for(IDCMetaModelAttribute attr : attributes) {
			ret += "> " + attr + "\n";	
		}
		
		return ret;
		
	}

}
