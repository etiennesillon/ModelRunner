package com.indirectionsoftware.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.messagemedia.messages.MessageMediaMessagesClient;
import com.messagemedia.messages.controllers.MessagesController;
import com.messagemedia.messages.controllers.RepliesController;
import com.messagemedia.messages.http.client.APICallBack;
import com.messagemedia.messages.http.client.HttpContext;
import com.messagemedia.messages.models.CheckRepliesResponse;
import com.messagemedia.messages.models.GetMessageStatusResponse;
import com.messagemedia.messages.models.Message;
import com.messagemedia.messages.models.Reply;
import com.messagemedia.messages.models.SendMessagesRequest;
import com.messagemedia.messages.models.SendMessagesResponse;
import com.sun.jna.platform.win32.WinUser.MSG;

public class IDCSMS {
	
    private static String MESSAGEMEDIAKEY = "slqpyIyWk9z92IXviL7k"; 
    private static String MESSAGEMEDIASECRET = "bARWd9srE3TV8aU4jhM9DzweXgolpt";
    
    private static MessageMediaMessagesClient client = new MessageMediaMessagesClient(MESSAGEMEDIAKEY, MESSAGEMEDIASECRET, false);
    
    private static final String MSGID = "2c7d3bca-34f8-4d51-9166-2759c6f16fc0";
    
	/**************************************************************************************************/
	

    public static void main( String[] args) {
//    	sendSMS("+61478407695", "Please reply again");
//    	getReplies();    	
//    	checkStatus("86d793e5-f12f-4d91-89e8-859afb09696b");
    }
    
	/**************************************************************************************************/
	
    public static void sendSMS(List<String> toDist, String text) {
    	
        for(String to : toDist) {
        	sendSMS(to, text);
        }
    	
    }
    
	/**************************************************************************************************/
	    
    public static void sendSMS(String destinationStr, String messageStr) {
    	
    	
		IDCUtils.debug("IDCSMSsendSMS(): destinationStr=" + destinationStr + " / messageStr=" + messageStr);

        MessagesController messages = client.getMessages();

        Message message = new Message();
        message.setContent(messageStr);
        message.setDestinationNumber(destinationStr);

        List<Message> messagesList = new ArrayList<>();
        messagesList.add(message);

        SendMessagesRequest body = new SendMessagesRequest();
        
        body.setMessages(messagesList);

        messages.sendMessagesAsync(body, new APICallBack<SendMessagesResponse>() {
        	
			public void onFailure(HttpContext arg0, Throwable arg1) {
               System.out.println("failure");
			}

			public void onSuccess(HttpContext arg0, SendMessagesResponse response) {
            	for(Message msg : response.getMessages()) {
            		UUID messageId = msg.getMessageId();
                	System.out.println("MessageId: " + messageId + " From: " + msg.getDestinationNumber() + " Content: " + msg.getContent());
            		if(messageId.equals(MSGID)) {
            			System.out.println("Found MessageId: " + messageId + " From: " + msg.getDestinationNumber() + " Content: " + msg.getContent());
            		}
            	}
                System.out.println("success");
			}
            
        });
        
    }
    
	/**************************************************************************************************/
    
    public static void checkStatus(String messageId) {
        
        MessagesController messages = client.getMessages();

        messages.getMessageStatusAsync(messageId, new APICallBack<GetMessageStatusResponse>() {
        	
            public void onSuccess(HttpContext context, GetMessageStatusResponse response) {
            	System.out.println("success");
            }
            public void onFailure(HttpContext context, Throwable error) {
            	System.out.println("failure");
            }
    	});
    
    }
        	
}