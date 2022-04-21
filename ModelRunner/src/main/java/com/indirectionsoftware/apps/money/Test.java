package com.indirectionsoftware.apps.money;

import java.lang.reflect.Method;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.utils.IDCUtils;

public class Test {

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException { 
  
    	for(int i=-99999; i< 1000; i++) {
        	String test = IDCUtils.getAmountString(i, false);
        	System.out.println("test: i=" + i + " / " + test);
    	}
        
    } 
    
    public static void mainBack(String[] args) throws ClassNotFoundException, NoSuchMethodException { 
    	  
        // returns the Class object for this class 
        Class myClass = Class.forName("com.indirectionsoftware.apps.money.MyMoneyApp"); 
        Class[] parameterTypes = {IDCApplication.class, IDCData.class, IDCData.class}; 
        String methodName = "createReturn"; 
        Method method = myClass.getMethod(methodName, parameterTypes);
        
    } 
    
}
