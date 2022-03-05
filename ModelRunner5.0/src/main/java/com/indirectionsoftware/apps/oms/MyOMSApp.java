package com.indirectionsoftware.apps.oms;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.indirectionsoftware.backend.database.IDCDbManager;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.runtime.nlu.IDCAttributeValueDataList;
import com.indirectionsoftware.runtime.nlu.IDCLanguageEngine;
import com.indirectionsoftware.runtime.nlu.IDCLanguageQuestion;
import com.indirectionsoftware.utils.IDCUtils;

public class MyOMSApp {
	
	/*******************************************************************************************************/
	
	private static final String APP_NAME = "OMS";
	private static IDCDbManager dbManager;
	private static IDCApplication app;
	
	static final String[] TEST_QUESTIONS = {
			"could I please get the name and age for our customers living in Melbourne",
	};
	
	static final String[] NOT_WORKING_QUESTIONS = {
			"i need a list of name age and city for all our customers", // city is attr as well as type
	};
	
	static final String[] TEST_QUESTIONS_DONE = {
			"could I please get the name, age and city for our customers living in Brighton",
			"could I please get the name and age for our customers living in Melbourne",
			"please give me a list of our customers living in Melbourne",
			"please give me a list of our customers name and age and city",
			"could you please get me a list of all our large products with a red colour and a duration of 5",
			"could you please get me a list of all our large products with a red colour",
			"could you please get me a list of all our large gadgets with colour red",
			"could you please get me a list of names colours types and sizes of all our products",
			"could you please get me a list of all our large red or blue gadgets",
			"could you please get me a list of all our small red or blue products",
			"Could I please get a list of all our cities",
			"Give me a list of all our products",
			"Give me a list of all our customers",
	};
	
	static final String[] TEST_QUESTIONS_TODO = {
			"could I please get the name and price for our 10 most expensive products",
			"could I please get a list of our top 10 product",
	};
	
	static final String[] TEST_QUESTIONS_ALL = {
			"could you please get me the name colour type and size of all our products",
			"could you please get me a list of all our small red or blue products which cost more than 100 dollars thanks",
			"Could I please get a list of all our cities",
			"Give me a list of all our products",
			"Give me a list of all our customers",
			"Give me a list of our product names",
			"Give me a list of our products name and price",
			"could you please get me a list of all the big red and blue city bought by people in brighton we sell thanks",
			"Give me a list of all our product's names and duration",
			"Give me a list of all our product's names, duration and price",
			"could you please tell me what is the age of Peter",
			"could you please tell me how old is Peter",
			"i need to know what is the address of peter",
			"what's peter's address",
			"could you please tell me where does peter live",
			"Give me a list of all our customers called peter age 45",
			"Give me a list of products which cost 100 dolars",
			"I want a list of people's names",
			"I need everybody's date of birth",
			"Could you please tell me where etienne was born?",
			"Where was Etienne born?",
			"Where was Mr Sillon born?",
			"Where was Mr Etienne Sillon born?",
			"When is Etienne’s birthday?",
			"Can you please get me  Etienne’s birthday thanks",
			"get me  Etienne’s birthday?",
			"Who was born in 1968?",
			"I want a list of everybody Who was born in January 1968?",
			"Who was born in Talence in January 1968?",
			"Who was born in Talence?",
			"Who lives in Brighton East?",
			"How many people live in Brighton East?",
			"How many old people live in Brighton East?",
			"I want a list of everybody living in Brighton East",
			"I want a list of every adult living in Brighton East",
			"Give me a list of people having the same birthday living in Brighton East and born in Talence",
			"Give me a list of people born in 1968?",
			"Could I please have a list of people born in 1968?",
			"Give me a list of adults in Brighton East",
			"Give me a list of children born in 1991",
			"Give me a list of children who were born in Sandringham in 1991",
			"could you please get me a list of all our best selling blue and red products thanks",
			"I need to see a list of all our customers",
			"Please get me a list of our customers",
			"Please get me our customer's list",
			"Could you please get me a list of our customers in Melbourne",
			"Get me the list of our customers in Melbourne and Sydney",
			"Could you please show me a list of our customers living in Melbourne and Sydney",
			"Give me a list of all our customers called Peter aged 45",
			"Can I please have a list of customers who's name is Peter and who are 45 years old",
			"I need the name and address of all our customers called Peter and who are 45 years old",
			"Get me a list of customers called Peter, who are 45 years old and live in Brighton",
			"Give me a list of products which cost 100 dolars",
			"I want a list of people's names",
			"I need everybody's date of birth",
			"Could you please tell me where etienne was born?",
			"Where was Etienne born?",
			"Where was Mr Sillon born?",
			"Where was Mr Etienne Sillon born?",
			"When is Etienne’s birthday?",
			"Can you please get me  Etienne’s birthday thanks",
			"get me  Etienne’s birthday?",
			"Who was born in 1968?",
			"I want a list of everybody Who was born in January 1968?",
			"Who was born in Talence in January 1968?",
			"Who was born in Talence?",
			"Who lives in Brighton East?",
			"How many people live in Brighton East?",
			"How many old people live in Brighton East?",
			"I want a list of everybody living in Brighton East",
			"I want a list of every adult living in Brighton East",
			"Give me a list of people having the same birthday living in Brighton East and born in Talence",
			"Give me a list of people born in 1968?",
			"Could I please have a list of people born in 1968?",
			"Give me a list of adults in Brighton East",
			"Give me a list of children born in 1991",
			"Give me a list of children who were born in Sandringham in 1991",
		};
		
	/*******************************************************************************************************/
	
	public static void main(String[] args) {
		
		if (args.length != 1) {
            System.err.println("Invalid arguments ...");
        } else {
    		
    		dbManager = IDCDbManager.getIDCDbManager(args[0], true);
    		if(dbManager != null) {
    			
    			app = dbManager.getApplication(APP_NAME);
    			if(app  != null) {
    				app.getOntology().loadLexicon(new File("OMSLexicon.txt"));
    				test(app);
    			} else {
    	            System.err.println("Can't get app");
    			}	
    			
    			dbManager.disconnect();

    		} else {
                System.err.println("Can't get DbManager");
    		}
    		
        }

	}
	
	/*******************************************************************************************************/
	
	public static void test(IDCApplication app) {
		
		IDCUtils.setMinDebugLevel(IDCUtils.DEBUG_NLU);
//		IDCUtils.debug("=================================================================================================================================");
//		IDCUtils.debug(app.getOntology().toString());
		
		IDCLanguageEngine engine = new IDCLanguageEngine(app, null);
				
		for(String sentence : TEST_QUESTIONS) {
			IDCUtils.debugNLU("=================================================================================================================================");
			IDCUtils.debugNLU(sentence);
			engine.processSentence(sentence, null);
//			engine.getQueryChunks(sentence);
		}

	}

	/*******************************************************************************************************/
	
	public static void test1(IDCApplication app) {
		
		IDCLanguageQuestion quest = new IDCLanguageQuestion();
		
		quest.type = app.getType("Customer");
		quest.selectionValues.add(new IDCAttributeValueDataList(quest.type.getAttribute("Name"), "John"));
		quest.requestedAttrs.add(quest.type.getAttribute("Age"));
		quest.answer();
				
	}

	/*******************************************************************************************************/
	
	public static void test2(IDCApplication app) {
		
		
		try {
			
			IDCLanguageEngine proc = new IDCLanguageEngine(app, null);

			boolean looping = true;
			while(looping) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		        String query = reader.readLine();
		        if(query.equalsIgnoreCase("quit") ) {
		        	looping = false;
		        } else {
					proc.processSentence(query, null);
		        }
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
 
	}
	
	/*******************************************************************************************************/
	
	public static void test3(IDCApplication app) {
		
		IDCLanguageEngine engine = new IDCLanguageEngine(app, null);
				
		for(String sentence : TEST_QUESTIONS) {
			IDCUtils.debug("=================================================================================================================================");
			IDCUtils.debug(sentence);
			engine.processSentence(sentence, null);
		}

	}

	/*******************************************************************************************************/
	
	public static void dumpWords() {
		
		Map<String, String> words = new HashMap<String, String>();
		
		for(String s : TEST_QUESTIONS_ALL) {
			s = s.trim().toLowerCase();
			if(s.endsWith("?")) {
				s = s.substring(0, s.length()-1);
			}
			for(String word : s.split(" ")) {
				words.put(word,"");
			}
		}
		
		for(String word : words.keySet()) {
			IDCUtils.debug(word);
		}
		
	}

}