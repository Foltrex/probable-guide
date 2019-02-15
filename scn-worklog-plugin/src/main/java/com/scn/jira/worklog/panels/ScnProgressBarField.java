package com.scn.jira.worklog.panels;

import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.ProgressBarSystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.TimeTrackingGraphBean;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory.Style;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;

public class ScnProgressBarField extends ProgressBarSystemField
{
	private static final Logger log = Logger.getLogger(ScnProgressBarField.class);
	
	private final JiraAuthenticationContext jaContext;
	private final JiraDurationUtils utils;
	private final IScnExtendedIssueStore eiStore;
	
	public ScnProgressBarField(
			VelocityTemplatingEngine templatingEngine, 
			ApplicationProperties applicationProperties, 
			JiraAuthenticationContext authenticationContext, 
			TimeTrackingGraphBeanFactory factory,
			IScnExtendedIssueStore eiStore)
	{
		super(templatingEngine, applicationProperties, authenticationContext, factory);
		
		this.jaContext = Assertions.notNull("JiraAuthenticationContext", ComponentAccessor.getJiraAuthenticationContext());
		this.utils = Assertions.notNull("utils", ComponentAccessor.getComponent(JiraDurationUtils.class));
		this.eiStore = eiStore;
	}
	
	public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, @SuppressWarnings("rawtypes") Map displayParams, Issue issue)
	{
		IScnExtendedIssue extendedIssue = eiStore.getByIssue(issue);
		Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, authenticationContext.getI18nHelper(), displayParams, issue);
		Long spent = Long.valueOf(0L), originalEstimate = Long.valueOf(0L), remainingEstimate = Long.valueOf(0L);
		if(extendedIssue != null){
			spent = extendedIssue.getTimeSpent();
			originalEstimate = extendedIssue.getOriginalEstimate();
			remainingEstimate = extendedIssue.getEstimate();
		}
		AggregateTimeTrackingBean aggregateTTBean = (AggregateTimeTrackingBean) displayParams.get("scnAggTTBean");
		if (aggregateTTBean == null)
		{
			velocityParams.put("innerGraphWidth", "100%");
		}
		else
		{
			velocityParams.put("innerGraphWidth", "100%");
			Long greatestEstimate = aggregateTTBean.getGreastestSubTaskEstimate();
			Long subTaskEstimate = AggregateTimeTrackingBean.getTheGreaterOfEstimates(originalEstimate, remainingEstimate, spent);
			if (greatestEstimate != null && subTaskEstimate != null && greatestEstimate.longValue() > 0L)
			{
				int width = (int) (((float) subTaskEstimate.longValue() / (float) greatestEstimate.longValue()) * 100F);
				velocityParams.put("innerGraphWidth", (width + "%"));
			}
		}
		Long percentage;
		try
		{
			percentage = calculateProgressPercentage(spent, remainingEstimate);
		}
		catch (IllegalArgumentException probablyNegative)
		{
			percentage = Long.valueOf(0L);
			log.error("Issue: '" + issue.getKey() + "' has an uncalculable percentage", probablyNegative);
		}
		velocityParams.put("percentComplete", percentage != null ? ("*" + percentage + "%") : "*");
		velocityParams.put("graphBean", getGraphBean(originalEstimate, remainingEstimate, spent));
		velocityParams.put("graphDisplayId", getDisplayId());
		return renderTemplate("progress-bar.vm", velocityParams);
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
}
