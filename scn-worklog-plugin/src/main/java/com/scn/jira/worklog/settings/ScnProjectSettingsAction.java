package com.scn.jira.worklog.settings;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;

/**
 * Created by IntelliJ IDEA. User: Khadarovich Date: 05.08.2010 Time: 12:27:52 To change this template use File | Settings | File
 * Templates.
 */
public class ScnProjectSettingsAction extends AbstractScnProjectSettingsAction {

	private static final long serialVersionUID = 3458031703740979391L;

	private final IScnProjectSettingsService psService;

	private Long pid;
	private Date blockingDate;
	private boolean wlAutoCopy;
	private boolean wlTypeRequired;
	private Collection<ProjectRole> projectRolesToViewWL;
	private Collection<WorklogType> wlTypes;
	private boolean unspecifiedWLTypeOption;

	public ScnProjectSettingsAction(ProjectRoleManager prManager, ExtendedConstantsManager ecManager,
			IScnProjectSettingsService psService) {
		super(prManager, ecManager);
		this.psService = psService;
	}

	@Override
	public String doDefault() throws Exception {

		if ((!hasProjectAdminPermission()) && (!hasAdminPermission())) {
			return "securitybreach";
		}

		if (getProjectObject() == null) {
			addErrorMessage(getJiraServiceContext().getI18nBean().getText("admin.errors.project.no.project.with.id"));
			return ERROR;
		}

		final Date blockingDate = psService.getWLBlockingDate(getJiraServiceContext(), pid);
		final boolean wlAutoCopy = psService.isWLAutoCopyEnabled(getJiraServiceContext(), pid);
		boolean isWLTypeRequired = psService.isWLTypeRequired(getJiraServiceContext(), pid);
		final Collection<ProjectRole> selectedProjectRoles = psService.getProjectRolesToViewWL(getJiraServiceContext(), pid);
		final Collection<WorklogType> selectedWorklogTypes = psService.getWorklogTypes(getJiraServiceContext(), pid);
		final boolean unspecifiedWorklogTypeOption = psService.getUnspecifiedWorklogTypeOption(getJiraServiceContext(), pid);

		if (getJiraServiceContext().getErrorCollection().hasAnyErrors()) {
			return ERROR;
		} else {
			setInputBlockingDate(blockingDate != null ? getFormattedInputBlockingDate(blockingDate) : "");
			setInputWLAutoCopy(String.valueOf(wlAutoCopy));
			setInputWLTypeRequired(String.valueOf(isWLTypeRequired));
			setInputProjectRolesToViewWL(selectedProjectRoles);
			setInputWorklogTypes(selectedWorklogTypes);
			setInputUnspecifiedWorklogType(unspecifiedWorklogTypeOption);
			getHttpRequest().setAttribute(
					"com.atlassian.jira.projectconfig.util.ServletRequestProjectConfigRequestCache:project", getProjectObject());
			return INPUT;
		}
	}

	@Override
	protected String doExecute() throws Exception {

		if ((!hasProjectAdminPermission()) && (!hasAdminPermission())) {
			return "securitybreach";
		}

		psService.setWLAutoCopy(getJiraServiceContext(), pid, isWlAutoCopy());
		psService.setWLTypeRequired(getJiraServiceContext(), pid, isWLTypeRequired());
		psService.setWLBlockingDate(getJiraServiceContext(), pid, getBlockingDate());
		psService.setProjectRolesToViewWL(getJiraServiceContext(), pid, getProjectRolesToViewWL());
		psService.setWorklogTypes(getJiraServiceContext(), pid, getWlTypes());
		psService.setUnspecifiedWorklogTypeOption(getJiraServiceContext(), pid, getUnspecifiedWLType());
		getHttpRequest().setAttribute("com.atlassian.jira.projectconfig.util.ServletRequestProjectConfigRequestCache:project",
				getProjectObject());

		if (getJiraServiceContext().getErrorCollection().hasAnyErrors()) {
			return ERROR;
		} else {
			return getRedirect("project/ViewProject.jspa?pid=" + pid);
		}
	}

	@Override
	protected void doValidation() {
		super.doValidation();

		setBlockingDate(getParsedBlockingDate());
		setWlAutoCopy(Boolean.valueOf(getInputWLAutoCopy()));
		setWLTypeRequired(Boolean.valueOf(getInputWLTypeRequired()));
		setProjectRolesToViewWL(getSelectedProjectRolesToViewWL());
		setWlTypes(getSelectedWorklogTypes());
		setUnspecifiedWLType(isInputUnspecifiedWorklogType());
		getHttpRequest().setAttribute("com.atlassian.jira.projectconfig.util.ServletRequestProjectConfigRequestCache:project",
				getProjectObject());

	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public Date getBlockingDate() {
		return blockingDate;
	}

	public void setBlockingDate(Date blockingDate) {
		if (blockingDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(blockingDate);
			cal.set(Calendar.HOUR, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MILLISECOND, 990);

			this.blockingDate = cal.getTime();
		}
	}

	public boolean isWlAutoCopy() {
		return wlAutoCopy;
	}

	public void setWlAutoCopy(boolean wlAutoCopy) {
		this.wlAutoCopy = wlAutoCopy;
	}

	public boolean isWLTypeRequired() {
		return wlTypeRequired;
	}

	public void setWLTypeRequired(boolean wlTypeRequired) {
		this.wlTypeRequired = wlTypeRequired;
	}

	public Collection<ProjectRole> getProjectRolesToViewWL() {
		return projectRolesToViewWL;
	}

	public void setProjectRolesToViewWL(Collection<ProjectRole> projectRolesToViewWL) {
		this.projectRolesToViewWL = projectRolesToViewWL;
	}

	public Collection<WorklogType> getWlTypes() {
		return wlTypes;
	}

	public void setWlTypes(Collection<WorklogType> wlTypes) {
		this.wlTypes = wlTypes;
	}

	public boolean getUnspecifiedWLType() {
		return this.unspecifiedWLTypeOption;
	}

	public void setUnspecifiedWLType(boolean value) {
		this.unspecifiedWLTypeOption = value;
	}
}
