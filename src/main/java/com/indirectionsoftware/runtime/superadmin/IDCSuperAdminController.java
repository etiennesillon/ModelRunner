package com.indirectionsoftware.runtime.superadmin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indirectionsoftware.backend.database.IDCSuperAdminDbManager;
import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCSuperAdminApplication;
import com.indirectionsoftware.runtime.IDCController;
import com.indirectionsoftware.utils.IDCEmail;
import com.indirectionsoftware.utils.IDCUtils;

public class IDCSuperAdminController extends IDCController {
	
	IDCSuperAdminDbManager dbManager; 
	IDCSuperAdminApplication superAdminApp;
	
	public static final int NA = -1;
	
	static final int OUT_HTML=0, OUT_XML=1, OUT_JSON=2;
	
	public static final String ACTION_PARM="action";
	static final String FIRSTNAME_PARM="firstName";
	static final String LASTNAME_PARM="lastName";
	static final String EMAIL_PARM="email";
	static final String NOTES_PARM="notes";

	static final String TOKEN_PARM="token";
	
	static final String SESSIONID="appsessionid";

	public static final int REGISTER=0, CONFIRMREGISTRATION=1, CONTACT=2, LIST=3, RESEND_LINK=4;
	
	static final String[] REGISTER_FIELDS = {FIRSTNAME_PARM, LASTNAME_PARM, EMAIL_PARM};
	
	/****************************************************************************
     *  init()                                                                  *
     ****************************************************************************/

	public void init(ServletConfig conf) {
		
		IDCUtils.info("IDCSuperAdminController starting ...");
		
		dbManager = (IDCSuperAdminDbManager) servletInit(conf, true);
		if(dbManager != null) {
			superAdminApp = dbManager.getSuperAdminApplication();
		} else {
			IDCUtils.error("IDCSuperAdminController.init(): dbManager == null");
		}
		
	}
	
	/****************************************************************************
     *  destroy()                                                               *
     ****************************************************************************/

	public void destroy() { 

		IDCUtils.info("IDCSuperAdminController.destroy() ... closing connection");
		dbManager.disconnect(); 

	}
	
	/****************************************************************************
     *  dispatch()                                                              *
     ****************************************************************************/

	public void dispatch(HttpServletRequest request, HttpServletResponse response, String input) throws ServletException, IOException {
		
		IDCUtils.debug("IDCSuperAdminController Request received: input=" + input);
		
		PrintWriter out = response.getWriter();
		    
        try {
        	
        	int query = getIntParam(request, ACTION_PARM);
        	if(query == NA) {
    			sendError(out, "Missing mandatory action parameter");
    		} else if(query == REGISTER) {
        		register(request, out);
    		} else if(query == CONFIRMREGISTRATION) {
        		confirmRegistration(request, out);
    		} else if(query == RESEND_LINK) {
        		resendLink(request, out);
    		} else if(query == CONTACT) {
        		contact(request, out);
    		} else if(query == LIST) {
        		list(request, out);
        	}

    		IDCUtils.debug("IDCWebAppController All done ...");

        } catch(java.lang.NumberFormatException ex) {
        } catch(Error er) {
        	sendError(out, er.getMessage());
        }

	}

	/****************************************************************************
	 *  register()                                                              *
	 ****************************************************************************/
	
	private void register(HttpServletRequest request, PrintWriter out) {

		String firstName = getParam(request, FIRSTNAME_PARM);
		String lastName = getParam(request, LASTNAME_PARM);
		String email = getParam(request, EMAIL_PARM);
		
		IDCUtils.debug("IDCSuperAdminController.register: firstName = " + firstName);
		IDCUtils.debug("IDCSuperAdminController.register: lastName = " + lastName);
		IDCUtils.debug("IDCSuperAdminController.register: email = " + email);

		if(checkMandatoryFields(request, REGISTER_FIELDS)) {
			
			if(superAdminApp != null) {
				
				IDCData reg = superAdminApp.getRegistrationFromEmail(email);
				if(reg == null) {
					
					String token = UUID.randomUUID().toString();

					superAdminApp.registerEmail(token ,firstName, lastName, email);
					
					sendLink(email, token, out, false);
					
				} else {
					
					String text = IDCSuperAdminApplication.HTML + "<h1 id=\"text\">Ooops!</h1><p id=\"text\">Sorry, this email address is already registered, please use another one";

					if(reg.getInt(IDCSuperAdminApplication.REG_STATUS) == IDCSuperAdminApplication.REG_STATUS_PENDING) {
						String link = "https://modelrunner.org/SuperAdmin?action=" + RESEND_LINK + "&" + EMAIL_PARM + "=" + email;
						text += " or click <a href=\"" + link + "\">here</a> to get the registration email again"; 
					}
					text += ".</p><br></div></html>"; 

					sendHTML(out, text);
				
				}
				
			} else {
				sendError(out, "No DB Manager to login");
			}
			
		} else {
			sendError(out, "Missing mandatory fields");
		}
    	
	}
	
	/****************************************************************************
	 *  resendLink()                                                            *
	 ****************************************************************************/
	
	private void resendLink(HttpServletRequest request, PrintWriter out) {

		String email = getParam(request, EMAIL_PARM);
		
		IDCUtils.debug("IDCSuperAdminController.resendLink: email = " + email);

		if(superAdminApp != null) {
			
			IDCData reg = superAdminApp.getRegistrationFromEmail(email);
			if(reg != null) {

				String token = reg.getString(superAdminApp.REG_TOKEN);

				sendLink(email, token, out, true);
				
			} else {
				IDCUtils.debug("IDCSuperAdminController.resendLink: can't find registration for email = " + email);
				sendHTML(out, IDCSuperAdminApplication.HTML + "<h1 id=\"text\">Ooops!</h1><p id=\"text\">Sorry, can't find registration for " + email + ".</p><br></div></html>");
			}
			
		} else {
			sendError(out, "No DB Manager to login");
		}
    	
	}
	
	/****************************************************************************
	 *  sendLink()                                                              *
	 ****************************************************************************/
	
	private void sendLink(String email, String token, PrintWriter out, boolean isResend) {

		String link = "https://modelrunner.org/SuperAdmin?action=" + CONFIRMREGISTRATION + "&" + TOKEN_PARM + "=" + token;
		String text = "Please click on this link to activate your new account: " + link;
		
		boolean isEmailSent = IDCEmail.send(email, "Model Runner registration", text);
		if(isEmailSent) {
			sendHTML(out, IDCSuperAdminApplication.HTML + "<h1 id=\"text\">Thank you!</h1><p id=\"text\">An email has been sent to " + email + ". Please click on the link to activate your new account.</p><br></div></html>");
			IDCEmail.send(IDCEmail.DEFAULT_FROM, "New Model Runner registration " + (isResend ? "resend" : "") + " - " + email, text);
		} else {
			sendHTML(out, IDCSuperAdminApplication.HTML + "<h1 id=\"text\">Ooops!</h1><p id=\"text\"> There was a problem sending the registration email to " + email + ". Please try again later?</p><br></div></html>");
			IDCEmail.send(IDCEmail.DEFAULT_FROM, "Model Runner registration " + (isResend ? "resend" : "") + "Error - " + email, "Email couldn't be sent");
		}
    	
	}
	

	/****************************************************************************
	 *  confirmRegistration()                                                   *
	 ****************************************************************************/
	
	private void confirmRegistration(HttpServletRequest request, PrintWriter out) {

		String token = getParam(request, TOKEN_PARM);

		IDCUtils.debug("IDCSuperAdminController.confirmRegistration: token = " + token);
    	
		if(token == null || token.length() ==0) {
			sendError(out, "Missing mandatory fields");
		} else {
			IDCData reg = superAdminApp.confirmRegistration(token); 
			if(reg != null) {
				sendHTML(out, IDCSuperAdminApplication.getNewAccountForm(reg));
			} else {
				sendHTML(out, IDCSuperAdminApplication.HTML + "<h1 id=\"text\">Ooops!</h1><p id=\"text\">Couldn't find your registration.</p><br></div></html>");
			}
		}
		
	}
       
	/****************************************************************************
	 *  contact()                                                              *
	 ****************************************************************************/
	
	private void contact(HttpServletRequest request, PrintWriter out) {

		String firstName = getParam(request, FIRSTNAME_PARM);
		String lastName = getParam(request, LASTNAME_PARM);
		String email = getParam(request, EMAIL_PARM);
		String notes = getParam(request, NOTES_PARM);
		
		IDCUtils.debug("IDCSuperAdminController.register: firstName = " + firstName);
		IDCUtils.debug("IDCSuperAdminController.register: lastName = " + lastName);
		IDCUtils.debug("IDCSuperAdminController.register: email = " + email);
		IDCUtils.debug("IDCSuperAdminController.register: notes = " + notes);

		if(checkMandatoryFields(request, REGISTER_FIELDS)) {
			
			if(superAdminApp != null) {
				
				sendHTML(out, "<h1>Thank you " + firstName + "!</h1><p>We will come back to you shortly.</p>");
				IDCEmail.send(IDCEmail.DEFAULT_FROM, "Contact - " + email, "firstName = " + firstName + "\nlastName = " + lastName + "\nemail=" + email + "\nnotes=" + notes);
				
			} else {
				sendError(out, "No DB Manager to login");
			}
			
		} else {
			sendError(out, "Missing mandatory fields");
		}
    	
	}
	
	/****************************************************************************
	 *  list()                                                                  *
	 ****************************************************************************/
	
	private void list(HttpServletRequest request, PrintWriter out) {
		
		sendHTML(out, superAdminApp.getAllRegistrationsHTML());
    	
	}
	
}
