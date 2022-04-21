package com.indirectionsoftware.apps.movies;

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

public class MyMoviesApp {
	
	/*******************************************************************************************************/
	
	private static final String APP_NAME = "Movies";
	private static IDCDbManager dbManager;
	private static IDCApplication app;
	
	static final String[] TEST_QUESTIONS = {
			"give me a list of movies with Bradd Pitt",
	};
	
	static final String[] NOT_WORKING_QUESTIONS = {
	};
	
	static final String[] TEST_QUESTIONS_DONE = {
			"give me a list of movies by Quentin Tarantino",
	};
	
	static final String[] TEST_QUESTIONS_TODO = {
	};
	
	static final String[] TEST_QUESTIONS_ALL = {
			"give me a list of movies by John",
		};
		
	/*******************************************************************************************************/
	
	public static void main(String[] args) {
		
		IDCUtils.setMinDebugLevel(IDCUtils.DATABASE);      

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
		
		IDCLanguageEngine engine = new IDCLanguageEngine(app);
				
		for(String sentence : TEST_QUESTIONS) {
			IDCUtils.debugNLU("=================================================================================================================================");
			IDCUtils.debugNLU(sentence);
			engine.processSentence(sentence, null);
		}

	}

}