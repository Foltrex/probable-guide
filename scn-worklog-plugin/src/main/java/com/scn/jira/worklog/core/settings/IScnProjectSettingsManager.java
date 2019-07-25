package com.scn.jira.worklog.core.settings;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.opensymphony.module.propertyset.PropertyException;
import com.scn.jira.worklog.core.wl.WorklogType;

import java.util.Collection;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Khadarovich
 * Date: 05.08.2010
 * Time: 13:16:27
 * To change this template use File | Settings | File Templates.
 */
public interface IScnProjectSettingsManager
{
	public static final String WL_AUTO_COPY = "scn_wl_auto_copy_";
	public static final String WL_BLOCKING_DATE = "scn_wl_blocking_date_";
	public static final String WL_WORKLOG_BLOCKING_DATE = "scn_wl_worklog_blocking_date_";
	public static final String PROJECT_ROLES_TO_VIEW_WL = "scn_wl_project_roles_";
	public static final String WORKLOG_TYPES = "scn_wl_worklog_types_";
	public static final String UNSPECIFIED_WORKLOG_TYPE = "scn_wl_unspecified_worklog_type_";
    public static final String WL_TYPE_REQUIRED = "scn_wl_type_required_";

	boolean isWLAutoCopyEnabled(Long projectId) throws PropertyException;
	
	void setWLAutoCopy(Long projectId, boolean value) throws PropertyException;
	
	void setWorklogTypes(Long projectId, Collection<WorklogType> worklogTypes) throws PropertyException;
	
	Collection<WorklogType> getWorklogTypes(Long projectId) throws PropertyException;
	
	boolean isWLTypeRequired(Long projectId) throws PropertyException;
    
    void setWLTypeRequired(Long projectId, boolean value) throws PropertyException;

    Date getWLBlockingDate(Long projectId) throws PropertyException;
	
    Date getWLWorklogBlockingDate(Long projectId) throws PropertyException;
	
	void setWLWorklogBlockingDate(Long projectId, Date value) throws PropertyException;
    
	void setWLBlockingDate(Long projectId, Date value) throws PropertyException;
	
	void setUnspecifiedWLTypeAutoCopyEnabled(Long projectId, String value) throws PropertyException;
	
	boolean isUnspecifiedWLTypeAutoCopyEnabled(Long projectId) throws PropertyException;
	
	void setProjectRolesToViewWL(Long projectId, Collection<ProjectRole> projectRoles) throws PropertyException;
	
	Collection<ProjectRole> getProjectRolesToViewWL(Long projectId) throws PropertyException;
	
	boolean hasPermissionToViewWL(ApplicationUser user, Project project) throws PropertyException;
}
