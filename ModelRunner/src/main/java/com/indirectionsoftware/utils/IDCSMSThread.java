package com.indirectionsoftware.utils;

import java.util.UUID;

import com.indirectionsoftware.metamodel.IDCApplication;
import com.messagemedia.messages.MessageMediaMessagesClient;
import com.messagemedia.messages.controllers.RepliesController;
import com.messagemedia.messages.http.client.APICallBack;
import com.messagemedia.messages.http.client.HttpContext;
import com.messagemedia.messages.models.CheckRepliesResponse;
import com.messagemedia.messages.models.Reply;

public class IDCSMSThread extends Thread {
	
    private static String MESSAGEMEDIAKEY = "slqpyIyWk9z92IXviL7k"; 
    private static String MESSAGEMEDIASECRET = "bARWd9srE3TV8aU4jhM9DzweXgolpt";
    
    private static MessageMediaMessagesClient client = new MessageMediaMessagesClient(MESSAGEMEDIAKEY, MESSAGEMEDIASECRET, false);
    
	/**************************************************************************************************/
	

    public static void main( String[] args) {
    	new IDCSMSThread().start();
    }

	/**************************************************************************************************/
	
	public void run(IDCApplication app) {
		
    	while(true) {
    		getReplies();
    		try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
    	}

	}
    
	/**************************************************************************************************/
    
    public static void getReplies() {
        
    	System.out.println("Checking Replies ...");

    	RepliesController replies = client.getReplies();

        replies.checkRepliesAsync(new APICallBack<CheckRepliesResponse>() {
        	
            public void onSuccess(HttpContext context, CheckRepliesResponse response) {
            	for(Reply reply : response.getReplies()) {
            		UUID messageId = reply.getMessageId();
            		UUID replyId = reply.getReplyId();
                	System.out.println("MessageId: " + messageId + " ReplyId: " + replyId + " From: " + reply.getSourceNumber() + " Content: " + reply.getContent());
            	}
            	
            }
            public void onFailure(HttpContext context, Throwable error) {
            	System.out.println("failure");
            }
            
    	});  
    
    }

}