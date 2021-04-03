package com.scn.jira.timesheet.util;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SearchRequestValuesGenerator implements ValuesGenerator<String> {
    private final SearchRequestService searchRequestService = ComponentAccessor.getComponent(SearchRequestService.class);
    private final JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();

    @Override
    public Map<String, String> getValues(Map params) {
        final ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
        Collection<SearchRequest> savedFiltersList = searchRequestService.getFavouriteFilters(user);
        Map<String, String> savedFilters = new ListOrderedMap<>();
        savedFilters.put("", "None");
        for (SearchRequest request : savedFiltersList) {
            savedFilters.put(request.getId().toString(), request.getName());
        }

        return savedFilters;
    }
}
