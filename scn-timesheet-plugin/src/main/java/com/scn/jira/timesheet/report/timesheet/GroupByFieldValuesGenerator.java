package com.scn.jira.timesheet.report.timesheet;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.SearchableField;
import com.scn.jira.timesheet.util.TextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Component
@RequiredArgsConstructor
public class GroupByFieldValuesGenerator implements ValuesGenerator<String> {
    private final FieldManager fieldManager = ComponentAccessor.getFieldManager();
    private final ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();

    @Override
    public Map<String, String> getValues(Map arg0) {
        Map<String, String> values = new ListOrderedMap<>();
        values.put("", "None");
        Set<SearchableField> fields = fieldManager.getAllSearchableFields();

        Set<SearchableField> sortedFields = new TreeSet<>(Comparator.comparing(Field::getName));
        sortedFields.addAll(fields);

        String groupByFieldsP = applicationProperties.getDefaultString("jira.plugin.timesheet.groupbyfields");

        Collection<String> groupByFields = null;
        if (groupByFieldsP != null)
            groupByFields = Arrays.asList(groupByFieldsP.split(","));

        for (Field field : sortedFields) {
            if ((groupByFields == null) || (groupByFields.contains(field.getId()))
                || (groupByFields.contains(field.getName()))) {
                values.put(field.getId(), TextUtil.getUnquotedString(field.getName()));
            }
        }

        return values;
    }
}
