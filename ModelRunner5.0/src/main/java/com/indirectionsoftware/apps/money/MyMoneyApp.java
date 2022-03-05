package com.indirectionsoftware.apps.money;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.backend.database.IDCDataRef;
import com.indirectionsoftware.backend.database.IDCDatabaseTableBrowser;
import com.indirectionsoftware.backend.database.IDCDbManager;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCCSVImportParser;
import com.indirectionsoftware.runtime.IDCXMLImportParser;
import com.indirectionsoftware.runtime.webapp.IDCWebAppContext;
import com.indirectionsoftware.utils.IDCCalendar;
import com.indirectionsoftware.utils.IDCUtils;

public class MyMoneyApp {
	
	/*******************************************************************************************************/
	
	private static final String[] FUNCTIONS = {"Clear", "Load", "ClearLoad", "InitCats", "ExportCats", "InitPatterns", "Test", "Match", "Export", "Return", "BankStatement", "Forecast", "ClearForecast", "SetForecast", "ClearDupes", "ImportEntries", "ExportEntries"}; 
	private static final int CLEAR=0, LOAD=1, CLEARLOAD=2, INITCATS=3, EXPORTCATS=4, INITPATTERNS=5, TEST=6, MATCH=7, EXPORT=8, RETURN=9, BANK_STATEMENT=10, FORECAST=11, CLEARFORECAST=12, SETFORECAST=13, CLEARDUPES=14, IMPORTENTRIES=15, EXPORTENTRIES=16;
	
	private static final String APP_NAME = "Money";
	private static IDCDbManager dbManager;
	private static IDCApplication app;
	
	private static IDCType categoryType, oldCategoryType, patternType;
	
	private static final String NO_MATCH = "zz No Match";
	
	/*******************************************************************************************************/
	
	public static void main(String[] args) {
				
		int func=0;

		if (args.length < 2) {
            System.err.println("Invalid arguments ...");
        } else {
    		func = IDCDbManager.decodeArgs(args[0], FUNCTIONS);
    		
    		dbManager = IDCDbManager.getIDCDbManager(args[1], true);
    		if(dbManager != null) {
    			
    			app = dbManager.getApplication(APP_NAME);
    			if(app  != null) {

    				switch(func) {

    					case TEST:
    						test();
    						break;

    					case MATCH:
    						matchAllEntries();
    						break;

    					case CLEAR:
    						if (args.length != 3) {
    				            System.err.println("Invalid arguments ... please specify the Properties file and the Account Name");
    				            usage();
    				        } else {
    				        	clearAccount(args[2]);
    				        }
    						break;

    					case LOAD:
    						if (args.length != 4) {
    				            System.err.println("Invalid arguments ... please specify the Properties file, the Account Name and the file name");
    				            usage();
    				        } else {
    				        	importAccountEntries(args[2], args[3]);
    				        }
    						break;

    					case CLEARLOAD:
    						if (args.length != 4) {
    				            System.err.println("Invalid arguments ... please specify the Properties file, the Account Name and the file name");
    				            usage();
    				        } else {
    				        	clearAccount(args[2]);
    				        	importAccountEntries(args[2], args[3]);
    				        }
    						break;

    					case INITCATS:
    						if (args.length != 3) {
    				            System.err.println("Invalid arguments ... please specify the Properties file and the Config file name");
    				            usage();
    				        } else {
    				        	initCats(args[2]);
    				        }
    						break;

    					case EXPORTCATS:
    						if (args.length != 2) {
    				            System.err.println("Invalid arguments ... please specify the Properties file");
    				            usage();
    				        } else {
    				        	exportCats();
    				        }
    						break;

    					case EXPORT:
    						if (args.length != 3) {
    				            System.err.println("Invalid arguments ... please specify the Properties file and the export file name");
    				            usage();
    				        } else {
    				        	export(args[2]);
    				        }
    						break;

    					case IMPORTENTRIES:
    						if (args.length != 3) {
    				            System.err.println("Invalid arguments ... please specify the Properties file and the import file name");
    				            usage();
    				        } else {
    				        	importCSV(args[2]);
    				        }
    						break;

    					case EXPORTENTRIES:
    						if (args.length != 3) {
    				            System.err.println("Invalid arguments ... please specify the Properties file and the export file name");
    				            usage();
    				        } else {
    				        	exportEntries(args[2]);
    				        }
    						break;

    					case INITPATTERNS:
    						if (args.length != 3) {
    				            System.err.println("Invalid arguments ... please specify the Properties file and the Config file name");
    				            usage();
    				        } else {
    				        	initPatterns(args[2]);
    				        }
    						break;

    					case RETURN:
    						if (args.length != 4) {
    				            System.err.println("Invalid arguments ... please specify the Properties file, Tax File name and the Financial Year");
    				            usage();
    				        } else {
    				        	createReturnCommandLine(args[2], args[3]);
    				        }
    						break;
    						
    					case BANK_STATEMENT:
    						if (args.length != 3) {
    				            System.err.println("Invalid arguments ... please specify the Properties file and the report file name");
    				            usage();
    				        } else {
    				        	String report = MoneyApp.getBankStatement(app);
    				        	PrintWriter out;
    							try {
    								out = new PrintWriter(args[2]);
    					        	out.print(report);
    					        	out.close();
    							} catch (FileNotFoundException e) {
    								e.printStackTrace();
    							}
    				        }
    						break;

    					case FORECAST:
    						if (args.length != 3) {
    				            System.err.println("Invalid arguments ... please specify the Properties file and the forecast file name");
    				            usage();
    				        } else {
//    				        	String report = MoneyApp.getForecast(app);
//    				        	PrintWriter out;
//    							try {
//    								out = new PrintWriter(args[2]);
//    					        	out.print(report);
//    					        	out.close();
//    							} catch (FileNotFoundException e) {
//    								e.printStackTrace();
//    							}
    				        }
    						break;

    					case CLEARFORECAST:
    						if (args.length != 3) {
    				            System.err.println("Invalid arguments ... please specify the Properties file and the forecast name");
    				            usage();
    				        } else {
//    				        	clearForecast(app, args[2]);
    				        }
    						break;

    					case CLEARDUPES:
    						if (args.length != 3) {
    				            System.err.println("Invalid arguments ... please specify the Properties file and the account name");
    				            usage();
    				        } else {
    				        	clearDupes(app, args[2]);
    				        }
    						break;

    				}
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
	
	static final String[] ATTRS = {"Date", "Description", "Amount"};
	
	private static void clearDupes(IDCApplication app2, String accountName) {
		
		Map<Long, IDCData> map = new HashMap<Long, IDCData>();
		List<IDCData> list = new ArrayList<IDCData>();
		
		IDCCalendar cal = app.getCalendar();

		IDCData account = app.requestDataByName("Account", accountName);
		List<IDCData> entries = account.getList("Entries");
		List<IDCData> entries2 = account.getList("Entries");
		
		for(IDCData entry : entries) {
			for(IDCData entry2 : entries) {
				if(!entry.equals(entry2) && entry.matches(entry2, ATTRS)) {
		            System.out.println(">>> MATCHED entry  = " + entry.getId() + " / date = " + entry.getString("Date") + " / Amount = " + entry.getString("Amount") + " / desc = " + entry.getString("Description"));
		            System.out.println(">>> MATCHED entry2 = " + entry2.getId() + " / date = " + entry2.getString("Date") + " / Amount = " + entry2.getString("Amount") + " / desc = " + entry2.getString("Description"));
		            map.put(entry.getId(), entry);
		            long id = entry2.getId();
		            if(map.get(id) == null) {
			            list.add(entry2);
		            }
				}
			}
			
		}

		for(IDCData entry2 : list) {
	        System.out.println(">>> DELETE entry2 = " + entry2.getId() + " / date = " + entry2.getString("Date") + " / Amount = " + entry2.getString("Amount") + " / desc = " + entry2.getString("Description"));
	        entry2.delete(true);
		}

		
//		IDCData entry1 = app.requestDataById("Entry", 3036);
//		long date1 = entry1.getLong("Date");
//		long amount1 = entry1.getLong("Amount");
//		String desc1 = entry1.getString("Description");
//
//		IDCData entry2 = app.requestDataById("Entry", 799);
//		long date2 = entry2.getLong("Date");
//		long amount2 = entry2.getLong("Amount");
//		String desc2 = entry2.getString("Description");
//				
//        System.out.println("Date1 = " + date1 + " / " + cal.displayTimeDateSeconds(date1));
//        System.out.println("Date2 = " + date2 + " / " +  cal.displayTimeDateSeconds(date2));
//		
//		if(cal.isSameDay(date1, date2) && amount1 == amount2 && desc1.equals(desc2)) {
//            System.out.println(">>> MATCHED values = " + entry2.getId() + " / date = " + entry2.getString("Date") + " / Amount = " + entry2.getString("Amount") + " / desc = " + entry2.getString("Description"));
//		}
//		if(entry1.matches(entry2,ATTRS)) {
//            System.out.println(">>> MATCHED matches = " + entry2.getId() + " / date = " + entry2.getString("Date") + " / Amount = " + entry2.getString("Amount") + " / desc = " + entry2.getString("Description"));
//		}

	}

	/*******************************************************************************************************/
	
	private static void createReturnCommandLine(String taxFileName, String fyName) {
		
		IDCData taxFile = app.requestSingleData("TaxFile", "Name == '" + taxFileName + "'", null);
		IDCData fy = app.requestSingleData("TaxFinancialYear", "Name == '" + fyName + "'", null);
		
		if(taxFile != null && fy != null) {
			
			IDCData ret = app.createData("TaxReturn");
			ret.set("FinancialYear", fy);
			ret.save();
			taxFile.insertReference("Returns", ret);
			
			for(IDCData cat : taxFile.getList("Categories")) {
				System.out.println("cat = " + cat);
				IDCData retCat = app.createData("TaxReturnCategory");
				retCat.set("Category", cat);
				for(IDCData entry : cat.getData("Category").getList("Entries")) {
					retCat.insertReference("Entries", entry);
				}
				retCat.save();
				ret.insertReference("Categories", retCat);
			}
			
			ret.save();
			
		}
		
	}

	/*******************************************************************************************************/
	
	private static void matchAllEntries() {
		
		for(IDCData account : app.requestAllData("Account")) {
        	matchAccountEntries(account);
		}
	}

	/*******************************************************************************************************/
	
	public void test2() {}
	
	private static void test() {
		
		IDCData account = app.requestSingleData("Account", "Name == 'AMEX'", null);
		if(account != null) {
			for(IDCData entry : account.getList("Entries")) {
				long amount = entry.getLong("Amount") * -1;
				entry.set("Amount", amount);
				entry.save();
			}
		}
		
	}

	/*******************************************************************************************************/
	
	private static void usage() {
		
		IDCUtils.info("Parameters: [function] [properties file] [[arguments]]");
		IDCUtils.info("       where function is : ");
		IDCUtils.info("              Clear [account name]");
		IDCUtils.info("              Load [account name] [file name]");
		IDCUtils.info("              ClearLoad [account name] [file name]");
		
	}
	
	/*******************************************************************************************************/
	
	private static void clearAccount(String accountName) {
		
		IDCData account = app.requestSingleData("Account", "Name == '" + accountName + "'", null);
		if(account != null) {
			for(IDCData entry : account.getList("Entries")) {
				entry.delete(true);
			}
		}
		
	}
	
	/*******************************************************************************************************/
	
	private static void importAccountEntries(String accountName, String fileName) {
		
		IDCData account = app.requestSingleData("Account", "Name == '" + accountName + "'", null);
		if(account == null) {
			System.err.println("Can't find account name " + accountName);
		} else {
			File file = new File(fileName);
			if(!file.exists()) {
				System.out.println("ERROR: Can't find file name " + fileName);
			} else {
				IDCType entryType = app.getType("Entry");
				if(entryType == null) {
					System.err.println("ERROR: This one is a BIG PROBLEM: can't find the Entry type!!!!");
				} else {
					importAccountEntries(file, account, entryType);
		        	matchAccountEntries(account);
				}

			}
		}
		
	}
	
    /************************************************************************************************/

	public static void importAccountEntries(File file, IDCData account, IDCType entryType) {
		
		System.out.println("Processing File: " + file.getAbsolutePath());
		
		IDCData institution = account.getData("Institution");
		if(institution != null) {
			
			int headerLines = institution.getInt("HeaderLines");

			int dateCol = institution.getInt("DateColumn");
			int amountCol = institution.getInt("AmountColumn");
			int descCol = institution.getInt("DescriptionColumn");
			int debitCol = institution.getInt("DebitColumn");
			
			boolean isReverse = institution.getBoolean("IsReverse");
			boolean isCents = institution.getBoolean("IsCents");
			
			int nLine = 0;
			for(List<String> cols : IDCUtils.parseCSVFile(file)) {
				
				if(nLine++ >= headerLines) {

					if(cols.size() > 2) {
						
						String dateStr = cols.get(dateCol);
						String amountStr = cols.get(amountCol);
						String descStr = cols.get(descCol);
						
						Date date = null; 
						
						try {
							date = IDCUtils.sdf.parse(dateStr);
							int year = date.getYear();
							if(year < 100) {
								date.setYear(year + 2000);
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}

						if(date != null) {
							
							long amount = 0;

							if(debitCol == -1) {
								amount = Math.round(Double.parseDouble(amountStr) * 100);
							} else {
								if(amountStr.length() > 0) {
									amount = Math.round(Double.parseDouble(amountStr) * 100);
								} else {
									String debitStr = cols.get(debitCol);
									amount = Math.round(Double.parseDouble(debitStr) * 100);
								}
							}
							
							if(isCents) {
								amount = amount / 100;
							}
							
							if(isReverse) {
								amount *= -1;
							}

							System.out.println("Loading Entry: date = " + dateStr + " / amount = " + amountStr + " / desc = " + descStr);

							IDCData entry = entryType.createData();
							entry.set("Date", date);
							entry.set("Amount", amount);
							entry.set("Description", descStr);
							addEntryIfNotDuped(account, entry);

						}

					}
					
				}
				
			}

			
		}

	}

	static List<Pattern> patterns = null;
	private static IDCData noMatchCat;

	/*******************************************************************************************************/
	
	private static void matchAccountEntries(IDCData account) {
		
		patterns = new ArrayList<Pattern>();

		noMatchCat = app.requestSingleData("Category", "Name == '" + NO_MATCH + "'", null);
		if(noMatchCat == null) {
			System.err.println("ERROR: This one is a BIG PROBLEM: can't find the No Match Category!!!!");
		} else {

			for(IDCData pat : account.getList("Patterns")) {
				patterns.add(new Pattern(pat));
			}

			for(IDCData entry : account.getList("Entries")) {
				System.out.println("Checking1 Entry: date = " + entry.getDisplayValue("Date") + " / Amount = " + entry.getDisplayValue("Amount") + " / Description = " + entry.getDisplayValue("Description") + " / Category = " + entry.getDisplayValue("Category"));
				IDCData cat = entry.getData("Category");
				if(cat == null || cat.equals(noMatchCat)) {
					if(entry.getString("Description").startsWith("VCI")) {
						int i =1;
					}
					System.out.println("Checking2 Entry: date = " + entry.getDisplayValue("Date") + " / Amount = " + entry.getDisplayValue("Amount") + " / Description = " + entry.getDisplayValue("Description") + " / Category = " + entry.getDisplayValue("Category"));
					matchEntry(entry);
				}
			}

		}
		
	}
	

	/************************************************************************************************/

    private static void matchEntry(IDCData entry) {
    	
    	IDCData cat = null;
    	
		for(Pattern pat : patterns) {
			cat = pat.getCategory(entry);
			if(cat != null) {
				break;
			}
		}

		if(cat == null) {
			cat = noMatchCat;
			System.out.println("No pattern matching for Entry: date = " + entry.getDisplayValue("Date") + " / Amount = " + entry.getDisplayValue("Amount") + " / Description = " + entry.getDisplayValue("Description"));
		}
		entry.set("Category", cat);
		entry.save();

	}

	/************************************************************************************************/

	private static void addEntryIfNotDuped(IDCData account, IDCData entry) {

		boolean found = false;
		for(IDCData oldEntry : account.getList("Entries")) {
			if(oldEntry.matches(entry, ATTRS)) {
				found = true;
				System.out.println("Ignoring duplicates NEW Entry: date = " + entry.getDisplayValue("Date") + " / Amount = " + entry.getDisplayValue("Amount") + " / Description = " + entry.getDisplayValue("Description"));
				System.out.println("Ignoring duplicates OLD Entry: date = " + oldEntry.getDisplayValue("Date") + " / Amount = " + oldEntry.getDisplayValue("Amount") + " / Description = " + oldEntry.getDisplayValue("Description"));
				break;
			}
		}
		
		if(!found) {
			entry.save();
			account.insertReference("Entries", entry);
		}

	}
	
	/*******************************************************************************************************/
	
	public static final String CONFIG="Config", MISC = "Misc", CATEGORY="Category", VENDOR="Vendor", PATTERN="Pattern", ACCOUNT="Account", ENTRY="Entry", SALES_ENTRY="SalesEntry"; 
	public static final String INITBALANCE="initbalance", DIR = "dir", NAME="name", ID="xmi.id", TYPE="type", KEY="key", CAT="category", DEFAULT="isdefault", REVERSE="isreverse", CASH="iscash", DATE="date", DESC="desc", AMOUNT="amount", CATID="cat.idref",
			DATE_COL="datecol", AMOUNT_COL="amountcol", DESC_COL="desccol", DEBIT_COL="debitcol",TAX_RATE="taxRate", HEADER_LINES="headerlines"; 

	/*******************************************************************************************************/
	
	private static void initCats(String fileName) {
		
		categoryType = app.getType("Category");
		oldCategoryType = app.getType("OldCategory");
		
		clearCats();
		
		IDCXMLImportParser importer = new IDCXMLImportParser(app);
		importer.importXML(new File(fileName));

		
	}
	
	/*******************************************************************************************************/
	
	private static void clearCats() {
		
		app.clearType("Category");
		app.clearType("OldCategory");
		
	}
	
	/*******************************************************************************************************/
	
	private static void initCats(File file) {
		
        try {

        	FileInputStream stream = new FileInputStream(file);

        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
        	Document document;
        	
            document = builder.parse(stream);
            
            processNode(document.getFirstChild(), null);
            
        } catch(Exception ex) {
        	ex.printStackTrace();            	
        }
		
        
		
	}
	
	/************************************************************************************************/

    private static void processNode(Node node, IDCData parent) throws Exception {

        String nodeType = node.getNodeName();    
        
    	if(nodeType.equals(CATEGORY)) {
            NamedNodeMap attrs = node.getAttributes();
        	String name = IDCUtils.getXMLAttrValue(attrs, NAME);
        	String id = IDCUtils.getXMLAttrValue(attrs, ID);
            parent = processCategoryNode(node, name, id, parent);
    	} 
    	
        Node childNode = node.getFirstChild();
        while(childNode != null) {
        	if(childNode.getNodeName() != "#text") {
                processNode(childNode, parent);
        	}
            childNode = childNode.getNextSibling();
        }
        
   } 

	/************************************************************************************************/

    private static IDCData processCategoryNode(Node node, String name, String id, IDCData parent) throws Exception  {
    	
    	
    	System.out.println("Processing Category name=" + name  + " id=" + id);

    	IDCData cat = oldCategoryType.createData();
    	cat.set("Name", name);
    	cat.set("Id", id);
    	cat.save();

    	if(parent != null) {
    		parent.insertReference("SubCategories", cat);
    	}
    	
    	return cat;

    }
    
	/*******************************************************************************************************/
	
	private static void exportCats() {
		
		IDCType entryType = app.getType("Entry");
		IDCType catType = app.getType("Category");
		
		for(IDCData cat : catType.loadAllDataObjects()) {			
	    	try {
	    		PrintWriter out = new PrintWriter(new File("/Users/etiennesillon/Downloads/" + cat.getName() + ".csv"));
				out.print(entryType.getCSVHeader());
				for(IDCData data : cat.getList("Entries")) {
					out.print(data.getCSVString(false));
				}
	    		out.close();
	    	} catch (FileNotFoundException e) {
				e.printStackTrace();
			} 

		}
		
	}

	/*******************************************************************************************************/
	
	private static void exportEntries(String fileName) {
		
    	try {
    		
    		PrintWriter out1 = new PrintWriter(new File("/Users/etiennesillon/Downloads/categories.csv"));
    		IDCType catType = app.getType("Category");
    		for(IDCData cat : catType.loadAllDataObjects()) {			
				out1.println(cat.getName());
    		}
    		out1.close();
    		
    		IDCType entryType = app.getType("Entry");

    		PrintWriter out = new PrintWriter(new File(fileName));
			out.print(entryType.getCSVHeader());
			
			IDCType accountType = app.getType("Account");
			for(IDCData account : accountType.loadAllDataObjects()) {			
				for(IDCData data : account.getList("Entries")) {
					out.print(data.getCSVString(false));
				}
			}

			out.close();
			
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
	}

	/*******************************************************************************************************/
	
	private static void export(String fileName) {
		
		app.exportXML(fileName, true);
		
	}
	
	/*******************************************************************************************************/
	
	private static void importCSV(String fileName) {
		
		IDCCSVImportParser parser = new IDCCSVImportParser(app);
		parser.importCSV(new File(fileName));
		
	}
	
	/*******************************************************************************************************/
	
	private static void initPatterns(String fileName) {
		
		patternType = app.getType("AccountPattern");
		
		clearPatterns();
		
		importPatterns(fileName);
		
		
	}
	
	/*******************************************************************************************************/
	
	private static void clearPatterns() {
		
		app.clearType("AccountPattern");
		
	}
	

	/*******************************************************************************************************/
	
	private static void importPatterns(String fileName) {
		
		IDCType patternType = app.getType("AccountPattern");
		File file = new File(fileName);
		if(file != null && patternType != null) {
			importPatterns(file, patternType);
		}
		
	}
	
    /************************************************************************************************/

	public static void importPatterns(File file, IDCType patternType) {
		
		System.out.println("MyMoneyApp.loadFile(): processing File: " + file.getAbsolutePath());
		
		int nLine = 0;
		for(List<String> cols : IDCUtils.parseCSVFile(file)) {
			
			if(cols.size() == 2) {
				
				String patternStr = cols.get(0);
				String oldCatId = cols.get(1);
				
				IDCData oldCat = app.requestSingleData("OldCategory", "Id == '" + oldCatId + "'", null);

				IDCData pattern = patternType.createData();
				pattern.set("Pattern", patternStr);
				pattern.set("OldCategory", oldCat);
				pattern.set("Category", oldCat.getData("NewCategory"));
				pattern.save();

			}
			
		}
		
	}
	
	/*******************************************************************************************************/
	// Method called by ExecMethod
	/*******************************************************************************************************/
	
	public void createReturn(IDCApplication app, IDCData ret, IDCData ref) {
		
		IDCData fy = ret.getData("FinancialYear");
		if(fy != null) {
			
			long startDate = fy.getLong("StartDate");
			long endDate = fy.getLong("EndDate");
			
			for(IDCData cat : ret.getNamespaceParent().getList("Categories")) {
				System.out.println("cat = " + cat);
				IDCData retCat = app.createData("TaxReturnCategory");
				retCat.set("Category", cat);
				for(IDCData entry : cat.getData("Category").getList("Entries")) {
					long date = entry.getLong("Date");
					if(date >= startDate && date <= endDate) {
						retCat.insertReference("Entries", entry);
					}
				}
				retCat.save();
				ret.insertReference("Categories", retCat);
			}
			
			ret.save();
			
		}
		
	}

	/*******************************************************************************************************/
	
	public void matchEntries(IDCApplication app, IDCData account, IDCData ref) {
		matchAccountEntries(account);
	}



}