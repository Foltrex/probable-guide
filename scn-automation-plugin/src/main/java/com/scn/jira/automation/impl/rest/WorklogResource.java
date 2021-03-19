package com.scn.jira.automation.impl.rest;

import com.scn.jira.automation.api.domain.service.JiraContextService;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.api.domain.service.WorklogSQLService;
import com.scn.jira.automation.impl.domain.dto.Validator;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
@RequiredArgsConstructor
public class WorklogResource extends BaseResource {
    private final WorklogContextService worklogContextService;
    private final WorklogSQLService worklogSQLService;
    private final JiraContextService contextService;

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
        if (!this.contextService.isCurrentUserAdmin()) {
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
