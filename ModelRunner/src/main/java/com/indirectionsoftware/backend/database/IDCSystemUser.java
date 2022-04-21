package com.indirectionsoftware.backend.database;

import com.indirectionsoftware.metamodel.IDCApplication;

public class IDCSystemUser {

	IDCData userData;
	IDCApplication app;
	String[] faves;
	
	/*******************************************************************************************************/
	
	public final static String USER_NAME = "Name", USER_PASSWD = "Password", USER_APPS = "Applications";

	/*******************************************************************************************************/
	
	public IDCSystemUser(IDCData userData, IDCApplication app) {
		this.userData = userData;
		this.app = app;
		this.app.resetType(this.userData);
		String faves = "";
		this.faves = faves.split("\\|");
	}

	/*******************************************************************************************************/
	
	public IDCApplication getApplication() {
		return app;
	}

	/*******************************************************************************************************/
	
	public IDCData getUserData() {
		return userData;
	}

	/*******************************************************************************************************/
	
	public String[] getFavourites() {
		return faves;
	}

}
