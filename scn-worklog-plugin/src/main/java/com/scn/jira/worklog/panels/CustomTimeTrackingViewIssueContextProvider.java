package com.scn.jira.worklog.panels;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.plugin.viewissue.TimeTrackingViewIssueContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CustomTimeTrackingViewIssueContextProvider extends TimeTrackingViewIssueContextProvider {

    @Inject
    public CustomTimeTrackingViewIssueContextProvider(JiraAuthenticationContext authenticationContext) {
        super(authenticationContext, ComponentAccessor.getComponent(AggregateTimeTrackingCalculatorFactory.class),
                ComponentAccessor.getComponent(TimeTrackingGraphBeanFactory.class));
    }
}
