package com.scn.jira.worklog.scnwl;

import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.issue.worklog.TimeTrackingIssueUpdater;
import com.scn.jira.worklog.core.scnwl.DefaultScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.OfBizScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.OfBizScnWorklogStore;
import com.scn.jira.worklog.core.scnwl.ScnTimeTrackingIssueManager;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnUserBlockingManager;
import com.scn.jira.worklog.core.wl.DefaultExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManagerImpl;
import com.scn.jira.worklog.globalsettings.GlobalSettingsManager;
import org.apache.commons.lang.StringUtils;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.CommentVisibility;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraDurationUtils;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;

import javax.inject.Inject;

public class UpdateScnWorklogAction extends AbstractScnWorklogAction {
	private static final long serialVersionUID = -5362590359328831184L;

	private Long newEstimateLong;
	private IScnWorklog worklog;

	@Inject
	public UpdateScnWorklogAction(ProjectRoleManager projectRoleManager, GroupManager groupManager) {
		super(ComponentAccessor.getComponent(CommentService.class), projectRoleManager,
				ComponentAccessor.getComponent(JiraDurationUtils.class), groupManager,
				new OfBizScnExtendedIssueStore(ComponentAccessor.getOfBizDelegator()),
				new DefaultScnWorklogService(ComponentAccessor.getComponent(VisibilityValidator.class),
						ComponentAccessor.getApplicationProperties(), projectRoleManager,
						ComponentAccessor.getIssueManager(),
						ComponentAccessor.getComponent(TimeTrackingConfiguration.class),
						ComponentAccessor.getGroupManager(),
						new ScnProjectSettingsManager(projectRoleManager, new DefaultExtendedConstantsManager()),
						new DefaultScnWorklogManager(
								new OfBizScnWorklogStore(ComponentAccessor.getOfBizDelegator(),
										ComponentAccessor.getIssueManager(), projectRoleManager,
										new ExtendedWorklogManagerImpl()),
								ComponentAccessor.getComponent(TimeTrackingIssueUpdater.class),
								new ScnTimeTrackingIssueManager(
										new OfBizScnExtendedIssueStore(ComponentAccessor.getOfBizDelegator()))

						),

						new GlobalSettingsManager(ComponentAccessor.getGroupManager()),
						new OfBizScnExtendedIssueStore(ComponentAccessor.getOfBizDelegator()),
						new ScnUserBlockingManager()

				), new ScnProjectSettingsManager(projectRoleManager, new DefaultExtendedConstantsManager()),
				new DefaultExtendedConstantsManager());
	}

	@Override
	public String doDefault() throws Exception {
		this.worklog = this.scnWorklogService.getById(getJiraServiceContext(), getWorklogId());
		if (this.worklog == null) {
			addErrorMessage(getJiraServiceContext().getI18nBean().getText("logwork.error.update.invalid.id",
					(getWorklogId() == null) ? null : getWorklogId().toString()));
			return "error";
		}
		if (!this.scnWorklogService.hasPermissionToUpdate(getJiraServiceContext(), this.worklog)) {
			return "securitybreach";
		}

		setTimeLogged(DateUtils.getDurationString(this.worklog.getTimeSpent().longValue(), getHoursPerDay().intValue(),
				getDaysPerWeek().intValue()));
		setStartDate(getFormattedStartDate(this.worklog.getStartDate()));
		setComment(this.worklog.getComment());
		setWorklogType(this.worklog.getWorklogTypeId());
		setCommentLevel(CommentVisibility.getCommentLevelFromLevels(this.worklog.getGroupLevel(),
				this.worklog.getRoleLevelId()));

		return super.doDefault();
	}

	@Override
	protected void doValidation() {
		CommentVisibility visibility = getCommentVisibility();

		if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(this.adjustEstimate)) {
			IScnWorklogService.WorklogNewEstimateResult worklogNewEstimateResult = this.scnWorklogService
					.validateUpdateWithNewEstimate(getJiraServiceContext(), getWorklogId(), getTimeLogged(),
							getParsedStartDate(), getComment(), visibility.getGroupLevel(), visibility.getRoleLevel(),
							getNewEstimate(), getWorklogType());

			if (worklogNewEstimateResult != null) {
				this.worklog = worklogNewEstimateResult.getWorklog();
				this.newEstimateLong = worklogNewEstimateResult.getNewEstimate();
			}
		} else {
			this.worklog = this.scnWorklogService.validateUpdate(getJiraServiceContext(), getWorklogId(),
					getTimeLogged(), getParsedStartDate(), getComment(), visibility.getGroupLevel(),
					visibility.getRoleLevel(), getWorklogType());
		}

		if (projectSettignsManager.isWLTypeRequired(getIssueObject().getProjectObject().getId())
				&& StringUtils.isBlank(getWorklogType()))
			getJiraServiceContext().getErrorCollection().addError("worklogType",
					getJiraServiceContext().getI18nBean().getText("logwork.worklogtype.error.null"));

		ApplicationUser reporter = null;
		if (!StringUtils.isBlank(getInputReporter())) {
			reporter = ComponentAccessor.getUserManager().getUserByName(getInputReporter());
		}

		if (reporter == null) {
			getJiraServiceContext().getErrorCollection().addError("inputReporter",
					getJiraServiceContext().getI18nBean().getText("logwork.reporter.error.null"));
		} else if (worklog != null) {
			worklog = reassignWorklog(worklog, reporter);
		}
	}

	@Override
	protected String doExecute() throws Exception {
		if (ADJUST_ESTIMATE_AUTO.equalsIgnoreCase(this.adjustEstimate)) {
			this.scnWorklogService.updateAndAutoAdjustRemainingEstimate(getJiraServiceContext(), this.worklog, true,
					isWlAutoCopy());
		} else if (ADJUST_ESTIMATE_NEW.equalsIgnoreCase(this.adjustEstimate)) {
			this.scnWorklogService.updateWithNewRemainingEstimate(getJiraServiceContext(),
					new IScnWorklogService.WorklogNewEstimateResult(this.worklog, this.newEstimateLong), true,
					isWlAutoCopy());
		} else {
			this.scnWorklogService.updateAndRetainRemainingEstimate(getJiraServiceContext(), this.worklog, true,
					isWlAutoCopy());
		}

		if (getHasErrorMessages()) {
			return "error";
		}

		if (isInlineDialogMode()) {
			return returnComplete();
		}

		return getRedirect("/browse/" + getIssue().getString("key"));
	}

	@Override
	public boolean isWlAutoCopyChecked() {
		if (getJiraServiceContext().getErrorCollection().hasAnyErrors())
			return isWlAutoCopy();

		if (this.worklog.getLinkedWorklog() == null)
			return false;
		else
			return true;

		// if(getWorklogAutoCopyOption())
		// return getWorklogTypeIsChecked(getWorklogType());

		// return false;
	}

	public IScnWorklog getWorklog() {
		return this.worklog;
	}
}
