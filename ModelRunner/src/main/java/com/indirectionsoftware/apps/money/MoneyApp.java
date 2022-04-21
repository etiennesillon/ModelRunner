package com.indirectionsoftware.apps.money;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.indirectionsoftware.backend.database.IDCData;
import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCFormulaContext;
import com.indirectionsoftware.runtime.webapp.IDCWebAppSettings;
import com.indirectionsoftware.utils.IDCCalendar;
import com.indirectionsoftware.utils.IDCUtils;

public class MoneyApp {
	
	/*******************************************************************************************************/
	
	IDCApplication app;
	List<Pattern> patterns = null;
	IDCData noMatchCat;
	IDCType categoryType, oldCategoryType, patternType;
	
	static final String NO_MATCH = "zz No Match";
	
	/*******************************************************************************************************/
	// Methods called by ExecMethod
	/*******************************************************************************************************/
	
	public void createReturn(IDCApplication app, IDCData ret, IDCFormulaContext context) {
		
		this.app = app;
		
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
	
	public void matchEntries(IDCApplication app, IDCData account, IDCFormulaContext context) {
		this.app = app;		
		matchAccountEntries(account);
	}


	/*******************************************************************************************************/
	
	public void uploadStatement(IDCApplication app, IDCData account, IDCFormulaContext context) {
		
		this.app = app;
		
		String fileContent = context.getFileContent();
		
		IDCType entryType = app.getType("Entry");
		
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
			for(List<String> cols : IDCUtils.parseCSVString(fileContent)) {
				
				if(nLine++ >= headerLines) {

					if(cols.size() > 2) {
						
						String dateStr = cols.get(dateCol);
						String amountStr = cols.get(amountCol);
						String descStr = cols.get(descCol);
						
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
						entry.set("Date", dateStr);
						entry.set("Amount", amount);
						entry.set("Description", descStr);
						addEntryIfNotDuped(account, entry);

					}
					
				}
				
			}

			
		}

	}

	/*******************************************************************************************************/
	// Internal Methods
	/*******************************************************************************************************/
	
	private void matchAccountEntries(IDCData account) {
		
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

    private void matchEntry(IDCData entry) {
    	
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

	static final String[] ATTRS = {"Date", "Description", "Amount"};
	
	private static void addEntryIfNotDuped(IDCData account, IDCData entry) {

		System.out.println("addEntryIfNotDuped(): date = " + entry.getDisplayValue("Date") + " / Amount = " + entry.getDisplayValue("Amount") + " / Description = " + entry.getDisplayValue("Description"));

		IDCData foundEntry = null;
		for(IDCData oldEntry : account.getList("Entries")) {
			System.out.println("Comparing to: date = " + oldEntry.getDisplayValue("Date") + " / Amount = " + oldEntry.getDisplayValue("Amount") + " / Description = " + oldEntry.getDisplayValue("Description"));
			if(oldEntry.matches(entry, ATTRS)) {
				foundEntry = oldEntry;
				System.out.println("Ignoring duplicates NEW Entry: date = " + entry.getDisplayValue("Date") + " / Amount = " + entry.getDisplayValue("Amount") + " / Description = " + entry.getDisplayValue("Description"));
				System.out.println("Ignoring duplicates OLD Entry: date = " + oldEntry.getDisplayValue("Date") + " / Amount = " + oldEntry.getDisplayValue("Amount") + " / Description = " + oldEntry.getDisplayValue("Description"));
				break;
			}
		}
		
		if(foundEntry == null) {
			entry.save();
			account.insertReference("Entries", entry);
		}

	}
	
	/************************************************************************************************/

    public static String getBankStatement(IDCApplication app) {
    	
		String ret = "<H1>Bank Statement</H1>";
		
    	ret += "<table>";
    	
    	ret += "<tr>";
    	ret += "<td width=50px><B>Id</B></td>";
    	ret += "<td width=100px><B>Date</B></td>";
    	ret += "<td width=100px><B>Amount</B></td>";
    	ret += "<td width=500px><B>Description</B></td>";
    	ret += "<td width=500px><B>Category</B></td>";
    	
    	Map<Long, List<IDCData>> dateEntriesMap = new HashMap<Long, List<IDCData>>(); 
    	
    	List<Account> accounts = new ArrayList<Account>();
    	
		for(IDCData accountData : app.getType("Account").loadAllDataObjects()) {
			
			accounts.add(new Account(accountData));
			ret += "<td width=200px><B>" + accountData.getName() + " </B></td>"; 
			for(IDCData entry : accountData.getList("Entries")) {
				long date = entry.getLong("Date");
				if(IDCWebAppSettings.isWithinDateRange(date)) {
					List<IDCData> entries = dateEntriesMap.get(date);
					if(entries == null) {
						entries = new ArrayList<IDCData>();
						dateEntriesMap.put(date,  entries);
					}
					entries.add(entry);
				}
			}

		}
		
    	List<Long> dates = new ArrayList<Long>();
    	for(long date : dateEntriesMap.keySet()) {
    		dates.add(date);
    	}
    	Collections.sort(dates);

    	
		ret += "<td width=200px><B>Total</B></td>"; 
    	ret += "</tr>";

    	boolean isGrey = false;
    	
    	for(long date : dates) {
    		
			for(IDCData entry : dateEntriesMap.get(date)) {
				
	        	ret += "<tr " + (isGrey ? "bgcolor=\"#ececec\"" : "") + ">";
		    	if(entry != null) {
			    	ret += "<td>" +  IDCUtils.makeFixedLength("" + entry.getId(), 10) + "</td>";
			    	ret += "<td>" +  IDCUtils.getDateString(entry.getLong("Date")) + "</td>";
			    	ret += "<td>" +  IDCUtils.getAmountString(entry.getLong("Amount")) + "</td>";
			    	ret += "<td>" +  entry.getString("Description") + "</td>";
			    	IDCData catData = entry.getData("Category");
			    	String catName = catData == null ? "(no cat)" : catData.getName(); 
			    	ret += "<td>" + catName  + "</td>";
		    	}

		    	long total = 0;
		    	
				long accountId = entry.getData("Account").getId();
				for(Account account : accounts) {
					if(accountId == account.account.getId()) {
						account.balance += entry.getLong("Amount");
					}
					ret += "<td width=300px><B>" + IDCUtils.getAmountString(account.balance) + " </B></td>";
					total += account.balance;
				}

				ret += "<td width=300px><B>" + IDCUtils.getAmountString(total) + " </B></td>"; 

				isGrey = !isGrey;
	        	
	        	ret += "</tr>";

			}
		}
    	
    	ret += "</table>";
		
    	return ret;

    }
	
	/*******************************************************************************************************/
	
    public static void clearForecast(IDCApplication app, IDCData forecast, IDCFormulaContext context) {
		
		IDCData scenario = app.requestDataByName("Scenario", forecast.getName());
		if(scenario != null) {
			
			for(IDCData event : scenario.getList("Events")) {
				
				System.out.println("getForecast(): event = " + event.getName());
				
				IDCData accountData = event.getData("Account");
				String descForm = event.getString("DescriptionFormula");
				
				for(IDCData entry : accountData.requestChildren("Description == '" + descForm + "'", "Entries")) {
					System.out.println("setForecast(): entry = " + entry.getId() + " " + entry.getString("Description"));
					entry.delete(true);
				}

			}
			
		}

		
	}

	/************************************************************************************************/

    public static void createForecast(IDCApplication app, IDCData forecast, IDCFormulaContext context) {
    	
		IDCData scenario = app.requestDataByName("Scenario", forecast.getName());
		if(scenario != null) {
			
			for(IDCData event : scenario.getList("Events")) {
				
				System.out.println("createForecast(): event = " + event.getName());
				
				IDCData accountData = event.getData("Account");
				IDCData categoryData = event.getData("Category");
				String descForm = event.getString("DescriptionFormula");
				String amountForm = event.getString("AmountFormula");
				String recForm = event.getString("Recurrence");
				int day = event.getInt("Day");
				
				long date = startDate;
				while((date = getNextDate(date, endDate, recForm, day)) != -1) {
					IDCData entry = app.createData("Entry", accountData, "Entries");
					entry.set("Date", date);
					entry.set("Description", descForm);
					entry.set("Amount", amountForm);
					entry.set("Category", categoryData);
					entry.set("ForecastId", forecast.getName());
					entry.save();
				}
				
			}

			
			List<IDCData> forecastWEntries = accountData.requestChildren("Description == '" + descForm + "'", "Entries")
			
			IDCCalendar cal = IDCCalendar.getCalendar();
			long startDate = cal.getDayStart();
			long endDate = startDate + IDCCalendar.DAY * 365;
			
			for(IDCData event : scenario.getList("Events")) {
				
				System.out.println("createForecast(): event = " + event.getName());
				
				IDCData accountData = event.getData("Account");
				IDCData categoryData = event.getData("Category");
				String descForm = event.getString("DescriptionFormula");
				String amountForm = event.getString("AmountFormula");
				String recForm = event.getString("Recurrence");
				int day = event.getInt("Day");
				
				long date = startDate;
				while((date = getNextDate(date, endDate, recForm, day)) != -1) {
					IDCData entry = app.createData("Entry", accountData, "Entries");
					entry.set("Date", date);
					entry.set("Description", descForm);
					entry.set("Amount", amountForm);
					entry.set("Category", categoryData);
					entry.set("ForecastId", forecast.getName());
					entry.save();
				}
				
			}
			
		}

    }
    
	/************************************************************************************************/

    public static String getForecastReport(IDCApplication app) {
    	
		String ret = "<H1>Forecast</H1>";
		
    	ret += "<table>";
    	
    	ret += "<tr>";
    	ret += "<td width=50px><B>Id</B></td>";
    	ret += "<td width=100px><B>Date</B></td>";
    	ret += "<td width=100px><B>Amount</B></td>";
    	ret += "<td width=500px><B>Description</B></td>";
    	ret += "<td width=500px><B>Category</B></td>";
    	
    	IDCType accountType = app.getType("Account");
    	
    	Map<Long, List<IDCData>> dateEntriesMap = new HashMap<Long, List<IDCData>>(); 
    	
    	List<Account> accounts = new ArrayList<Account>();
    	Map<Long, Account> accountsMap = new HashMap<Long, Account>();
    	
		for(IDCData accountData : accountType.loadAllDataObjects()) {
			
			Account acc = new Account(accountData);
			accounts.add(acc);
			accountsMap.put(accountData.getId(), acc);
			ret += "<td width=200px><B>" + accountData.getName() + " </B></td>"; 
			for(IDCData entry : accountData.getList("Entries")) {
				long date = entry.getLong("Date");
				List<IDCData> entries = dateEntriesMap.get(date);
				if(entries == null) {
					entries = new ArrayList<IDCData>();
					dateEntriesMap.put(date,  entries);
				}
				entries.add(entry);
			}

//			if(accountData.getName().equals("ING Bien Etre")) {
//			}
			
		}
		
    	List<Long> dates = new ArrayList<Long>();
    	for(long date : dateEntriesMap.keySet()) {
    		dates.add(date);
    	}
    	Collections.sort(dates);
    	
		ret += "<td width=200px><B>Total</B></td>"; 
    	ret += "</tr>";

    	boolean isGrey = false;
    	
    	for(long date : dates) {
    		
			for(IDCData entry : dateEntriesMap.get(date)) {
				
	        	ret += "<tr " + (isGrey ? "bgcolor=\"#ececec\"" : "") + ">";
		    	if(entry != null) {
			    	ret += "<td>" +  IDCUtils.makeFixedLength("" + entry.getId(), 10) + "</td>";
			    	ret += "<td>" +  IDCUtils.getDateString(entry.getLong("Date")) + "</td>";
			    	ret += "<td>" +  IDCUtils.getAmountString(entry.getLong("Amount")) + "</td>";
			    	ret += "<td>" +  entry.getString("Description") + "</td>";
			    	ret += "<td>" +  entry.getData("Category").getName() + "</td>";
		    	}

		    	long total = 0;
		    	
				long accountId = entry.getData("Account").getId();
				for(Account account : accounts) {
					if(accountId == account.account.getId()) {
						account.balance += entry.getLong("Amount");
					}
					ret += "<td width=300px><B>" + IDCUtils.getAmountString(account.balance) + " </B></td>";
					total += account.balance;
				}

				ret += "<td width=300px><B>" + IDCUtils.getAmountString(total) + " </B></td>"; 

				isGrey = !isGrey;
	        	
	        	ret += "</tr>";

			}
		}
    	
    	ret += "</table>";
		
    	return ret;

    }
    
    /****************************************************************/

	private static long getNextDate(long date, long endDate, String recForm, int day) {

		long ret = -1;
		
		IDCCalendar cal = IDCCalendar.getCalendar();
		
		System.out.println("getNextDate(): date = " + cal.displayDate(date));
		
		if(recForm.equals("WEEK")) {
			ret = cal.getWeekStart(date) + IDCCalendar.WEEK;
			if(ret <= date) {
				ret += IDCCalendar.WEEK;
			}
		} else if(recForm.equals("MONTH")) {
			ret = cal.getMonthStart(date) + IDCCalendar.MONTH;
			if(ret <= date) {
				ret += IDCCalendar.MONTH;
			}
		} else if(recForm.equals("QUARTER")) {
			ret = cal.getMonthStart(date) + IDCCalendar.MONTH * 3;
			if(ret <= date) {
				ret += IDCCalendar.MONTH * 3;
			}
		} else if(recForm.equals("YEAR")) {
			ret = cal.getYearStart(date) + IDCCalendar.YEAR;
			if(ret <= date) {
				ret += IDCCalendar.YEAR;
			}
		}
		
		ret += day * IDCCalendar.DAY;
		if(ret > endDate) {
			ret = -1;
		}
		
		return ret;
		
	}
	
}