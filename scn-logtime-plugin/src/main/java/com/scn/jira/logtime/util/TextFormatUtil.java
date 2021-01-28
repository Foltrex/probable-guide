package com.scn.jira.logtime.util;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.DateUtils.Duration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;

@SuppressWarnings("unused")
public class TextFormatUtil {
	private double hoursPerDay;
	private double daysPerWeek;
	private ResourceBundle resourceBundle;
	private NumberFormat decimalFormat;
	private final DateFormat dateFormat1;
	private final DateFormat dateFormat2;
	private final DateFormat dateFormat3;
	private final DateFormat dateFormat4;
	private static final long SECONDS_IN_DAYS = Duration.DAY.getSeconds() / 3;
	private static final long SECONDS_IN_WEEK = SECONDS_IN_DAYS * 5;

	public TextFormatUtil() {
		ApplicationProperties ap = ComponentAccessor.getApplicationProperties();

		String formatDay1 = ap.getDefaultBackedString("jira.timesheet.plugin.dayFormat1");
		if (formatDay1 == null) {
			formatDay1 = "E";
		}
		String formatDay2 = ap.getDefaultBackedString("jira.timesheet.plugin.dayFormat2");
		if (formatDay2 == null) {
			formatDay2 = "d/MMM";
		}
		String formatDay3 = ap.getDefaultBackedString("jira.timesheet.plugin.dayFormat3");
		if (formatDay3 == null) {
			formatDay3 = "E  d";
		}
		String formatDay4 = ap.getDefaultBackedString("jira.timesheet.plugin.dayFormat4");
		if (formatDay4 == null) {
			formatDay4 = "d";
		}
		this.dateFormat1 = new SimpleDateFormat(formatDay1);
		this.dateFormat2 = new SimpleDateFormat(formatDay2);
		this.dateFormat3 = new SimpleDateFormat(formatDay3);
		this.dateFormat4 = new SimpleDateFormat(formatDay4);
	}

	public static String timeToString(Long seconds) {
		String dateString = "";
		if (seconds == 0)
			return "00:00";
		long restSeconds = seconds;
		if (seconds >= Duration.HOUR.getSeconds()) {
			long hours = seconds / Duration.HOUR.getSeconds();

			if (hours == 0) {
				dateString = "00";
			}
			if (hours > 0 && hours < 10) {
				dateString = "0" + hours;
			}
			if (hours > 9) {
				dateString = String.valueOf(hours);
			}

			restSeconds = seconds - hours * Duration.HOUR.getSeconds();
		} else {
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
		} else {
			dateString = dateString + "00";
		}
		return dateString;
	}

	public static String timeToString(String date) {
		String dateString = "";
		if (date.equals("0"))
			return "00:00";
		long seconds = Long.parseLong(date);
		long restSeconds = seconds;
		if (seconds >= Duration.HOUR.getSeconds()) {
			long hours = seconds / Duration.HOUR.getSeconds();

			if (hours == 0) {
				dateString = "00";
			}
			if (hours > 0 && hours < 10) {
				dateString = "0" + hours;
			}
			if (hours > 9) {
				dateString = String.valueOf(hours);
			}

			restSeconds = seconds - hours * Duration.HOUR.getSeconds();
		} else {
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
		} else {
			dateString = dateString + "00";
		}
		return dateString;
	}

	public static String timeToString2(String date) {
		String dateString = "";
		if (date.equals("0"))
			return "";
        long restSeconds = Long.parseLong(date);

		if (restSeconds >= SECONDS_IN_WEEK) {
			long weeks = restSeconds / SECONDS_IN_WEEK;
			if (weeks > 0) {
				dateString = String.valueOf(weeks) + "w";
			}
			restSeconds = restSeconds - weeks * SECONDS_IN_WEEK;
		}

		if (restSeconds >= SECONDS_IN_DAYS) {
			long days = restSeconds / SECONDS_IN_DAYS;
			if (days > 0) {
				dateString = dateString.length() == 0 ? String.valueOf(days) + "d"
						: dateString + " " + String.valueOf(days) + "d";
			}
			restSeconds = restSeconds - days * SECONDS_IN_DAYS;
		}

		if (restSeconds >= Duration.HOUR.getSeconds()) {
			long hours = restSeconds / Duration.HOUR.getSeconds();

			if (hours > 0) {
				if (dateString.length() > 0) {
					dateString = dateString + " " + hours + "h";
				} else {
					dateString = hours + "h";
				}
			}

			restSeconds = restSeconds - hours * Duration.HOUR.getSeconds();
		}

		if (restSeconds >= Duration.MINUTE.getSeconds()) {
			long minute = restSeconds / Duration.MINUTE.getSeconds();
			if (minute > 0) {
				if (dateString.length() > 0) {
					dateString = dateString + " " + minute + "m";
				} else {
					dateString = minute + "m";
				}
			}
		}
		return dateString;
	}

	public static Long string2ToTime(String timeString) {
		long timeLong = 0L;
		java.util.regex.Pattern p1 = java.util.regex.Pattern
				.compile("^\\s*(([0-9]*[0-9])w)*\\s*(([0-9]*[0-9])d)*\\s*(([0-9]*[0-9])h)?\\s*(([0-5]*[0-9])m)?\\s*$");
		Matcher m1 = p1.matcher(timeString);
		if (m1.find()) {

			String w = m1.group(2);
			String d = m1.group(4);
			String h = m1.group(6);
			String m = m1.group(8);
			timeLong = (w != null ? Long.parseLong(w) : 0) * SECONDS_IN_WEEK
					+ (d != null ? Long.parseLong(d) : 0) * SECONDS_IN_DAYS
					+ (h != null ? Long.parseLong(h) : 0) * Duration.HOUR.getSeconds()
					+ (m != null ? Long.parseLong(m) : 0) * Duration.MINUTE.getSeconds();
		}

		return timeLong;
	}

	public static Long string3ToTime(String timeString) {
		long timeLong = 0L;
		java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("^\\s*([0-9]*)\\s*$");
		Matcher m1 = p1.matcher(timeString);
		if (m1.find()) {
			String h = m1.group(1);
			timeLong = (h != null ? Long.parseLong(h) : 0) * Duration.HOUR.getSeconds();
		}

		return timeLong;
	}

	public static boolean matchesPattern3(String timeString) {
		java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("^\\s*([0-9]*)\\s*$");
		Matcher m1 = p1.matcher(timeString);
        return m1.matches();
    }

	public static boolean matchesPattern2(String timeString) {
		java.util.regex.Pattern p1 = java.util.regex.Pattern
				.compile("^\\s*(([0-9]*[0-9])w)*\\s*(([0-9]*[0-9])d)*\\s*(([0-9]*[0-9])h)?\\s*(([0-5]*[0-9])m)?\\s*$");
		Matcher m1 = p1.matcher(timeString);
        return m1.matches();
    }

	public static boolean matchesPattern1(String timeString) {
		java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("^\\s*([0-9]*[0-9]):[0-5][0-9]\\s*$");
		Matcher m1 = p1.matcher(timeString);
        return m1.matches();
    }

	public static Long stringToTime(String timeString) {
		long timeLong = 0L;
		timeString = timeString.trim();
		if (timeString.contains(":")) {
			String[] parts = timeString.split(":");
			timeLong = Long.parseLong(parts[0]) * Duration.HOUR.getSeconds()
					+ Long.parseLong(parts[1]) * Duration.MINUTE.getSeconds();
		}

		return timeLong;
	}

	public static String replaceHTMLSymbols(String stringToReplace) {
	    return Optional.ofNullable(stringToReplace).orElse("").replaceAll("<", "").replaceAll(">", "").replaceAll("#", "").replaceAll("'", "")
				.replaceAll("\"", "").replaceAll("\\$", "").replaceAll(";", "").replaceAll("\n", " ")
				.replaceAll("\r", " ");
	}

	public String formatDay(Date date) {
		return this.dateFormat1.format(date) + "<br/>" + this.dateFormat4.format(date);
		// return this.dateFormat3.format(date);
	}

	public String getPrettyDuration(long value) {
		return DateUtils.getDurationPretty(value, (int) hoursPerDay, (int) daysPerWeek, resourceBundle);
	}

	public String getPrettyHours(long value) {
		return getHours(value) + "h";
	}

	public String getHours(long value) {
		return this.decimalFormat.format((float) value / 60.0F / 60.0F);
	}
}
