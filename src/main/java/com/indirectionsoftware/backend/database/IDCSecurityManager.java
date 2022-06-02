package com.indirectionsoftware.backend.database;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCSecurityManager {
	
	/*******************************************************************************************************/
	
	public static String adminUser, adminPwd, emailUser, emailPwd, dbUser, dbPwd;

	/*******************************************************************************************************/
	
	public static final String DBUSER_ENV = "MRDbUser";
	public static final String DBPWD_ENV = "MRDbPassword";
	
	public static final String EMAILUSER_ENV = "MREmailUser";
	public static final String EMAILPWD_ENV = "MREmailPassword";
	
	public static final String ADMINUSER_ENV = "MRAdminUser";
	public static final String ADMINPWD_ENV = "MRAdminPassword";
	
	/****************************************************************************
     * GCP stuff                                                                *
     ****************************************************************************/

	public static final String GCP_PROJECT_ID = "modelrunner-346406";

	public static final String GCP_SECRET_VERSION_ID = "1";
	
	/*******************************************************************************************************/
	
	static {
		
		dbUser = System.getenv(DBUSER_ENV);
		
		if(dbUser != null) {
			IDCUtils.info("IDCSecurityManager: using env variables");
			dbPwd = System.getenv(DBPWD_ENV);
			adminUser = System.getenv(ADMINUSER_ENV); 
			adminPwd = System.getenv(ADMINPWD_ENV);
			emailUser = System.getenv(EMAILUSER_ENV);
			emailPwd = System.getenv(EMAILPWD_ENV);
		} else { 
			IDCUtils.info("IDCSecurityManager: using GCP secrets");
			dbUser = getGCPSecret(DBUSER_ENV, GCP_SECRET_VERSION_ID);
			dbPwd = getGCPSecret(DBPWD_ENV, GCP_SECRET_VERSION_ID);
			adminUser = getGCPSecret(ADMINUSER_ENV, GCP_SECRET_VERSION_ID);
			adminPwd = getGCPSecret(ADMINPWD_ENV, GCP_SECRET_VERSION_ID);
			emailUser = getGCPSecret(EMAILUSER_ENV, GCP_SECRET_VERSION_ID);
			emailPwd = getGCPSecret(EMAILPWD_ENV, GCP_SECRET_VERSION_ID);
		}

	}

	/**************************************************************************************************/
    // GCP stuff
	/**************************************************************************************************/
    
    public static String getGCPSecret(String secretId, String versionId) throws Error {
    	
    	String ret = null;
    	
		try {
			
			SecretManagerServiceClient client = SecretManagerServiceClient.create();
			
			if(client != null) {
				
		    	SecretVersionName secretVersionName = SecretVersionName.of(GCP_PROJECT_ID, secretId, versionId);
				
		    	IDCUtils.debug("IDCSecurityManager.getGCPSecret(): secretVersionName == " + secretVersionName);

				AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

				IDCUtils.debug("IDCSecurityManager.getGCPSecret(): response == " + response);

				ret = response.getPayload().getData().toStringUtf8();
				
				IDCUtils.debug("IDCSecurityManager.getGCPSecret(): ret == " + ret);
				
			} else {
				IDCUtils.error("IDCSecurityManager.getGCPSecret(): client == null");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return ret;
    	
    }
    

}