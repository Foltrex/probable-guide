package com.scn.jira.timesheet.util;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.user.ApplicationUser;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.collections.map.ListOrderedMap;

@SuppressWarnings("rawtypes")
public class SearchRequestValuesGenerator implements ValuesGenerator {
	@Override
	public Map<String, String> getValues(Map params) {
		final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		Collection<SearchRequest> savedFiltersList = ((SearchRequestService) ComponentAccessor
				.getComponent(SearchRequestService.class)).getFavouriteFilters(user);
		@SuppressWarnings("unchecked")
		Map<String, String> savedFilters = new ListOrderedMap();
		Iterator i$ = savedFiltersList.iterator();
		while (i$.hasNext()) {
			SearchRequest request = (SearchRequest) i$.next();
			savedFilters.put(request.getId().toString(), request.getName());
		}

		return savedFilters;
	}
}
