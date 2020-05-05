package com.scn.jira.worklog.wl;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.issue.DeleteWorklog;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.DefaultExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManagerImpl;
import com.scn.jira.worklog.core.wl.WorklogType;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collection;

public class ExtendedDeleteWorklog extends DeleteWorklog {
	private static final long serialVersionUID = 1L;

	protected final ExtendedConstantsManager extendedConstantsManager;
	protected final ExtendedWorklogService worklogService;
	private final WorklogManager worklogManager;
	private final IScnProjectSettingsManager psManager;

	private String worklogType;

	public ExtendedDeleteWorklog(WorklogService worklogService, CommentService commentService,
								 ProjectRoleManager projectRoleManager, DateTimeFormatterFactory dateTimeFormatterFactory,
								 FieldVisibilityManager fieldVisibilityManager, FieldLayoutManager fieldLayoutManager,
								 RendererManager rendererManager, @Qualifier("overridedWorklogManager") WorklogManager worklogManager,
								 UserUtil userUtil) {
		super(worklogService, commentService, projectRoleManager, ComponentAccessor.getComponent(JiraDurationUtils.class),
				dateTimeFormatterFactory, fieldVisibilityManager, fieldLayoutManager, rendererManager, worklogManager, userUtil,
				null, null, null, null);
		this.worklogManager = worklogManager;
		this.worklogService = new ExtendedWorklogService(new ExtendedWorklogManagerImpl(), new ScnProjectSettingsManager(projectRoleManager, new DefaultExtendedConstantsManager()));
		this.extendedConstantsManager = new DefaultExtendedConstantsManager();
		this.psManager = new ScnProjectSettingsManager(projectRoleManager, new DefaultExtendedConstantsManager());
	}

	public boolean shouldDisplay() {
		return isIssueValid() && hasIssuePermission("work", getIssueObject()) && !isTimeTrackingFieldHidden(getIssueObject())
				&& isWorkflowAllowsEdit(getIssueObject())
				&& psManager.hasPermissionToViewWL(getLoggedInApplicationUser(), getIssueObject().getProjectObject());
	}

	@Override
	public void doValidation() {
		if (worklogService.isDateExpired(getJiraServiceContext(), getParsedStartDate(),
				getIssueObject().getProjectObject(), true))
			return;

		super.doValidation();

		/* second check is needed for update worklog, it checks if new date is valid */
		/*
		 * if (worklogService.isDateExpired(getJiraServiceContext(), getParsedStartDate(), getIssueObject().getProjectObject(),
		 * true)) return;
		 */
	}

	public String doExecute() throws Exception {
		String redirect = super.doExecute();
		if (!getJiraServiceContext().getErrorCollection().hasAnyErrors()) {
			this.worklogService.deleteWorklogType(getJiraServiceContext(), getWorklogId());
		}
		return redirect;
	}

	public String getWorklogType() {
		if (this.worklogType == null) {
			return "";
		}
		return this.worklogType;
	}

	public void setWorklogType(String worklogType) {
		this.worklogType = worklogType;
	}

	public Collection<WorklogType> getWorklogTypeObjects() {
		return extendedConstantsManager.getWorklogTypeObjects();
	}

	public String getModifierKey() {
		return BrowserUtils.getModifierKey();
	}
}
