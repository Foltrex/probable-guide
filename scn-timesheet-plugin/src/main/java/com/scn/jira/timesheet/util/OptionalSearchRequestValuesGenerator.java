package com.scn.jira.timesheet.util;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OptionalSearchRequestValuesGenerator extends SearchRequestValuesGenerator {
    public Map<String, String> getValues(Map arg0) {
        Map<String, String> values = super.getValues(arg0);
        return values;
    }
}
