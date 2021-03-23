package com.scn.jira.timesheet.report.timesheet;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.priority.Priority;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
public class PrioritiesValuesGenerator implements ValuesGenerator<String> {
    @Autowired
    private final ConstantsManager constantsManager;

    @Override
    public Map<String, String> getValues(Map arg0) {
        Map<String, String> values = new TreeMap<>();
        values.put("", "");

        Collection<Priority> priorities = constantsManager.getPriorities();

        for (Priority priority : priorities) {
            values.put(priority.getId(), priority.getName());
        }
        return values;
    }
}
