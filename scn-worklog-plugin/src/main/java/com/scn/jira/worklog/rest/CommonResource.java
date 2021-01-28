package com.scn.jira.worklog.rest;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import com.scn.jira.worklog.remote.service.IRemoteScnExtIssueService;
import com.scn.jira.worklog.remote.service.object.BlockedProject;
import com.scn.jira.worklog.remote.service.object.RemoteScnExtIssue;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Path("/")
public class CommonResource extends BaseResource {
    private final IRemoteScnExtIssueService remoteScnExtIssueService;
    private final IGlobalSettingsManager settingsManager;
    private final ProjectManager projectManager;
    private final IScnProjectSettingsManager projectSettingManager;
    private final GlobalPermissionManager permissionManager;

    @Inject
    public CommonResource(IRemoteScnExtIssueService remoteScnExtIssueService, IGlobalSettingsManager settingsManager,
                          ProjectManager projectManager, IScnProjectSettingsManager projectSettingManager,
                          GlobalPermissionManager permissionManager, JiraAuthenticationContext jiraAuthenticationContext) {
        super(jiraAuthenticationContext);
        this.remoteScnExtIssueService = remoteScnExtIssueService;
        this.settingsManager = settingsManager;
        this.projectManager = projectManager;
        this.projectSettingManager = projectSettingManager;
        this.permissionManager = permissionManager;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/scn-ext-issues")
    @PublicApi
    public Response getScnExtendedIssues(@QueryParam("ikey") List<String> issueKeys) throws RemoteException {
        ApplicationUser user = this.getApplicationUser();
        if (user == null)
            return Response.status(Status.BAD_REQUEST).entity("User credentials are not valid. ").build();
        if (issueKeys == null || issueKeys.isEmpty())
            return Response.status(Status.BAD_REQUEST).entity("Issue keys can't be NULL or Empty. ").build();
        RemoteScnExtIssue[] scnExtIssues = remoteScnExtIssueService.getScnExtIssues(user.getDirectoryUser(), issueKeys);
        if (scnExtIssues == null || scnExtIssues.length == 0)
            return Response.ok("There are no SCN Extended Issues or you don't have permission to see them. ")
                .cacheControl(this.getNoCacheControl()).build();
        return Response.ok(scnExtIssues).cacheControl(getNoCacheControl()).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/globalsettings/moveGroup")
    @PublicApi
    public Response moveGlobalSecurityGroup(@FormParam("operation") String operation, @FormParam("groupnames") List<String> groupnames) {
        // TODO: Admin user required???
        ApplicationUser user = this.getApplicationUser();
        if (user == null)
            return Response.status(Status.BAD_REQUEST).entity("User credentials are not valid. ").build();
        if (operation.equals("add")) {
            settingsManager.addGroups(groupnames);
        } else if (operation.equals("remove")) {
            settingsManager.removeGroups(groupnames);
        } else
            return Response.status(Status.BAD_REQUEST).entity("Unknown operation code. ").build();
        return Response.ok().cacheControl(this.getNoCacheControl()).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/project/wl/blocking")
    @PublicApi
    public Response setWLBlockingDate(@Nonnull List<BlockedProject> projects) {
        if (!permissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, this.getApplicationUser()))
            return Response.status(Status.BAD_REQUEST).entity("Don't have permission.").build();
        try {
            projectSettingManager.setWLBlockingDate(projectManager.getProjectObjByKey("projectKey").getId(),
                new SimpleDateFormat("yyyyMMddhhmmss").parse("123123123"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return Response.ok().cacheControl(this.getNoCacheControl()).entity("OK").build();
    }
}
