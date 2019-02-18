package com.scn.jira.plugin.report.timesheet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.priority.Priority;

public class PrioritiesValuesGenerator implements ValuesGenerator
{
	public Map<String, Object> getValues(Map arg0)
	{
		Map<String, Object> values = new TreeMap<String, Object>();
		values.put("", "");
		ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
		
		Collection<Priority> priorities = constantsManager.getPriorityObjects();
		
		for (Iterator<Priority> i = priorities.iterator(); i.hasNext();)
		{
			Priority priority = (Priority) i.next();
			values.put(priority.getId(), priority.getName());
		}
		return values;
	}
}