package com.scn.jira.plugin.report.timesheet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.map.LinkedMap;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.Field;
import com.scn.jira.util.TextUtil;

public class GroupByFieldValuesGenerator implements ValuesGenerator
{
	public Map<String, Object> getValues(Map arg0)
	{
		Map<String, Object> values = new LinkedMap();
		values.put("", "");
		
		Set fields = ComponentAccessor.getFieldManager().getAllSearchableFields();
		
		Set sortedFields = new TreeSet(new Comparator()
		{
			public int compare(Object o, Object other)
			{
				Field f = (Field) o;
				Field otherF = (Field) other;
				return f.getName().compareTo(otherF.getName());
			}
		});
		sortedFields.addAll(fields);
		
		String groupByFieldsP = ComponentAccessor.getApplicationProperties().getDefaultString("jira.plugin.timesheet.groupbyfields");
		
		Collection groupByFields = null;
		if (groupByFieldsP != null)
		{
			groupByFields = Arrays.asList(groupByFieldsP.split(","));
		}
		
		for (Iterator i = sortedFields.iterator(); i.hasNext();)
		{
			Field field = (Field) i.next();
			if ((groupByFields == null) || (groupByFields.contains(field.getId())) || (groupByFields.contains(field.getName())))
			{
				values.put(field.getId(), TextUtil.getUnquotedString(field.getName()));
			}
		}
		
		return values;
	}
}