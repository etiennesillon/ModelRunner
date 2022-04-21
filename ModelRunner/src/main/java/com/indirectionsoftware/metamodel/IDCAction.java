package com.indirectionsoftware.metamodel;

import java.util.List;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCAction extends IDCModelData {
	
	private boolean isMetaAction, isPreSave, isUpload;
	private int actionType;
	private String formula;

	public static int ISMETAACTION=START_ATTR, ISPRESAVE=START_ATTR+1, ISUPLOAD=START_ATTR+2, TYPE=START_ATTR+3, FORMULA=START_ATTR+4;
	
	private static final int CREATE=0, UPDATE=1, DELETE=2, GUI=3, BACKGROUND=4;
	
	public static final String ACTION_TYPES[] = {"CREATE", "UPDATE", "DELETE", "GUI", "BACKGROUND"};

	
	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCAction(IDCModelData parent, long id, List<Object> values) {
		super(parent, IDCModelData.ACTION, id, values);
	}

	/**************************************************************************************************/

	public void init(IDCData userData) {
		
		if(isInitRequired()) {
			
			super.init(userData);
			
			isMetaAction = IDCUtils.translateBoolean(getString(ISMETAACTION));
			isPreSave = IDCUtils.translateBoolean(getString(ISPRESAVE));
			isUpload = IDCUtils.translateBoolean(getString(ISUPLOAD));

			String actionTypeStr = getString(TYPE);
			actionType = 0;
			for(String typeName : ACTION_TYPES) {
				if(actionTypeStr.equals(typeName)) {
					break;
				} else {
					actionType++;
				}
			}
			formula = getString(FORMULA);

			completeInit();

		}
		
	}

	/**************************************************************************************************/
	// Actions methods ...
	/**************************************************************************************************/
	
	public boolean isMetaAction() {
    	return isMetaAction;
	}

    /************************************************************************************************/
    
	public boolean isPreSave() {
    	return isPreSave;
	}

    /************************************************************************************************/
    
	public int getActionType() {
    	return actionType;
	}
	
	public boolean isCreateAction() {
		return actionType == CREATE;
	}

	public boolean isUpdateAction() {
		return actionType == UPDATE;
	}
    
	public boolean isDeleteAction() {
		return actionType == DELETE;
	}

	public boolean isGUIAction() {
		return actionType == GUI;
	}

	public boolean isBackgroundAction() {
		return actionType == BACKGROUND;
	}

    /************************************************************************************************/
    
	public String getFormula() {
    	return formula;
	}

    /************************************************************************************************/
    
    public void execute(Object data, String content) {
    	
    	if(isMetaAction()) {
    		executeMetaAction((IDCModelData)data, content);
    	} else {
    		executeAction((IDCData)data, content, true);
    	}

    }
    
    /************************************************************************************************/
    
    public void executeMetaAction(IDCModelData modelData, String content) {
    	
		switch(((IDCModelData)modelData).getEntityType()) {
		
		case IDCModelData.TYPE:
			
			List<IDCData> list = ((IDCType)modelData).loadAllDataObjects();
			for(IDCData data : list) {
				if(data.isEditable(this)) {
					executeAction(data, content, true);
				}
			}
			break;
		
		}
    
    }
    
    /************************************************************************************************/
        
    public void executeAction(IDCData data, String content, boolean isSave) {
    	
    	IDCUtils.debug("executing action formula = " + formula + " for data = " + data);
    	
		data.applyUpdates(null, formula, content, isSave);
    
    }

    /************************************************************************************************/
    
	public boolean isUpload() {
		return isUpload;
	}

}