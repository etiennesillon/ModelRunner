package com.indirectionsoftware.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.indirectionsoftware.backend.database.IDCSecurityManager;

public class IDCEmail {

	public static final String DEFAULT_FROM = IDCSecurityManager.emailUser;
	private static final String DEFAULT_PWD = IDCSecurityManager.emailPwd;
	
	/*******************************************************************************************************/
	
	public static boolean send(String to, String subject, String text) {
		
		List<String> toDist = new ArrayList<String>();
		toDist.add(to);
		
		List<String> ccDist = new ArrayList<String>();
		
		return send(DEFAULT_FROM, toDist,ccDist, subject, text);
		
	}
	
	/*******************************************************************************************************/
	
	public static boolean send(List<String> toDist, List<String> ccDist, String subject, String text) {
		return send(DEFAULT_FROM, toDist,ccDist, subject, text);
	}
	
	/*******************************************************************************************************/
	
	public static boolean send(String from, List<String> toDist, List<String> ccDist, String subject, String text) {
		
		boolean ret = true;
		
        Properties properties = System.getProperties();

        properties.put("mail.smtp.host", "smtp.mail.yahoo.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true"); 

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(DEFAULT_FROM, DEFAULT_PWD);
            }
        });

        session.setDebug(true);

        try {

        	MimeMessage msg = new MimeMessage(session);
        	
            msg.setFrom(new InternetAddress(from));
            for(String to : toDist) {
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            }
            for(String cc : ccDist) {
	            msg.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
            }
			if (subject == null) {
				subject = "";
			}
			msg.setSubject(subject);
			msg.setHeader("X-Mailer", "MyEmailSender");
			msg.setSentDate(new Date());
            msg.setText(text);
            
            Transport.send(msg);
            
        } catch (Exception ex) {
			IDCUtils.debug("Exception in Send():" + ex.getMessage());
			Writer writer = new StringWriter();
		    PrintWriter printWriter = new PrintWriter(writer);
		    ex.printStackTrace(printWriter);
		    IDCUtils.debug(writer.toString());
			ret = false;
        }
	 
		return ret;
	
	}

}

