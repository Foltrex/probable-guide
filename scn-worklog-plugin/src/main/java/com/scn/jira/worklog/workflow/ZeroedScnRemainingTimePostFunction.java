package com.scn.jira.worklog.workflow;

import java.util.Map;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.ScnExtendedIssue;

public class ZeroedScnRemainingTimePostFunction implements FunctionProvider {

	private final IScnExtendedIssueStore scnExtendedIssueStore;

    public ZeroedScnRemainingTimePostFunction(
    		IScnExtendedIssueStore extendedIssueStore)
    {
    	this.scnExtendedIssueStore = extendedIssueStore;
    }

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        final Issue issue = (Issue) transientVars.get("issue");
        final IScnExtendedIssue scnExtendedIssue = scnExtendedIssueStore.getByIssue(issue);
        try {
            if (scnExtendedIssue == null) {
                final IScnExtendedIssue newScnExtendedIssue = new ScnExtendedIssue(issue, null, null, 0L, null);
                scnExtendedIssueStore.create(newScnExtendedIssue);
            } else {
                final IScnExtendedIssue newScnExtendedIssue =
                        new ScnExtendedIssue(scnExtendedIssue.getIssue(),
                                scnExtendedIssue.getId(),
                                scnExtendedIssue.getOriginalEstimate(),
                                0L,
                                scnExtendedIssue.getTimeSpent());
                scnExtendedIssueStore.update(newScnExtendedIssue);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
}
