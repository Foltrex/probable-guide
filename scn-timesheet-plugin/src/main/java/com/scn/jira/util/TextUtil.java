package com.scn.jira.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.AffectedVersionsSystemField;
import com.atlassian.jira.issue.fields.ComponentsSystemField;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FixVersionsSystemField;
import com.atlassian.jira.issue.fields.IssueTypeSystemField;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.ofbiz.OfBizValueWrapper;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.OutlookDate;

public class TextUtil {
	
	//private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TextUtil.class);
	
	private double hoursPerDay;
	private double daysPerWeek;
	private ResourceBundle resourceBundle;
	private NumberFormat decimalFormat;
	private NumberFormat percentFormat;
	private Pattern servletUrlPattern;
	private DateFormat dateFormat1;
	private DateFormat dateFormat2;
	
	private TextUtil() {
	}

	public TextUtil(I18nBean i18nBean) {
		ApplicationProperties ap = ComponentAccessor.getApplicationProperties();
		this.hoursPerDay = new Double(ap.getDefaultBackedString("jira.timetracking.hours.per.day"))
				.doubleValue();
		this.daysPerWeek = new Double(ap.getDefaultBackedString("jira.timetracking.days.per.week"))
				.doubleValue();
		this.resourceBundle = i18nBean.getDefaultResourceBundle();
		this.decimalFormat = NumberFormat.getInstance(i18nBean.getLocale());
		this.percentFormat = NumberFormat.getPercentInstance(i18nBean.getLocale());
		this.servletUrlPattern = Pattern.compile("^(.+?)://(.+?)/(.+)$");
		String formatDay1 = ap.getDefaultBackedString("jira.timesheet.plugin.dayFormat1");
		if (formatDay1 == null || formatDay1.isEmpty()) {
			formatDay1 = "E";
		}
		String formatDay2 = ap.getDefaultBackedString("jira.timesheet.plugin.dayFormat2");
		if (formatDay2 == null || formatDay2.isEmpty()) {
			formatDay2 = "d/MMM";
		}
		this.dateFormat1 = new SimpleDateFormat(formatDay1, i18nBean.getLocale());
		this.dateFormat2 = new SimpleDateFormat(formatDay2, i18nBean.getLocale());

		String decimalSeparator = ap.getDefaultBackedString("jira.timesheet.plugin.decimalSeparator");
		if ((decimalSeparator != null && !decimalSeparator.isEmpty()) && (this.decimalFormat instanceof DecimalFormat)) {
			DecimalFormatSymbols dfs = ((DecimalFormat) this.decimalFormat).getDecimalFormatSymbols();
			dfs.setDecimalSeparator(decimalSeparator.charAt(0));
			((DecimalFormat) this.decimalFormat).setDecimalFormatSymbols(dfs);
		}
		
	}

	public String formatDate(Date date) {
		return this.dateFormat2.format(date);
	}
	
	public String getWeekDay(Date date) {
		return this.dateFormat1.format(date);
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

	public String expandUrl(HttpServletRequest req, String url) {
		String path = req.getRequestURL().toString();
		Matcher m = this.servletUrlPattern.matcher(path);
		if (m.matches()) {
			return m.group(1) + "://" + m.group(2) + req.getContextPath() + url;
		}
		return url;
	}

	public String getPercents(long value, long hundred) {
		if (hundred == 0L) {
			return "&nbsp;";
		}
		float percents = (float) value * 100.0F / (float) hundred;
		return this.percentFormat.format(percents);
	}

	public static String getFieldValue(String groupByFieldID, Issue issue, OutlookDate outlookDate) {
		Field groupByField = ComponentAccessor.getFieldManager().getField(groupByFieldID);

		String fieldValue = null;
		if (groupByField instanceof CustomField) {
			Object value = issue.getCustomFieldValue((CustomField) groupByField);

			if (value != null) {
				if (groupByField instanceof CustomFieldStattable) {
					StatisticsMapper sm = ((CustomFieldStattable) groupByField)
							.getStatisticsMapper((CustomField) groupByField);

					fieldValue = sm.getValueFromLuceneField(value.toString()).toString();
				}
				if (value instanceof List)
					fieldValue = getMultiValue((List) value);
				else if (value instanceof Date)
					fieldValue = outlookDate.format((Date) value);
				else
					fieldValue = value.toString();
			}
		} else if (groupByField instanceof ComponentsSystemField) {
			fieldValue = getMultiValue(issue.getComponentObjects());
		} else if (groupByField instanceof AffectedVersionsSystemField) {
			fieldValue = getMultiValue(issue.getAffectedVersions());
		} else if (groupByField instanceof FixVersionsSystemField) {
			fieldValue = getMultiValue(issue.getFixVersions());
		} else if (groupByField instanceof IssueTypeSystemField) {
			fieldValue = issue.getIssueTypeObject().getNameTranslation();
		} else {
			try {
				fieldValue = issue.getString(groupByFieldID);
			} catch (RuntimeException e) {
				fieldValue = "FieldTypeValueNotApplicableForGrouping";
			}

		}

		if ((fieldValue == null) || (fieldValue.trim().length() == 0)) {
			fieldValue = "NoValueForFieldOnIssue";
		}

		return fieldValue;
	}

	private static String getMultiValue(Collection values) {
		StringBuffer fieldValue = new StringBuffer();
		for (Iterator i = values.iterator(); i.hasNext();) {
			Object o = i.next();
			String value;
			if (o instanceof Map) {
				Map map = (Map) o;

				value = (String) map.get("name");
			} else {
				if (o instanceof OfBizValueWrapper) {
					OfBizValueWrapper map = (OfBizValueWrapper) o;
					value = map.getString("name");
				} else {
					value = o.toString();
				}
			}
			if (fieldValue.length() != 0) {
				fieldValue.append(", ");
			}
			fieldValue.append(value);
		}
		return fieldValue.toString();
	}

	public static String getUnquotedString(String s) {
		StringBuffer r = new StringBuffer(s.length());
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if ((c == '\'') || (c == '{')) {
				r.append("'");
			}
			r.append(c);
		}
		return r.toString();
	}

	public static String getFieldName(String fieldID) {
		Field groupByField = ComponentAccessor.getFieldManager().getField(fieldID);
		return groupByField.getName();
	}
}