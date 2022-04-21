package com.indirectionsoftware.utils;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class IDCEmailAuthenticator extends Authenticator {

	private static final String USER = "etiennesillon@gmail.com", PWD = "25AvaAva09";
	
    public PasswordAuthentication getAuth() {
        return new PasswordAuthentication(USER, PWD);
     }
    
}
