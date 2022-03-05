package com.indirectionsoftware.utils;

import java.util.*;

public class IDCCalendar {
		
	private static IDCCalendar calendar = null;

	private Calendar 	cal;
	private long	 	base;
	
	public static final int STARTYEAR=2000, STARTMONTH=Calendar.JANUARY, STARTDAY=2;
	
	public static final long 	SEC=1000, MIN=60*SEC, HR=60*MIN, DAY=24*HR, WEEK=DAY*7, FORTNIGHT=WEEK*2, MONTH=DAY*31, YEAR=DAY*365; 
	
	public static final String dayNames[] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
	public static final String shortDayNames[] = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
	public static final String monthNames[] = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	
	/******************************************************************/
	
	public static IDCCalendar getCalendar() {
		
		if(calendar == null) {
			calendar = new IDCCalendar();
		}
			
		return calendar;
				
	}	

	/******************************************************************/
		
	public IDCCalendar() {
		
		cal = Calendar.getInstance();

		cal.set(Calendar.DAY_OF_MONTH, STARTDAY);
		cal.set(Calendar.MONTH, STARTMONTH);
		cal.set(Calendar.YEAR, STARTYEAR);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		base = cal.getTimeInMillis();

	}
	
	/******************************************************************/
	
	public long getDayStart(long day) {
			
		setDate(day);

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTimeInMillis();
				
	}	

	public long getDayStart() {
		
		return getDayStart(getTime());
			
	}	

	/******************************************************************/
	
	public long getNextDayStart(long day) {
		
		return getDayStart(day) + DAY;
			
	}	

	public long getNextDayStart() {
		
		return getNextDayStart(getTime());
			
	}	

	/******************************************************************/
	
	public boolean isSameDay(long day1, long day2) {
		
		long start1 = getDayStart(day1);
		long start2 = getDayStart(day2);
		
//        System.out.println("day1 = " + day1 + " / " + displayTimeDate(day1) + " / start = " + start1);
//        System.out.println("day2 = " + day2 + " / " + displayTimeDate(day2) + " / start = " + start2);

		
		return start1 == start2;
			
	}	

	/******************************************************************/
	
	public long getWeekStart(long day) {
			
		setDate(day);

		cal.set(Calendar.DAY_OF_WEEK, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTimeInMillis();
				
	}	

	public long getWeekStart() {
		
		return getWeekStart(getTime());
			
	}	

	/******************************************************************/
	
	public long getFornightStart(long day) {
			
		setDate(day);
		
		long wom = cal.get(Calendar.WEEK_OF_MONTH); 
		cal.set(Calendar.WEEK_OF_MONTH, (wom < 2 ? 0 : 2));
		cal.set(Calendar.DAY_OF_WEEK, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTimeInMillis();
				
	}	

	public long getFornightStart() {
		
		return getFornightStart(getTime());
			
	}	

	/******************************************************************/
	
	public long getMonthStart(long day) {
			
		setDate(day);

		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTimeInMillis();
				
	}	

	public long getMonthStart() {
		
		return getMonthStart(getTime());
			
	}	

	/******************************************************************/
	
	public long getNextMonthStart(long day) {
			
		setDate(day);

		int month = cal.get(Calendar.MONTH);
		
		if(month < 11) {
			cal.set(Calendar.MONTH,  month + 1);
		} else {
			cal.set(Calendar.MONTH,  0);
			cal.set(Calendar.YEAR,  cal.get(Calendar.YEAR) + 1);
		}
		
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTimeInMillis();
				
	}	

	public long getNextMonthStart() {
		
		return getNextMonthStart(getTime());
			
	}	

	/******************************************************************/
	
	public long getPrevMonthStart(long day) {
			
		setDate(day);

		int month = cal.get(Calendar.MONTH);
		
		if(month > 0) {
			cal.set(Calendar.MONTH,  month - 1);
		} else {
			cal.set(Calendar.MONTH,  11);
			cal.set(Calendar.YEAR,  cal.get(Calendar.YEAR) - 1);
		}
		
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTimeInMillis();
				
	}	

	public long getPrevMonthStart() {
		
		return getNextMonthStart(getTime());
			
	}	

	/******************************************************************/
	
	public long getYearStart(long day) {
			
		setDate(day);

		cal.set(Calendar.DAY_OF_YEAR, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTimeInMillis();
				
	}	

	public long getYearStart() {
		
		return getYearStart(getTime());
			
	}	

	/******************************************************************/
	
	public long getNextYearStart(long day) {
			
		setDate(day);

		int year = cal.get(Calendar.YEAR);
		cal.set(Calendar.YEAR,  year + 1);

		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTimeInMillis();
				
	}	

	public long getNextYearStart() {
		
		return getNextYearStart(getTime());
			
	}	

	/******************************************************************/
	
	public long getPrevYearStart(long day) {
			
		setDate(day);

		int year = cal.get(Calendar.YEAR);
		cal.set(Calendar.YEAR,  year - 1);
		
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTimeInMillis();
				
	}	

	public long getPrevYearStart() {
		
		return getPrevYearStart(getTime());
			
	}	

	/******************************************************************/
	
	public long getTimeofDay(long time) {
		
		return time - getDayStart(time);
			
	}	

	public long getTimeofDay() {
		
		return getTimeofDay(getTime());
			
	}	

	/******************************************************************/
	
	public long getDate(int day, int month, int year, int hour, int minute) {
		
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTimeInMillis();

	}	

	/******************************************************************/
	
	public long getDate(String date) {
		return getDate(date, '/');
	}		

	
	public long getDate(String date, char separator) {
		
		long ret = -1;
		
		int slash1 = date.indexOf(separator);
		if(slash1 != -1) {
			int slash2 = date.indexOf(separator, slash1+1);
			if(slash2 != -1) {
				
				try {
					
					String dayStr = date.substring(0, slash1);
					String monthStr = date.substring(slash1+1, slash2);
					String yearStr = date.substring(slash2+1);
					
					int day = Integer.parseInt(dayStr);
					int month = Integer.parseInt(monthStr)-1;
					
					int year = Integer.parseInt(yearStr);
					if(year < 1000) {
						year += 2000;
					}
					
					ret = getDate(day, month, year, 0 ,0);
					
				} catch(Exception ex) {					
				}
			}			
		}
		
		return ret;

	}	

	/******************************************************************/
	
	public long getDate(int day, int hour, int minute) {
		return getDate(day * DAY, hour, minute);
	}	

	/******************************************************************/
	
	public long getDate(long dayStart, int hour, int minute) {
		
		long ret = dayStart + hour * HR + minute * MIN;
		
		return ret;
		
	}	

	/******************************************************************/
	
	public long getDate(int day, int month, int hour, int minute) {
		
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return getDate(cal.get(Calendar.DAY_OF_YEAR), hour, minute);

	}	

	/******************************************************************/
	
	public Date getDate(long day) {
		
		return new Date(day) ;
		
	}	

	public Date getDate() {
		
		return getDate(getTime()) ;
		
	}	

	/******************************************************************/
	
	public static long getTime() {
		
		return System.currentTimeMillis() ;
		
	}	

	/******************************************************************/
	
	public long getDayEnd() {
		
		return getDayEnd(getTime());
	
	}	

	/******************************************************************/
	
	public long getDayEnd(long time) {
		
		return getDayStart(time) + DAY - SEC;
	
	}	

	/******************************************************************/
	
	public long getWeekEnd() {
		
		return getWeekEnd(getTime());
	
	}	

	/******************************************************************/
	
	public long getWeekEnd(long time) {
		
		return getWeekStart(time) + WEEK - SEC;
	
	}	

	/******************************************************************/
	
	public long getMonthEnd() {
		
		return getMonthEnd(getTime());
	
	}	

	/******************************************************************/
	
	public long getMonthEnd(long time) {
		
		return getNextMonthStart(time) - SEC;
	
	}	

	/******************************************************************/
	
	public String displayDate(long time) {
		
		String ret = "";
		
		setDate(time);
		ret = getCalendarDayName() + ", " + getCalendarDayOfMonth() + " " + getCalendarMonthName()+ " " + getCalendarYear();
			
		return ret;
			
	}
	
	public static String displayDate() {
		
		String ret = "";
		
		IDCCalendar cal = new IDCCalendar();
		ret = cal.displayDate(getTime());
			
		return ret;
		
	}
		
	/******************************************************************/

	public String displayDateShort(long time) {
		return displayDateShort(time, "/");
	}		
	
	public String displayDateShort(long time, String separator) {
		
		String ret = "";
		
		setDate(time);
		ret = getCalendarDayOfMonth() + separator + (getCalendarMonth() + 1) + separator + getCalendarYear();
			
		return ret;
		
	}
		
	public static String displayDateShort() {
		
		String ret = "";
		
		IDCCalendar cal = new IDCCalendar();
		ret = cal.displayDateShort(getTime());
			
		return ret;
		
	}
		
	public static String displayDateShortStatic(long time) {
		
		String ret = "";
		
		IDCCalendar cal = new IDCCalendar();
		ret = cal.displayDateShort(time);
			
		return ret;
		
	}
		
	/******************************************************************/
	
	public String displayTimeDate(long time) {
		
		String ret = "";
		
		setDate(time);
		ret = getTimeString(getCalendarHour()) + ":" + getTimeString(getCalendarMinute()) + ", " + displayDate(time);
			
		return ret;
			
	}
		
	/******************************************************************/
	
	public String displayTimeDateSeconds(long time) {
		
		String ret = "";
		
		setDate(time);
		ret = getTimeString(getCalendarHour()) + ":" + getTimeString(getCalendarMinute())  + ":" + getTimeString(getCalendarSecond())+ ", " + displayDate(time);
			
		return ret;
			
	}
		
	/******************************************************************/
	
	public static String displayTimeDate() {
		
		String ret = "";
		
		IDCCalendar cal = new IDCCalendar();
		ret = cal.displayTimeDate(getTime());
			
		return ret;
		
	}
		
	/******************************************************************/
	
	public String displayTimeDateShort(long time) {
		
		String ret = "";
		
		setDate(time);
		ret = getTimeString(getCalendarHour()) + ":" + getTimeString(getCalendarMinute()) + ", " + displayDateShort(time);
			
		return ret;
			
	}
		
	/******************************************************************/
	
	public String displayTime2(long time) {
		
		String ret = "";
		
		setDate(time);
		ret = getTimeString(getCalendarHour()) + ":" + getTimeString(getCalendarMinute());
			
		return ret;
			
	}
		
	/******************************************************************/
	
	public static String displayTime(long time) {
		
		String ret = "";
	
		long h = time / HR;
		long m = (time % HR) / MIN;
		
		ret = getTimeString(h) + ":" + getTimeString(m);
			
		return ret;
		
	}
	
	/******************************************************************/
	
	public String displayWeek(long time) {
		
		
		long weekStart =getWeekStart(time); 
		
		setDate(weekStart);
			
		return "Week starting " + displayDate(weekStart) + " (" + cal.get(Calendar.WEEK_OF_YEAR) + ")";
			
	}
		
	/******************************************************************/
	
	public String displayMonth(long time) {
		
		setDate(getMonthStart(time));
			
		return getCalendarMonthName() + " " + getCalendarYear();
			
	}
		
	/******************************************************************/
	
	public static String getTimeString(long time) {
		
		String ret = "" + time;
		
		if(ret.length()==1) {
			ret = "0" + ret;
		}
			
		return ret;
		
	}
	
	/******************************************************************/
	
	public long getAbsoluteDate(long day) {
			
		return day + base;
				
	}	

	/******************************************************************/
	
	public long getRelativeDate(long day) {
			
		return day - base;
				
	}	

	/******************************************************************/
	
	public void setDate(long date) {
			
		cal.setTimeInMillis(date);
				
	}	

	/******************************************************************/
	
	public void setTodaysDate() {
			
		setDate(getTime());
				
	}	

	/******************************************************************/
	
	public int getDay(long date) {
		setDate(date);
		return getCalendarDay();
	}
	
	public int getDay() {
		return getDay(getTime());
	}
	
	public int getCalendarDay() {
		 return cal.get(Calendar.DAY_OF_MONTH);
	}	

	/******************************************************************/
	
	public int getDayOfWeek(long date) {
		setDate(date);
		return getCalendarDayOfWeek();
	}
	
	public int getDayOfWeek() {
		return getDayOfWeek(getTime());
	}
	
	public int getCalendarDayOfWeek() {
		 return cal.get(Calendar.DAY_OF_WEEK);
	}	

	/******************************************************************/
	
	public int getDayOfMonth(long date) {
		setDate(date);
		return getCalendarDayOfMonth();
	}
	
	public int getDayOfMonth() {
		return getDayOfMonth(getTime());
	}
	
	public int getCalendarDayOfMonth() {
		 return cal.get(Calendar.DAY_OF_MONTH);
	}	

	/******************************************************************/
	
	public int getDayOfYear(long date) {
		setDate(date);
		return getCalendarDayOfYear();
	}
	
	public int getDayOfYear() {
		return getDayOfMonth(getTime());
	}
	
	public int getCalendarDayOfYear() {
		 return cal.get(Calendar.DAY_OF_YEAR);
	}	

	/******************************************************************/
	
	public int getMonth(long date) {
		setDate(date);
		return getCalendarMonth();
	}
	
	public int getMonth() {
		return getMonth(getTime());
	}
	
	public int getCalendarMonth() {
		 return cal.get(Calendar.MONTH);
	}	

	/******************************************************************/
	
	public int getWeek(long date) {
		setDate(date);
		return getCalendarWeek();
	}
	
	public int getWeek() {
		return getWeek(getTime());
	}
	
	public int getCalendarWeek() {
		 return cal.get(Calendar.WEEK_OF_YEAR);
	}	

	/******************************************************************/
	
	public int getYear(long date) {
		setDate(date);
		return getCalendarYear();
	}
	
	public int getYear() {
		return getYear(getTime());
	}
	
	public int getCalendarYear() {
		 return cal.get(Calendar.YEAR);
	}	

	/******************************************************************/
	
	public int getHour(long date) {
		setDate(date);
		return getCalendarHour();
	}
	
	public int getHour() {
		return getHour(getTime());
	}
	
	public int getCalendarHour() {
		 return cal.get(Calendar.HOUR_OF_DAY);
	}	

	/******************************************************************/
	
	public int getMinute(long date) {
		setDate(date);
		return getCalendarMinute();
	}
	
	public int getMinute() {
		return getMinute(getTime());
	}
	
	public int getCalendarMinute() {
		 return cal.get(Calendar.MINUTE);
	}	

	public int getCalendarSecond() {
		 return cal.get(Calendar.SECOND);
	}	

	/******************************************************************/
	
	public String getDayName(long date) {
		setDate(date);
		return getCalendarDayName();
	}
	
	public String getDayName() {
		 return getDayName(getTime());
	}	

	public String getCalendarDayName() {
		 return getDayName(cal.get(Calendar.DAY_OF_WEEK)-1);
	}	

	public String getDayName(int i) {
		return dayNames[i];
	}
	
	/******************************************************************/
	
	public String getMonthName(long date) {
		setDate(date);
		return getCalendarMonthName();
	}
	
	public String getMonthName() {
		 return getMonthName(getTime());
	}	

	public String getCalendarMonthName() {
		 return getMonthName(getCalendarMonth());
	}	

	public String getMonthName(int i) {
		return monthNames[i];
	}
	
	/******************************************************************/
	
	public long getBase() {
		return base;
	}
	
	/******************************************************************/
	
	public static long roundMinute(long time) {
		
		long ret = time / SEC * SEC;
		ret = ret / MIN * MIN;

		return ret;
		
	}
	
	/******************************************************************/
	
	public static String getDaysHoursMinutesString(long time) {
		
		String ret = "";
		
		time = time / MIN;
		
		long days = 0;
		long hours = 0;
		long minutes = time;

		if(minutes >= 60) {
			hours = time / 60;
			minutes = time - (hours * 60);
		}
		
		if(hours >= 24) {
			days = hours / 24;
			hours = hours - (days * 24);
			ret += days + "d";
		}
		
		if(hours > 0) {
			if(ret.length() > 0) {
				ret += " ";
			}
			ret += hours + "h";
		}
		
		if(minutes > 0) {
			if(ret.length() > 0) {
				ret += " ";
			}
			ret += minutes + "mn";
		}
		
		return ret;
		
	}
	
	/************************************************************************************************/

	public static Long parseDuration(String strValue) {

		long ret = 0;
		
		int nextx = 0;
		int dx = strValue.indexOf("d");
		int hx = strValue.indexOf("h");
		int mx = strValue.indexOf("mn");
		
		if(dx != -1) {
			String ds = strValue.substring(0, dx).trim();
			ret += Integer.parseInt(ds) * 24;
			nextx = dx + 1;
		}
		
		if(hx != -1) {
			String hs = strValue.substring(nextx, hx).trim();
			ret += Integer.parseInt(hs);
			nextx = hx + 1;
		}

		ret *= 60;
		
		if(mx != -1) {
			String ms = strValue.substring(nextx, mx).trim();
			ret += Integer.parseInt(ms);
		}
		
		ret *= MIN;

		return new Long(ret);
	
	}

}
	
