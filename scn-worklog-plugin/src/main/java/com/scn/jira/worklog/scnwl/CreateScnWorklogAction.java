package com.scn.jira.worklog.scnwl;

import java.util.Date;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.commons.lang.StringUtils;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CommentVisibility;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.IScnWorklogManager;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;

import javax.inject.Inject;

public class CreateScnWorklogAction extends AbstractScnWorklogAction {
	private static final long serialVersionUID = 7460600544618806144L;

	private static final String SECURITY_BREACH = "securitybreach";

	private final FieldVisibilityManager fvManager;

	private IScnWorklog worklog;
	private Long newEstimateLong;
	private Long adjustmentAmountLong;

	@Inject
	public CreateScnWorklogAction(ProjectRoleManager projectRoleManager,
		  GroupManager groupManager, IScnExtendedIssueStore extIssueStore,
		  @ComponentImport IScnWorklogService defaultScnWorklogService, IScnWorklogManager worklogManager,
		  IScnProjectSettingsManager projectSettignsManager, ExtendedConstantsManager extendedConstantsManager) {

		super(ComponentAccessor.getComponent(CommentService.class), projectRoleManager, ComponentAccessor.getComponent(JiraDurationUtils.class), groupManager,
				extIssueStore, defaultScnWorklogService, worklogManager, projectSettignsManager, extendedConstantsManager);
		this.fvManager = ComponentAccessor.getComponent(FieldVisibilityManager.class);
	}

	protected boolean isTimeTrackingFieldHidden(Issue issue) {
		return fvManager.isFieldHidden("timetracking", issue);
	}

	public String doDefault() throws Exception {
		if (!isIssueValid()) {
			return ERROR;
		}

		if (!scnWorklogService.hasPermissionToCreate(getJiraServiceContext(), getIssueObject())) {
			return SECURITY_BREACH;
		}

		if (isTimeTrackingFieldHidden(getIssueObject())) {
			return SECURITY_BREACH;
		} else {
			setStartDate(getFormattedStartDate(new Date()));
			return super.doDefault();
		}
	}

	protected void doValidation() {
		if (!isIssueValid()) {
			return;
		}
		CommentVisibility visibility = getCommentVisibility();
		if ("new".equalsIgnoreCase(adjustEstimate)) {
			IScnWorklogService.WorklogNewEstimateResult worklogNewEstimateResult = scnWorklogService
					.validateCreateWithNewEstimate(getJiraServiceContext(), getIssueObject(), getTimeLogged(),
							getParsedStartDate(), getComment(), visibility.getGroupLevel(), visibility.getRoleLevel(),
							getNewEstimate(), getWorklogType());
			if (worklogNewEstimateResult != null) {
				worklog = worklogNewEstimateResult.getWorklog();
				newEstimateLong = worklogNewEstimateResult.getNewEstimate();
			}
		} else if ("manual".equalsIgnoreCase(adjustEstimate)) {
			IScnWorklogService.WorklogAdjustmentAmountResult worklogAdjustmentAmountResult = scnWorklogService
					.validateCreateWithManuallyAdjustedEstimate(getJiraServiceContext(), getIssueObject(), getTimeLogged(),
							getParsedStartDate(), getComment(), visibility.getGroupLevel(), visibility.getRoleLevel(),
							getAdjustmentAmount(), getWorklogType());
			if (worklogAdjustmentAmountResult != null) {
				worklog = worklogAdjustmentAmountResult.getWorklog();
				adjustmentAmountLong = worklogAdjustmentAmountResult.getAdjustmentAmount();
			}
		} else {
			worklog = scnWorklogService.validateCreate(getJiraServiceContext(), getIssueObject(), getTimeLogged(),
					getParsedStartDate(), getComment(), visibility.getGroupLevel(), visibility.getRoleLevel(), getWorklogType());
		}

		if (projectSettignsManager.isWLTypeRequired(getIssueObject().getProjectObject().getId())
				&& StringUtils.isBlank(getWorklogType())) getJiraServiceContext().getErrorCollection().addError("worklogType",
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

	protected String doExecute() throws Exception {
		if (isTimeTrackingFieldHidden(getIssueObject())) return SECURITY_BREACH;
		if ("auto".equalsIgnoreCase(adjustEstimate)) worklog = scnWorklogService.createAndAutoAdjustRemainingEstimate(
				getJiraServiceContext(), worklog, true, isWlAutoCopy());
		else if ("new".equalsIgnoreCase(adjustEstimate)) worklog = scnWorklogService.createWithNewRemainingEstimate(
				getJiraServiceContext(), new IScnWorklogService.WorklogNewEstimateResult(worklog, newEstimateLong), true,
				isWlAutoCopy());
		else if ("manual".equalsIgnoreCase(adjustEstimate)) worklog = scnWorklogService.createWithManuallyAdjustedEstimate(
				getJiraServiceContext(), new IScnWorklogService.WorklogAdjustmentAmountResult(worklog, adjustmentAmountLong),
				true, isWlAutoCopy());
		else worklog = scnWorklogService.createAndRetainRemainingEstimate(getJiraServiceContext(), worklog, true, isWlAutoCopy());
		if (getHasErrorMessages()) return ERROR;
		else if (isInlineDialogMode()) return returnComplete();
		else return getRedirect((new StringBuilder()).append("/browse/").append(getIssue().getString("key")).toString());
	}

	public IScnWorklog getWorklog() {
		return worklog;
	}

	public boolean isCreateWorklog() {
		return true;
	}
}
