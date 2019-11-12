package com.scn.jira.worklog.customfield;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.worklog.core.scnwl.*;
import org.apache.commons.lang3.StringUtils;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.AbstractCustomFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import org.springframework.beans.factory.annotation.Autowired;

public class ScnTimeTrackingType extends AbstractCustomFieldType<Map<String, String>, String> {
	public static final String ORIGINAL_ESTIMATE = "originalEstimate";
	public static final String REMAINING_ESTIMATE = "remainingEstimate";

	private final JiraDurationUtils jiraDurationUtils;
	private final JiraAuthenticationContext authenticationContext;
	private final ApplicationProperties applicationProperties;
	private final OfBizScnExtendedIssueStore issueStore;
	private final DefaultScnWorklogManager wlManager;
	private final IGlobalSettingsManager scnPermissionManager;

	@Autowired
	public ScnTimeTrackingType(JiraAuthenticationContext authenticationContext, @ComponentImport ApplicationProperties applicationProperties,
							   OfBizScnExtendedIssueStore issueStore, DefaultScnWorklogManager wlManger,
							   IGlobalSettingsManager scnPermissionManager) {
		this.jiraDurationUtils = ComponentAccessor.getComponent(JiraDurationUtils.class);
		this.authenticationContext = authenticationContext;
		this.applicationProperties = applicationProperties;
		this.issueStore = issueStore;
		this.wlManager = wlManger;
		this.scnPermissionManager = scnPermissionManager;
	}

	@Override
	public void validateFromParams(CustomFieldParams cfParams, ErrorCollection errors, FieldConfig fieldConfig) {
		Map<String, String> values = getValueFromCustomFieldParams(cfParams);

		if (!isTimeTrackingEnabled() && !values.isEmpty()) {
			errors.addError(fieldConfig.getFieldId(), getI18nBean().getText("createissue.error.timetracking.disabled"));
			return;
		}

		String customFieldId = cfParams.getCustomField().getId();
		for (String key : values.keySet()) {
			if (StringUtils.isNotBlank(values.get(key)) && !isValidDuration(values.get(key)))
				errors.addError(customFieldId + ":" + key, getI18nBean().getText("worklog.service.error.invalid.time.duration"));
		}
	}

	@Override
	public void createValue(CustomField cf, Issue issue, Map<String, String> values) {
		updateValue(cf, issue, values);
	}

	@Override
	public void updateValue(CustomField cf, Issue issue, Map<String, String> values) {
		if (!hasPermissionToViewScnWorklog())
			return;

		IScnExtendedIssue oldScnExtIssue = issueStore.getByIssue(issue);
		Long originalEstimate = null;
		if (values.containsKey(ORIGINAL_ESTIMATE))
			originalEstimate = getDurationInMillis(values.get(ORIGINAL_ESTIMATE));
		Long remainingEstimate = null;
		if (values.containsKey(REMAINING_ESTIMATE))
			remainingEstimate = getDurationInMillis(values.get(REMAINING_ESTIMATE));

		if (isLegacyBehaviorEnabled()) {
			if (hasWorkStarted(issue))
				originalEstimate = (oldScnExtIssue == null) ? null : oldScnExtIssue.getOriginalEstimate();
			else
				remainingEstimate = originalEstimate;
		} else {
			if (originalEstimate != null && remainingEstimate == null)
				remainingEstimate = originalEstimate;
			if (originalEstimate == null && remainingEstimate != null)
				originalEstimate = remainingEstimate;
		}

		if (oldScnExtIssue == null)
			issueStore.create(new ScnExtendedIssue(issue, null, originalEstimate, remainingEstimate, null));
		else
			issueStore.update(new ScnExtendedIssue(issue, oldScnExtIssue.getId(), originalEstimate, remainingEstimate,
					oldScnExtIssue.getTimeSpent()));
	}

	@Override
	public Object getStringValueFromCustomFieldParams(CustomFieldParams cfParams) {
		return getValueFromCustomFieldParams(cfParams).toString();
	}

	@Override
	public Map<String, String> getValueFromCustomFieldParams(CustomFieldParams cfParams) throws FieldValidationException {
		Map<String, String> values = new HashMap<String, String>();

		if (cfParams.containsKey(ORIGINAL_ESTIMATE))
			values.put(ORIGINAL_ESTIMATE, (String) cfParams.getFirstValueForKey(ORIGINAL_ESTIMATE));
		if (cfParams.containsKey(REMAINING_ESTIMATE))
			values.put(REMAINING_ESTIMATE, (String) cfParams.getFirstValueForKey(REMAINING_ESTIMATE));

		return values;
	}

	@Override
	public Map<String, String> getValueFromIssue(CustomField cf, Issue issue) {
		Map<String, String> values = new HashMap<String, String>();
		IScnExtendedIssue scnExtendedIssue = issueStore.getByIssue(issue);

		if (hasPermissionToViewScnWorklog() && scnExtendedIssue != null) {
			if (isLegacyBehaviorEnabled()) {
				if (hasWorkStarted(issue))
					values.put(REMAINING_ESTIMATE, getFormattedDuration(scnExtendedIssue.getEstimate()));
				else
					values.put(ORIGINAL_ESTIMATE, getFormattedDuration(scnExtendedIssue.getOriginalEstimate()));
			} else {
				values.put(ORIGINAL_ESTIMATE, getFormattedDuration(scnExtendedIssue.getOriginalEstimate()));
				values.put(REMAINING_ESTIMATE, getFormattedDuration(scnExtendedIssue.getEstimate()));
			}
		}

		return values;
	}

	@Override
	public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
		Map<String, Object> params = new HashMap<String, Object>();

		params.putAll(super.getVelocityParameters(issue, field, fieldLayoutItem));
		params.put("scnVisible", hasPermissionToViewScnWorklog());

		boolean legacyBehaviorEnabled = isLegacyBehaviorEnabled();
		params.put("legacyBehaviour", Boolean.valueOf(legacyBehaviorEnabled));
		if (legacyBehaviorEnabled) {
			if (hasWorkStarted(issue)) {
				params.put("fieldId", field.getId() + ":" + REMAINING_ESTIMATE);
				params.put("fieldName", "common.concepts.remaining.estimate");
				params.put("value", getValueFromIssue(field, issue).get(REMAINING_ESTIMATE));
				params.put("description", "timetracking.field.original.estimate.description");
			} else {
				params.put("fieldId", field.getId() + ":" + ORIGINAL_ESTIMATE);
				params.put("fieldName", "common.concepts.original.estimate");
				params.put("value", getValueFromIssue(field, issue).get(ORIGINAL_ESTIMATE));
				params.put("description", "timetracking.field.remaining.estimate.description");
			}
		} else {
			params.put("value", getValueFromIssue(field, issue));
		}

		return params;
	}

	protected boolean isValidDuration(String duration) {
		return DateUtils.validDuration(duration);
//		return true;
	}

	protected Long getDurationInMillis(String duration) {
		if (StringUtils.isBlank(duration))
			return null;

		try {
			return jiraDurationUtils.parseDuration(duration, authenticationContext.getLocale());
		} catch (InvalidDurationException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected String getFormattedDuration(Long duration) {
		if (duration == null)
			return null;

		return this.jiraDurationUtils.getShortFormattedDuration(duration);
	}

	@Override
	public boolean valuesEqual(Map<String, String> v1, Map<String, String> v2) {
		if (v1 == v2)
			return true;
		else if (v1 == null || v2 == null)
			return false;

		if (!StringUtils.equals(v1.get(ORIGINAL_ESTIMATE), v2.get(ORIGINAL_ESTIMATE)))
			return false;
		if (!StringUtils.equals(v1.get(REMAINING_ESTIMATE), v2.get(REMAINING_ESTIMATE)))
			return false;

		return true;
	}

	private boolean hasPermissionToViewScnWorklog() {
		return scnPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, authenticationContext.getLoggedInUser());
	}

	private boolean isLegacyBehaviorEnabled() {
		return applicationProperties.getOption("jira.timetracking.estimates.legacy.behaviour");
	}

	private boolean isTimeTrackingEnabled() {
		return applicationProperties.getOption("jira.option.timetracking");
	}

	public boolean hasWorkStarted(Issue issue) {
		try {
			return !wlManager.getByIssue(issue).isEmpty();
		} catch (Exception e) {
			return true;
		}
	}

	@Override
	public String getSingularObjectFromString(String str) throws FieldValidationException {
		return str;
	}

	@Override
	public String getStringFromSingularObject(String str) {
		return str;
	}

	@Override
	public Set<Long> remove(CustomField cf) {
		return null;
	}

	@Override
	public String getChangelogValue(CustomField cf, Map<String, String> values) {
		// we do not return string with values because this string
		// appears in history tab panel and is visible by customer.
		return "";
	}

	@Override
	public Map<String, String> getDefaultValue(FieldConfig fieldConfig) {
		return MapBuilder.build(ORIGINAL_ESTIMATE, "", REMAINING_ESTIMATE, "");
	}

	@Override
	public void setDefaultValue(FieldConfig fieldConfig, Map<String, String> value) {
		// TODO: code this!
	}
}
