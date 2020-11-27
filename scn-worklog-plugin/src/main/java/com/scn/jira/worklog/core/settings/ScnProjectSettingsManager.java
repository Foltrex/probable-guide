package com.scn.jira.worklog.core.settings;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.google.common.base.Splitter;
import com.opensymphony.module.propertyset.PropertyException;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@ExportAsService({ScnProjectSettingsManager.class})
@Named("scnProjectSettingsManager")
public class ScnProjectSettingsManager implements IScnProjectSettingsManager {
    private final ProjectRoleManager projectRoleManager;
    private final ExtendedConstantsManager ecManager;
    private final PropertiesManager propertiesManager;
    private final String[] defaultProjectRoleNamesToViewWL = new String[]{
        "Customer", "Project Coordinator", "Project Manager"
    };

    @Inject
    public ScnProjectSettingsManager(ProjectRoleManager projectRoleManager, ExtendedConstantsManager ecManager) {
        this.propertiesManager = ComponentAccessor.getComponent(PropertiesManager.class);
        this.projectRoleManager = projectRoleManager;
        this.ecManager = ecManager;
    }

    public boolean isWLAutoCopyEnabled(Long projectId) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        String propValue = propertiesManager.getPropertySet().getString(WL_AUTO_COPY + projectId);
        if (!StringUtils.isBlank(propValue)) {
            return Boolean.parseBoolean(propValue);
        }
        return true;

    }

    public void setWLAutoCopy(Long projectId, boolean value) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        propertiesManager.getPropertySet().setString(WL_AUTO_COPY + projectId, String.valueOf(value));
    }

    public boolean isUnspecifiedWLTypeAutoCopyEnabled(Long projectId) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        String value = propertiesManager.getPropertySet()
            .getString(UNSPECIFIED_WORKLOG_TYPE + projectId);

        if (value == null) {
            return isWLAutoCopyEnabled(projectId);
        } else {
            return Boolean.parseBoolean(value);
        }
    }

    public void setUnspecifiedWLTypeAutoCopyEnabled(Long projectId, String value) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        if (value == null) {
            value = "";
        }

        propertiesManager.getPropertySet().setString(UNSPECIFIED_WORKLOG_TYPE + projectId, value);
    }

    @Nonnull
    public Collection<WorklogType> getWorklogTypes(Long projectId) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        String optionValue = propertiesManager.getPropertySet().getString(WORKLOG_TYPES + projectId);

        if (optionValue == null) {
            return isWLAutoCopyEnabled(projectId) ? ecManager.getWorklogTypeObjects()
                : Collections.emptyList();
        } else {
            List<WorklogType> types = new ArrayList<>();

            for (String name : Splitter.on(";").omitEmptyStrings().split(optionValue)) {
                WorklogType wlType = ecManager.getWorklogTypeObject(name);
                if (wlType != null) {
                    types.add(wlType);
                }
            }

            return types;
        }
    }

    public void setWorklogTypes(Long projectId, Collection<WorklogType> worklogTypes) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        if (worklogTypes == null) {
            propertiesManager.getPropertySet().setString(WORKLOG_TYPES + projectId, "");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Iterator<WorklogType> iter = worklogTypes.iterator(); iter.hasNext(); ) {
                WorklogType wlType = iter.next();
                sb.append(wlType.getId()).append(iter.hasNext() ? ";" : "");
            }

            propertiesManager.getPropertySet().setString(WORKLOG_TYPES + projectId, sb.toString());
        }
    }

    public boolean isWLTypeRequired(Long projectId) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        String value = propertiesManager.getPropertySet().getString(WL_TYPE_REQUIRED + projectId);
        return Boolean.parseBoolean(value);
    }

    public void setWLTypeRequired(Long projectId, boolean value) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        propertiesManager.getPropertySet().setString(WL_TYPE_REQUIRED + projectId,
            String.valueOf(value));
    }

    @Override
    public WorklogType getDefaultWorklogType(@Nonnull Long projectId) throws PropertyException {
        String worklogTypeId = propertiesManager.getPropertySet().getString(DEFAULT_WL_TYPE + projectId);
        return ecManager.getWorklogTypeObject(worklogTypeId);
    }

    @Override
    public void setDefaultWorklogType(@Nonnull Long projectId, String worklogTypeId) throws PropertyException {
        propertiesManager.getPropertySet().setString(DEFAULT_WL_TYPE + projectId, worklogTypeId);
    }

    @Nullable
    public Date getWLBlockingDate(Long projectId) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        return propertiesManager.getPropertySet().getDate(WL_BLOCKING_DATE + projectId);
    }

    public void setWLBlockingDate(Long projectId, Date value) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        propertiesManager.getPropertySet().setDate(WL_BLOCKING_DATE + projectId, value);
    }

    @Nullable
    public Date getWLWorklogBlockingDate(Long projectId) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        return propertiesManager.getPropertySet().getDate(WL_WORKLOG_BLOCKING_DATE + projectId);
    }

    public void setWLWorklogBlockingDate(Long projectId, Date value) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        propertiesManager.getPropertySet().setDate(WL_WORKLOG_BLOCKING_DATE + projectId, value);
    }

    @Nonnull
    public Collection<ProjectRole> getProjectRolesToViewWL(Long projectId) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        String optionValue = propertiesManager.getPropertySet()
            .getString(PROJECT_ROLES_TO_VIEW_WL + projectId);

        if (optionValue == null) {
            return getDefaultRolesToViewWL();
        } else {
            List<ProjectRole> result = new ArrayList<>();

            for (String id : Splitter.on(";").omitEmptyStrings().split(optionValue)) {
                ProjectRole pr = projectRoleManager.getProjectRole(Long.valueOf(id));
                if (pr != null) {
                    result.add(pr);
                }
            }

            return result;
        }
    }

    public void setProjectRolesToViewWL(Long projectId, Collection<ProjectRole> projectRoles) throws PropertyException {
        Assertions.notNull("projectId", projectId);

        StringBuilder sb = new StringBuilder();

        if (projectRoles != null) {
            for (Iterator<ProjectRole> iter = projectRoles.iterator(); iter.hasNext(); ) {
                ProjectRole pr = iter.next();
                sb.append(pr.getId()).append(iter.hasNext() ? ";" : "");
            }
        }

        propertiesManager.getPropertySet().setString(PROJECT_ROLES_TO_VIEW_WL + projectId,
            sb.toString());
    }

    public boolean hasPermissionToViewWL(ApplicationUser user, Project project) throws PropertyException {
        Assertions.notNull("user", user);
        Assertions.notNull("project", project);

        Collection<ProjectRole> projRoles = getProjectRolesToViewWL(project.getId());

        for (ProjectRole projRole : projRoles) {
            if (projectRoleManager.isUserInProjectRole(user, projRole, project)) {
                return true;
            }
        }

        return false;
    }

    @Nonnull
    private Collection<ProjectRole> getDefaultRolesToViewWL() {
        Collection<ProjectRole> defaultWLRoles = new ArrayList<>();

        for (String roleName : defaultProjectRoleNamesToViewWL) {
            ProjectRole projectRole = projectRoleManager.getProjectRole(roleName);
            if (projectRole != null) {
                defaultWLRoles.add(projectRole);
            }
        }

        return defaultWLRoles;
    }
}
