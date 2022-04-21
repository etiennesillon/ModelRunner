package com.indirectionsoftware.backend.database;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.protobuf.ByteString;

public class Quickstart {
	
	public static final String projectId = "modelrunner-346406";
	public static final String secretId = "MySecret";
	
	/*******************************************************************************************************/
	
	public static void main(String[] args) throws Exception {
		
		quickstart(projectId, secretId);
		
	}

	/*******************************************************************************************************/
	
	public static void quickstart(String projectId, String secretId) throws Exception {

		try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {

			ProjectName projectName = ProjectName.of(projectId);

			Secret secret = Secret.newBuilder().setReplication(Replication.newBuilder().setAutomatic(Replication.Automatic.newBuilder().build()).build()).build();

			Secret createdSecret = client.createSecret(projectName, secretId, secret);

			SecretPayload payload = SecretPayload.newBuilder().setData(ByteString.copyFromUtf8("secret word")).build();
			SecretVersion addedVersion = client.addSecretVersion(createdSecret.getName(), payload);
	
			AccessSecretVersionResponse response = client.accessSecretVersion(addedVersion.getName());
	
			String data = response.getPayload().getData().toStringUtf8();
			System.out.printf("Plaintext: %s\n", data);
		}
		
	}
	
}