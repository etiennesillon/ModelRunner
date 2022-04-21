package com.indirectionsoftware.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.runtime.IDCExpression;
import com.indirectionsoftware.runtime.IDCFormulaContext;
	
public class IDCUtils {
	
	static String EOL = "\n"; 
	
	static final String[] IFOPERATORS = {"=", "!=", "GT", "LT", "GE", "LE", "AND", "OR"};
	public static final int EQUALS=0, NOTEQUALS=1, GREATER=2, LESS=3, GREATEREQUAL=4, LESSEQUAL=5, AND=6, OR=7;
	
	public static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	
	public static final int COL_WIDTH = 15;
	
	public static final String SEPARATOR = " , ";
	
	public static boolean DEMO_MODE = false;
	
	private static final String[] DEBUG_LEVELS = { "TRACE", "DEBUG", "DATABASE", "NLU", "INFO", "ERROR"};
	public static final int TRACE=0, DEBUG=1, DATABASE=2, DEBUG_NLU=3, INFO=4, ERROR=5, TEMP=6;
	
	private static int debugLevel=-1, minDebugLevel=TRACE;
	
	private static PrintWriter dblog = null; 
	private static String dblogFileName = null;

	static final int LINEWIDTH=100;
	
	/*******************************************************************************************************/
	
	public static void setDebugLevel(int level) {
		debugLevel = level;
	}

	/*******************************************************************************************************/
	
	public static void setMinDebugLevel(int level) {
		minDebugLevel = level;
	}

	/*******************************************************************************************************/
	
	public static void setDebugLevel(String level) {
		debugLevel = getDebugLevel(level);
	}

	/*******************************************************************************************************/
	
	public static void setMinDebugLevel(String level) {
		minDebugLevel = getDebugLevel(level);
	}

	/*******************************************************************************************************/
	
	public static int getDebugLevel(String level) {
		
		int ret = -1;
		
		int nLevel=0;
		for(String debugLevel : DEBUG_LEVELS) {
			if(debugLevel.equalsIgnoreCase(level)) {
				ret = nLevel;
				break;
			}
			nLevel++;
		}
		
		return ret;
		
	}

	/*******************************************************************************************************/
	
	public static void setDemoMode(boolean demoMode) {
		DEMO_MODE = demoMode;
	}

	/************************************************************************************************/
    // STRINGS ...
    /************************************************************************************************/

    public static String makeFixedLength(String s, int len, boolean isSuffix) { 
    	
    	String ret = "";
    	
    	int i=0;
    	while(i<s.length() && i<len) {
    		ret+= s.charAt(i++);
    	}
    	
    	ret = ret.trim();
    	
    	i = ret.length();
    	
    	while(i++<len) {
        	if(isSuffix) {
        		ret+=" ";
        	} else {
        		ret = " " + ret;
        	}
    	}
    	
    	return ret;
    	
    }

	/*******************************************************************************************************/
	
    public static String makeFixedLength(String s, int len) { 
    	return makeFixedLength(s, len, true);
    }
    	
    /************************************************************************************************/

    public static String convert2SQL(String s) { 
    	
    	String ret = "";
    	
    	for(int nChar=0, maxChars=s.length(); nChar < maxChars; nChar++) {
    		char c = s.charAt(nChar);
    		if(c != '\'') {
    			ret += c;
    		} else {
    			ret += "''";
    		}
    	}
    	
    	return ret.trim();
    	
    }
    	
    /************************************************************************************************/
    
    public static boolean translateBoolean(String s) {
    	return translateBoolean(s, true);
    }
    
    public static boolean translateBoolean(String s, boolean defValue) {
    	
    	boolean ret = defValue;
    	
    	if(s != null && s.length() > 0) {
        	if(s.equalsIgnoreCase("true") || s.equals("1")) {
        		ret = true;
        	} else if(s.equalsIgnoreCase("false") || s.equals("0")) {
        		ret = false;
        	} 
    	} 
    	
    	return ret;
    }
    
    /************************************************************************************************/

    public static int translateInteger(String s) {
    	
    	int ret = -1;
    	
    	try {
    		ret = Integer.parseInt(s);
    	} catch(Exception ex) {}
    	
    	return ret;
    	
    }
    
    /************************************************************************************************/

    public static long translateLong(String s) {
    	
    	long ret = -1;
    	
    	try {
    		ret = Long.parseLong(s);
    	} catch(Exception ex) {}
    	
    	return ret;
    	
    }
    
    /************************************************************************************************/

	public static String getPlural(String singular) {

		String ret = singular;
		
		String upper = singular.toUpperCase();
		boolean isUpper = singular.equals(upper);
		
		int len = singular.length();
		
		if(upper.charAt(len-1) == 'Y' && !isVowel(singular.charAt(len-2))) {
			ret = singular.substring(0, len-1) + (isUpper ? "IES" : "ies");
		} else if(upper.charAt(len-1) == 'S' || upper.charAt(len-1) == 'Z' || upper.charAt(len-1) == 'X' || upper.endsWith("CH") || upper.endsWith("SH")) {
			ret += (isUpper ? "ES" : "es");
		} else if(!upper.equals("ROOF") && upper.charAt(len-1) == 'F' || upper.endsWith("FE")) {
			ret = singular.substring(0, len-1) + (isUpper ? "VES" : "ves");
		} else {
			ret += (isUpper ? "S" : "s");
		}

		
		return ret;
	}
	
    /************************************************************************************************/

	private static boolean isUpperCase(String s) {
		return s.equals(s.toUpperCase());
	}

    /************************************************************************************************/

	public static boolean isVowel(char c) {
		
		boolean ret = false;
		
		switch(Character.toLowerCase(c)) {
		
			case 'a':
			case 'e':
			case 'i':
			case 'o':
			case 'u':
				ret = true;
				break;
		}
		
		return ret;
		
	}

	/*******************************************************************************************************/
	
	public static String getAmountString(long amount) {
		return getAmountString(amount, false);
	}
		
	/*******************************************************************************************************/
	
	public static String getAmountStringOLD(long amount, boolean isUpdate) {
		
		String ret = "";

		if(DEMO_MODE) {
			amount = 999999;
		}
		
		String amountStr = "                             " + amount;
		amountStr = amountStr.substring(amountStr.length() - COL_WIDTH +1);
		
		int pointIndx = amountStr.length() -2;

		boolean started = false;
		int i=0;
		for(char c : amountStr.toCharArray()) {
			if(i == pointIndx) {
				if(!started) {
					ret += "0.";
					ret = ret.substring(1);
					started = true;
				} else {
					ret += ".";
				}
			}
			if(c == '0') {
				if(started) {
					ret += c;
				}
			} else if(c == ' ') {
				if(started) {
					ret += '0';
				} else {
					ret += c;
				}
			} else {
				ret += c;
				started = true;
			}
			i++;
		}
		
		if(isUpdate) {
			ret = ret.trim();
		}
		
		return ret;
		
	}
	
	/*******************************************************************************************************/
	
	public static String getAmountString(long amount, boolean isUpdate) {
		
		String ret = "";

		if(DEMO_MODE) {
			amount = 999999;
		}
		
		boolean isNeg = false;
		
		if(amount < 0) {
			isNeg = true;
			amount *= -1;
		}
		
		if(amount < 10) {
			ret = "0.0" + amount;
		} else if(amount < 100) {
			ret = "0." + amount;
		} else {
			ret = "" + amount;
			int len = ret.length();
			ret = ret.substring(0, len-2) + "." + ret.substring(len-2);  
		}
		
		if(!isUpdate) {
			ret = "$" + ret;
		}

		if(isNeg) {
			ret = "-" + ret;
		}
		
		if(!isUpdate) {
			String amountStr = "                             " + ret;
			ret = amountStr.substring(amountStr.length() - COL_WIDTH +1);
		}
		
		return ret;
		
	}
	
	/*******************************************************************************************************/
	
	public static long parseAmountString(String amountStr) {
		
		long ret = 0;

		debug(amountStr);

		boolean isNegative = false;
		boolean foundDecimalPoint = false;
		
		for(char c : amountStr.toCharArray()) {

			switch(c) {
			
				case '-':
					isNegative = true;
					break;

				case '.':
					foundDecimalPoint = true;
					break;

				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					int val = c - '0';
					ret = ret * 10 + val;
					break;

				default:
					break;

			}
			
		}

		if(isNegative) {
			ret *= -1;
		}
		
		if(!foundDecimalPoint) {
			ret *= 100;
		}
		
		return ret;

	}
	
	/*******************************************************************************************************/
	
	public static String getSeparator() {
		return SEPARATOR;
	}
    	
    /************************************************************************************************/

	static final int DECS = 2;
	
	public static long getAmount(String s) {
		
		long ret = 0;
		
		if(s.length() > 0) {
			
			boolean foundDec = false;
			int decs = DECS;
			for(char c : s.toCharArray()) {
				if(c >= '0' && c <= '9') {
					if(!foundDec || decs > 0) {
						ret = ret * 10 + c - '0';
					}
				} else if(c == '.') {
					foundDec = true;
					decs = DECS;
				}
			}
			
			if(s.charAt(0) == '-') {
				ret *= -1;
			}
			
		}
		
		return ret;
		
	}

	/*******************************************************************************************************/
	
	static final String[][] SINGPLUR_SUFX = {
    	{"ss", "ss"},
    	{"us", "us"},
    	{"ies", "y"},
    	{"s", ""}
    };

    static final String[][] SINGPLUR_EXP = {
    	{"children", "child"},
    	{"matrices", "matrix"},
    };

	/*******************************************************************************************************/
	
	public static String getSingular(String s) {
		
		String ret = s;

		boolean found = false;
		
		for(String[] exps : SINGPLUR_EXP) {
			if(ret.equalsIgnoreCase(exps[0])) {
				ret = exps[1];
				found = true;
				break;
			}
		}
		
		if(!found) {
			
			for(String[] sufs : SINGPLUR_SUFX) {
				if(ret.endsWith(sufs[0])) {
					int i = ret.lastIndexOf(sufs[0]);
					ret = ret.substring(0,  i) + sufs[1];
					break;
				}
			}

		}
		
		return ret;
				
	}
	
	/***************************************************************/

	public static void testSingular() {
		
		System.out.println("Singular of children is " + getSingular("children"));
		System.out.println("Singular of properties is " + getSingular("properties"));
		System.out.println("Singular of databases is " + getSingular("databases"));
		System.out.println("Singular of computers is " + getSingular("computers"));
		
		
	}
	
	/***************************************************************/
	
	public static int getMin(int[] ints) {
		
		int ret = 999999999;
		
		for(int i : ints) {
			ret = getMin(ret, i);
		}
		
		return ret;
		
	}
	
	/***************************************************************/
	
	public static int getMin(int i1, int i2) {
			return i1 < i2 ? i1 : i2;
	}
	

    /************************************************************************************************/

    private static void replace(String inFileName, String outFileName) {

    	int FLAG[] = {80, 104,97, 114, 50};
    	
    	try {
    		
        	FileReader in = new FileReader(new File(inFileName));
        	FileWriter out = new FileWriter(new File(outFileName));
        	
        	int c, nFlag=0;
            while ((c = in.read()) != -1) {
                //IDCUtils.debug(" c = " + c + " / nFlag = " + nFlag);   
            	if(nFlag == 5) {
            		if(c == 58) {
                        //IDCUtils.debug(" found it!");
            			c = 95;
            			nFlag=0;
            		}
            	} else if(c == FLAG[nFlag]) {
            		nFlag++;
        		} else {
        			nFlag = 0;
            	}
                out.write(c);
            }
            
            in.close();
            out.close();
        		

    	} catch(Exception e) {
    	}
    	
		System.exit(0);
	}

    /***************************************************************/
	/** String replace method ...                                 **/
	/** String.replaceAll() is a pain to use and doesn't work     **/
	/** when the replacement string includes a $ !                **/
	/***************************************************************/
	
   	public static String replaceAll(String val, String key, String rep) {
   		
   		String ret = val;
   		
   		int ind;
   		while((ind = ret.indexOf(key)) != -1) {
   			ret = ret.substring(0,ind) + rep + ret.substring(ind+key.length());
   		}
   		
   		return ret;
   		
   	}
	  
  	/***************************************************************/
		  	
    public static String getString(String s) {
	    	 
    	return (s == null ? "" : s);
	    	 
    }
    
  	/***************************************************************/
	
    public static String getPrefix(String name) {
    	
    	String ret = name;
    	
    	int ind = name.indexOf('.');
    	if(ind != -1) {
        	ret = name.substring(0, ind);
    	}
    	
    	//IDCUtils.debug("getPrefix: name = " + name + " / ret = " + ret);
    	
    	return ret;
    	
    }
     
  	/***************************************************************/
	
    public static String getSuffix(String name) {
    	
    	String ret = name;
    	
    	int ind = name.indexOf('.');
    	if(ind != -1) {
        	ret = name.substring(ind+1, name.length());
    	}
    	
    	//IDCUtils.debug("getSuffix: name = " + name + " / ret = " + ret);
    	
    	return ret;
    	
    }
    
	/*****************************************************************************/

	public static String getFirstChunk(String s, char c) {
		
		String ret = "";
		
		if(s != null && s.length() > 0) {
			int i = s.indexOf(c);
			if(i != -1) {
				ret = s.substring(0,i);
			}
		}
		
		return ret;
		
	}

	/*****************************************************************************/

	public static String getNextChunk(String s, char c1, char c2, int i) {
		
		String ret = "";
		
		if(s != null && s.length() > 0) {
			int i0 = s.indexOf(c1, i);
			if(i0 != -1) {
				int i1 = s.indexOf(c2, i0+1);
				if(i1 != -1) {
					ret = s.substring(i0 +1, i1);
				}
			}
		}
		
		return ret;
		
	}
	
    /************************************************************************************************/

	public static List <String> splitLine(String expr, char separator) {
		
		List <String> ret = new ArrayList<String> ();
		
		String s = "";
		int openParenthesis=0;
		int openBrackets=0;
		boolean openQuote=false;
		for(int nChar=0, maxChar=expr.length(); nChar < maxChar; nChar++) {
			
			char c = expr.charAt(nChar);
			
			switch(c) {
			
				case '(':
					openParenthesis++;
					s+=c;
					break;
			
				case ')':
					openParenthesis--;
					s+=c;
					break;
			
				case '{':
					openBrackets++;
					s+=c;
					break;
			
				case '}':
					openBrackets--;
					s+=c;
					break;
			
				case '\'':
					openQuote = !openQuote;
					s+=c;
					break;

				default: 
					if(c == separator) {
						if(openQuote || openParenthesis > 0 || openBrackets>0) {
							s+=c;
						} else {
							if(s.length() > 0 || separator == ',') {
								ret.add(s.trim());
								s = "";
							}
						}
					} else {
						s+=c;
					}
					break;
			
			}
			
		}
		
		if(s.length() > 0 || separator == ',') {
			ret.add(s.trim());
			s = "";
		}
		
		return ret;
		
	}
	
    /************************************************************************************************/

	public static String parseQuotedString(String s) {
		return s.substring(1,s.length()-1);
	}
	
	
    /************************************************************************************************/

	public static String capitalise(String str) {
		
		   String ret = "";

		   String[] words = str.split(" ");
		   for (String word : words) {
		      if(ret.length() > 0) {
		         ret += " ";
		      }
		      
		      ret += word.substring(0, 1).toUpperCase() + word.substring(1);

		   }

		   return ret;

	}
	
	/************************************************************************************************/
    // SQL ...
    /************************************************************************************************/

    public static List<String> parseSQLExpression(String s) {
    	
    	List<String> ret = new ArrayList<String>();
    	
    	String word = "";
    	
    	boolean foundWord=false;
    	int openPar=0;
    	boolean openBrack=false;
    	for(int nChar=0, maxChar= s.length(); nChar < maxChar; nChar++) {
    		
			char c = s.charAt(nChar);
			
			switch(c) {
			
				case '(':
					openPar++;
					word += c;
					break;
					
				case ')':
					openPar--;
					word += c;
					if(openPar == 0 && foundWord) {
						ret.add(word);
						foundWord = false;
						word = "";
					}
					break;
			
				case '\'':
					if(openPar == 0) {
						if(openBrack) {
							word += c;
							openBrack=false;
							ret.add(word);
							foundWord = false;
							word = "";
						} else {
							openBrack = true;
							if(foundWord) {
								ret.add(word);
								foundWord = false;
								word = "";
							}
							word += c;
						}
					} else {
						word += c;
					}
					break;
					
				case ' ':
				case ',':
				case '\t':
				
					if(openPar ==0 && !openBrack) {
						if(foundWord) {
							ret.add(word);
							foundWord = false;
							word = "";
						}
					} else {
						word += c;
					}
					break;

					
				case '+':
				case '-':
				case '*':
				case '/':
				
					if(openPar ==0 && !openBrack) {
						if(foundWord) {
							ret.add(word);
							foundWord = false;
							word = "" + c;
							ret.add(word);
							word = "";
						}
					} else {
						word += c;
					}
					break;

				case '|':
					
					if(openPar ==0) {
						if(foundWord) {
							char c2 = s.charAt(nChar+1);
							if(c2 == '|') {
								ret.add(word);
								foundWord = false;
								word = "||";
								ret.add(word);
								word = "";
								nChar++;
							}
						}
					} else {
						word += c;
					}
					break;

				default:
					foundWord=true;
					word += c;
					break;
						
			}

    	}
    	
    	if(word.length() > 0) {
			ret.add(word);
    	}
    	
    	//IDCUtils.debug("openPar = " + openPar);
    	
    	return ret;
    	
    }
    
	/************************************************************************************************/
    // COMPARE OBJECTS ...
    /************************************************************************************************/

	public static int compare(Object op1, Object op2, int oper) {
    	
    	int ret=-2;
    	
		if(op1 == null) {

			if(op2 == null) {
				ret=0;
			} else {
				ret=-1;
			}
			
		} else if(op2 == null) {

			ret=1;
		
		} else {
			
			String val1Str = ""+op1;
			String val2Str = ""+op2;
			
			boolean bothNumbers=true;
			double val1Num=0;
			double val2Num=0;
			
			try {
				val1Num = Double.parseDouble(val1Str);
				val2Num = Double.parseDouble(val2Str);
			} catch(Exception ex) {
				bothNumbers=false;
			}
			
			if(bothNumbers) {
				
				if((val1Num == val2Num)) {
					ret=0;
				} else if((val1Num > val2Num)) {
					ret=1;
				} else {
					ret = -1;
				}

			} else {
				
				if(oper == IDCExpression.EQUALS_IGNORE_CASE) {
					val1Str = val1Str.toLowerCase();
					val2Str = val2Str.toLowerCase();
					ret = val1Str.compareTo(val2Str);
				} else if(oper == IDCExpression.EQUALS_IGNORE_CASE) {
					val1Str = val1Str.toLowerCase();
					val2Str = val2Str.toLowerCase();
					ret = (val1Str.startsWith(val2Str) ? 0 : -1);
				} else {
					ret = val1Str.compareTo(val2Str);
				}

			
			}

		}
		
    	return ret;
    	
    }

	/************************************************************************************************/

	public static int compare(Object op1, Object op2) {
		return compare(op1, op2, -1);
	}
    	
	/************************************************************************************************/
    // FILES ...
    /************************************************************************************************/

    public static Properties loadProperties(String fn) throws IOException {

   	  Properties ret = new Properties();	
    	
	  ret.load(new FileInputStream(new File(fn)));
	  
	  return ret;
   		
    }
    
    public static void listProperties() {
        System.getProperties().list(System.out);
    }

    /*******************************************************************************************************/
	
	public static String readFile(String fn) {
		
		String ret = "";
		
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(fn));
			String line = in.readLine();
			while(line != null) {
				ret += line;
				line = in.readLine();
			}
			in.close();
		} catch (Exception e) {
			ret = null;
		}
		
		return ret;
		
	}
	
	/*******************************************************************************************************/
	
	public static List<String> readFileLines(String fn) {
		
		List<String> ret = new ArrayList<String>();
		
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(fn));
			String line = in.readLine();
			while(line != null) {
				ret.add(line);
				line = in.readLine();
			}
			in.close();
		} catch (Exception e) {
			ret = null;
		}
		
		return ret;
		
	}
	
	/**********************************************************************************/

	public static void compareDirectories(String fromDir, String toDir) {

		System.out.println("Processing " + fromDir + " vs " + toDir);

		File fromDirFile = new File (fromDir); 
		File toDirFile = new File (toDir);
		
		String[] fromDirArray = fromDirFile.list();
		String[] toDirArray = toDirFile.list();
		
		if(fromDirArray == null || toDirArray == null) {
			System.out.println("Invalid directory names");
		} else {

			List<String> fromDirList = getList(fromDirArray);
			List<String> toDirList = getList(toDirArray);
			
			for(String fromFileName : fromDirList) {
			
				int toInd = toDirList.indexOf(fromFileName);
				if(toInd != -1) {
					toDirList.remove(fromFileName);
					File fromFile = new File(fromDir + "/" + fromFileName);
					File toFile = new File(toDir + "/" + fromFileName);
					if(fromFile.isDirectory() && toFile.isDirectory()) {
						compareDirectories(fromDir + "/" + fromFileName, toDir + "/" + fromFileName);
					} else {
						String difs = "";
						if(fromFile.length() != toFile.length()) {
							difs += " LENGTH DIF!";
						}
						if(fromFile.lastModified() != toFile.lastModified()) {
							difs += " DATE DIF!";
						}
						if(difs.length() > 0) {
							System.out.println(">>> FOUND DIF " +  fromFileName + difs);
							compareFiles(fromDir + "/" + fromFileName, toDir + "/" + fromFileName);
						}

					}
				} else {
					System.out.println(">>> MISSING " + fromFileName);
				}
			
			}
		}
		
	}

	/**********************************************************************************/

	static List<String> getList(String[] list) {
		
		List<String> ret = new ArrayList<String>();
		
		for(String s : list) {
			ret.add(s);
		}

		return ret;
	
	}
	
	/*******************************************************************************************************/

	public static void compareFiles(String fileName1, String fileName2) {

		File file1 = new File(fileName1);
		Set<String> lines1 = new TreeSet<String>();
		lines1.addAll(readFile(file1));
		
		File file2 = new File(fileName2);
		Set<String> lines2 = new TreeSet<String>();
		lines2.addAll(readFile(file2));
		
		Iterator<String> it1 = lines1.iterator();
		Iterator<String> it2 = lines2.iterator();
		
		List<String> outlines = new ArrayList<String>();
		
		String line1 = it1.next();
		String line2 = it2.next();
		
		boolean foundDiffs = false;
		
		while(line1 != null && line2 != null) {
			
			if(line1.equalsIgnoreCase(line2)) {
				outlines.add("(1) " + makeFixedLength(line1, LINEWIDTH) + " == (2) " + makeFixedLength(line2, LINEWIDTH));
				line1 = advanceStringIteratorSafely(it1);
				line2 = advanceStringIteratorSafely(it2);
			} else {
				foundDiffs = true;
				//System.out.println(">>>(1) *" + line1 + "*");
				//System.out.println(">>>(2) *" + line2 + "*\n");
				if(line1.compareTo(line2) < 0) {
					outlines.add("(1) " + makeFixedLength(line1, LINEWIDTH) + " << (2) " + makeFixedLength(line2, LINEWIDTH));
					line1 = advanceStringIteratorSafely(it1);
				} else {
					outlines.add("(1) " + makeFixedLength(line1, LINEWIDTH) + " >> (2) " + makeFixedLength(line2, LINEWIDTH));
					line2 = advanceStringIteratorSafely(it2);
				}
			}
			
		}
		
		if(foundDiffs) {
			for(String line : outlines) {
				System.out.println(line);
			}
		}
	
	}
	
	/*******************************************************************************************************/
	
	public static String advanceStringIteratorSafely(Iterator<String> it) {
		
		String ret = null;
		
		try {
			ret = it.next();
			
		} catch(Exception ex) {
		}
		
		return ret;
		
		
	}
	
	/*******************************************************************************************************/
	
	public static List<String> readFile(File file) {
		
		List<String> ret = new ArrayList<String>();
		
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(file));
			String line = in.readLine();
			while(line != null) {
				ret.add(line);
				line = in.readLine();
			}
			in.close();
		} catch (Exception e) {
			ret = null;
		}
		
		return ret;
		
	}
	
	/*******************************************************************************************************/
	
	public static void writeFile(String fn, String text) {
		
		try {
			PrintWriter out = new PrintWriter(new File(fn));
			out.print(text);
			out.close();
		} catch (Exception e) {
		}
		
	}
	
	/*******************************************************************************************************/
	
	public static String getDirName(String dirName) {
		
		String ret = dirName;
		
		String fileSeparator = System.getProperty("file.separator");
		if(!ret.endsWith(fileSeparator)) {
			ret += fileSeparator;
		}

		File dir = new File(dirName);
		if(!dir.exists()) {
			ret = null;
		}
		
		return ret;
		
	}
	  
	/*******************************************************************************************************/
	// CSV
	/*******************************************************************************************************/
	
	public static List<List<String>> parseCSVString(String csv) {
		
		List<List<String>> ret = null;
		
		try {
			ret = parseCSV(new StringReader(csv));
		} catch (Exception e) {
			ret = null;
		}
		
		return ret;

	}
	
	/*******************************************************************************************************/
	
	public static List<String> parseCSVLine(String line) {

		//Utils.debug(line);

		List<String> ret = new ArrayList<String>();
					
		boolean openQuote = false;
		String word = "";
		boolean newWord = false;
		for(char c : line.toCharArray()) {
			switch(c) {
			
				case '"':
					if(openQuote) {
						openQuote = false;
					} else {
						openQuote = true;
					}
					break;

				case ',':
					if(!openQuote) {
						ret.add(word.trim());
						word = "";
						newWord = true;
					} else {
						word += c;
					}
					break;

				default:
					word += c;
					newWord = false;
					break;

			}
			
		}

		if(word.length() > 0 || newWord) {
			ret.add(word.trim());
		}
		
		//for(String w : ret) {
		//	Utils.debug(w);
		//}
		
		return ret;

	}
	
	/*******************************************************************************************************/
	
	public static List<List<String>> parseCSVFile(File file) {
		
		List<List<String>> ret = null;
		
		try {
			ret = parseCSV(new FileReader(file));
		} catch (Exception e) {
			ret = null;
		}
		
		return ret;

	}
	
	/*******************************************************************************************************/
	
	public static List<List<String>> parseCSV(Reader reader) {
		
		List<List<String>> ret = new ArrayList<List<String>>();
		
		BufferedReader in;
		
		try {
			in = new BufferedReader(reader);
			String line = in.readLine();
			while(line != null) {
				ret.add(parseCSVLine(line));
				line = in.readLine();
			}
			in.close();
		} catch (Exception e) {
			ret = null;
		}
		
		return ret;
		
	}
	
    /************************************************************************************************/
  	
    public static String convert2CSV(String s) {
  	  
    	String ret = "";
    	
    	for(int i=0; i<s.length(); i++) {
    		char c = s.charAt(i);
    		if(c == '\n') {
    			ret += '|';
    		} else if(c == ','){
    			ret += ";";
    		} else {
    			ret += c;
    		}
    	}
    	  
    	return ret;

    }

	/************************************************************************************************/
    // JSON ...
    /************************************************************************************************/

    public static void writeJSONHeader(PrintWriter out) {
    	writeJSONHeader(out,true);
    }

	/************************************************************************************************/

    public static void writeJSONTrailer(PrintWriter out) {
    	writeJSONTrailer(out,true);
    }

	/************************************************************************************************/

    public static void writeJSONHeader(PrintWriter out, boolean isItemList) {
    	out.println("{");
    }

	/************************************************************************************************/

    public static void writeJSONTrailer(PrintWriter out, boolean isItemList) {
    	out.println("}");
    }

    /************************************************************************************************/
  	
    public static String convert2JSON(String s) {
  	  
    	String ret = "";
    	
    	for(int i=0; i<s.length(); i++) {
    		char c = s.charAt(i);
    		if(c == '\n') {
    			ret += '|';
    		} else {
    			ret += c;
    		}
    	}
    	  
    	return ret;

    }

	/***************************************************************************************/
    
    public static Object getJSONValue(JSONObject jsonObj, String key) {

    	Object ret = null;
    	
    	try {
    		ret = jsonObj.get(key);
    	} catch(Exception ex) {
    	}

    	return ret;
    	
    }
    
	/***************************************************************************************/
    
    public static JSONObject getJSONObject(String jsonStr) {

    	JSONObject ret = null;
    	
		try {
			ret = new JSONObject(jsonStr);
		} catch (JSONException e) {
		}
    	
    	return ret;
    	
    }

	/***************************************************************************************/
    
    public static List<String> getStringsFromJSONArray(JSONObject jsonObject, String key) {

    	List<String> ret = new ArrayList<String>();
    	
    	JSONArray array = (JSONArray) IDCUtils.getJSONValue(jsonObject, key);
    	for(int i=0; i<array.length(); i++) {
    		try {
				ret.add(array.getString(i));
			} catch (JSONException e) {
			}    		
        }

    	return ret;
    	
    }
    
	/************************************************************************************************/
    // XML ...
    /************************************************************************************************/

    public static void writeXMLHeader(PrintWriter out) {
    	writeXMLHeader(out,true);
    }

	/************************************************************************************************/

    public static void writeXMLTrailer(PrintWriter out) {
    	writeXMLTrailer(out,true);
    }

	/************************************************************************************************/

    public static void writeXMLHeader(PrintWriter out, boolean isItemList) {
    	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    	if(isItemList) {
    		out.println("<ItemList>");
    	}
    }

	/************************************************************************************************/

    public static void writeXMLTrailer(PrintWriter out, boolean isItemList) {
    	if(isItemList) {
    		out.println("</ItemList>");
    	}
    }

    /************************************************************************************************/

    public static Node getXMLAttrNode(NamedNodeMap attrs, String name) {
    	return attrs.getNamedItem(name);
    }

    /************************************************************************************************/

    public static String getXMLAttrValue(NamedNodeMap attrs, String name) {
    	
    	String ret = "";
    	
    	Node node = getXMLAttrNode(attrs, name);
    	if(node != null) {
    		ret = node.getNodeValue(); 
    	}
    	
    	return ret;
    	
    }

    /************************************************************************************************/

    static String xmlCtrlChar[] = {
    	"?", "&#x263a;", "&#x263b;", "&#x2665;", "&#x2666;", "&#x2663;", "&#x2660;", "&#x2022;", "&#x25d8;", "&#x9;",
    	"\n", "&#x2642;", "&#x2640;", "&#xd;", "&#x266b;", "&#x263c;", "&#x25ba;", "&#x25c4;", "&#x2195;", "&#x203c;",
    	"&#x00b6;", "&#x00a7;", "&#x25ac;", "&#x21a8;", "&#x2191;", "&#x2193;", "&#x2192;", "&#x2190;", "&#x221f;", "&#x2194;",
    	"&#x25b2;", "&#x25bc;", " "
    };

    public static String convert2XML(String s) {
    	  
    	String ret = "";
    	  
    	if(s != null) {
    		
    		StringBuffer sb = new StringBuffer();
    		  
    		for(int nChar=0, maxChar=s.length(); nChar < maxChar; nChar++) {
    			  
    			char c = s.charAt(nChar);
    			  
    			
    			if(c == '\n') {
    				sb.append(" | ");
    			} else if(c <= ' ') {
    				sb.append(xmlCtrlChar[c]);
    			} else if(c <= '\u0080') {
    				switch(c) {
	    				case 60: // '<'
	    					sb.append("&lt;");
	    					break;
	
	  	                case 62: // '>'
	  	                	sb.append("&gt;");
	  	                	break;
	
	  	                case 34: // '"'
	  	                	sb.append("&quot;");
	  	                	break;
	
	  	                case 39: // '\''
	  	                	sb.append("&apos;");
	  	                	break;
	
	  	                case 38: // '&'
	  	                	sb.append("&amp;");
	  	                	break;
	
	  	                default:
	  	                	sb.append(c);
	  	                	break;
  	                }
    			}
    		}
    	  
	        ret = sb.substring(0);

	    }

    	return ret;

    }

    /************************************************************************************************/
	  	
    public static String getXMIId(String itemType, long itemId) {
	    	 
    	String ret = convert2XML("_" + itemType + "_" + itemId);
    	
    	return ret;
	    	 
    }
    
    /************************************************************************************************/
    
	public static String getXMIHeader() {

		String ret = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		ret += "<XMI xmi.version=\"1.0\" xmlns:dt=\"urn:schemas-microsoft-com:datatypes\">\n";
		ret += "<XMI.content>\n";

		return ret;
	
	}

    /************************************************************************************************/

	public static String getXMIFooter() {

		String ret = "</XMI.content>\n";
		ret += "</XMI>\n";

		return ret;
	
	}
	
    /************************************************************************************************/

	public void applyXSLT(String input, String xsltFileName, PrintWriter out) {
		
	      try {
	
			  TransformerFactory fact = TransformerFactory.newInstance();
	    	  Transformer trans = fact.newTransformer(new StreamSource(xsltFileName));
	    	  trans.transform( new DOMSource(stringToDom(input)), new StreamResult(out) );
	    	  
	      } catch (Exception e) { 
	    	  e.printStackTrace(); 
    	  }

	}
	
    /************************************************************************************************/

    public static Document stringToDom(String xmlSource) {

    	Document ret = null;
    	
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
	    	ret = builder.parse(new InputSource(new StringReader(xmlSource)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ret;
		
    }


    
	/************************************************************************************************/
    // JSP ...
    /************************************************************************************************/

	public static String  getJSPParam(HttpServletRequest request, String param) {
          	   
		String paramStr = request.getParameter(param);

		return (paramStr == null ? "" : paramStr);

	}

    /************************************************************************************************/

	public static boolean  getJSPBooleanParam(HttpServletRequest request, String param) {
          	   
		int value = getJSPIntParam(request, param);
          	   
		return value > 0;
          	   
	}

    /************************************************************************************************/

	public static int getJSPIntParam(HttpServletRequest request, String param) throws Error {

		int ret = -1;
		
		String paramStr = request.getParameter(param);
   	   	
		if(paramStr != null && paramStr.length() > 0) {
			try {
				ret = Integer.parseInt(paramStr);
			} catch(java.lang.NumberFormatException ex) {
			}
		}
    	   
		return ret;
    	   
	}
       
	/****************************************************************************
	 *  getLongParam()                                                           *
	 ****************************************************************************/

	public static long getJSPLongParam(HttpServletRequest request, String param) throws Error {

		long ret = -1;
		
		String paramStr = request.getParameter(param);
   	   	
		if(paramStr != null && paramStr.length() > 0) {
			try {
				ret = Long.parseLong(paramStr);
			} catch(java.lang.NumberFormatException ex) {
			}
		}
    	   
		return ret;
    	   
	}
	
	/************************************************************************************************/
    // LOAD CLASSES ...
    /************************************************************************************************/

    public static Object loadCustomComponent(String className) {

    	Object ret = null;
    	
    	String msg = null;
		Class viewClass = null;
    	
    	if(className != null && className.length() > 0) {
    	    try {
    			viewClass = Class.forName(className);
    			ret = viewClass.newInstance();
    		} catch (Exception e) {
    			msg = "Could not find class " + className + " ...";
    		}
    	} else {
			msg = "No class name specified.";
    	}

		return ret;

    }
    
	/************************************************************************/

    public static Class loadCustomClass(String className) {

		Class ret = null;
    	
    	if(className != null && className.length() > 0) {
    	    try {
    			ret = Class.forName(className);
    		} catch (Exception e) {
    		}
    	}

		return ret;

    }

	/************************************************************************************************/
    // CALENDAR ...
    /************************************************************************************************/
	
	public static String getDateString(Date date) {
		return sdf.format(date);
	}

	/*******************************************************************************************************/
	
	public static String getDateString(long date) {
		return sdf.format(date);
	}

	/*******************************************************************************************************/
	
	public static Date getDate(String dateStr) {
		
		Date ret = null;
		
		try {
			ret = sdf.parse(dateStr);
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		
		return ret;
		
	}

	/************************************************************************************************/
    // PDF ...
    /************************************************************************************************/
	
    public static List<String> readPDF(String fileName) {
    	
    	List<String> ret = new ArrayList<String>();
    	
    	IDCUtilsPDF pdf = new IDCUtilsPDF(fileName);

    	boolean looping = true;
    	while(looping) {
    		String pageText = pdf.readNextPage();
    		if(pageText == null) {
    			looping = false;
    		} else {
    			ret.add(pageText);
    		}
    	}
    	
    	return ret;
    	
    }
    
	/************************************************************************************************/
    // SHELL COMMANDS ...
    /************************************************************************************************/
		
	public static void copyCmd(String fromDirName, String fromFileName, String toDirName, boolean isReplace) throws IOException, InterruptedException {
		
		String fromFilePath = fromDirName + "/" + fromFileName; 
		String toFilePath = toDirName + "/" + fromFileName;
		
		boolean isCopyNeeded = true;
		
		if(!isReplace) {
			File toFile = new File(toFilePath);
			if(toFile.exists()) {
				isCopyNeeded = false;
			}
		}
		
		if(isCopyNeeded) {
	        List<String> commands = new ArrayList<String>();
	        commands.add("/bin/cp");
	        commands.add(fromFilePath);
	        commands.add(toDirName);

	        ProcessBuilder pb = new ProcessBuilder(commands);
	        Process process = pb.start();

	        if (process.waitFor() != 0) {
	            System.out.println(">>> !!!!! Error copying: " + fromFilePath + " to " + toDirName);
	        }
		} else {
            System.out.println(">>> " + toFilePath + " already exisits ... not copying");
		}

    }
	
	/************************************************************************************************/
    // REST ...
    /************************************************************************************************/
    
	public static String getRawResponse(String url) {
		
    	System.out.println("getRawResponse(): url=" + url);

		String ret = "";
		
    	HttpClient httpclient = HttpClients.createDefault();
    	
    	HttpGet httpGet = new HttpGet(url);
    	httpGet.addHeader("Accept" , "application/json, text/javascript, */*; q=0.01");
    	httpGet.addHeader("Content-Type" , "application/x-www-form-urlencoded; charset=UTF-8");

        int nlines = 0;

        try {
    		
	    	HttpResponse response = httpclient.execute(httpGet);
	    	HttpEntity entity = response.getEntity();

	    	if (entity != null) {
	    	    InputStream instream = entity.getContent();
	    	    try {
	    	    	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(instream), 1);
	                String line;
	                while ((line = bufferedReader.readLine()) != null) {
	                	ret += line;
	                	//System.out.println("nlines=" + nlines);
	                	//System.out.print(".");
	                	nlines++;
	                }
	                instream.close();
	                bufferedReader.close();	    	    
	            } finally {
	    	        instream.close();
	    	    }
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	if(ret.length() == 0) {
    		ret = "I'm sorry, there was an error processing your request";
    	}


    	System.out.println("getRawResponse(): ret=" + ret);
    	
    	return ret;
    	
	}
	
    /************************************************************************************************/
    
	public static void download(String url, String fileName) {
		
    	System.out.println("download(): url=" + url + " /  fileName=" + fileName);

    	HttpClient httpclient = HttpClients.createDefault();
    	
    	HttpGet httpGet = new HttpGet(url);
    	httpGet.addHeader("Accept" , "application/json, text/javascript, */*; q=0.01");
    	httpGet.addHeader("Content-Type" , "application/x-www-form-urlencoded; charset=UTF-8");

        try {
    		
	    	HttpResponse response = httpclient.execute(httpGet);
	    	HttpEntity entity = response.getEntity();

	    	if (entity != null) {
	    	    InputStream instream = entity.getContent();
	    	    try {
	    	    	
	    			FileOutputStream fileOutputStream = new FileOutputStream(fileName); 
	    		    byte dataBuffer[] = new byte[1024];
	    		    int bytesRead;
	    		    while ((bytesRead = instream.read(dataBuffer, 0, 1024)) != -1) {
	    		        fileOutputStream.write(dataBuffer, 0, bytesRead);
	    		    }
	    		    fileOutputStream.close();
	            } catch(Exception ex) {
	                System.out.println("EXCEPTION: " + ex.getMessage());
	            } finally {
	                instream.close();
	    	    }
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
	}

	/************************************************************************************************/
    // KeyStore ...
    /************************************************************************************************/
    
	public static String getFirstKey(String fullKey) {

		String ret = "";
		
		int ind = fullKey.indexOf('.');
		if(ind == -1) {
			ret = fullKey;
		} else {
			ret = fullKey.substring(0, ind);
		}
		
		return ret;
	}

	/***************************************************************************************/
	
	public static String getLastKey(String fullKey) {
		
		String ret = "";
		
		int ind = fullKey.lastIndexOf('.');
		if(ind == -1) {
			ret = fullKey;
		} else {
			ret = fullKey.substring(ind+1, fullKey.length());
		}
		
		return ret;
		
	}

	/************************************************************************************************/
    // DEBUG ...
    /************************************************************************************************/

	public static void sendNotification(List<String> distributionList, String title, String content) {
		
		debug("sendNotification(): ");
		for(String email : distributionList) {
			debug("sendNotification(): email = " + email);
		}
		debug("sendNotification(): title = " + title);
		debug("sendNotification(): content = " + content);
		
	}

	/************************************************************************************************/
    // DEBUG ...
    /************************************************************************************************/

	public static void debugNow(String s) {
		System.out.println("Thread(" + Thread.currentThread().getId() + ") DEBUG: " + s); 
	}

	public static void error(String s) {
		debug(ERROR, "ERROR: " + s);
	}

	public static void debug(String s) {
		debug(DEBUG, "DEBUG: " + s);
	}

	/**********************************************************************/
	
	public static void debugNLU(String s) {
		debug(DEBUG_NLU, "NLU: " + s);
	}

	/**********************************************************************/
	
	public static void debugTemp(String s) {
		debug(TEMP, "DEBUG: " + s);
	}

	public static void startDbLog(String logName) {
		
		if(logName != null) {
			try {
				dblog = new PrintWriter(new File(logName + "." + System.currentTimeMillis() + ".txt"));
			} catch(Exception ex) {
				error(ex.getMessage());
			}
		}
		
	}

	public static void dbLog(String s) {
		
		debug(DATABASE, "DATABASE: " + s);

		if(dblog != null) {
			dblog.write(s + "\n");
		}
		
	}

	public static void closeDbLog() {
		if(dblog != null) {
			dblog.close();
		}
	}

	public static void info(String s) {
		debug(INFO, "INFO: " + s);
	}

    /************************************************************************************************/

	public static void debug(int level, String s) {
		
		boolean debug = false;
		
		if(debugLevel == -1) {
			if(level >= minDebugLevel) {
				debug = true;
			}
		} else {
			if(debugLevel == level) {
				debug = true;
			}
			
		}
		
		if(debug) {
//			System.out.println("Thread(" + Thread.currentThread().getId() + ") " + Thread.currentThread().getStackTrace()[2].getMethodName() + ": " + s); 
			System.out.println("Thread(" + Thread.currentThread().getId() + ") " +s); 
		}
		
	}

    /************************************************************************************************/

	public static void traceStart(String s) { 
		debug(TRACE, "TRACE START: " +s); 
	}

    /************************************************************************************************/

	public static void traceEnd(String s) { 
		debug(TRACE, "TRACE END: " +s); 
	}
	
    /************************************************************************************************/

    public static Object executeMethod(String className, String methodName, IDCApplication app) {
    	
        Object ret = null;
        
        try {
            Class c = Class.forName(className);
            Method method = c.getMethod(methodName, IDCApplication.class);
            Object methodInstance = c.newInstance();
            ret = method.invoke(methodInstance, app);
            
        } catch (Exception e) {
            IDCUtils.error(e.getMessage());
			e.printStackTrace();
		}
        
        return ret;
    	
    }
	


}
