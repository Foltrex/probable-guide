package com.scn.jira.worklog.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.impl.ReadOnlyCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory.Style;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import java.util.Map;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractScnTimeCFType extends ReadOnlyCFType {

    protected final IScnExtendedIssueStore store;
    private final JiraDurationUtils durationUtils;
    private final IssueManager issueManager;
    private final JiraAuthenticationContext context;
    private final IGlobalSettingsManager settingsManager;

    protected AbstractScnTimeCFType(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager,
        TextFieldCharacterLengthValidator textFieldCharacterLengthValidator, JiraAuthenticationContext jiraAuthenticationContext,
        IScnExtendedIssueStore store, JiraDurationUtils durationUtils, IssueManager issueManager, IGlobalSettingsManager settingsManager) {
        super(customFieldValuePersister, genericConfigManager, textFieldCharacterLengthValidator, jiraAuthenticationContext);
        this.store = store;
        this.durationUtils = durationUtils;
        this.issueManager = issueManager;
        this.context = jiraAuthenticationContext;
        this.settingsManager = settingsManager;
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> params = super.getVelocityParameters(issue, field, fieldLayoutItem);
        params.put("formatShortValue", (UnaryOperator<String>) (this::formatShortValue));
        params.put("formatNormalValue", (UnaryOperator<String>) (this::formatNormalValue));
        return params;
    }

    @Nullable
    @Override
    protected String getValueFromIssue(@Nonnull CustomField field, @Nullable Long issueId, @Nullable String issueKey) {
        if (!settingsManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, context.getLoggedInUser())) {
            return null;
        }
        IScnExtendedIssue extIssue = store.getByIssue(issueManager.getIssueObject(issueId));
        Long value = getTime(extIssue);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public String formatShortValue(String value) {
        if (value == null) {
            return null;
        }
        return Style.SHORT.getDuration(Long.parseLong(value), context.getLocale(), durationUtils);
    }

    public String formatNormalValue(String value) {
        if (value == null) {
            return null;
        }
        return Style.NORMAL.getDuration(Long.parseLong(value), context.getLocale(), durationUtils);
    }

    protected abstract Long getTime(IScnExtendedIssue extIssue);
}
