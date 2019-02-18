package com.scn.jira.util;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.user.ApplicationUsers;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.collections.map.ListOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchRequestValuesGenerator implements ValuesGenerator {
    private static final Logger log = LoggerFactory.getLogger(SearchRequestValuesGenerator.class);

    public SearchRequestValuesGenerator() {
    }

    public Map getValues(Map params) {
        Map savedFilters = null;
        User u = (User)params.get("User");
        Collection<SearchRequest> savedFiltersList = ((SearchRequestService)ComponentAccessor.getComponent(SearchRequestService.class)).getFavouriteFilters(ApplicationUsers.from(u));
        savedFilters = new ListOrderedMap();
        Iterator i$ = savedFiltersList.iterator();

        while(i$.hasNext()) {
            SearchRequest request = (SearchRequest)i$.next();
            savedFilters.put(request.getId().toString(), request.getName());
        }

        return savedFilters;
    }
}