package com.indirectionsoftware.backend.database;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;

public class GCPSecretTest {

	/************************************************************************************************/

	public static final String projectId = "modelrunner-346406";
	public static final String secretId = "MySQLPassword";
	public static final String versionId = "1";
	
	public static final String NAME1 = "projects/551091996157/secrets/MySQLPassword/versions/1";
	public static final String NAME2 = "projects/modelrunner-346406/secrets/MySQLPassword/versions/1";

	/**************************************************************************************************/
    
	public static void main(String[] args) {
    	
		try {
			
			SecretManagerServiceClient client = SecretManagerServiceClient.create();
			
	    	debug("client = " + client);
	    	
	    	SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);
	    	debug("secretVersionName = " + secretVersionName.getProject() + " / " + secretVersionName.getSecret() + " / " + secretVersionName.getSecretVersion()); 
	    	debug("secretVersionName = " + secretVersionName.toString());
			
	    	getSecret(client, secretVersionName);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
    	    	
    }
    
	/**************************************************************************************************/
    
	public static void getSecret(SecretManagerServiceClient client, SecretVersionName secretVersionName) {
    	
		try {
			
	    	debug("secretVersionName = " + secretVersionName.getProject() + " / " + secretVersionName.getSecret() + " / " + secretVersionName.getSecretVersion()); 
	    	debug("secretVersionName = " + secretVersionName.toString());
			
			AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

			debug("response = " + response);
			
			String secret = response.getPayload().getData().toStringUtf8();
			
	    	debug("secret = " + secret);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    	    	
    }
    
    /************************************************************************************************/

	public static void debug(String s) {
		System.out.println("Thread(" + Thread.currentThread().getId() + ") " +s); 
	}

}