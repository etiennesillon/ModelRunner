package com.indirectionsoftware.backend.database;

import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.runtime.webapp.IDCWebAppController;

public class IDCSuperAdminApplication {

	public final static String REG_TYPE = "Registration"; 
	public final static String REG_TOKEN = "Token", REG_FIRST_NAME = "FirstName", REG_LAST_NAME = "LastName", REG_EMAIL = "Email", REG_STATUS = "Status", REG_URL = "URL";
	public final static int REG_STATUS_PENDING = 0, REG_STATUS_CONFIRMED = 1;

	public final static String ADMIN_APPL = "$$SuperAdmin";
	
	public static String HTML = "<!DOCTYPE html><html><head><title>Model Runner</title><link rel=\"stylesheet\" href=\"commonstyles.css\"></head><body><div id=\"fullDiv\"><div id=\"header\"><img id=\"logo\" src=\"ModelRunnerSmall.png\" alt=\"logo\"><h1 id=\"title\" >Model Runner - New Account</h1><ul id=\"menu\"><li id=\"menuli\"><a id=\"menulia\" href=\"index.html\">Home</a></li><li id=\"menuli\"><a id=\"menulia\" href=\"contact.html\">Contact</a></li><li id=\"menuli\"><a id=\"menulia\" href=\"appeditor.html\">Models</a></li><li id=\"menuli\"><a id=\"menulia\" href=\"app.html\">Apps</a></li><li id=\"menuli\"><a id=\"menulia\" href=\"workfloweditor.html\">Workflows</a></li></ul></div><div id=\"body\">";
	
	/*******************************************************************************************************/
	
	private IDCApplication superAdminApp;
	
	/*******************************************************************************************************/
	// System Application
	/*******************************************************************************************************/
	
	public IDCSuperAdminApplication(IDCApplication superAdminApp) {
		this.superAdminApp = superAdminApp;
		superAdminApp.connect();
	}
	
	/*******************************************************************************************************/
	
	public IDCApplication getSystemApplication() {
		return superAdminApp;
	}
	
	/*******************************************************************************************************/
	
	public void generateSchema() {
		superAdminApp.generateSchema(true);
	}

	/*******************************************************************************************************/
	
	public String getModelXML() {
		return superAdminApp.getModelXML();
	}

	public String getName() {
		return superAdminApp.getName();
	}
	
	/*******************************************************************************************************/
	
	public void registerEmail(String token, String firstName, String lastName, String email) {
	
		IDCData reg = superAdminApp.getType(REG_TYPE).createData();
		reg.set(REG_TOKEN, token);
		reg.set(REG_FIRST_NAME, firstName);
		reg.set(REG_LAST_NAME, lastName);
		reg.set(REG_EMAIL, email);
		reg.set(REG_STATUS, REG_STATUS_PENDING);
		reg.save(false, false);
		
	}

	/*******************************************************************************************************/
	
	public IDCData getRegistrationFromToken(String token) {
		return superAdminApp.getType(REG_TYPE).requestSingleData(REG_TOKEN + " == '" + token + "'");

	}

	/*******************************************************************************************************/
	
	public IDCData getRegistrationFromEmail(String email) {
		return superAdminApp.getType(REG_TYPE).requestSingleData(REG_EMAIL + " == '" + email + "'");

	}

	/*******************************************************************************************************/
	
	public String getAllRegistrationsHTML() {
		
		String ret = "<h1>Current registrations:</h1>" ;
		
		for(IDCData reg : superAdminApp.getType(REG_TYPE).loadAllDataObjects()) {
			ret += "<p><b>email = " + reg.getString(REG_EMAIL);
			ret += "</b><ul><li>First Name = " + reg.getString(REG_FIRST_NAME);
			ret += "</li><li>Last Name = " + reg.getString(REG_LAST_NAME);
			ret += "</li><li>Status = " + reg.getString(REG_STATUS);
			ret += "</li></ul></p>"; 
			
		}
		
		return ret;

	}

	/*******************************************************************************************************/
	
	public String getAllRegistrationsCSV() {
		
		String ret = "email, First Name, Last Name, Status, token\n" ;
		
		for(IDCData reg : superAdminApp.getType(REG_TYPE).loadAllDataObjects()) {
			ret += reg.getString(REG_EMAIL) + "," + reg.getString(REG_FIRST_NAME) + "," + reg.getString(REG_LAST_NAME) + "," + reg.getString(REG_STATUS) + "," + reg.getString(REG_TOKEN) + "\n";
		}
		
		return ret;

	}

	/*******************************************************************************************************/
	
	public IDCData confirmRegistration(String token) {
		
		IDCData ret = getRegistrationFromToken(token);
		if(ret != null) {
			if(ret.getInt(REG_STATUS) == REG_STATUS_PENDING) {
				ret.set(REG_STATUS, REG_STATUS_CONFIRMED);
				ret.save(false, false);
			} else {
				ret = null;
			}
		}
				
		return ret;
		
	}

	/*******************************************************************************************************/
	
	public static String getNewAccountForm(IDCData reg) {
		return getNewAccountForm(reg.getString(REG_EMAIL), "", "", "");
	}
	
	public static String getNewAccountForm(String email, String password, String password2, String message) {

		String ret = HTML;
		
		if(email != null && email.length() > 0) {
			
			String emailPH = null, passwdPH = null, passwd2PH = null;
			
			if(password == null || password.length() == 0) {
				password = null;
				passwdPH = "password";
			}
			
			if(password2 == null || password2.length() == 0) {
				password2 = null;
				passwd2PH = "password";
			}
			
			ret += "<h1 id=\"text\">New Account</h1><p id=\"text\">Thank you for confirming your email. Please fill in the following form to create your account:</p>";
			ret += "<form name=\"input\" action=\"WebApp?action=" + IDCWebAppController.CREATE_ACCOUNT + "&email=" + email + "\" method=\"post\"><table>";
			
//			ret += getNewFormField("Account Name", IDCWebAppController.ACCOUNT_PARM, "text");
			ret += getNewFormField("UserId", IDCWebAppController.USERID_PARM, "text", email, null, true);
			ret += getNewFormField("Password", IDCWebAppController.PASSWD_PARM, "password",password, passwdPH, false);
			ret += getNewFormField("Confirm Password ", IDCWebAppController.PASSWD2_PARM, "password",password2, passwd2PH, false);
			
	        ret += "</table><button id=\"text\" type=\"submit\">Create Account</button><br></form>";
	        
	        if(message.length() > 0) {
	        	ret += "<p>" + message + "</p>";
	        }
			
		} else {
			ret += "<h1 id=\"text\">Ooops!</h1><p id=\"text\">Sorry, there was a problem processing your request.</p>";
		}
		
		ret += "<br></div></html>";

		return ret;
		
	}

	/*******************************************************************************************************/
	
	public static String getNewFormField(String label, String name, String type, String value, String placeholder, boolean isDisabled) {
        return "<tr><td><label for=\"" + name + "\"><b>" + label + "</b></label></td><td><input type=\"" + type + (placeholder != null ? "\" placeholder=\"" + label : "") + "\" name=\"" + name + (value != null ? "\" value=\"" + value : "") + "\"" + (isDisabled ? "disabled" : "") + "></td></tr>";
	}
	
}