package com.indirectionsoftware.utils;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class IDCEmailAuthenticator extends Authenticator {

	private static final String USER = "xxxx", PWD = "xxxx";
	
    public PasswordAuthentication getAuth() {
        return new PasswordAuthentication(USER, PWD);
     }
    
}
