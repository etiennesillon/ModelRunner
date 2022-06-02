package com.indirectionsoftware.backend.database;

import com.indirectionsoftware.metamodel.IDCApplication;

public class IDCSystemUser {

	IDCData accountData;
	IDCData userData;
	IDCApplication app;
	int role;
	
	String[] faves;
	

	/*******************************************************************************************************/
	
	public IDCSystemUser(IDCData userData, IDCApplication app) {
		this.userData = userData;
		if(app != null) {
			this.app = app;
			this.app.resetType(this.userData);
		}
		String faves = "";
		this.faves = faves.split("\\|");
	}

	/*******************************************************************************************************/
	
	public IDCSystemUser(IDCData accountData, IDCData userData, IDCApplication app, int role) {
		this.accountData = accountData;
		this.userData = userData;
		if(app != null) {
			this.app = app;
			this.app.resetType(this.userData);
		}
		this.role = role;
		String faves = "";
		this.faves = faves.split("\\|");
	}

	/*******************************************************************************************************/
	
	public IDCApplication getApplication() {
		return app;
	}

	/*******************************************************************************************************/
	
	public IDCData getAccountData() {
		return accountData;
	}

	/*******************************************************************************************************/
	
	public IDCData getUserData() {
		return userData;
	}

	/*******************************************************************************************************/
	
	public String getName() {
		return userData.getName();
	}

	/*******************************************************************************************************/
	
	public int getRole() {
		return role;
	}

	/*******************************************************************************************************/
	
	public String[] getFavourites() {
		return faves;
	}

}
