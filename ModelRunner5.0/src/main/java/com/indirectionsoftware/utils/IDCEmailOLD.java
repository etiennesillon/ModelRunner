package com.indirectionsoftware.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public abstract class IDCEmailOLD {

	protected String smtpServer, popServer, emailUser, emailPassword;
	
	private static final String PDF_CONTENT = "application/pdf", TEXT_CONTENT = "text/plain", HTML_CONTENT = "text/html";
	
	protected String pdfDir = "ProcessedPDFs";
	
	Store store=null;
    Folder folder=null;
    
    protected boolean debug;
    
	/*******************************************************************************************************/
	
	public IDCEmailOLD(String smtpServer, String popServer, String emailUser, String emailPassword) {
		
		this.smtpServer = smtpServer;
		this.popServer = popServer;
		this.emailUser = emailUser;
		this.emailPassword = emailPassword;
		this.debug = false;
	
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public void setPDFDir(String pdfDir) {
		this.pdfDir = pdfDir;
	}
	
	/*******************************************************************************************************/
		
	public boolean send(String to, String cc, String subject, String body) {
	 
		debug("Starting send() ...");
		
		boolean ret = true;
		
		Properties props = System.getProperties();

		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", smtpServer);
		props.put("mail.smtp.auth", "true");
		
        Authenticator auth = new IDCEmailAuthenticator(emailUser, emailPassword);
		Session session = Session.getDefaultInstance(props, auth);

		Message msg = new MimeMessage(session);

		try {

			msg.setFrom(new InternetAddress(emailUser));
			msg.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to, false));
			if (cc != null) {
				msg.setRecipients(Message.RecipientType.CC,InternetAddress.parse(cc, false));
			}	
			msg.setSubject(subject);
			msg.setText(body);
			msg.setHeader("X-Mailer", "MyEmailSender");
			msg.setSentDate(new Date());
			
			//ByteArrayOutputStream byteStream=new ByteArrayOutputStream();
			//ObjectOutputStream objectStream=new ObjectOutputStream(byteStream);
			//objectStream.writeObject(theObject);
			//msg.setDataHandler(new DataHandler( new ByteArrayDataSource( byteStream.toByteArray(), "lotontech/javaobject" )));
			
			Transport.send(msg);
			
		} catch (Exception e) {
			processException("Exception in Send()", e, "to=" + to + " / cc=" + cc + " / subject=" + subject + " / body=" + body);
			ret = false;
		}
		
		debug("Ending send() ...");
		
		return ret;
	
	}

	/*******************************************************************************************************/
	
	public void receive() {
	    receive("INBOX");
	}
	
	/*******************************************************************************************************/
		
		public void receive(String fldrName) {
		    
	    try {

	    	Properties props = System.getProperties();
	    	
	    	Session session = Session.getDefaultInstance(props, null);
	    	
	    	store = session.getStore("pop3");
	    	store.connect(popServer, emailUser, emailPassword);
	      
	    	folder = store.getDefaultFolder();
	    	if (folder == null) {
	    		throw new Exception("No default folder");
	    	}

	    	//Folder[] folders = folder.list();
	    	//for(Folder f : folders) {
	    	//	IDCUtils.debug("folder = " + f.getFullName());
	    	//}
	    	
	    	folder = folder.getFolder(fldrName);
	    	if (folder == null) {
	    		throw new Exception("No POP3 folder" + fldrName);
	    	}

	    	folder.open(Folder.READ_WRITE);
	    	Message[] msgs = folder.getMessages();
	    	
	    	
			debug("Messages in " + fldrName + " folder to process = " + msgs.length);
			for(int nMsg=0, maxMsgs = msgs.length; nMsg < maxMsgs; nMsg++) {
				processMessage(msgs[nMsg]);
				System.gc();
			}
			
	    } catch (Exception ex) {
			processException("Exception in IDCEmail.receive()", ex, "");
	    } finally {
	    	try {
	    		if(folder != null) {
	    			folder.close(true);
	    		}
	    		if(store!=null) {
	    			store.close();
	    		}
		    } catch (Exception ex) {
		    	ex.printStackTrace();
		    } 
	    }
	    
	}

	/*******************************************************************************************************/
	
	public boolean forward(String to, String cc, String from, String subject, Message message) {
	    
		debug("Starting forward() ...");
		
		boolean ret = false;
		
		/************************************
		Properties props = System.getProperties();

		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", smtpServer);
		props.put("mail.smtp.auth", "true");
		
        Authenticator auth = new IDCEmailAuthenticator(popUser, popPassword);
		Session session = Session.getDefaultInstance(props, auth);

		Message fwdMessage = new MimeMessage(session);

		try {

			fwdMessage.setFrom(new InternetAddress(from));
			fwdMessage.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to, false));
			if (cc != null) {
				fwdMessage.setRecipients(Message.RecipientType.CC,InternetAddress.parse(cc, false));
			}	
			if (subject == null) {
				subject = "FWD: " + message.getSubject();
			}
			debug("forward() ... subject = " + subject);
			fwdMessage.setSubject(subject);
			fwdMessage.setHeader("X-Mailer", "MyEmailSender");
			fwdMessage.setSentDate(new Date());

			Object content = message.getContent();

			if (content instanceof Multipart) {
				fwdMessage.setContent((Multipart)content);
			} else {
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				DataHandler dh = new DataHandler(message, "message/rfc822");
				messageBodyPart.setDataHandler(dh);
				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);
				fwdMessage.setContent(multipart);
			}

			Transport.send(fwdMessage);
			
			ret = true;
			
		} catch (Exception ex) {
			processException("Exception in IDCEmail.forward()", ex, "to=" + to + " / cc=" + cc + " / subject=" + subject + " / message=" + message);
		}
		
		debug("Ending forward() ...");
		
		**********************************/
    	return ret;
	    
	}
	
	/*******************************************************************************************************/
	
	public Message createMessage(String to, String cc, String from, String subject, String text) {
	    
		debug("Starting getMessage() ...");
		
		Properties props = System.getProperties();

		props.put("mail.smtp.host", smtpServer);
		
		Session session = Session.getDefaultInstance(props, null);

		Message ret = new MimeMessage(session);

		try {

			ret.setFrom(new InternetAddress(from));
			ret.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to, false));
			if (cc != null) {
				ret.setRecipients(Message.RecipientType.CC,InternetAddress.parse(cc, false));
			}	
			if (subject == null) {
				subject = "";
			}
			ret.setSubject(subject);
			ret.setHeader("X-Mailer", "MyEmailSender");
			ret.setSentDate(new Date());

			ret.setText(text);

			Transport.send(ret);
			
		} catch (Exception ex) {
			processException("Exception in IDCEmail.createMessage()", ex, "to=" + to + " / cc=" + cc + " / subject=" + subject + " / text=" + text);
		}
		
		debug("Ending getMessage() ...");
		
    	return ret;
	    
	}
	
	/*******************************************************************************************************/
	
	public boolean move(Message message, String folderName) {
		
		boolean ret = false;
		
		try { 
		
			Folder srcFolder = message.getFolder(); 
			Store store = srcFolder.getStore(); 
			Folder destFolder = store.getFolder(folderName); 
		
			if (destFolder != null && destFolder.exists()) { 
				destFolder.appendMessages(new Message[]{message}); 
				message.setFlag(Flags.Flag.DELETED,true); 
			}

			ret = true;
			
		} catch (Exception ex) {
			processException("Exception in IDCEmail.move()", ex, "message" + message + " / folderName=" + folderName);
		}
		
		return ret;
		
	}


	/*******************************************************************************************************/
	
	public void processMessage(Message message) {
	    
		String subject = "";

		try {

			String from = ((InternetAddress)message.getFrom()[0]).getPersonal();
			if (from == null) {
				from = ((InternetAddress)message.getFrom()[0]).getAddress();
			}
			
			subject = message.getSubject();

			if(preProcess(message, from, subject)) {
				
				Part messagePart = message;
				Object content = messagePart.getContent();

				if (content instanceof Multipart) {
					for(int nPart=0, maxPart = ((Multipart)content).getCount(); nPart < maxPart; nPart++) {
						processMessagePart(subject, ((Multipart)content).getBodyPart(nPart));
					}
				} else {
					processMessagePart(subject, messagePart);
				}

			}
			
			postProcess(message);

			debug("-----------------------------");
			
    	} catch (Exception ex) {
			processException("Exception in IDCEmail.processMessage()", ex, "subject: " + subject);
	    }
	    
	}

	/*******************************************************************************************************/
	
	public void processMessagePart(String subject, Part messagePart) {
	    
		try {

			String contentType=messagePart.getContentType();
			debug("CONTENT:"+contentType);

			String messageText = "";

			if (contentType.startsWith(TEXT_CONTENT) || contentType.startsWith(HTML_CONTENT)) {
				InputStream is = messagePart.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String lineText = reader.readLine();
				while (lineText != null) {
					messageText += lineText;
					lineText = reader.readLine();
				}
				
				processTextContent(subject, messageText);
				
			} else if (contentType.startsWith(PDF_CONTENT)) {
				
				String filename = pdfDir + "/" + messagePart.getFileName();
				File outputFile = new File(filename);
				FileOutputStream output = new FileOutputStream(outputFile);
				InputStream inputStream = messagePart.getInputStream();

				int i;
				while((i = inputStream.read()) != -1) {
					output.write(i);
				}
				output.close();

				processPDFContent(subject, filename);
				
			}
			
    	} catch (Exception ex) {
			processException("Exception in IDCEmail.processMessagePart()", ex, "subject: " + subject);
	    }
    	
	}

	/*******************************************************************************************************/
	
	public void debug(String s) {

		if(debug) {
			IDCUtils.debug(s);
		}

	}
	
	/*******************************************************************************************************/
	
	protected void processException(String string, Exception e, String details) {
		
		debug(string + " : " + details);
		if(e != null) {
			Writer writer = new StringWriter();
		    PrintWriter printWriter = new PrintWriter(writer);
		    e.printStackTrace(printWriter);
			debug(writer.toString());
		}

	}

	/*******************************************************************************************************/
	
	protected abstract boolean preProcess(Message message, String from, String subject);
	
	protected abstract void postProcess(Message message);

	protected abstract void processTextContent(String subject, String text);

	protected abstract void processPDFContent(String subject, String fileName);

}

