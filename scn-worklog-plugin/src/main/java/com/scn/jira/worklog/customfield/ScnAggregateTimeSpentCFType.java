package com.scn.jira.worklog.customfield;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraDurationUtils;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;

import java.util.Objects;

public class ScnAggregateTimeSpentCFType extends AbstractScnTimeCFType {

    public ScnAggregateTimeSpentCFType(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager, TextFieldCharacterLengthValidator textFieldCharacterLengthValidator, JiraAuthenticationContext jiraAuthenticationContext, IScnExtendedIssueStore store, JiraDurationUtils durationUtils, IssueManager issueManager, IGlobalSettingsManager settingsManager) {
        super(customFieldValuePersister, genericConfigManager, textFieldCharacterLengthValidator, jiraAuthenticationContext, store, durationUtils, issueManager, settingsManager);
    }

    @Override
    protected Long getTime(IScnExtendedIssue extIssue) {
        if (extIssue == null || extIssue.getIssue() == null) {
            return null;
        }
        return extIssue.getIssue().getSubTaskObjects().stream()
            .map(store::getByIssue)
            .filter(Objects::nonNull)
            .map(IScnExtendedIssue::getTimeSpent)
            .reduce(extIssue.getTimeSpent(), AggregateTimeTrackingBean::addAndPreserveNull);
    }
}
