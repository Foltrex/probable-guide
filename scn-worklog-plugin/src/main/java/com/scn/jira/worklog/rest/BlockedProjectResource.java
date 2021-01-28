package com.scn.jira.worklog.rest;

import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.remote.service.object.BlockedProject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

@Path("/blocked/project")
@Named
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes(MediaType.APPLICATION_JSON)
public class BlockedProjectResource extends BaseResource {
    private final GlobalPermissionManager permissionManager;
    private final ProjectManager projectManager;
    private final IScnProjectSettingsManager projectSettingManager;

    @Autowired
    public BlockedProjectResource(JiraAuthenticationContext jiraAuthenticationContext, GlobalPermissionManager permissionManager,
                                  ProjectManager projectManager, IScnProjectSettingsManager projectSettingManager) {
        super(jiraAuthenticationContext);
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.projectSettingManager = projectSettingManager;
    }

    @GET
    @Path("/scnwl")
    public Response getScnWlBlocking(@Nonnull List<BlockedProject> projects) {
        return getBlockingGetResponse(projects, projectSettingManager::getWLBlockingDate);
    }

    @GET
    @Path("/wl")
    public Response getWlBlocking(@Nonnull List<BlockedProject> projects) {
        return getBlockingGetResponse(projects, projectSettingManager::getWLWorklogBlockingDate);
    }

    @POST
    @Path("/scnwl")
    public Response setScnWlBlocking(@Nonnull List<BlockedProject> projects) {
        return getBlockingPostResponse(projects, projectSettingManager::setWLBlockingDate);
    }

    @POST
    @Path("/wl")
    public Response setWlBlocking(@Nonnull List<BlockedProject> projects) {
        return getBlockingPostResponse(projects, projectSettingManager::setWLWorklogBlockingDate);
    }

    private Response getBlockingGetResponse(@Nonnull List<BlockedProject> projects, LongFunction<Date> getter) {
        if (!permissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, this.getApplicationUser())
            && !this.getApplicationUser().getKey().equals("atlasdev")) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Don't have permission.").build();
        }

        BlockedProject[] blockedProjects = projects.stream().distinct()
            .map(project -> projectManager.getProjectObjByKey(project.getProjectKey()))
            .filter(Objects::nonNull)
            .map(project -> new BlockedProject(project.getKey(), getter.apply(project.getId())))
            .toArray(BlockedProject[]::new);
        return Response.ok(blockedProjects).cacheControl(this.getNoCacheControl()).build();
    }

    private Response getBlockingPostResponse(@Nonnull List<BlockedProject> projects, BiConsumer<Long, Date> consumer) {
        if (!permissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, this.getApplicationUser())
            && !this.getApplicationUser().getKey().equals("atlasdev")) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Don't have permission.").build();
        }

        Map<Long, BlockedProject> projectsMap = projects.stream().distinct()
            .collect(Collectors.toMap(project -> project, project -> projectManager.getProjectObjByKey(project.getProjectKey())))
            .entrySet().stream().filter(entry -> entry.getValue() != null)
            .collect(Collectors.toMap(entry -> entry.getValue().getId(), Entry::getKey));

        projectsMap.forEach((id, blockedProject) -> consumer.accept(id, blockedProject.getDate()));

        return Response.ok(projectsMap.values().toArray(new BlockedProject[0])).cacheControl(this.getNoCacheControl()).build();
    }
}
