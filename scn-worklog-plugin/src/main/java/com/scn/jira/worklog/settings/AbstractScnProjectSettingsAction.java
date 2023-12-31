package com.scn.jira.worklog.settings;

import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.action.project.ViewProject;
import com.atlassian.jira.web.action.util.CalendarResourceIncluder;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractScnProjectSettingsAction extends ViewProject {
    private static final long serialVersionUID = 7848837845788272370L;

    protected final ProjectRoleManager prManager;
    protected final ExtendedConstantsManager ecManager;

    final CalendarResourceIncluder calendarResourceIncluder = new CalendarResourceIncluder();

    private Long[] inputProjectRolesToViewWL;
    private String[] inputWorklogTypes;
    private String[] inputExcludedWorklogTypes;
    private boolean inputUnspecifiedWorklogType;
    private String inputBlockingDate;
    private String inputWorklogBlockingDate;
    private String inputWLAutoCopy;
    private String inputWLTypeRequired;
    private String inputDefaultWorklogType;

    private Collection<ProjectRole> projectRoles;
    private Collection<WorklogType> worklogTypes;

    public AbstractScnProjectSettingsAction(ProjectRoleManager prManager, ExtendedConstantsManager ecManager) {
        super();

        this.prManager = prManager;
        this.ecManager = ecManager;
    }

    protected Date getParsedBlockingDate() {
        try {
            if (StringUtils.isNotBlank(getInputBlockingDate())) {
                return getDateTimeFormatter().withStyle(DateTimeStyle.DATE).parse(getInputBlockingDate());
            } else {
                return null;
            }
        } catch (Exception e) {
            addError(
                "inputBlockingDate",
                getJiraServiceContext().getI18nBean().getText("scn.project_settings.wl_blocking_date.error.format",
                    getApplicationProperties().getDefaultBackedString("jira.date.picker.java.format"),
                    getDateTimeFormatter().withStyle(DateTimeStyle.DATE).format(new Date())));
            return null;
        }
    }

    protected Date getParsedWorklogBlockingDate() {
        try {
            if (StringUtils.isNotBlank(getInputWorklogBlockingDate())) {
                return getDateTimeFormatter().withStyle(DateTimeStyle.DATE).parse(getInputWorklogBlockingDate());
            } else {
                return null;
            }
        } catch (Exception e) {
            addError(
                "inputWorklogBlockingDate",
                getJiraServiceContext().getI18nBean().getText("scn.project_settings.wl_blocking_date.error.format",
                    getApplicationProperties().getDefaultBackedString("jira.date.picker.java.format"),
                    getDateTimeFormatter().withStyle(DateTimeStyle.DATE).format(new Date())));
            return null;
        }
    }

    protected String getFormattedInputBlockingDate(Date date) {
        Assertions.notNull("Date", date);

        return getDateTimeFormatter().withStyle(DateTimeStyle.DATE).format(date);
    }

    public Collection<ProjectRole> getProjectRoles() {
        projectRoles = prManager.getProjectRoles();
        return (projectRoles == null) ? Collections.emptyList() : projectRoles;
    }

    public Collection<WorklogType> getWorklogTypes() {
        worklogTypes = ecManager.getWorklogTypeObjects();
        return (worklogTypes == null) ? Collections.emptyList() : worklogTypes;
    }

    public boolean getHasCalendarTranslation() {
        return calendarResourceIncluder.hasTranslation(getJiraServiceContext().getI18nBean().getLocale());
    }

    public Calendar getCurrentCalendar() {
        return Calendar.getInstance(getJiraServiceContext().getI18nBean().getLocale());
    }

    public String getInputBlockingDate() {
        return inputBlockingDate;
    }

    public void setInputBlockingDate(String inputBlockingDate) {
        this.inputBlockingDate = inputBlockingDate;
    }

    public String getInputWorklogBlockingDate() {
        return inputWorklogBlockingDate;
    }

    public void setInputWorklogBlockingDate(String inputWorklogBlockingDate) {
        this.inputWorklogBlockingDate = inputWorklogBlockingDate;
    }

    public String getInputWLAutoCopy() {
        return inputWLAutoCopy;
    }

    public void setInputWLAutoCopy(String inputWLAutoCopy) {
        this.inputWLAutoCopy = inputWLAutoCopy;
    }

    public String getInputWLTypeRequired() {
        return inputWLTypeRequired;
    }

    public void setInputWLTypeRequired(String inputWLTypeRequired) {
        this.inputWLTypeRequired = inputWLTypeRequired;
    }

    public String getInputDefaultWorklogType() {
        return inputDefaultWorklogType;
    }

    public void setInputDefaultWorklogType(String inputDefaultWorklogType) {
        this.inputDefaultWorklogType = inputDefaultWorklogType;
    }

    public CalendarResourceIncluder getCalendarIncluder() {
        return calendarResourceIncluder;
    }

    public Long[] getInputProjectRolesToViewWL() {
        return this.inputProjectRolesToViewWL;
    }

    public void setInputProjectRolesToViewWL(Long[] inputProjectRolesToViewWL) {
        this.inputProjectRolesToViewWL = inputProjectRolesToViewWL;
    }

    public String[] getInputWorklogTypes() {
        return this.inputWorklogTypes;
    }

    public void setInputWorklogTypes(String[] inputWorklogTypes) {
        this.inputWorklogTypes = inputWorklogTypes;
    }

    public String[] getInputExcludedWorklogTypes() {
        return inputExcludedWorklogTypes;
    }

    public void setInputExcludedWorklogTypes(String[] inputExcludedWorklogTypes) {
        this.inputExcludedWorklogTypes = inputExcludedWorklogTypes;
    }

    public void setInputExcludedWorklogTypes(Collection<WorklogType> inputExcludedWorklogTypes) {
        this.setInputExcludedWorklogTypes(
            inputExcludedWorklogTypes.stream()
                .map(WorklogType::getId)
                .toArray(String[]::new)
        );
    }

    public boolean isInputUnspecifiedWorklogType() {
        return this.inputUnspecifiedWorklogType;
    }

    public void setInputUnspecifiedWorklogType(boolean value) {
        this.inputUnspecifiedWorklogType = value;
    }

    public void setInputProjectRolesToViewWL(Collection<ProjectRole> projectRolesToViewWL) {
        if (projectRolesToViewWL == null) {
            this.inputProjectRolesToViewWL = new Long[0];
        } else {
            this.inputProjectRolesToViewWL = new Long[projectRolesToViewWL.size()];
            int i = 0;
            for (ProjectRole pr : projectRolesToViewWL) {
                this.inputProjectRolesToViewWL[i] = pr.getId();
                i++;
            }
        }
    }

    public Collection<ProjectRole> getSelectedProjectRolesToViewWL() {
        if (inputProjectRolesToViewWL == null) {
            return Collections.emptyList();
        }

        final List<ProjectRole> result = new ArrayList<>(inputProjectRolesToViewWL.length);
        for (Long projRoleId : inputProjectRolesToViewWL) {
            final ProjectRole role = prManager.getProjectRole(projRoleId);
            if (role != null) {
                result.add(role);
            }
        }
        return result;
    }

    public void setInputWorklogTypes(Collection<WorklogType> worklogTypes) {
        if (worklogTypes == null) {
            this.inputWorklogTypes = new String[0];
        } else {
            this.inputWorklogTypes = new String[worklogTypes.size()];
            int i = 0;
            for (WorklogType wlType : worklogTypes) {
                this.inputWorklogTypes[i++] = wlType.getId();
            }
        }
    }

    public Collection<WorklogType> getSelectedWorklogTypes() {
        if (inputWorklogTypes == null) {
            return Collections.emptyList();
        }
        final List<WorklogType> result = new ArrayList<>(inputWorklogTypes.length);
        for (String worklogId : inputWorklogTypes) {
            final WorklogType worklogType = ecManager.getWorklogTypeObject(worklogId);
            if (worklogType != null) {
                result.add(worklogType);
            }
        }
        return result;
    }

    public Collection<WorklogType> getSelectedExcludedWorklogTypes() {
        if (inputExcludedWorklogTypes == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(inputExcludedWorklogTypes)
            .map(ecManager::getWorklogTypeObject)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public boolean isDefaultWorklogTypeSelected(String worklogType) {
        return (getInputDefaultWorklogType() != null) && (getInputDefaultWorklogType().equals(worklogType));
    }
}
