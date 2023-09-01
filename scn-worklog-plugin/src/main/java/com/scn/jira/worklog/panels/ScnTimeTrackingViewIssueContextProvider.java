package com.scn.jira.worklog.panels;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.plugin.viewissue.TimeTrackingViewIssueContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.web.bean.TimeTrackingGraphBean;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.helper.TimetrackingHelper;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class ScnTimeTrackingViewIssueContextProvider extends TimeTrackingViewIssueContextProvider {
    private static final String HAS_DATA = "hasData";

    private static final String EPIC_ISSUE_TYPE_NAME = "Epic";

    private final JiraAuthenticationContext jaContext;
    private final TimetrackingHelper timetrackingHelper;

    @Inject
    public ScnTimeTrackingViewIssueContextProvider(
        JiraAuthenticationContext authenticationContext,
        TimetrackingHelper timetrackingHelper
    ) {
        super(authenticationContext, ComponentAccessor.getComponent(AggregateTimeTrackingCalculatorFactory.class),
            ComponentAccessor.getComponent(TimeTrackingGraphBeanFactory.class));

        this.jaContext = authenticationContext;
        this.timetrackingHelper = timetrackingHelper;
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> params) {
        Map<String, Object> context = new HashMap<>(super.getContextMap(params));
        context.put("i18n", jaContext.getI18nHelper());

        Issue issue = (Issue)context.get("issue");
        IScnExtendedIssue extIssue = timetrackingHelper.getExtendedIssue(issue);

        IssueType issueType = issue.getIssueType();
        if (issueType != null && EPIC_ISSUE_TYPE_NAME.equalsIgnoreCase(issueType.getName())) {
            timetrackingHelper.putEpicTimetrackingBeansIntoContext(context, extIssue);
        }

        context.put("scn", "scn_");
        return context;
    }
}
