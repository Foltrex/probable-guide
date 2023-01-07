package com.scn.jira.worklog.core.settings;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.opensymphony.module.propertyset.PropertyException;
import com.scn.jira.worklog.core.wl.WorklogType;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Date;

public interface IScnProjectSettingsManager {
    String WL_AUTO_COPY = "scn_wl_auto_copy_";
    String WL_BLOCKING_DATE = "scn_wl_blocking_date_";
    String WL_WORKLOG_BLOCKING_DATE = "scn_wl_worklog_blocking_date_";
    String PROJECT_ROLES_TO_VIEW_WL = "scn_wl_project_roles_";
    String WORKLOG_TYPES = "scn_wl_worklog_types_";
    String EXCLUDED_WORKLOG_TYPES = "scn_wl_excluded_worklog_types_";
    String UNSPECIFIED_WORKLOG_TYPE = "scn_wl_unspecified_worklog_type_";
    String WL_TYPE_REQUIRED = "scn_wl_type_required_";
    String DEFAULT_WL_TYPE = "scn_default_wl_type_";
    String WL_COMMENT_REQUIRED = "scn_wl_comment_required_";

    boolean isWLAutoCopyEnabled(Long projectId) throws PropertyException;

    void setWLAutoCopy(Long projectId, boolean value) throws PropertyException;

    Collection<WorklogType> getWorklogTypes(Long projectId) throws PropertyException;

    void setWorklogTypes(Long projectId, Collection<WorklogType> worklogTypes) throws PropertyException;

    Collection<WorklogType> getExcludedWorklogTypes(@Nonnull Long projectId) throws PropertyException;

    void setExcludedWorklogTypes(@Nonnull Long projectId, Collection<WorklogType> worklogTypes) throws PropertyException;

    boolean isWLTypeRequired(Long projectId) throws PropertyException;

    void setWLTypeRequired(Long projectId, boolean value) throws PropertyException;

    WorklogType getDefaultWorklogType(@Nonnull Long projectId) throws PropertyException;

    void setDefaultWorklogType(@Nonnull Long projectId, String worklogTypeId) throws PropertyException;

    boolean isWLCommentRequired(@Nonnull Long projectId) throws PropertyException;

    void setWlCommentRequired(@Nonnull Long projectId, boolean value) throws PropertyException;

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
