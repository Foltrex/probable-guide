package com.scn.jira.worklog.customfield;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.AbstractCustomFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.JiraDurationUtils;
import com.scn.jira.worklog.core.scnwl.DefaultScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.OfBizScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.ScnExtendedIssue;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;
import java.util.*;

@Named
public class ScnTimeTrackingType extends AbstractCustomFieldType<Estimate, Estimate> {
    public static final String ORIGINAL_ESTIMATE = "originalEstimate";
    public static final String REMAINING_ESTIMATE = "remainingEstimate";

    private final GenericConfigManager genericConfigManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
    private final OfBizScnExtendedIssueStore issueStore;
    private final DefaultScnWorklogManager wlManager;
    private final IGlobalSettingsManager scnPermissionManager;

    @Autowired
    public ScnTimeTrackingType(GenericConfigManager genericConfigManager, JiraAuthenticationContext authenticationContext,
                               ApplicationProperties applicationProperties, OfBizScnExtendedIssueStore issueStore,
                               DefaultScnWorklogManager wlManger, IGlobalSettingsManager scnPermissionManager) {
        this.genericConfigManager = genericConfigManager;
        this.jiraDurationUtils = ComponentAccessor.getComponent(JiraDurationUtils.class);
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
        this.issueStore = issueStore;
        this.wlManager = wlManger;
        this.scnPermissionManager = scnPermissionManager;
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> params = new HashMap<>(super.getVelocityParameters(issue, field, fieldLayoutItem));
        params.put("scnVisible", hasPermissionToViewScnWorklog());

        boolean legacyBehaviorEnabled = isLegacyBehaviorEnabled();
        params.put("legacyBehaviour", legacyBehaviorEnabled);
        if (legacyBehaviorEnabled) {
            if (hasWorkStarted(issue)) {
                params.put("fieldId", field.getId() + ":" + REMAINING_ESTIMATE);
                params.put("fieldName", "common.concepts.remaining.estimate");
                params.put("value", Objects.requireNonNull(getValueFromIssue(field, issue)).getFormattedRemaining());
                params.put("description", "timetracking.field.original.estimate.description");
            } else {
                params.put("fieldId", field.getId() + ":" + ORIGINAL_ESTIMATE);
                params.put("fieldName", "common.concepts.original.estimate");
                params.put("value", Objects.requireNonNull(getValueFromIssue(field, issue)).getFormattedOriginal());
                params.put("description", "timetracking.field.remaining.estimate.description");
            }
        } else {
            params.put("value", getValueFromIssue(field, issue));
        }

        return params;
    }

    @Override
    public String getStringFromSingularObject(Estimate estimate) {
        return estimate != null && estimate.getOriginal() != null ? estimate.getOriginal().toString() : "";
    }

    @Override
    public Estimate getSingularObjectFromString(String seconds) throws FieldValidationException {
        return seconds.isEmpty() ? new Estimate() :
            new Estimate(Long.valueOf(seconds),
                Long.valueOf(seconds),
                getFormattedDuration(Long.valueOf(seconds)),
                getFormattedDuration(Long.valueOf(seconds)));
    }

    @Override
    public Set<Long> remove(CustomField customField) {
        // TODO. Removing impl.
        return null;
    }

    @Override
    public void validateFromParams(CustomFieldParams params, ErrorCollection errorCollection, FieldConfig fieldConfig) {
        Collection<String> allKeys = params.getAllKeys();

        if (!isTimeTrackingEnabled() && !allKeys.isEmpty()) {
            errorCollection.addError(fieldConfig.getFieldId(), getI18nBean().getText("createissue.error.timetracking.disabled"));
            return;
        }

        String customFieldId = params.getCustomField().getId();

        String originalEstimate = (String) params.getFirstValueForKey(ORIGINAL_ESTIMATE);
        if (StringUtils.isNotBlank(originalEstimate) && isNotValidDuration(originalEstimate)) {
            errorCollection.addError(customFieldId + ":" + ORIGINAL_ESTIMATE, getI18nBean().getText("worklog.service.error.invalid.time.duration"));
        }

        String remainingEstimate = (String) params.getFirstValueForKey(REMAINING_ESTIMATE);
        if (StringUtils.isNotBlank(remainingEstimate) && isNotValidDuration(remainingEstimate)) {
            errorCollection.addError(customFieldId + ":" + REMAINING_ESTIMATE, getI18nBean().getText("worklog.service.error.invalid.time.duration"));
        }

        String value = (String) params.getFirstValueForNullKey();
        if (StringUtils.isNotBlank(value) && isNotValidDuration(value)) {
            errorCollection.addError(customFieldId, getI18nBean().getText("worklog.service.error.invalid.time.duration"));
        }
    }

    @Override
    public Estimate getValueFromCustomFieldParams(CustomFieldParams params) throws FieldValidationException {
        Estimate estimate = new Estimate();

        if (params.containsKey(ORIGINAL_ESTIMATE)) {
            estimate.setOriginal(getDurationSeconds((String) params.getFirstValueForKey(ORIGINAL_ESTIMATE)));
            estimate.setFormattedOriginal((String) params.getFirstValueForKey(ORIGINAL_ESTIMATE));
        }
        if (params.containsKey(REMAINING_ESTIMATE)) {
            estimate.setRemaining(getDurationSeconds((String) params.getFirstValueForKey(REMAINING_ESTIMATE)));
            estimate.setFormattedRemaining((String) params.getFirstValueForKey(REMAINING_ESTIMATE));
        }
        String value = (String) params.getFirstValueForNullKey();
        if (value != null && !value.isEmpty()) {
            Long seconds = Long.valueOf(value);
            return new Estimate(seconds,
                seconds,
                getFormattedDuration(seconds),
                getFormattedDuration(seconds));
        }

        return estimate;
    }

    @Override
    public void createValue(CustomField customField, Issue issue, @Nonnull Estimate estimate) {
        updateValue(customField, issue, estimate);
    }

    @Override
    public void updateValue(CustomField customField, Issue issue, Estimate estimate) {
        if (!hasPermissionToViewScnWorklog()) {
            return;
        }
        IScnExtendedIssue oldEstimate = issueStore.getByIssue(issue);
        Long originalEstimate = null;
        if (estimate.getOriginal() != null)
            originalEstimate = estimate.getOriginal();
        Long remainingEstimate = null;
        if (estimate.getRemaining() != null)
            remainingEstimate = estimate.getRemaining();

        if (isLegacyBehaviorEnabled()) {
            if (hasWorkStarted(issue))
                originalEstimate = (oldEstimate == null) ? null : oldEstimate.getOriginalEstimate();
            else
                remainingEstimate = originalEstimate;
        } else {
            if (originalEstimate != null && remainingEstimate == null) {
                remainingEstimate = originalEstimate;
            }
            if (originalEstimate == null && remainingEstimate != null) {
                originalEstimate = remainingEstimate;
            }
        }

        if (oldEstimate == null) {
            issueStore.create(new ScnExtendedIssue(issue, null, originalEstimate, remainingEstimate, null));
        } else {
            issueStore.update(new ScnExtendedIssue(issue, oldEstimate.getId(), originalEstimate, remainingEstimate, oldEstimate.getTimeSpent()));
        }
    }

    @Override
    public Object getStringValueFromCustomFieldParams(CustomFieldParams customFieldParams) {
        return getValueFromCustomFieldParams(customFieldParams);
    }

    @Nullable
    @Override
    public Estimate getValueFromIssue(CustomField customField, Issue issue) {
        if (issue == null) {
            return new Estimate();
        } else if (issue.getKey() == null) {
            return (Estimate) customField.getDefaultValue(issue);
        }
        IScnExtendedIssue scnExtendedIssue = issueStore.getByIssue(issue);

        if (hasPermissionToViewScnWorklog() && scnExtendedIssue != null) {
            return new Estimate(
                scnExtendedIssue.getOriginalEstimate(),
                scnExtendedIssue.getEstimate(),
                getFormattedDuration(scnExtendedIssue.getOriginalEstimate()),
                getFormattedDuration(scnExtendedIssue.getEstimate())
            );
        }

        return new Estimate();
    }

    @Override
    public Estimate getDefaultValue(FieldConfig fieldConfig) {
        final Long originalEstimate = (Long) genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        return new Estimate(originalEstimate, originalEstimate, getFormattedDuration(originalEstimate), getFormattedDuration(originalEstimate));
    }

    @Override
    public void setDefaultValue(FieldConfig fieldConfig, Estimate estimate) {
        genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), estimate.getOriginal());
    }

    @Nullable
    @Override
    public String getChangelogValue(CustomField customField, Estimate estimate) {
        // we do not return string with values because this string
        // appears in history tab panel and is visible by customer.
        return "";
    }

    private boolean hasPermissionToViewScnWorklog() {
        return scnPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, authenticationContext.getLoggedInUser());
    }

    private boolean isLegacyBehaviorEnabled() {
        return applicationProperties.getOption("jira.timetracking.estimates.legacy.behaviour");
    }

    private boolean hasWorkStarted(Issue issue) {
        try {
            return !wlManager.getByIssue(issue).isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    private String getFormattedDuration(Long duration) {
        if (duration == null)
            return null;

        return this.jiraDurationUtils.getShortFormattedDuration(duration);
    }

    private Long getDurationSeconds(String duration) {
        if (StringUtils.isBlank(duration))
            return null;

        try {
            return jiraDurationUtils.parseDuration(duration, authenticationContext.getLocale());
        } catch (InvalidDurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isNotValidDuration(String duration) {
        return !DateUtils.validDuration(duration);
    }

    private boolean isTimeTrackingEnabled() {
        return applicationProperties.getOption("jira.option.timetracking");
    }
}
