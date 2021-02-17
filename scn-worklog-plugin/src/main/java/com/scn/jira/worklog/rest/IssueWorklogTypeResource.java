package com.scn.jira.worklog.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import com.scn.jira.worklog.impl.domain.dto.WorklogTypeDto;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Path("/issue")
@Named
@Provider
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class IssueWorklogTypeResource extends BaseResource {
    private final ExtendedConstantsManager extendedConstantsManager;
    private final IScnProjectSettingsManager projectSettignsManager;
    private final IssueManager issueManager;

    @Autowired
    public IssueWorklogTypeResource(JiraAuthenticationContext jiraAuthenticationContext,
                                    ExtendedConstantsManager extendedConstantsManager,
                                    IScnProjectSettingsManager projectSettignsManager,
                                    IssueManager issueManager) {
        super(jiraAuthenticationContext);
        this.extendedConstantsManager = extendedConstantsManager;
        this.projectSettignsManager = projectSettignsManager;
        this.issueManager = issueManager;
    }

    @GET
    @Path("/{issueIdOrKey}/worklog-types")
    public Response getIssueWorklogTypes(@PathParam("issueIdOrKey") String issueIdOrKey) {
        Issue issue;
        try {
            Long issueId = Long.parseLong(issueIdOrKey);
            issue = issueManager.getIssueObject(issueId);
        } catch (NumberFormatException e) {
            issue = issueManager.getIssueObject(issueIdOrKey);
        }

        if (issue == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Collection<WorklogType> worklogTypes = extendedConstantsManager.getWorklogTypeObjects();
        Collection<WorklogType> excludedWorklogTypes = this.projectSettignsManager.getExcludedWorklogTypes(
            Objects.requireNonNull(issue.getProjectId()));

        return Response.ok(
            worklogTypes.stream()
                .filter(worklogType -> !excludedWorklogTypes.contains(worklogType))
                .map(worklogType -> new WorklogTypeDto(worklogType.getId(), worklogType.getName()))
                .collect(Collectors.toList())
        ).build();
    }
}
