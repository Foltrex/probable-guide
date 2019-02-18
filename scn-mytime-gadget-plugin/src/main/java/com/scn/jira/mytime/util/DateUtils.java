package com.scn.jira.mytime.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.core.util.DateUtils.Duration;

/**
 * Created by Paradinets Tatsiana. Date: 25.03.2011 Time: 19:30:34
 */

public class DateUtils {
	protected static Logger logger = Logger.getLogger(DateUtils.class);
	
	public static String formatStringDayNumber = "d";
	public static String formatStringDay = "yyyy-MM-dd";
	public static String formatStringDate = "dd-MM-yyyy";
	public static String formatStringDateDb = "yyyyMMdd";
	public static String monthlyFormatStringDate = "MMMM yyyy";
	
	public static String blackDay = " black";
	public static String greenDay = " green";
	public static String redDay = " red";
	
	public static List<Date> getDatesListDate(Date startDate, Date endDate) {
		
		List<Date> dates = new ArrayList<Date>();
		
		Date date = startDate;
		while (date.before(endDate)) {
			dates.add(date);
			date = DateUtils.getStartDate(1, date);
		}
		return dates;
	}
	
	public static List<String> getStringListDate(Date startDate, Date endDate, String format) {
		
		List<String> dates = new ArrayList<String>();
		
		Date date = startDate;
		while (date.before(endDate)) {
			dates.add(DateUtils.stringDate(date, format));
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
	
	public static String stringDate(Date date, String format) {
		DateFormat df = new SimpleDateFormat(format);
		
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
			// Sun Apr 10 2011 00:00:00 GMT+0300
			// 2011-04-12T00:00:00
			try {
				SimpleDateFormat formatter = new SimpleDateFormat(formatStringDate);
				date = formatter.parse(day);
				// logger.info("Received date:" + date);
			} catch (ParseException e) {
				logger.info("Date was not parsed:" + day);
				logger.error(e);
			}
		}
		return date;
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
	//	cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		// start of the next week
		cal.add(Calendar.WEEK_OF_YEAR, i);
		return cal.getTime();
	}
	
	public static Date getWeekEndDate(int i, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getWeekStartDate(i, date));
		System.out.println("getWeekStartDate: " +getWeekStartDate(i, date));
		cal.add(Calendar.DAY_OF_MONTH, 6);
		cal.set(Calendar.AM_PM, 0);
		cal.set(Calendar.HOUR, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static Date getMonthStartDateFromWeekStart(int i, Date date) {
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
		return getWeekStartDate(0, cal.getTime());
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
	
	public static Date getMonthEndDateToWeekEnd(int i, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, i);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.AM_PM, 0);
		cal.set(Calendar.HOUR, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return getWeekEndDate(0, cal.getTime());
	}
	
	public static String timeToString(Long seconds) {
		String dateString = "";
		if (seconds == 0)
			return "   ";
		long restSeconds = seconds;
		if (seconds >= Duration.HOUR.getSeconds()) {
			long hours = seconds / Duration.HOUR.getSeconds();
			
			if (hours == 0) {
				dateString = "0";
			}
			if (hours > 0 && hours < 10) {
				dateString = "" + hours;
			}
			if (hours > 9) {
				dateString = String.valueOf(hours);
			}
			
			restSeconds = seconds - hours * Duration.HOUR.getSeconds();
		}
		else {
			dateString = "00";
			restSeconds = seconds;
		}
		dateString = dateString + ":";
		if (restSeconds >= Duration.MINUTE.getSeconds()) {
			long minute = restSeconds / Duration.MINUTE.getSeconds();
			if (minute == 0) {
				dateString = dateString + "00";
			}
			if (minute > 0 && minute < 10) {
				dateString = dateString + "0" + minute;
			}
			if (minute > 9) {
				dateString = dateString + String.valueOf(minute);
			}
		}
		else {
			dateString = dateString + "00";
		}
		return dateString;
	}
	
	public static String getDayColor(Long seconds, boolean isTotal) {
		if (seconds == 0)
			return "";
		long hours = seconds / Duration.HOUR.getSeconds();
		long restSeconds = seconds - hours * Duration.HOUR.getSeconds();
		int norma = isTotal ? 40 : 8;
		if (hours == norma && restSeconds == 0) {
			return "";
		}
		if (hours >= norma) {
			return greenDay;
		}
		if (hours < norma) {
			return redDay;
		}
		return "";
	}
	
}