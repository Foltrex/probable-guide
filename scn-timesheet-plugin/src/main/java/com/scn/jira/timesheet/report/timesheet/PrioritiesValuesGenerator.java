package com.scn.jira.timesheet.report.timesheet;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.priority.Priority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
public class PrioritiesValuesGenerator implements ValuesGenerator<String> {
    private final ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();

    @Override
    public Map<String, String> getValues(Map arg0) {
        Map<String, String> values = new TreeMap<>();
        values.put("", "None");

        Collection<Priority> priorities = constantsManager.getPriorities();

        for (Priority priority : priorities) {
            values.put(priority.getId(), priority.getName());
        }
        return values;
    }
}
