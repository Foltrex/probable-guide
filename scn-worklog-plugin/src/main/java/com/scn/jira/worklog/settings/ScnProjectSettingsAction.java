package com.scn.jira.worklog.settings;

import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.scn.jira.worklog.core.settings.ScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class ScnProjectSettingsAction extends AbstractScnProjectSettingsAction {
    private static final long serialVersionUID = 3458031703740979391L;

    private final IScnProjectSettingsService psService;

    private Long pid;
    private Date blockingDate;
    private Date worklogBlockingDate;
    private boolean wlAutoCopy;
    private boolean wlTypeRequired;
    private String defaultWorklogType;
    private Collection<ProjectRole> projectRolesToViewWL;
    private Collection<WorklogType> wlTypes;
    private Collection<WorklogType> excludedWLTypes;
    private boolean unspecifiedWLTypeOption;

    public ScnProjectSettingsAction(ProjectRoleManager prManager, ProjectRoleManager projectRoleManager,
                                    ExtendedConstantsManager extendedConstantsManager) {
        super(prManager, extendedConstantsManager);
        this.psService = new ScnProjectSettingsService(new ScnProjectSettingsManager(projectRoleManager, extendedConstantsManager));
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
        final Date worklogBlockingDate = psService.getWLWorklogBlockingDate(getJiraServiceContext(), pid);
        final boolean wlAutoCopy = psService.isWLAutoCopyEnabled(getJiraServiceContext(), pid);
        boolean isWLTypeRequired = psService.isWLTypeRequired(getJiraServiceContext(), pid);
        WorklogType defaultWorklogType = psService.getDefaultWorklogType(getJiraServiceContext(), pid);
        final Collection<ProjectRole> selectedProjectRoles = psService.getProjectRolesToViewWL(getJiraServiceContext(), pid);
        final Collection<WorklogType> selectedWorklogTypes = psService.getWorklogTypes(getJiraServiceContext(), pid);
        final Collection<WorklogType> selectedExcludedWorklogTypes = psService.getExcludedWorklogTypes(getJiraServiceContext(), pid);
        final boolean unspecifiedWorklogTypeOption = psService.getUnspecifiedWorklogTypeOption(getJiraServiceContext(), pid);

        if (getJiraServiceContext().getErrorCollection().hasAnyErrors()) {
            return ERROR;
        } else {
            setInputBlockingDate(blockingDate != null ? getFormattedInputBlockingDate(blockingDate) : "");
            setInputWorklogBlockingDate(worklogBlockingDate != null ? getFormattedInputBlockingDate(worklogBlockingDate) : "");
            setInputWLAutoCopy(String.valueOf(wlAutoCopy));
            setInputWLTypeRequired(String.valueOf(isWLTypeRequired));
            setInputDefaultWorklogType(defaultWorklogType != null ? defaultWorklogType.getId() : "");
            setInputProjectRolesToViewWL(selectedProjectRoles);
            setInputWorklogTypes(selectedWorklogTypes);
            setInputExcludedWorklogTypes(selectedExcludedWorklogTypes);
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
//        psService.setWLTypeRequired(getJiraServiceContext(), pid, isWLTypeRequired());
        psService.setDefaultWorklogType(getJiraServiceContext(), pid, getDefaultWorklogType());
        psService.setWLBlockingDate(getJiraServiceContext(), pid, getBlockingDate());
        psService.setWLWorklogBlockingDate(getJiraServiceContext(), pid, getWorklogBlockingDate());
        psService.setProjectRolesToViewWL(getJiraServiceContext(), pid, getProjectRolesToViewWL());
        psService.setWorklogTypes(getJiraServiceContext(), pid, getWlTypes());
        psService.setExcludedWorklogTypes(getJiraServiceContext(), pid, getExcludedWLTypes());
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
        setWorklogBlockingDate(getParsedWorklogBlockingDate());
        setWlAutoCopy(Boolean.parseBoolean(getInputWLAutoCopy()));
        setWLTypeRequired(Boolean.parseBoolean(getInputWLTypeRequired()));
        setDefaultWorklogType(getInputDefaultWorklogType());
        setProjectRolesToViewWL(getSelectedProjectRolesToViewWL());
        setWlTypes(getSelectedWorklogTypes());
        setExcludedWLTypes(getSelectedExcludedWorklogTypes());
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

    public Date getWorklogBlockingDate() {
        return worklogBlockingDate;
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

    public void setWorklogBlockingDate(Date worklogBlockingDate) {
        if (worklogBlockingDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(worklogBlockingDate);
            cal.set(Calendar.HOUR, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 990);

            this.worklogBlockingDate = cal.getTime();
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

    public String getDefaultWorklogType() {
        return defaultWorklogType;
    }

    public void setDefaultWorklogType(String defaultWorklogType) {
        this.defaultWorklogType = defaultWorklogType;
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

    public Collection<WorklogType> getExcludedWLTypes() {
        return excludedWLTypes;
    }

    public void setExcludedWLTypes(Collection<WorklogType> excludedWLTypes) {
        this.excludedWLTypes = excludedWLTypes;
    }

    public boolean getUnspecifiedWLType() {
        return this.unspecifiedWLTypeOption;
    }

    public void setUnspecifiedWLType(boolean value) {
        this.unspecifiedWLTypeOption = value;
    }
}
