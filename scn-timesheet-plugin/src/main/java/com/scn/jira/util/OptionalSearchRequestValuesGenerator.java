package com.scn.jira.util;

import java.util.Map;

public class OptionalSearchRequestValuesGenerator extends SearchRequestValuesGenerator
{
	public Map getValues(Map arg0)
	{
		Map values = super.getValues(arg0);
		values.put("", "");
		return values;
	}
}