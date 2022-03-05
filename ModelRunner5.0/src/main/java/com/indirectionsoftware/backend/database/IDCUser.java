package com.indirectionsoftware.backend.database;

import java.lang.reflect.Array;

import com.indirectionsoftware.metamodel.IDCApplication;

public class IDCUser {

	String userid, pwd;
	String[] faves;
	IDCApplication appl;
	
	/*******************************************************************************************************/
	
	public static final String ADMIN_USER = "admin", ADMIN_PASSWD = "passwd";

	/*******************************************************************************************************/
	
	public IDCUser() {
		this.userid = null;
		this.pwd = null;
		this.faves = null;
	}

	/*******************************************************************************************************/
	
	public IDCUser(String userid, IDCApplication appl) {
		this.userid = userid;
		this.appl = appl;
		String faves = "";
		this.faves = faves.split("\\|");
	}

	/*******************************************************************************************************/
	
	public IDCUser(String userid, String pwd, String faves) {
		this.userid = userid;
		this.pwd = pwd;
		
		if(faves == null) {
			faves = "";
		}
		this.faves = faves.split("\\|");

	}

	/*******************************************************************************************************/
	
	public boolean checkPassword(String pwd) {
		
		boolean ret = false;
		
		if(this.pwd.equals(pwd)) {
			ret = true;
		}
		
		return ret;
		
	}

	/*******************************************************************************************************/
	
	public IDCApplication getApplication() {
		return appl;
	}

	/*******************************************************************************************************/
	
	public String getId() {
		return userid;
	}

	/*******************************************************************************************************/
	
	public String[] getFavourites() {
		return faves;
	}

	/*******************************************************************************************************/
	
	public void setCurrentApplication(IDCApplication appl) {
		this.appl = appl;
	}

	/*******************************************************************************************************/
	
	public void updateFavourites(String faves) {
		
	}


}
