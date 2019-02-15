package com.scn.jira.worklog.wl;

import static com.atlassian.jira.issue.fields.TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE;
import static com.atlassian.jira.issue.fields.TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE;
import static com.atlassian.jira.issue.IssueFieldConstants.TIMETRACKING;
import static com.scn.jira.worklog.customfield.ScnTimeTrackingType.ORIGINAL_ESTIMATE;
import static com.scn.jira.worklog.customfield.ScnTimeTrackingType.REMAINING_ESTIMATE;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import org.apache.commons.lang.StringUtils;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraDurationUtils;

public abstract class BaseFilter implements Filter
{
	protected static final String ISSUE_ID = "issueId";
	protected static final String PID = "pid";
	protected static final String FIELDS = "fields";
	protected static final String USER_PREFERENCES = "userPreferences";
	protected static final String SORTED_TABS = "sortedTabs";
	protected static final String ID = "id";
	protected static final String PROJECT = "project";
	protected static final String EDIT_HTML = "editHtml";
	
	protected IssueManager issueManager;
	protected ProjectManager projectManager;
	protected CustomFieldManager cfManager;
	protected JiraAuthenticationContext authenticationContext;
	protected ApplicationProperties applicationProperties;
	protected JiraDurationUtils jiraDurationUtils;

	@Inject
	protected ScnProjectSettingsManager psManager;
	
	private CustomField scncf;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
		issueManager = ComponentAccessor.getIssueManager();
		projectManager = ComponentAccessor.getProjectManager();
		cfManager = ComponentAccessor.getCustomFieldManager();
		authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
		applicationProperties = ComponentAccessor.getApplicationProperties();
		jiraDurationUtils = ComponentAccessor.getComponent(JiraDurationUtils.class);
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		ScnRequestWrapper wrappedRequest = new ScnRequestWrapper((HttpServletRequest) request);
		ScnResponseWrapper wrappedResponse = new ScnResponseWrapper((HttpServletResponse) response);
		
		overrideRequest(wrappedRequest, wrappedResponse);
		
		chain.doFilter(wrappedRequest, wrappedResponse);
		
		overrideResponse(wrappedRequest, wrappedResponse);
	}
	
	protected abstract void overrideRequest(ScnRequestWrapper request, ScnResponseWrapper response);
	
	protected abstract void overrideResponse(ScnRequestWrapper request, ScnResponseWrapper response) throws IOException;
	
	@Override
	public void destroy()
	{
	}
	
	protected boolean shouldOverrideRequest(Map<String, String[]> params)
	{
		ApplicationUser user = authenticationContext.getUser();
		Project project = getProject(params);
		
		if (project == null)
			return false;
		if (psManager.hasPermissionToViewWL(user, project))
			return false;
		if (!psManager.isWLAutoCopyEnabled(project.getId()))
			return true;
		
		CustomField cf = getTimetrackingCustomField();
        if (cf == null) 
        	return false;
        if (!cf.hasParam(params))
        	return false;
        
        Map<String, String> cfParams = getParametersForCustomField(params, cf);
        Issue issue = getIssue(params);
        if (issue != null && cf.getCustomFieldType().valuesEqual(cf.getValue(issue), cfParams))
        	return false;
        
        return true;
	}
	
	protected boolean shouldOverrideResponse(Map<String, String[]> params, String content)
	{
		if (StringUtils.isBlank(content))
			return false;
		
		Project project = getProject(params);
		if (project == null)
			return false;
		
		ApplicationUser user = authenticationContext.getUser();
		return !psManager.hasPermissionToViewWL(user, project);
	}
	
	protected Map<String, String[]> overrideParams(Map<String, String[]> params)
	{
        Map<String, String> cfParams = getParametersForCustomField(params, getTimetrackingCustomField());
        Project project = getProject(params);
        
        if (isLegacyBehaviorEnabled())
        {
        	if (psManager.isWLAutoCopyEnabled(project.getId()))
        	{
        		if (cfParams.containsKey(ORIGINAL_ESTIMATE))
            		params.put(TIMETRACKING, new String[] {cfParams.get(ORIGINAL_ESTIMATE)});
            	else if (cfParams.containsKey(REMAINING_ESTIMATE))
            		params.put(TIMETRACKING, new String[] {cfParams.get(REMAINING_ESTIMATE)});
        	}
        	else
        	{
        		Issue issue = getIssue(params);
        		if (cfParams.containsKey(ORIGINAL_ESTIMATE))
            		params.put(TIMETRACKING, new String[] {formatMillisIntoDisplayFormat(issue == null ? null : issue.getOriginalEstimate())});
            	else if (cfParams.containsKey(REMAINING_ESTIMATE))
            		params.put(TIMETRACKING, new String[] {formatMillisIntoDisplayFormat(issue == null ? null : issue.getEstimate())});
        	}
        }
        else
        {
        	if (psManager.isWLAutoCopyEnabled(project.getId()))
        	{
        		params.put(TIMETRACKING_ORIGINALESTIMATE, new String[] {cfParams.get(ORIGINAL_ESTIMATE)});
        		params.put(TIMETRACKING_REMAININGESTIMATE, new String[] {cfParams.get(REMAINING_ESTIMATE)});
        	}
        	else
        	{
        		Issue issue = getIssue(params);
        		params.put(TIMETRACKING_ORIGINALESTIMATE, new String[] {formatMillisIntoDisplayFormat(issue == null ? null : issue.getOriginalEstimate())});
        		params.put(TIMETRACKING_REMAININGESTIMATE, new String[] {formatMillisIntoDisplayFormat(issue == null ? null : issue.getEstimate())});
        	}
        }
        
        return params;
	}
	
	protected String formatMillisIntoDisplayFormat(Long estimate)
    {
        if (estimate == null) 
        	return null;
        else 
        	return jiraDurationUtils.getShortFormattedDuration(estimate, new Locale("en_UK"));
    }
	
	private Map<String, String> getParametersForCustomField(Map<String, String[]> actionParams, CustomField cf)
	{
		Map<String, Object> fromParams = new HashMap<String, Object>();
        cf.populateFromParams(fromParams, actionParams);
        return (Map<String, String>) cf.getValueFromParams(fromParams);
	}
	
	protected CustomField getTimetrackingCustomField()
	{
		if (scncf != null)
			return scncf;
		
		List<CustomField> customFields = this.cfManager.getCustomFieldObjects();
		for (CustomField field : customFields)
		{
			if ("com.scnsoft.jira.plugin.scnsoft-worklog-plugin:scn-timetracking-custom-field-type"
					.equals(field.getCustomFieldType().getKey()))
			{
				scncf = field;
				return field;
			}
		}
		
		return null;
	}
	
	protected Project getProject(Map<String, String[]> params)
	{
		if (params.containsKey(PID))
		{
			Long pid = Long.valueOf(first(params, PID));
			return projectManager.getProjectObj(pid);
		}
		
		Issue issue = getIssue(params);
		if (issue != null)
		{
			return issue.getProjectObject();
		}
		
		return null;
	}
	
	protected Issue getIssue(Map<String, String[]> params)
	{
		if (params.containsKey(ISSUE_ID))
		{
			Long issueId = Long.valueOf(first(params, ISSUE_ID));
			return issueManager.getIssueObject(issueId);
		}
		else if (params.containsKey(ID))
		{
			Long issueId = Long.valueOf(first(params, ID));
			return issueManager.getIssueObject(issueId);
		}
		
		return null;
	}
	
	private <K, V> V first(Map<K, V[]> params, K key)
	{
		V[] values = params.get(key);
		
		if(values == null || values.length == 0)
			return null;
		
		return values[0];
	}
	
	private boolean isLegacyBehaviorEnabled()
    {
        return applicationProperties.getOption("jira.timetracking.estimates.legacy.behaviour");
    }
}