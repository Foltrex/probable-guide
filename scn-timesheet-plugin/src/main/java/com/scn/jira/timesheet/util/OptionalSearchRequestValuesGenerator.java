package com.scn.jira.timesheet.util;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OptionalSearchRequestValuesGenerator extends SearchRequestValuesGenerator {
    public OptionalSearchRequestValuesGenerator(SearchRequestService searchRequestService, JiraAuthenticationContext jiraAuthenticationContext) {
        super(searchRequestService, jiraAuthenticationContext);
    }

    public Map<String, String> getValues(Map arg0) {
        Map<String, String> values = super.getValues(arg0);
        values.put("", "");
        return values;
    }
}
