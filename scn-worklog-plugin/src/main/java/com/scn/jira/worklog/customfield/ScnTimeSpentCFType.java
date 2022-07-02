package com.scn.jira.worklog.customfield;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.impl.ReadOnlyCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScnTimeSpentCFType extends ReadOnlyCFType {
    private final IScnExtendedIssueStore store;
    private final JiraDurationUtils durationUtils;
    private final IssueManager issueManager;
    private final JiraAuthenticationContext context;

    protected ScnTimeSpentCFType(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager,
                                 TextFieldCharacterLengthValidator textFieldCharacterLengthValidator, JiraAuthenticationContext jiraAuthenticationContext,
                                 IScnExtendedIssueStore store, JiraDurationUtils durationUtils, IssueManager issueManager) {
        super(customFieldValuePersister, genericConfigManager, textFieldCharacterLengthValidator, jiraAuthenticationContext);
        this.store = store;
        this.durationUtils = durationUtils;
        this.issueManager = issueManager;
        this.context = jiraAuthenticationContext;
    }

    @Nullable
    @Override
    protected String getValueFromIssue(@Nonnull CustomField field, @Nullable Long issueId, @Nullable String issueKey) {
        IScnExtendedIssue extIssue = store.getByIssue(issueManager.getIssueObject(issueId));
        TimeTrackingGraphBeanFactory.Style style = TimeTrackingGraphBeanFactory.Style.NORMAL;
        return style.getDuration(extIssue != null ? extIssue.getTimeSpent() : null, context.getLocale(), durationUtils);
    }
}
