package com.scn.jira.worklog.panels;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.action.IssueActionComparator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.tabpanels.GenericMessageAction;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.userformat.UserFormats;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import com.scn.jira.worklog.scnwl.IScnWorklogService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScnWorklogTabPanel extends AbstractIssueTabPanel {

	private final FieldLayoutManager fieldLayoutManager;
	private final RendererManager rendererManager;

	private final JiraDurationUtils jiraDurationUtils;
	private final FieldVisibilityManager fieldVisibilityManager;
	private final ExtendedConstantsManager extendedConstantsManager;
	private final IGlobalSettingsManager scnGlobalPermissionManager;
	private final IScnWorklogService worklogService;
	private final UserFormats userFormats;

	@Inject
	public ScnWorklogTabPanel(IScnWorklogService worklogService,
			ExtendedConstantsManager extendedConstantsManager, IGlobalSettingsManager scnGlobalPermissionManager) {

		this.jiraDurationUtils = ComponentAccessor.getComponent(JiraDurationUtils.class);
		this.fieldVisibilityManager = ComponentAccessor.getComponent(FieldVisibilityManager.class);
		this.extendedConstantsManager = extendedConstantsManager;
		this.worklogService = worklogService;
		this.scnGlobalPermissionManager = scnGlobalPermissionManager;
		this.fieldLayoutManager = ComponentAccessor.getComponent(FieldLayoutManager.class);
		this.rendererManager = ComponentAccessor.getComponent(RendererManager.class);
		this.userFormats = ComponentAccessor.getComponent(UserFormats.class);
	}

	@Override
	public List getActions(Issue issue, ApplicationUser remoteUser) {
		final List worklogs = new ArrayList();
		final JiraServiceContextImpl context = new JiraServiceContextImpl(remoteUser);
		final List<IScnWorklog> userWorklogs = worklogService.getByIssueVisibleToUser(context, issue);

		for (IScnWorklog worklog : userWorklogs) {
			boolean blocked = worklogService.isBlocked(context, worklog);
			worklogs.add(new ScnWorklogTabAction(descriptor, worklog, worklog.getWorklogTypeId(), jiraDurationUtils,
					extendedConstantsManager, !blocked && worklogService.hasPermissionToUpdate(context, worklog), !blocked
							&& worklogService.hasPermissionToDelete(context, worklog), hasPermissionToView(issue, remoteUser),
					fieldLayoutManager, rendererManager, this.userFormats));
		}

		if (worklogs.isEmpty()) {
			worklogs.add(new GenericMessageAction(descriptor.getI18nBean().getText("viewissue.nowork")));
		} else {
			Collections.sort(worklogs, IssueActionComparator.COMPARATOR);
		}

		return worklogs;
	}

	@Override
	public boolean showPanel(Issue issue, ApplicationUser remoteUser) {

		return ComponentAccessor.getApplicationProperties().getOption("jira.option.timetracking")
				&& (!fieldVisibilityManager.isFieldHidden("timetracking", issue))
				&& scnGlobalPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, remoteUser);
	}

	protected boolean hasPermissionToView(Issue issue, ApplicationUser remoteUser) {
		return worklogService.hasPermissionToView(new JiraServiceContextImpl(remoteUser), issue);
	}

}
