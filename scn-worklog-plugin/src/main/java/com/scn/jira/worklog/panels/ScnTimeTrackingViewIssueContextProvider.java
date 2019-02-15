package com.scn.jira.worklog.panels;

import static com.atlassian.jira.issue.util.AggregateTimeTrackingBean.addAndPreserveNull;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.plugin.viewissue.TimeTrackingViewIssueContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.TimeTrackingGraphBean;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory.Style;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.ScnExtendedIssue;

public class ScnTimeTrackingViewIssueContextProvider extends TimeTrackingViewIssueContextProvider
{
	private final JiraAuthenticationContext jaContext;
	private final PermissionManager permissionManager;
	private final IScnExtendedIssueStore eiStore;
	private final JiraDurationUtils utils;

	@Inject
	public ScnTimeTrackingViewIssueContextProvider(
			JiraAuthenticationContext authenticationContext,
			PermissionManager permissionManager,
			IScnExtendedIssueStore eiStore)
	{
		super(authenticationContext, ComponentAccessor.getComponent(AggregateTimeTrackingCalculatorFactory.class),
				ComponentAccessor.getComponent(TimeTrackingGraphBeanFactory.class));
		
		this.jaContext = authenticationContext;
		this.permissionManager = permissionManager;
		this.eiStore = eiStore;
		this.utils = Assertions.notNull("utils", ComponentAccessor.getComponent(JiraDurationUtils.class));
	}	
	
	@Override
	public Map<String, Object> getContextMap(Map<String, Object> params)
	{
		Map<String, Object> context = JiraVelocityUtils.getDefaultVelocityParams(params, jaContext);
		context.put("i18n", jaContext.getI18nHelper());
		
		Issue issue = (Issue)context.get("issue");
		IScnExtendedIssue extIssue = getExtendedIssue(issue);
		
        TimeTrackingGraphBean ttGraphBean = getGraphBean( 
        		extIssue.getOriginalEstimate(), 
        		extIssue.getEstimate(), 
        		extIssue.getTimeSpent());
        context.put("timeTrackingGraphBean", ttGraphBean);
        
        AggregateTimeTrackingBean aggBean = getAggregateTimeTrackingBean(extIssue);
        if (aggBean.getSubTaskCount() > 0)
        {
        	TimeTrackingGraphBean aggGraphBean = getGraphBean( 
        			aggBean.getOriginalEstimate(), 
        			aggBean.getRemainingEstimate(), 
        			aggBean.getTimeSpent());
        	context.put("aggregateTimeTrackingGraphBean", aggGraphBean);
        	context.put("hasData", ttGraphBean.hasData() || aggGraphBean.hasData());
        }
        else
        {
        	context.put("hasData", ttGraphBean.hasData());
        }
		
		context.put("scn", "scn_");
		
		return context;
	}
	
	@Nonnull
	private TimeTrackingGraphBean getGraphBean(Long originalEstimate, Long remainingEstimate, Long timeSpent)
	{
		Locale locale = jaContext.getI18nHelper().getLocale();
		Style style = TimeTrackingGraphBeanFactory.Style.SHORT;
		
		TimeTrackingGraphBean.Parameters params = new TimeTrackingGraphBean.Parameters(jaContext.getI18nHelper());
        params.setOriginalEstimate(originalEstimate);
        params.setRemainingEstimate(remainingEstimate);
        params.setTimeSpent(timeSpent);
        params.setTimeSpentStr(style.getDuration(timeSpent, locale, utils));
        params.setOriginalEstimateStr(style.getDuration(originalEstimate, locale, utils));
        params.setRemainingEstimateStr(style.getDuration(remainingEstimate, locale, utils));
        params.setTimeSpentTooltip(style.getTooltip(timeSpent, locale, utils));
        params.setOriginalEstimateTooltip(style.getTooltip(originalEstimate, locale, utils));
        params.setRemainingEstimateTooltip(style.getTooltip(remainingEstimate, locale, utils));
        return new TimeTrackingGraphBean(params);
	}
	
	private AggregateTimeTrackingBean getAggregateTimeTrackingBean(IScnExtendedIssue extIssue)
	{
		Assertions.notNull("extended issue", extIssue);
		Assertions.notNull("issue", extIssue.getIssue());
		
		final AggregateTimeTrackingBean bean = new AggregateTimeTrackingBean(
				extIssue.getOriginalEstimate(), extIssue.getEstimate(), extIssue.getTimeSpent(), 0);
		if (extIssue.getIssue().isSubTask())
			return bean;
		
		final Collection<Issue> subTasks = extIssue.getIssue().getSubTaskObjects();
		if (subTasks == null || subTasks.isEmpty()) 
			return bean;
		
		int subTaskCount = 0;
		for (Issue subTask : subTasks)
		{
			if (permissionManager.hasPermission(Permissions.BROWSE, subTask, jaContext.getUser()))
			{
				final IScnExtendedIssue extSubTask = getExtendedIssue(subTask);
				
				bean.setRemainingEstimate(addAndPreserveNull(extSubTask.getEstimate(), bean.getRemainingEstimate()));
				bean.setOriginalEstimate(addAndPreserveNull(extSubTask.getOriginalEstimate(), bean.getOriginalEstimate()));
				bean.setTimeSpent(addAndPreserveNull(extSubTask.getTimeSpent(), bean.getTimeSpent()));
				bean.bumpGreatestSubTaskEstimate(extSubTask.getOriginalEstimate(), extSubTask.getEstimate(), extSubTask.getTimeSpent());
				
				subTaskCount++;
			}
		}
		bean.setSubTaskCount(subTaskCount);
		
		return bean;
	}
	
	private IScnExtendedIssue getExtendedIssue(Issue issue)
	{
		IScnExtendedIssue extIssue = eiStore.getByIssue(issue);

		if (extIssue == null)
		{
			extIssue = new ScnExtendedIssue(issue, null, null, null, null);
		}
		
		return extIssue;
	}
}