package com.scn.jira.automation.impl.rest;

import com.atlassian.jira.project.ProjectManager;
import com.scn.jira.automation.api.domain.service.JiraContextService;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.api.domain.service.WorklogSQLService;
import com.scn.jira.automation.impl.domain.dto.Validator;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.text.ParseException;
import java.util.List;

@Path("/worklog")
@Named
@Provider
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class WorklogResource extends BaseResource {
    private final WorklogContextService worklogContextService;
    private final WorklogSQLService worklogSQLService;

    @Autowired
    public WorklogResource(WorklogContextService worklogContextService, WorklogSQLService worklogSQLService,
                           JiraContextService contextService, ProjectManager projectManager) {
        super(contextService, projectManager);
        this.worklogContextService = worklogContextService;
        this.worklogSQLService = worklogSQLService;
    }

    @GET
    @Path("/type")
    public Response getAllWorklogTypes() {
        return Response.ok(worklogContextService.getAllWorklogTypes()).build();
    }

    @POST
    @Path("/copy-from-scn-worklogs")
    public Response copyFromScnWorklogs(@QueryParam("pid") final Long pid,
                                        @QueryParam("from") final String from,
                                        @QueryParam("to") final String to) throws ParseException {
        if (!this.isAdministrationAllowed()) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(new Validator("No permissions to create WL from WL*.")).build();
        }
        List<WorklogDto> worklogs = worklogSQLService.getAllByProject(
            pid, this.parseDate(from), this.parseDate(to)
        );
        worklogs.forEach(worklogDto -> worklogContextService.deleteWorklogById(worklogDto.getId()));

        List<WorklogDto> scnWorklogs = worklogSQLService.getAllScnByProject(
            pid, this.parseDate(from), this.parseDate(to)
        );
        scnWorklogs.forEach(worklogContextService::createWorklog);

        return Response.ok().build();
    }
}
