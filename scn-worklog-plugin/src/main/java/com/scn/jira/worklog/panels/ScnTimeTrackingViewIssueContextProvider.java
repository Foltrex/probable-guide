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
import java.util.Map;

public class ScnTimeTrackingViewIssueContextProvider extends TimeTrackingViewIssueContextProvider
{
    private static final String HAS_DATA = "hasData";

    private static final String EPIC_ISSUE_TYPE_NAME = "Epic";

    private final JiraAuthenticationContext jaContext;
    private final TimetrackingHelper timetrackingHelper;

    @Inject
    public ScnTimeTrackingViewIssueContextProvider(
        JiraAuthenticationContext authenticationContext,
        TimetrackingHelper timetrackingHelper
    )
    {
        super(authenticationContext, ComponentAccessor.getComponent(AggregateTimeTrackingCalculatorFactory.class),
            ComponentAccessor.getComponent(TimeTrackingGraphBeanFactory.class));

        this.jaContext = authenticationContext;
        this.timetrackingHelper = timetrackingHelper;
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> params)
    {
        Map<String, Object> context = JiraVelocityUtils.getDefaultVelocityParams(params, jaContext);
        context.put("i18n", jaContext.getI18nHelper());

        Issue issue = (Issue)context.get("issue");
        IScnExtendedIssue extIssue = timetrackingHelper.getExtendedIssue(issue);

        TimeTrackingGraphBean ttGraphBean = timetrackingHelper.getGraphBean(
            extIssue.getOriginalEstimate(),
            extIssue.getEstimate(),
            extIssue.getTimeSpent());
        context.put("timeTrackingGraphBean", ttGraphBean);

        IssueType issueType = issue.getIssueType();
        if (issueType != null && EPIC_ISSUE_TYPE_NAME.equalsIgnoreCase(issueType.getName())) {
            context.put("isEpicIssue", true);

            AggregateTimeTrackingBean aggBeanWithTasks = timetrackingHelper.getAggregateTimeTrackingBeanWithTasks(extIssue);
            TimeTrackingGraphBean aggGraphBeanWithTasks = timetrackingHelper.getGraphBean(
                aggBeanWithTasks.getOriginalEstimate(),
                aggBeanWithTasks.getRemainingEstimate(),
                aggBeanWithTasks.getTimeSpent());
            context.put("aggregateTimeTrackingGraphBeanWithTasks", aggGraphBeanWithTasks);

            AggregateTimeTrackingBean aggBeanWithTaskAdnSubtasks = timetrackingHelper.getAggregateTimeTrackingBeanWithTasksAndSubtask(extIssue);
            TimeTrackingGraphBean aggGraphBeanWithTasksAndSubtasks = timetrackingHelper.getGraphBean(
                aggBeanWithTaskAdnSubtasks.getOriginalEstimate(),
                aggBeanWithTaskAdnSubtasks.getRemainingEstimate(),
                aggBeanWithTaskAdnSubtasks.getTimeSpent());
            context.put("aggregateTimeTrackingGraphBeanWithTasksAndSubtasks", aggGraphBeanWithTasksAndSubtasks);

            context.put(HAS_DATA, aggGraphBeanWithTasks.hasData() || aggGraphBeanWithTasksAndSubtasks.hasData());
        }

        AggregateTimeTrackingBean aggBean = timetrackingHelper.getAggregateTimeTrackingBean(extIssue);
        if (aggBean.getSubTaskCount() > 0)
        {
            TimeTrackingGraphBean aggGraphBean = timetrackingHelper.getGraphBean(
                aggBean.getOriginalEstimate(),
                aggBean.getRemainingEstimate(),
                aggBean.getTimeSpent());
            context.put("aggregateTimeTrackingGraphBean", aggGraphBean);;
            context.putIfAbsent(HAS_DATA, ttGraphBean.hasData() || aggGraphBean.hasData());
        }
        else
        {
            context.putIfAbsent(HAS_DATA, ttGraphBean.hasData());
        }

        context.put("scn", "scn_");

        return context;
    }
}
