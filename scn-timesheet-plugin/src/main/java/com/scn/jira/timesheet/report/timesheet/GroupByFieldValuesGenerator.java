package com.scn.jira.timesheet.report.timesheet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.map.LinkedMap;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.SearchableField;
import com.scn.jira.timesheet.util.TextUtil;

@SuppressWarnings("rawtypes")
public class GroupByFieldValuesGenerator implements ValuesGenerator {
	@Override
	public Map<String, Object> getValues(Map arg0) {
		@SuppressWarnings("unchecked")
		Map<String, Object> values = new LinkedMap();
		values.put("", "");

		Set<SearchableField> fields = ComponentAccessor.getFieldManager().getAllSearchableFields();

		Set<SearchableField> sortedFields = new TreeSet<SearchableField>(
				(o, other) -> o.getName().compareTo(other.getName()));
		sortedFields.addAll(fields);

		String groupByFieldsP = ComponentAccessor.getApplicationProperties()
				.getDefaultString("jira.plugin.timesheet.groupbyfields");

		Collection groupByFields = null;
		if (groupByFieldsP != null)
			groupByFields = Arrays.asList(groupByFieldsP.split(","));

		for (Iterator i = sortedFields.iterator(); i.hasNext();) {
			Field field = (Field) i.next();
			if ((groupByFields == null) || (groupByFields.contains(field.getId()))
					|| (groupByFields.contains(field.getName()))) {
				values.put(field.getId(), TextUtil.getUnquotedString(field.getName()));
			}
		}

		return values;
	}
}
