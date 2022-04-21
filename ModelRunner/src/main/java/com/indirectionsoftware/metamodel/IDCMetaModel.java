package com.indirectionsoftware.metamodel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class IDCMetaModel { 

	/*************************************************************************************************/
	// Fields ...
	/*************************************************************************************************/
	
	private String name;
	
	private List<IDCMetaModelEntity> entities;
	
	private static IDCMetaModel metaModel;
	
	/**************************************************************************************************/
	// Singleton ...
	/**************************************************************************************************
	
    public static void setMetamodel(IDCMetaModel metaModel) {
    	IDCMetaModel.metaModel = metaModel;
    }
    
    public static void setMetamodel(String fileName) {
    	setMetamodel(loadMetaModel(fileName));
    }
    
    public static void setMetamodel(File file) {
    	setMetamodel(loadMetaModel(file));
    }
    
	/**************************************************************************************************/

    public static IDCMetaModel getMetamodel() { 
    	return metaModel;
	}
    
	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCMetaModel(String name, List<IDCMetaModelEntity> entities) {
		
		this.name = name;
		this.entities = entities;

	}

	/**************************************************************************************************/

	public IDCMetaModel(String name) {
		this(name, new ArrayList<IDCMetaModelEntity>());
	}

	/**************************************************************************************************/
	// Getters ...
	/**************************************************************************************************/

	public String getName() {
		return name;
	}

	/**************************************************************************************************/

	public int getEntityCount() {
		return (int) entities.size();
	}

	/**************************************************************************************************/

	public List getEntities() {
		return entities;
	}

	/**************************************************************************************************/

	public IDCMetaModelEntity getEntity(int entityId) {

		IDCMetaModelEntity ret = null;

		if (entityId != -1) {
			ret = entities.get(entityId);
		}

		return ret;

	}

	/**************************************************************************************************/

	public IDCMetaModelEntity getEntity(String entityName) {

		IDCMetaModelEntity ret = null;

		for (int i = 0; i < entities.size() && ret ==  null; i++) {
			IDCMetaModelEntity entity = entities.get(i);
			if (entityName.equals(entity.getName())) {
				ret = entity;
			}
		}

		return ret;

	}

	/**************************************************************************************************/
	// Adders (!) ...
	/**************************************************************************************************/

	public IDCMetaModelEntity addEntity(String name, List<IDCMetaModelAttribute> attributes) {

		int nextEntityId = entities.size();

		IDCMetaModelEntity ret = new IDCMetaModelEntity(nextEntityId, name, attributes);

		entities.add(ret);

		return ret;

	}

	public IDCMetaModelEntity addEntity(String name) {
		return addEntity(name, new ArrayList<IDCMetaModelAttribute>());
	}
	
	/**************************************************************************************************/
	// Static loaders ...
	/**************************************************************************************************/

    public static IDCMetaModel loadMetaModel(File file) {
    	
    	IDCMetaModel ret = null;
    	
        try {

        	ret = new IDCMetaModelParser().loadMetaModel(new FileInputStream(file));
        	
        } catch(Exception ex) {
        	ex.printStackTrace();            	
        }
    	
    	return ret;

    }
    
	/************************************************************************************************/

    public static IDCMetaModel loadMetaModel(String xml) {
    	
    	IDCMetaModel ret = null;
    	
        try {

        	ret = new IDCMetaModelParser().loadMetaModel(new ByteArrayInputStream(xml.getBytes()));
        	
        } catch(Exception ex) {
        	ex.printStackTrace();            	
        }
    	
    	return ret;

    }

}
