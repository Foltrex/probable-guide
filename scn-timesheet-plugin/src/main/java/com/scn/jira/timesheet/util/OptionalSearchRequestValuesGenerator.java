package com.scn.jira.timesheet.util;

import java.util.Map;

@SuppressWarnings("rawtypes")
public class OptionalSearchRequestValuesGenerator extends SearchRequestValuesGenerator {
	public Map<String, String> getValues(Map arg0) {
		Map<String, String> values = super.getValues(arg0);
		values.put("", "");
		return values;
	}
}
