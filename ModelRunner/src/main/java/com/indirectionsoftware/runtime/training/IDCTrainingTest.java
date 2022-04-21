package com.indirectionsoftware.runtime.training;

import com.indirectionsoftware.backend.database.IDCDbManager;
import com.indirectionsoftware.backend.database.IDCSystemApplication;
import com.indirectionsoftware.backend.database.IDCSystemUser;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCType;

public class IDCTrainingTest {
	
	static IDCDbManager dbManager; 
	static IDCSystemApplication sysApp;
	
	static final String APPNAME = "Training";

	/*******************************************************************************************************/
	
	public static void main(String[] args) {
		
		if (args.length < 1) {
            System.err.println("Please specify properties file ...");
        } else {
    		dbManager = IDCDbManager.getIDCDbManager(args[0], true);
    		sysApp = dbManager.getSystemApplication();
    		if(sysApp == null) {
    			System.err.println("Can't load ModelAdmin application ... ");
    		} else {
    			IDCSystemUser user = sysApp.login(APPNAME, IDCSystemApplication.DEFAULT_USER, IDCSystemApplication.DEFAULT_PWD);
    			if(user != null) {
    				IDCApplication app = user.getApplication();
    				app.connect();
    				int nType = 0;
    				for(String typeName : TYPES) {
    					IDCType type = app.getType(typeName);
    					if(type != null) {
    						for(int nObj=0; nObj < RECORDS[nType]; nObj++) {
    							type.getNewTestObject();
    						}
    					}
    				}
    				app.disconnect();
    			}

    		}

        }

	}
	
}