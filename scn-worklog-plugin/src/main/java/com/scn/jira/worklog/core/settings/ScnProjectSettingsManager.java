package com.scn.jira.worklog.core.settings;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.opensymphony.module.propertyset.PropertyException;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.base.Splitter;

import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;

/**
 * Created by IntelliJ IDEA. User: Khadarovich Date: 05.08.2010 Time: 13:17:18
 * To change this template use File | Settings | File Templates.
 */
@ExportAsService({ScnProjectSettingsManager.class })
@Named("scnProjectSettingsManager")
public class ScnProjectSettingsManager implements IScnProjectSettingsManager {
	
	private final ProjectRoleManager projectRoleManager;
	private final ExtendedConstantsManager ecManager;
	
	private final PropertiesManager propertiesManager;

	private final String[] defaultProjectRoleNamesToViewWL = new String[] {
			"Customer",
			"Project Coordinator",
			"Project Manager" };

	@Inject
	public ScnProjectSettingsManager(ProjectRoleManager projectRoleManager,
			ExtendedConstantsManager ecManager) {
		this.propertiesManager = ComponentAccessor.getComponent(PropertiesManager.class);
		this.projectRoleManager = projectRoleManager;
		this.ecManager = ecManager;
	}

	public boolean isWLAutoCopyEnabled(Long projectId) throws PropertyException {
		Assertions.notNull("projectId", projectId);

		String propValue = propertiesManager.getPropertySet().getString(WL_AUTO_COPY + String.valueOf(projectId));
		if (!StringUtils.isBlank(propValue)) {
			return Boolean.parseBoolean(propValue);
		}
		return true;

	}

	public void setWLAutoCopy(Long projectId, boolean value)
			throws PropertyException {
		Assertions.notNull("projectId", projectId);

		propertiesManager.getPropertySet().setString(WL_AUTO_COPY + String.valueOf(projectId),String.valueOf(value));
	}

	public boolean isUnspecifiedWLTypeAutoCopyEnabled(Long projectId)
			throws PropertyException {
		Assertions.notNull("projectId", projectId);

		String value = propertiesManager.getPropertySet().getString(UNSPECIFIED_WORKLOG_TYPE + String.valueOf(projectId));
		
		if (value == null) {
			return isWLAutoCopyEnabled(projectId);
		} else {
			return Boolean.valueOf(value);
		}
	}

	public void setUnspecifiedWLTypeAutoCopyEnabled(Long projectId, String value)
			throws PropertyException {
		Assertions.notNull("projectId", projectId);

		if (value == null) {
			value = "";
		}

		propertiesManager.getPropertySet().setString(UNSPECIFIED_WORKLOG_TYPE + String.valueOf(projectId), value);
	}

	@Nonnull
	public Collection<WorklogType> getWorklogTypes(Long projectId)
			throws PropertyException {
		Assertions.notNull("projectId", projectId);

		String optionValue = propertiesManager.getPropertySet().getString(WORKLOG_TYPES + String.valueOf(projectId));
		
		if (optionValue == null) {
			return isWLAutoCopyEnabled(projectId) ? ecManager.getWorklogTypeObjects() : Collections.<WorklogType> emptyList();
		} else {
			List<WorklogType> types = new ArrayList<WorklogType>();

			for (String name : Splitter.on(";").omitEmptyStrings().split(optionValue)) {
				WorklogType wlType = ecManager.getWorklogTypeObject(name);
				if (wlType != null) {
					types.add(wlType);
				}
			}

			return types;
		}
	}

	public void setWorklogTypes(Long projectId, Collection<WorklogType> worklogTypes)
			throws PropertyException {
		Assertions.notNull("projectId", projectId);

		if (worklogTypes == null) {
			propertiesManager.getPropertySet().setString(WORKLOG_TYPES + String.valueOf(projectId), "");
		} else {
			StringBuffer sb = new StringBuffer("");
			for (Iterator<WorklogType> iter = worklogTypes.iterator(); iter.hasNext();) {
				WorklogType wlType = iter.next();
				sb.append(wlType.getId()).append(iter.hasNext() ? ";" : "");
			}

			propertiesManager.getPropertySet().setString(WORKLOG_TYPES + String.valueOf(projectId), sb.toString());
		}
	}

	public boolean isWLTypeRequired(Long projectId) throws PropertyException {
		Assertions.notNull("projectId", projectId);

		String value = propertiesManager.getPropertySet().getString(WL_TYPE_REQUIRED + String.valueOf(projectId));
		return (value == null) ? false : Boolean.valueOf(value);
	}

	public void setWLTypeRequired(Long projectId, boolean value)
			throws PropertyException {
		Assertions.notNull("projectId", projectId);

		propertiesManager.getPropertySet().setString(WL_TYPE_REQUIRED + String.valueOf(projectId),String.valueOf(value));
	}

	@Nullable
	public Date getWLBlockingDate(Long projectId) throws PropertyException {
		Assertions.notNull("projectId", projectId);
		
		return propertiesManager.getPropertySet().getDate(WL_BLOCKING_DATE + String.valueOf(projectId));
	}

	public void setWLBlockingDate(Long projectId, Date value)
			throws PropertyException {
		Assertions.notNull("projectId", projectId);

		propertiesManager.getPropertySet().setDate(WL_BLOCKING_DATE + String.valueOf(projectId), value);
	}
	
	@Nullable
	public Date getWLWorklogBlockingDate(Long projectId) throws PropertyException {
		Assertions.notNull("projectId", projectId);
		
		return propertiesManager.getPropertySet().getDate(WL_WORKLOG_BLOCKING_DATE + String.valueOf(projectId));
	}

	public void setWLWorklogBlockingDate(Long projectId, Date value) 
			throws PropertyException {
		Assertions.notNull("projectId", projectId);

		propertiesManager.getPropertySet().setDate(WL_WORKLOG_BLOCKING_DATE + String.valueOf(projectId), value);
	}

	@Nonnull
	public Collection<ProjectRole> getProjectRolesToViewWL(Long projectId)
			throws PropertyException {
		Assertions.notNull("projectId", projectId);

		String optionValue = propertiesManager.getPropertySet().getString(PROJECT_ROLES_TO_VIEW_WL + String.valueOf(projectId));
		
		if (optionValue == null) {
			return getDefaultRolesToViewWL();
		} else {
			List<ProjectRole> result = new ArrayList<ProjectRole>();

			for (String id : Splitter.on(";").omitEmptyStrings().split(optionValue)) {
				ProjectRole pr = projectRoleManager.getProjectRole(Long.valueOf(id));
				if (pr != null) {
					result.add(pr);
				}
			}

			return result;
		}
	}

	public void setProjectRolesToViewWL(Long projectId, Collection<ProjectRole> projectRoles)
			throws PropertyException {
		Assertions.notNull("projectId", projectId);

		StringBuffer sb = new StringBuffer("");

		if (projectRoles != null) {
			for (Iterator<ProjectRole> iter = projectRoles.iterator(); iter.hasNext();) {
				ProjectRole pr = iter.next();
				sb.append(pr.getId()).append(iter.hasNext() ? ";" : "");
			}
		}

		propertiesManager.getPropertySet().setString(PROJECT_ROLES_TO_VIEW_WL + String.valueOf(projectId), sb.toString());
	}

	public boolean hasPermissionToViewWL(ApplicationUser user, Project project)
			throws PropertyException {
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
		Collection<ProjectRole> defaultWLRoles = new ArrayList<ProjectRole>();

		for (String roleName : defaultProjectRoleNamesToViewWL) {
			ProjectRole projectRole = projectRoleManager.getProjectRole(roleName);
			if (projectRole != null) {
				defaultWLRoles.add(projectRole);
			}
		}

		return defaultWLRoles;
	}
}
