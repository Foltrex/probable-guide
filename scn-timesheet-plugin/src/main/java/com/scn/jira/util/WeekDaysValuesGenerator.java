package com.scn.jira.util;

import java.util.Map;

import org.apache.commons.collections.map.LinkedMap;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

@SuppressWarnings("rawtypes")
public class WeekDaysValuesGenerator implements ValuesGenerator {
	@Override
	public Map<Long, String> getValues(Map params) {
		JiraAuthenticationContext authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
		I18nHelper i18n = authenticationContext.getI18nHelper();

		@SuppressWarnings("unchecked")
		Map<Long, String> weekDays = new LinkedMap();
		weekDays.put(new Long(0L), i18n.getText("com.scn.jira.util.weekdays.today"));
		weekDays.put(new Long(2L), i18n.getText("com.scn.jira.util.weekdays.monday"));
		weekDays.put(new Long(3L), i18n.getText("com.scn.jira.util.weekdays.tuesday"));
		weekDays.put(new Long(4L), i18n.getText("com.scn.jira.util.weekdays.wednesday"));
		weekDays.put(new Long(5L), i18n.getText("com.scn.jira.util.weekdays.thursday"));
		weekDays.put(new Long(6L), i18n.getText("com.scn.jira.util.weekdays.friday"));
		weekDays.put(new Long(7L), i18n.getText("com.scn.jira.util.weekdays.saturday"));
		weekDays.put(new Long(1L), i18n.getText("com.scn.jira.util.weekdays.sunday"));
		return weekDays;
	}
}