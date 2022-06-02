package com.indirectionsoftware.utils;

public abstract class IDCEmailProcessor {

	public abstract boolean processEmail(String from, String subject, String email);
	
	public void processAttachment() {}
	

}

