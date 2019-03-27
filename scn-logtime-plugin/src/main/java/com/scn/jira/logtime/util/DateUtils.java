package com.scn.jira.logtime.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.scn.jira.logtime.representation.DayRepresentation;
import com.scn.jira.logtime.representation.WeekRepresentation;

public class DateUtils {
	protected static Logger logger = Logger.getLogger(DateUtils.class);
	
	public static String formatStringDay = "yyyy-MM-dd";
	public static String formatStringDate = "dd-MM-yyyy";
	public static String formatStringDateDb = "yyyyMMdd";
	public static String monthlyFormatStringDate = "MMMM yyyy";
	
	public static List<String> getDatesList(Date startDate, Date endDate) {
		
		List<String> dates = new ArrayList<String>();
		
		Date date = startDate;
		while (date.before(endDate)) {
			dates.add(DateUtils.shortStringDate(date));
			date = DateUtils.getStartDate(1, date);
		}
		return dates;
	}
	
	public static List<String> getDatesList(Date startDate, Date endDate, String format) {
		
		List<String> dates = new ArrayList<String>();
		
		Date date = startDate;
		while (date.before(endDate)) {
			dates.add(DateUtils.stringDate(date, format));
			date = DateUtils.getStartDate(1, date);
		}
		return dates;
	}
	
	public static int getWeekSlide(Date original, Date newDate) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(original);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(newDate);
		
		if (cal2.get(Calendar.YEAR) > cal1.get(Calendar.YEAR)) {
			return cal2.getActualMaximum(Calendar.WEEK_OF_YEAR) * (cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR))
					+ cal2.get(Calendar.WEEK_OF_YEAR) - cal1.get(Calendar.WEEK_OF_YEAR);
		}
		if (cal2.get(Calendar.YEAR) < cal1.get(Calendar.YEAR)) {
			return -cal2.getActualMaximum(Calendar.WEEK_OF_YEAR) * (cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR))
					+ cal2.get(Calendar.WEEK_OF_YEAR) - cal1.get(Calendar.WEEK_OF_YEAR);
		}
		return cal2.get(Calendar.WEEK_OF_YEAR) - cal1.get(Calendar.WEEK_OF_YEAR);
	}
	
	public static int getMonthSlide(Date original, Date newDate) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(original);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(newDate);
		if (cal2.get(Calendar.YEAR) > cal1.get(Calendar.YEAR)) {
			return (cal2.getActualMaximum(Calendar.MONTH) + 1) * (cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR)) + cal2.get(Calendar.MONTH)
					- cal1.get(Calendar.MONTH);
		}
		if (cal2.get(Calendar.YEAR) < cal1.get(Calendar.YEAR)) {
			return -(cal2.getActualMaximum(Calendar.MONTH) + 1) * (cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR)) + cal2.get(Calendar.MONTH)
					- cal1.get(Calendar.MONTH);
		}
		return cal2.get(Calendar.MONTH) - cal1.get(Calendar.MONTH);
	}
	
	public static Date getStartDate(int i, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, i);
		cal.set(Calendar.AM_PM, 0);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static Date getEndDate(int i, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, i);
		cal.set(Calendar.AM_PM, 0);
		cal.set(Calendar.HOUR, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static String stringDate(Date date, String format) {
		DateFormat df = new SimpleDateFormat(format);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return df.format(cal.getTime());
	}
	
	public static String dbStringDate(Date date) {
		DateFormat df = new SimpleDateFormat(formatStringDay);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return df.format(cal.getTime());
	}
	
	public static String shortStringDate(Date date) {
		DateFormat df = new SimpleDateFormat(formatStringDate);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return df.format(cal.getTime());
	}
	
	public static String fullStringDate(Date date) {
		DateFormat df = new SimpleDateFormat(formatStringDate);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return df.format(cal.getTime());
	}
	
	public static String formSlidePeriod(Date startDate, Date endDate, String type) {
		
		String period = "";
		
		if (type == null || type.equals("Weekly view")) {
			DateFormat df = new SimpleDateFormat(monthlyFormatStringDate);
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			period = df.format(cal.getTime());
		}
		else {
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(startDate);
			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(endDate);
			
			String fm = "";
			if (cal1.get(Calendar.MONTH) != cal2.get(Calendar.MONTH)) {
				fm = " of " + new SimpleDateFormat("MMMM").format(cal1.getTime());
				if (cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)) {
					fm = fm + " " + cal1.get(Calendar.YEAR);
				}
			}
			
			period = cal1.get(Calendar.DAY_OF_MONTH) + fm + " - " + cal2.get(Calendar.DAY_OF_MONTH) + " of "
					+ new SimpleDateFormat("MMMM").format(cal2.getTime()) + " " + cal2.get(Calendar.YEAR);
			
		}
		return period;
	}
	
	public static Date stringToDate(String day) {	
		Date date = null;
		if (day != null && !day.equals("")) {	
				
				String[] dates = day.toString().split("-");
				
				Calendar cal = Calendar.getInstance();		
				cal.setTime(new Date());				
				
				cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dates[0]) );
				cal.set(Calendar.YEAR,Integer.valueOf(dates[2]) );
				cal.set(Calendar.MONTH, Integer.valueOf(dates[1])-1);
				
				date = cal.getTime();		
		}
		return date;
	}
	
	public static String string1ToString2(String day) {
		//System.out.println("Day BEFORE "+  day);
		Date date = null;
		if (day != null && !day.equals("")) {
				
				String[] dates = day.toString().split("-");
				
				return dates[2]+"-"+dates[1]+"-"+dates[0];
			
		}
		return day;
	}
	
	
	
	
	public static List<Date> getDatesListDate(Date startDate, Date endDate) {
		
		List<Date> dates = new ArrayList<Date>();
		
		Date date = startDate;
		while (date.before(endDate)) {
			dates.add(date);
			System.out.print(DateUtils.shortStringDate(date));
			date = DateUtils.getStartDate(1, date);
		}
		
		return dates;
	}
	
	public static List<String> getStringListDate(Date startDate, Date endDate) {
		
		List<String> dates = new ArrayList<String>();
		
		Date date = startDate;
		while (date.before(endDate)) {
			dates.add(DateUtils.shortStringDate(date));
			date = DateUtils.getStartDate(1, date);
		}
		
		return dates;
	}
	
	public static Map<String, List<DayRepresentation>> getWeekMap(List<java.util.Date> datesWeek) {
		
		Map<String, List<DayRepresentation>> weeksMap = new HashMap<String, List<DayRepresentation>>();
		for (Date date : datesWeek) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int week = cal.get(Calendar.WEEK_OF_YEAR);
			
			List<DayRepresentation> days;
			days = weeksMap.get(String.valueOf(week));
			if (days == null) {
				days = new ArrayList<DayRepresentation>();
			}
			int dayOfWeek = cal.get(7);
			String isWorking = ((dayOfWeek == 7) || (dayOfWeek == 1) ? "weekendCss" : "workingCss");
			
			DayRepresentation day = new DayRepresentation(shortStringDate(date), date, isWorking, String.valueOf(week));
			days.add(day);
			weeksMap.put(String.valueOf(week), days);
		}
		
		return weeksMap;
	}
	
	public static String getDayColor(String dateString, Integer status) {
		Date date = stringToDate(dateString);
		return getDayColor(date, status);
	}
	
	public static String getDayColor(java.util.Date date, Integer status) {
		String isWorking = "";
		if (date == null)
			return isWorking;
		if(status==null || status<1 || status>3){
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int dayOfWeek = cal.get(7);
			isWorking = ((dayOfWeek == 7) || (dayOfWeek == 1) ? "weekendColor" : "");
		}else{
			isWorking = (status>1 ? "weekendColor" : "");			
		}
		
		return isWorking;
	}
	
	
	
	public static List<WeekRepresentation> getWeekRepresentationList(List<java.util.Date> datesWeek, Map<String, Integer> userMap) {
		
		List<WeekRepresentation> weekRepresentations = new ArrayList<WeekRepresentation>();
		
		int weekRem = 0;
		WeekRepresentation weekRepresentation;
		List<DayRepresentation> days = new ArrayList<DayRepresentation>();
		for (Date date : datesWeek) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.setFirstDayOfWeek(Calendar.MONDAY);
			int week = cal.get(Calendar.WEEK_OF_YEAR);
			if (weekRem != 0 && week != weekRem) {
				weekRepresentation = new WeekRepresentation(String.valueOf(weekRem), days);
				weekRepresentations.add(weekRepresentation);
				days = new ArrayList<DayRepresentation>();
			}
			int dayOfWeek = cal.get(7);
			String isWorking = ((dayOfWeek == 7) || (dayOfWeek == 1) ? "weekendCss" : "workingCss");
			if(userMap!=null){
				Integer status = userMap.get(dbStringDate(date));
				if(status!=null && status>0 && status<4){
					isWorking = (status>1 ? "weekendCss" : "workingCss");
				}
			}				
			
			DayRepresentation day = new DayRepresentation(shortStringDate(date), date, isWorking, String.valueOf(week));
			days.add(day);
			weekRem = week;
			
		}
		weekRepresentation = new WeekRepresentation(String.valueOf(weekRem), days);
		weekRepresentations.add(weekRepresentation);
		return weekRepresentations;
	}
	
	
	public static Date getWeekStartDate(int i, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of
											// day !
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		
		// get start of this week in milliseconds	
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		// start of the next week
		cal.add(Calendar.WEEK_OF_YEAR, i);
		
		return cal.getTime();
	}
	
	public static Date getWeekEndDate(int i, Date date) {
		Calendar cal = Calendar.getInstance();		
		cal.setTime(getWeekStartDate(i, date));
		cal.add(Calendar.DAY_OF_MONTH, 6);
		cal.set(Calendar.AM_PM, 0);
		cal.set(Calendar.HOUR, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static Date getMonthStartDate(int i, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of
											// day !
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		
		// get start of the month
		cal.set(Calendar.DAY_OF_MONTH, 1);
		
		// get start of the next month
		cal.add(Calendar.MONTH, i);
		
		return cal.getTime();
	}
	
	public static Date getMonthEndDate(int i, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, i);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.AM_PM, 0);
		cal.set(Calendar.HOUR, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTime();
	}
	
}