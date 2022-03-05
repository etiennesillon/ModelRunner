package com.indirectionsoftware.runtime;

import com.indirectionsoftware.runtime.nlu.IDCWord;

public class Test {
	
	/**********************************************************************************/

	static String s = "what is the average age of our customers in brighton";
	
	public static void main(String[] args) {
		
		for(String w : s.split(" ")) {
			System.out.println("Word = " + w);
			
			IDCWord word = new IDCWord(w);
		}
		
	}
	
}
