
package com.scn.jira.worklog.workflow;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.ScnWorklogImpl;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RequiredArgsConstructor
@Log4j
public class CreateScnWorklogFunction extends AbstractJiraFunctionProvider {

    private final CustomFieldManager customFieldManager;
    private final IScnWorklogService worklogService;
    private final IScnProjectSettingsManager projectSettingsManager;
    private final ProjectRoleManager projectRoleManager;

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        try {
            Issue issue = (Issue) transientVars.get("issue");
            String fieldName = (String) args.get(CreateScnWorklogFunctionPluginFactory.TARGET_FIELD_NAME);
            String worklogTypeId = (String) args.get(CreateScnWorklogFunctionPluginFactory.TARGET_WORKLOG_TYPE_ID);
            Long seconds = ((Double) ((Double) issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(fieldName)) * 60 * 60)).longValue();
            IScnWorklog worklog = new ScnWorklogImpl(projectRoleManager, issue, null, issue.getReporterId(),
                "", new Date(), null, null, seconds,
                worklogTypeId == null ? "0" : worklogTypeId);
            worklogService.createAndAutoAdjustRemainingEstimate(new JiraServiceContextImpl(issue.getReporter()),
                worklog, true, isWlAutoCopy(issue.getProjectId(), worklogTypeId));
        } catch (NullPointerException | ClassCastException e) {
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private boolean isWlAutoCopy(Long projectId, String worklogTypeId) {
        return projectSettingsManager.isWLAutoCopyEnabled(projectId)
            && (worklogTypeId == null ?
            projectSettingsManager.isUnspecifiedWLTypeAutoCopyEnabled(projectId)
            : projectSettingsManager.getWorklogTypes(projectId).stream()
                .anyMatch(worklogType -> worklogType.getId().equals(worklogTypeId))
        );
    }
}

