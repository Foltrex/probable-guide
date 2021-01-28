package com.scn.jira.worklog.wl;

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
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.customfield.Estimate;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.atlassian.jira.issue.IssueFieldConstants.TIMETRACKING;
import static com.atlassian.jira.issue.fields.TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE;
import static com.atlassian.jira.issue.fields.TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE;

public abstract class BaseFilter implements Filter {
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
    public void init(FilterConfig filterConfig) {
        issueManager = ComponentAccessor.getIssueManager();
        projectManager = ComponentAccessor.getProjectManager();
        cfManager = ComponentAccessor.getCustomFieldManager();
        authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        applicationProperties = ComponentAccessor.getApplicationProperties();
        jiraDurationUtils = ComponentAccessor.getComponent(JiraDurationUtils.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ScnRequestWrapper wrappedRequest = new ScnRequestWrapper((HttpServletRequest) request);
        ScnResponseWrapper wrappedResponse = new ScnResponseWrapper((HttpServletResponse) response);

        overrideRequest(wrappedRequest, wrappedResponse);

        chain.doFilter(wrappedRequest, wrappedResponse);

        overrideResponse(wrappedRequest, wrappedResponse);
    }

    protected abstract void overrideRequest(ScnRequestWrapper request, ScnResponseWrapper response);

    protected abstract void overrideResponse(ScnRequestWrapper request, ScnResponseWrapper response) throws IOException;

    @Override
    public void destroy() {
    }

    protected boolean shouldOverrideRequest(Map<String, String[]> params) {
        ApplicationUser user = authenticationContext.getLoggedInUser();
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

        Estimate cfParams = getParametersForCustomField(params, cf);
        Issue issue = getIssue(params);
        return issue == null || !Objects.equals(cf.getValue(issue), cfParams);
    }

    protected boolean shouldOverrideResponse(Map<String, String[]> params, String content) {
        if (StringUtils.isBlank(content))
            return false;

        Project project = getProject(params);
        if (project == null)
            return false;

        ApplicationUser user = authenticationContext.getLoggedInUser();
        return !psManager.hasPermissionToViewWL(user, project);
    }

    protected Map<String, String[]> overrideParams(Map<String, String[]> params) {
        Estimate cfParams = getParametersForCustomField(params, getTimetrackingCustomField());
        Project project = getProject(params);

        if (isLegacyBehaviorEnabled()) {
            if (psManager.isWLAutoCopyEnabled(project.getId())) {
                if (cfParams.getOriginal() != null)
                    params.put(TIMETRACKING, new String[]{formatMillisIntoDisplayFormat(cfParams.getOriginal())});
                else if (cfParams.getRemaining() != null)
                    params.put(TIMETRACKING, new String[]{formatMillisIntoDisplayFormat(cfParams.getRemaining())});
            } else {
                Issue issue = getIssue(params);
                if (cfParams.getOriginal() != null)
                    params.put(TIMETRACKING, new String[]{formatMillisIntoDisplayFormat(issue == null ? null : issue.getOriginalEstimate())});
                else if (cfParams.getRemaining() != null)
                    params.put(TIMETRACKING, new String[]{formatMillisIntoDisplayFormat(issue == null ? null : issue.getEstimate())});
            }
        } else {
            if (psManager.isWLAutoCopyEnabled(project.getId())) {
                params.put(TIMETRACKING_ORIGINALESTIMATE, new String[]{formatMillisIntoDisplayFormat(cfParams.getOriginal())});
                params.put(TIMETRACKING_REMAININGESTIMATE, new String[]{formatMillisIntoDisplayFormat(cfParams.getRemaining())});
            } else {
                Issue issue = getIssue(params);
                params.put(TIMETRACKING_ORIGINALESTIMATE, new String[]{formatMillisIntoDisplayFormat(issue == null ? null : issue.getOriginalEstimate())});
                params.put(TIMETRACKING_REMAININGESTIMATE, new String[]{formatMillisIntoDisplayFormat(issue == null ? null : issue.getEstimate())});
            }
        }

        return params;
    }

    protected String formatMillisIntoDisplayFormat(Long estimate) {
        if (estimate == null)
            return null;
        else
            return jiraDurationUtils.getShortFormattedDuration(estimate, new Locale("en_UK"));
    }

    private Estimate getParametersForCustomField(Map<String, String[]> actionParams, CustomField cf) {
        Map<String, Object> fromParams = new HashMap<>();
        cf.populateFromParams(fromParams, actionParams);
        return (Estimate) cf.getValueFromParams(fromParams);
    }

    protected CustomField getTimetrackingCustomField() {
        if (scncf != null)
            return scncf;

        List<CustomField> customFields = this.cfManager.getCustomFieldObjects();
        for (CustomField field : customFields) {
            if ("com.scn.jira.scn-worklog-plugin:scn-timetracking-custom-field-type"
                .equals(field.getCustomFieldType().getKey())) {
                scncf = field;
                return field;
            }
        }

        return null;
    }

    protected Project getProject(Map<String, String[]> params) {
        if (params.containsKey(PID)) {
            Long pid = Long.valueOf(Objects.requireNonNull(first(params, PID)));
            return projectManager.getProjectObj(pid);
        }

        Issue issue = getIssue(params);
        if (issue != null) {
            return issue.getProjectObject();
        }

        return null;
    }

    protected Issue getIssue(Map<String, String[]> params) {
        if (params.containsKey(ISSUE_ID)) {
            Long issueId = Long.valueOf(Objects.requireNonNull(first(params, ISSUE_ID)));
            return issueManager.getIssueObject(issueId);
        } else if (params.containsKey(ID)) {
            Long issueId = Long.valueOf(Objects.requireNonNull(first(params, ID)));
            return issueManager.getIssueObject(issueId);
        }

        return null;
    }

    private <K, V> V first(Map<K, V[]> params, K key) {
        V[] values = params.get(key);

        if (values == null || values.length == 0)
            return null;

        return values[0];
    }

    protected boolean isLegacyBehaviorEnabled() {
        return applicationProperties.getOption("jira.timetracking.estimates.legacy.behaviour");
    }
}
