package com.scn.jira.worklog.scnwl;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.opensymphony.util.TextUtils;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.IScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.ScnWorklogImpl;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnUserBlockingManager;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;

import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@ExportAsService({DefaultScnWorklogService.class })
@Named("defaultScnWorklogService")
public class DefaultScnWorklogService implements IScnWorklogService {

	private static final Logger LOGGER = Logger.getLogger(DefaultScnWorklogService.class);

	private final VisibilityValidator visibilityValidator;
	private final ApplicationProperties applicationProperties;
	private final ProjectRoleManager projectRoleManager;
	private final IssueManager issueManager;
	private final TimeTrackingConfiguration timeTrackingConfiguration;
	private final GroupManager groupManager;
	private final DateTimeFormatterFactory dateTimeFormatterFactory;
	private final IScnProjectSettingsManager scnProjectSettingsManager;
	private final IScnWorklogManager scnWorklogManager;
	private final IGlobalSettingsManager scnGlobalPermissionManager;
	private final IScnExtendedIssueStore extIssueStore;

	private ScnUserBlockingManager scnUserBlockingManager;

	@Inject
	public DefaultScnWorklogService(@ComponentImport VisibilityValidator visibilityValidator,
			ApplicationProperties applicationProperties, ProjectRoleManager projectRoleManager, IssueManager issueManager,
			@ComponentImport TimeTrackingConfiguration timeTrackingConfiguration, GroupManager groupManager,
			IScnProjectSettingsManager scnProjectSettingsManager,
		    IScnWorklogManager worklogManager, IGlobalSettingsManager scnGlobalPermissionManager,
			IScnExtendedIssueStore extendedIssueStore, ScnUserBlockingManager scnUserBlockingManager) {

		this.visibilityValidator = visibilityValidator;
		this.applicationProperties = applicationProperties;
		this.projectRoleManager = projectRoleManager;
		this.issueManager = issueManager;
		this.timeTrackingConfiguration = timeTrackingConfiguration;
		this.groupManager = groupManager;
		this.dateTimeFormatterFactory = ComponentAccessor.getComponent(DateTimeFormatterFactory.class);
		this.scnProjectSettingsManager = scnProjectSettingsManager;
		this.scnWorklogManager = worklogManager;
		this.scnGlobalPermissionManager = scnGlobalPermissionManager;
		this.extIssueStore = extendedIssueStore;
		this.scnUserBlockingManager = scnUserBlockingManager;
	}

	public IScnWorklog validateDelete(JiraServiceContext jiraServiceContext, Long worklogId) {
		try {
			IScnWorklog originalWorklog = scnWorklogManager.getById(worklogId);

			if (hasPermissionToDelete(jiraServiceContext, originalWorklog)) {
				return originalWorklog;
			}
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	public IScnWorklogService.WorklogNewEstimateResult validateDeleteWithNewEstimate(JiraServiceContext jiraServiceContext,
			Long worklogId, String newEstimate) {
		IScnWorklog originalWorklog = validateDelete(jiraServiceContext, worklogId);
		if ((originalWorklog != null) && (isValidNewEstimate(jiraServiceContext, newEstimate))) {
			Long estimate = (newEstimate == null) ? null : new Long(getDurationForFormattedString(newEstimate));
			return new IScnWorklogService.WorklogNewEstimateResult(originalWorklog, estimate);
		}
		return null;
	}

	public IScnWorklogService.WorklogAdjustmentAmountResult validateDeleteWithManuallyAdjustedEstimate(
			JiraServiceContext jiraServiceContext, Long worklogId, String adjustmentAmount) {
		IScnWorklog originalWorklog = validateDelete(jiraServiceContext, worklogId);
		if ((originalWorklog != null) && (isValidAdjustmentAmount(jiraServiceContext, adjustmentAmount))) {
			return new IScnWorklogService.WorklogAdjustmentAmountResult(originalWorklog, new Long(
					getDurationForFormattedString(adjustmentAmount)));
		}
		return null;
	}

	public boolean deleteWithNewRemainingEstimate(JiraServiceContext jiraServiceContext,
			IScnWorklogService.WorklogNewEstimateResult worklogNewEstimate, boolean dispatchEvent, boolean isLinkedWL) {
		final Long newEstimate = worklogNewEstimate.getNewEstimate();
		final Long newLinkedEstimate = worklogNewEstimate.getNewEstimate();
		return delete(jiraServiceContext, worklogNewEstimate.getWorklog(), newEstimate, newLinkedEstimate, dispatchEvent,
				isLinkedWL);
	}

	public boolean deleteWithManuallyAdjustedEstimate(JiraServiceContext jiraServiceContext,
			IScnWorklogService.WorklogAdjustmentAmountResult worklogAdjustmentAmountResult, boolean dispatchEvent,
			boolean isLinkedWL) {
		IScnWorklog worklog = worklogAdjustmentAmountResult.getWorklog();

		final IScnExtendedIssue extIssue = extIssueStore.getByIssue(worklog.getIssue());
		final Long newEstimate = increaseEstimate(extIssue == null ? null : extIssue.getEstimate(),
				worklogAdjustmentAmountResult.getAdjustmentAmount());
		final Long newLinkedEstimate = increaseEstimate(worklog.getIssue().getEstimate(),
				worklogAdjustmentAmountResult.getAdjustmentAmount());
		return delete(jiraServiceContext, worklog, newEstimate, newLinkedEstimate, dispatchEvent, isLinkedWL);
	}

	public boolean deleteAndRetainRemainingEstimate(JiraServiceContext jiraServiceContext, IScnWorklog worklog,
			boolean dispatchEvent, boolean isLinkedWL) {
		return delete(jiraServiceContext, worklog, null, null, dispatchEvent, isLinkedWL);
	}

	public boolean deleteAndAutoAdjustRemainingEstimate(JiraServiceContext jiraServiceContext, IScnWorklog worklog,
			boolean dispatchEvent, boolean isLinkedWL) {

		ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

		if (worklog == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
			return false;
		}

		if (worklog.getIssue() == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
			return false;
		}

		if (worklog.getId() == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.id.null"));
			return false;
		}

		final Long timeSpent = worklog.getTimeSpent();

		final IScnExtendedIssue extIssue = extIssueStore.getByIssue(worklog.getIssue());
		final Long newEstimate = increaseEstimate(extIssue == null ? null : extIssue.getEstimate(), timeSpent);
		Long newLinkedEstimate = null;
		if (worklog.getLinkedWorklog() != null) {
			final Long linkedEstimate = worklog.getIssue().getEstimate();
			final Long linkedTimeSpent = worklog.getLinkedWorklog().getTimeSpent();
			newLinkedEstimate = increaseEstimate(linkedEstimate, linkedTimeSpent);
		}
		return delete(jiraServiceContext, worklog, newEstimate, newLinkedEstimate, dispatchEvent, isLinkedWL);
	}

	public IScnWorklog validateUpdate(JiraServiceContext jiraServiceContext, Long worklogId, String timeSpent, Date startDate,
			String comment, String groupLevel, String roleLevelId, String worklogTypeId) {
		IScnWorklog updatedWorklog = null;
		try {
			final IScnWorklog originalWorklog = this.scnWorklogManager.getById(worklogId);
			ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();
			if (hasPermissionToUpdate(jiraServiceContext, originalWorklog)) {
				String updateAuthor = (user == null) ? null : user.getKey();
				updatedWorklog = validateParamsAndCreateWorklog(jiraServiceContext, originalWorklog.getIssue(),
						originalWorklog.getAuthorKey(), groupLevel, roleLevelId, timeSpent, startDate, worklogId, comment,
						originalWorklog.getCreated(), new Date(), updateAuthor, originalWorklog.getLinkedWorklog(), worklogTypeId);
			}
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
		return updatedWorklog;
	}

	public IScnWorklogService.WorklogNewEstimateResult validateUpdateWithNewEstimate(JiraServiceContext jiraServiceContext,
			Long worklogId, String timeSpent, Date startDate, String comment, String groupLevel, String roleLevelId,
			String newEstimate, String worklogTypeId) {
		IScnWorklog worklog = validateUpdate(jiraServiceContext, worklogId, timeSpent, startDate, comment, groupLevel,
				roleLevelId, worklogTypeId);
		if ((isValidNewEstimate(jiraServiceContext, newEstimate)) && (worklog != null)) {
			Long estimate = (newEstimate == null) ? null : new Long(getDurationForFormattedString(newEstimate));
			return new IScnWorklogService.WorklogNewEstimateResult(worklog, estimate);
		}
		return null;
	}

	public IScnWorklog updateWithNewRemainingEstimate(JiraServiceContext jiraServiceContext,
			IScnWorklogService.WorklogNewEstimateResult worklogNewEstimate, boolean dispatchEvent, boolean isLinkedWL) {
		final Long newEstimate = worklogNewEstimate.getNewEstimate();
		final Long newLinkedEstimated = worklogNewEstimate.getNewEstimate();
		return update(jiraServiceContext, worklogNewEstimate.getWorklog(), newEstimate, newLinkedEstimated, dispatchEvent,
				isLinkedWL);
	}

	public IScnWorklog updateAndRetainRemainingEstimate(JiraServiceContext jiraServiceContext, IScnWorklog worklog,
			boolean dispatchEvent, boolean isLinkedWL) {
		return update(jiraServiceContext, worklog, null, null, dispatchEvent, isLinkedWL);
	}

	public IScnWorklog updateAndAutoAdjustRemainingEstimate(JiraServiceContext jiraServiceContext, IScnWorklog worklog,
			boolean dispatchEvent, boolean isLinkedWL) {
		ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

		if (worklog == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
			return null;
		}

		if (worklog.getIssue() == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
			return null;
		}

		if (worklog.getId() == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.id.null"));
			return null;
		}

		IScnWorklog originalWorklog = null;
		try {
			originalWorklog = this.scnWorklogManager.getById(worklog.getId());
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}

		if (originalWorklog == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.worklog.for.id", worklog
					.getId().toString()));
			return null;
		}

		Long originalTimeSpent = originalWorklog.getTimeSpent();
		Long newTimeSpent = worklog.getTimeSpent();

		final IScnExtendedIssue extIssue = extIssueStore.getByIssue(worklog.getIssue());
		final Long newEstimate = getAutoAdjustNewEstimateOnUpdate(extIssue == null ? null : extIssue.getEstimate(), newTimeSpent,
				originalTimeSpent);
		Long newLinkedEstimate = null;
		if (originalWorklog.getLinkedWorklog() != null) {
			final Long originalLinkedTimeSpent = originalWorklog.getLinkedWorklog().getTimeSpent();
			final Long linkedEstimate = worklog.getIssue().getEstimate();
			newLinkedEstimate = getAutoAdjustNewEstimateOnUpdate(linkedEstimate, newTimeSpent, originalLinkedTimeSpent);
		}
		return update(jiraServiceContext, worklog, newEstimate, newLinkedEstimate, dispatchEvent, isLinkedWL);
	}

	protected boolean delete(JiraServiceContext jiraServiceContext, IScnWorklog worklog, Long newEstimate,
			Long newLinkedEstimate, boolean dispatchEvent, boolean isLinkedWL) {
		ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();
		ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

		if (worklog == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
			return false;
		}

		if (worklog.getIssue() == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
			return false;
		}

		if (worklog.getId() == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.id.null"));
			return false;
		}

		IScnWorklog originalWorklog = this.scnWorklogManager.getById(worklog.getId());
		if (isBlocked(jiraServiceContext, worklog) || isBlocked(jiraServiceContext, originalWorklog)) {
			return false;
		}

		try {
			if (hasPermissionToDelete(jiraServiceContext, worklog)) {
				return this.scnWorklogManager.delete(user, worklog, newEstimate, newLinkedEstimate, dispatchEvent, isLinkedWL);
			}
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
		return false;
	}

	protected IScnWorklog update(JiraServiceContext jiraServiceContext, IScnWorklog worklog, Long newEstimate,
			Long newLinkedEstimate, boolean dispatchEvent, boolean isLinkedWL) {
		IScnWorklog updatedWorklog = null;
		ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();
		ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

		if (worklog == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
			return null;
		}

		if (worklog.getIssue() == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
			return null;
		}

		if (worklog.getId() == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.id.null"));
			return null;
		}

		IScnWorklog originalWorklog = this.scnWorklogManager.getById(worklog.getId());
		if (isBlocked(jiraServiceContext, worklog) || isBlocked(jiraServiceContext, originalWorklog)) {
			return null;
		}

		try {
			if (hasPermissionToUpdate(jiraServiceContext, worklog)) {
				updatedWorklog = this.scnWorklogManager.update(user, worklog, newEstimate, newLinkedEstimate, dispatchEvent,
						isLinkedWL);
			}
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
		return updatedWorklog;
	}

	public IScnWorklog validateCreate(JiraServiceContext jiraServiceContext, Issue issue, String timeSpent, Date startDate,
			String comment, String groupLevel, String roleLevelId, String worklogTypeId) {
		IScnWorklog worklog = null;
		ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();// .getDirectoryUser();

		if (hasPermissionToCreate(jiraServiceContext, issue)) {
			String authorKey = (user == null) ? null : user.getKey();
			worklog = validateParamsAndCreateWorklog(jiraServiceContext, issue, authorKey, groupLevel, roleLevelId, timeSpent,
					startDate, null, comment, null, null, null, null, worklogTypeId);
		}

		return worklog;
	}

	public IScnWorklogService.WorklogNewEstimateResult validateCreateWithNewEstimate(JiraServiceContext jiraServiceContext,
			Issue issue, String timeSpent, Date startDate, String comment, String groupLevel, String roleLevelId,
			String newEstimate, String worklogTypeId) {
		IScnWorklog worklog = validateCreate(jiraServiceContext, issue, timeSpent, startDate, comment, groupLevel, roleLevelId,
				worklogTypeId);
		if ((isValidNewEstimate(jiraServiceContext, newEstimate)) && (worklog != null)) {
			Long estimate = (newEstimate == null) ? null : new Long(getDurationForFormattedString(newEstimate));
			return new IScnWorklogService.WorklogNewEstimateResult(worklog, estimate);
		}
		return null;
	}

	public IScnWorklogService.WorklogAdjustmentAmountResult validateCreateWithManuallyAdjustedEstimate(
			JiraServiceContext jiraServiceContext, Issue issue, String timeSpent, Date startDate, String comment,
			String groupLevel, String roleLevelId, String adjustmentAmount, String worklogTypeId) {
		IScnWorklog worklog = validateCreate(jiraServiceContext, issue, timeSpent, startDate, comment, groupLevel, roleLevelId,
				worklogTypeId);
		if ((isValidAdjustmentAmount(jiraServiceContext, adjustmentAmount)) && (worklog != null)) {
			return new IScnWorklogService.WorklogAdjustmentAmountResult(worklog, new Long(
					getDurationForFormattedString(adjustmentAmount)));
		}

		return null;
	}

	public IScnWorklog createWithNewRemainingEstimate(JiraServiceContext jiraServiceContext,
			IScnWorklogService.WorklogNewEstimateResult worklogNewEstimate, boolean dispatchEvent, boolean isLinkedWL) {
		final Long newEstimate = worklogNewEstimate.getNewEstimate();
		final Long newLinkedEstimate = worklogNewEstimate.getNewEstimate();
		return create(jiraServiceContext, worklogNewEstimate.getWorklog(), newEstimate, newLinkedEstimate, dispatchEvent,
				isLinkedWL);
	}

	public IScnWorklog createWithManuallyAdjustedEstimate(JiraServiceContext jiraServiceContext,
			IScnWorklogService.WorklogAdjustmentAmountResult worklogAdjustmentAmountResult, boolean dispatchEvent,
			boolean isLinkedWL) {
		IScnWorklog worklog = worklogAdjustmentAmountResult.getWorklog();

		final IScnExtendedIssue extIssue = extIssueStore.getByIssue(worklog.getIssue());
		final Long newEstimate = reduceEstimate(extIssue == null ? null : extIssue.getEstimate(),
				worklogAdjustmentAmountResult.getAdjustmentAmount());
		final Long newLinkedEstimate = reduceEstimate(worklog.getIssue().getEstimate(),
				worklogAdjustmentAmountResult.getAdjustmentAmount());
		return create(jiraServiceContext, worklog, newEstimate, newLinkedEstimate, dispatchEvent, isLinkedWL);
	}

	public IScnWorklog createAndRetainRemainingEstimate(JiraServiceContext jiraServiceContext, IScnWorklog worklog,
			boolean dispatchEvent, boolean isLinkedWL) {
		return create(jiraServiceContext, worklog, null, null, dispatchEvent, isLinkedWL);
	}

	public IScnWorklog createAndAutoAdjustRemainingEstimate(JiraServiceContext jiraServiceContext, IScnWorklog worklog,
			boolean dispatchEvent, boolean isLinkedWL) {
		ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

		if (worklog == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
			return null;
		}

		if (worklog.getIssue() == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
			return null;
		}

		final IScnExtendedIssue extIssue = extIssueStore.getByIssue(worklog.getIssue());
		final Long newEstimate = reduceEstimate(extIssue == null ? null : extIssue.getEstimate(), worklog.getTimeSpent());
		final Long newLinkedEstimate = reduceEstimate(worklog.getIssue().getEstimate(), worklog.getTimeSpent());
		return create(jiraServiceContext, worklog, newEstimate, newLinkedEstimate, dispatchEvent, isLinkedWL);
	}

	public boolean hasPermissionToCreate(JiraServiceContext jiraServiceContext, Issue issue) {
		ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();
		ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

		if (!isTimeTrackingEnabled()) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.time.tracking.not.enabed"));
			return false;
		}

		if (issue == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
			return false;
		}

		if (!isIssueInEditableWorkflowState(issue)) {
			errorCollection
					.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.not.editable.workflow.state"));
			return false;
		}

		boolean hasPerm = scnGlobalPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, user);

		if (!hasPerm) {
			if (user != null) {
				errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.permission",
						user.getDisplayName()));
			} else {
				errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.permission.no.user"));
			}
		}
		return hasPerm;
	}

	public boolean hasPermissionToUpdate(JiraServiceContext jiraServiceContext, IScnWorklog worklog) {
		ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();
		ErrorCollection errorCollection = new SimpleErrorCollection();

		if (!isTimeTrackingEnabled()) {
			jiraServiceContext.getErrorCollection().addErrorMessage(
					getText(jiraServiceContext, "worklog.service.error.time.tracking.not.enabed"));
			return false;
		}

		validateUpdateOrDeletePermissionCheckParams(worklog, errorCollection, jiraServiceContext);

		if (errorCollection.hasAnyErrors()) {
			jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
			return false;
		}

		if ((!hasEditAllPermission(user, worklog.getIssue())) && (!hasEditOwnPermission(user, worklog))) {
			if (user != null) {
				errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.edit.permission",
						user.getDisplayName()));
			} else {
				errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.edit.permission.no.user"));
			}
			jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
			return false;
		}

		boolean isValidVisibility = this.visibilityValidator.isValidVisibilityData(new JiraServiceContextImpl(user,
				errorCollection), "worklog", worklog.getIssue(), worklog.getGroupLevel(),
				(worklog.getRoleLevelId() == null) ? null : worklog.getRoleLevelId().toString());

		if (!isValidVisibility) {
			jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
			return false;
		}

		return true;
	}

	public boolean hasPermissionToDelete(JiraServiceContext jiraServiceContext, IScnWorklog worklog) {
		ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();
		ErrorCollection errorCollection = new SimpleErrorCollection();

		if (!isTimeTrackingEnabled()) {
			jiraServiceContext.getErrorCollection().addErrorMessage(
					getText(jiraServiceContext, "worklog.service.error.time.tracking.not.enabed"));
			return false;
		}

		validateUpdateOrDeletePermissionCheckParams(worklog, errorCollection, jiraServiceContext);

		if (errorCollection.hasAnyErrors()) {
			jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
			return false;
		}

		if (!hasDeleteAllPermission(user, worklog.getIssue()) && !hasDeleteOwnPermission(user, worklog)) {
			if (user != null) {
				errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.delete.permission",
						user.getDisplayName()));
			} else {
				errorCollection
						.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.delete.permission.no.user"));
			}
			jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
			return false;
		}

		boolean isValidVisibility = this.visibilityValidator.isValidVisibilityData(new JiraServiceContextImpl(user,
				errorCollection), "worklog", worklog.getIssue(), worklog.getGroupLevel(),
				(worklog.getRoleLevelId() == null) ? null : worklog.getRoleLevelId().toString());

		if (!isValidVisibility) {
			jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
			return false;
		}

		return true;
	}

	public IScnWorklog getById(JiraServiceContext jiraServiceContext, Long id) {
		IScnWorklog scnWorklog = null;
		try {
			scnWorklog = this.scnWorklogManager.getById(id);
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
		return scnWorklog;
	}

	public List<IScnWorklog> getByIssue(JiraServiceContext jiraServiceContext, Issue issue) {
		ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
		if (issue == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.null.issue"));
			return Collections.emptyList();
		}

		List<IScnWorklog> result = null;
		try {
			result = this.scnWorklogManager.getByIssue(issue);
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
		return result;
	}

	public List<IScnWorklog> getByProjectBetweenDates(JiraServiceContext jiraServiceContext, Project project, Date startDate,
			Date endDate) {
		final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
		if (project == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "scn.scnworklog.service.error.null.project"));
			return Collections.emptyList();
		}

		List<IScnWorklog> result = null;
		try {
			result = this.scnWorklogManager.getByProjectBetweenDates(project, startDate, endDate);
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
		return result;
	}

	public List<IScnWorklog> getByProjectVisibleToUserBetweenDates(JiraServiceContext jiraServiceContext, Project project,
			Date startDate, Date endDate) {
		final List<IScnWorklog> visibleWorklogs = new ArrayList<IScnWorklog>();
		final List<IScnWorklog> allWorklogs = getByProjectBetweenDates(jiraServiceContext, project, startDate, endDate);

		for (Iterator<IScnWorklog> iterator = allWorklogs.iterator(); iterator.hasNext();) {
			IScnWorklog worklog = (IScnWorklog) iterator.next();
			if (hasPermissionToView(jiraServiceContext, worklog)) {
				visibleWorklogs.add(worklog);
			}
		}
		return visibleWorklogs;
	}

	public List<IScnWorklog> getByIssueVisibleToUser(JiraServiceContext jiraServiceContext, Issue issue) {
		List<IScnWorklog> visibleWorklogs = new ArrayList<IScnWorklog>();
		List<IScnWorklog> allWorklogs = getByIssue(jiraServiceContext, issue);

		for (Iterator<IScnWorklog> iterator = allWorklogs.iterator(); iterator.hasNext();) {
			IScnWorklog worklog = (IScnWorklog) iterator.next();
			if (hasPermissionToView(jiraServiceContext, worklog)) {
				visibleWorklogs.add(worklog);
			}
		}

		return visibleWorklogs;
	}

	public boolean hasPermissionToView(JiraServiceContext jiraServiceContext, Issue issue) {
		ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();
		ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

		if (!isTimeTrackingEnabled()) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.time.tracking.not.enabed"));
			return false;
		}

		if (issue == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
			return false;
		}

		if (!isIssueInEditableWorkflowState(issue)) {
			errorCollection
					.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.not.editable.workflow.state"));
			return false;
		}

		boolean hasPerm = scnGlobalPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, user);
		if (!hasPerm) {
			if (user != null) {
				errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.permission",
						user.getDisplayName()));
			} else {
				errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.permission.no.user"));
			}
		}
		return hasPerm;
	}

	private boolean hasPermissionToView(JiraServiceContext jiraServiceContext, IScnWorklog worklog) {
		ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();
		return (hasViewOwnPermission(user, worklog) || hasViewAllPermission(user, worklog.getIssue()));
	}

	public boolean isTimeTrackingEnabled() {
		return this.applicationProperties.getOption("jira.option.timetracking");
	}

	public boolean isIssueInEditableWorkflowState(Issue issue) {
		return this.issueManager.isEditable(issue);
	}

	void validateUpdateOrDeletePermissionCheckParams(IScnWorklog worklog, ErrorCollection errorCollection,
			JiraServiceContext jiraServiceContext) {
		if (worklog == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
			return;
		}

		Issue issue = worklog.getIssue();

		if (issue == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
			return;
		}

		if (!isIssueInEditableWorkflowState(issue)) {
			errorCollection
					.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.not.editable.workflow.state"));
			return;
		}

		if (worklog.getId() == null) errorCollection.addErrorMessage(getText(jiraServiceContext,
				"worklog.service.error.worklog.id.null"));

		return;
	}

	boolean hasEditIssuePermission(User user, Issue issue) {
		return scnGlobalPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, ApplicationUsers.from(user));
	}

	protected IScnWorklog validateParamsAndCreateWorklog(JiraServiceContext jiraServiceContext, Issue issue, String author,
			String groupLevel, String roleLevelId, String timeSpent, Date startDate, Long worklogId, String comment,
			Date created, Date updated, String updateAuthor, Worklog linkedWorklog, String worklogTypeId) {
		IScnWorklog worklog = null;

		if (this.visibilityValidator.isValidVisibilityData(jiraServiceContext, "worklog", issue, groupLevel, roleLevelId)) {
			boolean defaultInputFieldsValidated = isValidWorklogInputFields(jiraServiceContext, issue, timeSpent, startDate);
			if (defaultInputFieldsValidated) {
				worklog = new ScnWorklogImpl(this.projectRoleManager, issue, worklogId, author, comment, startDate, groupLevel,
						(TextUtils.stringSet(roleLevelId)) ? Long.valueOf(roleLevelId) : null, new Long(
								getDurationForFormattedString(timeSpent)), updateAuthor, created, updated, worklogTypeId);
				worklog.setLinkedWorklog(linkedWorklog);
			}

		}

		return worklog;
	}

	protected IScnWorklog create(JiraServiceContext jiraServiceContext, final IScnWorklog worklog, Long newEstimate,
			Long newLinkedEstimate, boolean dispatchEvent, boolean isLinkedWL) {
		ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();
		ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

		if (worklog == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
			return null;
		}

		if (worklog.getIssue() == null) {
			errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
			return null;
		}

		if (isBlocked(jiraServiceContext, worklog)) {
			return null;
		}

		IScnWorklog newWorklog = null;
		try {
			if (hasPermissionToCreate(jiraServiceContext, worklog.getIssue())) {
				newWorklog = scnWorklogManager.create(user, worklog, newEstimate, newLinkedEstimate, dispatchEvent, isLinkedWL);
			}
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
		return newWorklog;
	}

	public boolean isBlocked(JiraServiceContext serviceContext, IScnWorklog wl) {
		return wl != null
				&& (isProjectWLBlocked(serviceContext, getProjectId(wl), wl.getStartDate()) || isUserWLBlocked(serviceContext,
						wl.getStartDate()));
	}

	protected boolean isProjectWLBlocked(JiraServiceContext serviceContext, Long projectId, Date date) {
		Date wlBlockingDate = this.scnProjectSettingsManager.getWLBlockingDate(projectId);

		if (wlBlockingDate == null || date.after(wlBlockingDate)) {
			return false;
		}

		addErrorMessage(serviceContext, "scn.scnworklog.service.error.project.blocking.date",
				formatDate(serviceContext, wlBlockingDate));

		return true;
	}

	protected boolean isUserWLBlocked(JiraServiceContext serviceContext, Date date) {
		Date userBlockingDate = scnUserBlockingManager.getBlockingDate(serviceContext.getLoggedInApplicationUser()
				.getDirectoryUser());

		if (userBlockingDate == null || date.after(userBlockingDate)) {
			return false;
		}

		addErrorMessage(serviceContext, "scn.scnworklog.service.error.user.blocking.date",
				formatDate(serviceContext, userBlockingDate));

		return true;
	}

	protected void addErrorMessage(JiraServiceContext serviceContext, String messageKey, String param) {
		String message = getText(serviceContext, messageKey, param);
		serviceContext.getErrorCollection().addErrorMessage(message);
	}

	private String formatDate(JiraServiceContext serviceContext, Date date) {
		Locale locale = serviceContext.getI18nBean().getLocale();
		return dateTimeFormatterFactory.formatter().withLocale(locale).withStyle(DateTimeStyle.DATE).format(date);
	}

	private Long getProjectId(Worklog wl) {
		return wl.getIssue().getProjectObject().getId();
	}

	protected boolean hasEditOwnPermission(ApplicationUser user, IScnWorklog worklog) {
		return scnGlobalPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, user);
	}

	protected boolean hasEditAllPermission(ApplicationUser user, Issue issue) {
		return scnGlobalPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, user);
	}

	protected boolean hasDeleteOwnPermission(ApplicationUser user, IScnWorklog worklog) {
		return scnGlobalPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, user);
	}

	protected boolean hasDeleteAllPermission(ApplicationUser user, Issue issue) {
		return scnGlobalPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, user);
	}

	protected boolean hasViewOwnPermission(ApplicationUser user, IScnWorklog worklog) {
		return scnGlobalPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, user);
	}

	protected boolean hasViewAllPermission(ApplicationUser user, Issue issue) {
		return scnGlobalPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, user);
	}

	protected boolean isSameAuthor(ApplicationUser user, IScnWorklog worklog) {
		if ((user != null) && (worklog.getAuthorKey() != null)) {
			return user.getKey().equals(worklog.getAuthorKey());
		}

		return (user == null) && (worklog.getAuthorKey() == null);
	}

	protected Long getAutoAdjustNewEstimateOnUpdate(Long timeEstimate, Long newTimeSpent, Long originalTimeSpent) {
		long oldTimeEstimate = (timeEstimate == null) ? 0L : timeEstimate.longValue();
		long newTimeEstimate = oldTimeEstimate + originalTimeSpent.longValue() - newTimeSpent.longValue();
		return new Long((newTimeEstimate < 0L) ? 0L : newTimeEstimate);
	}

	protected Long reduceEstimate(Long timeEstimate, Long amount) {
		long oldTimeEstimate = (timeEstimate == null) ? 0L : timeEstimate.longValue();
		long newTimeEstimate = oldTimeEstimate - amount.longValue();
		return new Long((newTimeEstimate < 0L) ? 0L : newTimeEstimate);
	}

	protected Long increaseEstimate(Long timeEstimate, Long amount) {
		long oldTimeEstimate = (timeEstimate == null) ? 0L : timeEstimate.longValue();
		long newTimeEstimate = oldTimeEstimate + amount.longValue();
		return new Long((newTimeEstimate < 0L) ? 0L : newTimeEstimate);
	}

	protected boolean isValidNewEstimate(JiraServiceContext jiraServiceContext, String newEstimate) {
		ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

		if (TextUtils.stringSet(newEstimate)) {
			if (!isValidDuration(newEstimate)) {
				errorCollection.addError("newEstimate", getText(jiraServiceContext, "worklog.service.error.newestimate"));
				return false;
			}
		} else {
			errorCollection.addError("newEstimate",
					getText(jiraServiceContext, "worklog.service.error.new.estimate.not.specified"));
			return false;
		}

		return true;
	}

	protected boolean isValidAdjustmentAmount(JiraServiceContext jiraServiceContext, String adjustmentAmount) {
		ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

		if (!TextUtils.stringSet(adjustmentAmount)) {
			errorCollection.addError("adjustmentAmount",
					getText(jiraServiceContext, "worklog.service.error.adjustment.amount.not.specified"));
			return false;
		}

		if (!isValidDuration(adjustmentAmount)) {
			errorCollection.addError("adjustmentAmount",
					getText(jiraServiceContext, "worklog.service.error.adjustment.amount.invalid"));
			return false;
		}

		return true;
	}

	protected boolean isValidWorklogInputFields(JiraServiceContext jiraServiceContext, Issue issue, String timeSpent,
			Date startDate) {
		ErrorCollection errorCollection = new SimpleErrorCollection();

		if (!TextUtils.stringSet(timeSpent)) {
			errorCollection.addError("timeLogged", getText(jiraServiceContext, "worklog.service.error.timespent.required"));
		} else if (!isValidDuration(timeSpent)) {
			errorCollection.addError("timeLogged", getText(jiraServiceContext, "worklog.service.error.invalid.time.duration"));
		} else if (getDurationForFormattedString(timeSpent) == 0L) {
			errorCollection.addError("timeLogged", getText(jiraServiceContext, "worklog.service.error.timespent.zero"));
		}

		if (startDate == null) {
			errorCollection.addError("startDate", getText(jiraServiceContext, "worklog.service.error.invalid.worklog.date"));
		}

		if (errorCollection.hasAnyErrors()) {
			jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
		}
		return !errorCollection.hasAnyErrors();
	}

	protected boolean isValidDuration(String duration) {
		return DateUtils.validDuration(duration);
	}

	protected long getDurationForFormattedString(String timeSpent) {
		try {
			return DateUtils.getDuration(timeSpent, getHoursPerDay(), getDaysPerWeek(),
					this.timeTrackingConfiguration.getDefaultUnit());
		} catch (InvalidDurationException e) {
			LOGGER.error("Trying to create/update a worklog with an invalid duration, this should never happen.", e);
			throw new RuntimeException(e);
		}
	}

	protected boolean isUserInGroup(User user, String groupLevel) {
		return (user != null) && (groupManager.isUserInGroup(user.getName(), groupLevel));
	}

	protected boolean isUserInRole(Long roleLevel, User user, Issue issue) {
		boolean isUserInRole = false;
		ProjectRole projectRole = this.projectRoleManager.getProjectRole(roleLevel);
		if (projectRole != null) {
			isUserInRole = this.projectRoleManager.isUserInProjectRole(ApplicationUsers.from(user), projectRole,
					issue.getProjectObject());
		}
		return isUserInRole;
	}

	private int getHoursPerDay() {
		return Integer.parseInt(this.applicationProperties.getDefaultBackedString("jira.timetracking.hours.per.day"));
	}

	private int getDaysPerWeek() {
		return Integer.parseInt(this.applicationProperties.getDefaultBackedString("jira.timetracking.days.per.week"));
	}

	private String getText(JiraServiceContext jiraServiceContext, String key) {
		return jiraServiceContext.getI18nBean().getText(key);
	}

	private String getText(JiraServiceContext jiraServiceContext, String key, String param) {
		return jiraServiceContext.getI18nBean().getText(key, param);
	}

	public List<IScnWorklog> getScnWorklogsByType(JiraServiceContext jiraServiceContext, String worklogTypeId) {
		List<IScnWorklog> result = null;
		try {
			result = scnWorklogManager.getScnWorklogsByType(worklogTypeId);
		} catch (DataAccessException e) {
			jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "scn.transaction.failure"));
			LOGGER.error(e.getMessage(), e);
		}
		return result;
	}
}