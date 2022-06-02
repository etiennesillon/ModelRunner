package com.indirectionsoftware.runtime;

import java.util.List;

import com.indirectionsoftware.utils.IDCUtils;

public class Test {
	
	/**********************************************************************************/

	static String filename = "/Users/etiennesillon/MyStuff/code/Projects/ModelRunner/projects/ModelRunner/src/main/webapp/TEMP.html";
	
	public static void main(String[] args) {
		
		String ret = "";
		
		String line = "pauletiennesillon@yahoo.com";
		
		for(int i=0; i < line.length(); i++) {
			char c = line.charAt(i);
			if(c != '.' && c != '@') {
				ret += c;
			}
			
		}
		
		System.out.println("ret = " + ret);
		
	}
	
}
