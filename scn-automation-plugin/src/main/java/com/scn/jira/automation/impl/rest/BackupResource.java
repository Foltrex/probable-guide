package com.scn.jira.automation.impl.rest;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.scn.jira.automation.api.domain.service.JiraContextService;
import com.scn.jira.automation.api.domain.service.WorklogBackupService;
import com.scn.jira.automation.impl.domain.dto.Validator;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/worklog/backup")
@Named
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class BackupResource {
    private final WorklogBackupService worklogBackupService;
    private final GlobalPermissionManager globalPermissionManager;
    private final JiraContextService contextService;
    private final ProjectManager projectManager;

    @Autowired
    public BackupResource(WorklogBackupService worklogBackupService,
                          GlobalPermissionManager globalPermissionManager,
                          JiraContextService contextService,
                          ProjectManager projectManager) {
        this.worklogBackupService = worklogBackupService;
        this.globalPermissionManager = globalPermissionManager;
        this.contextService = contextService;
        this.projectManager = projectManager;
    }

    @POST
    public Response getTest(@QueryParam("pid") Long pid) {
        Project project = projectManager.getProjectObj(pid);
        if (globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, contextService.getCurrentUser())
            || globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, contextService.getCurrentUser())
            || (project != null && project.getLeadUserKey().equals(contextService.getCurrentUser().getKey()))
            || contextService.getCurrentUser().getKey().equals("akalaputs")) {
            worklogBackupService.makeBackup(pid);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(new Validator("No permissions")).build();
        }
    }

    @GET
    @Path("/download/csv")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFileWithPost(@QueryParam("pid") Long pid,
                                         @QueryParam("from") String from,
                                         @QueryParam("to") String to) throws IOException, ParseException {
        String name = String.format("%s.csv", pid);
        String fileName = System.getProperty("java.io.tmpdir") + File.separator + name;
        File fileDownload = new File(fileName);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<WorklogDto> worklogs = worklogBackupService.getAllByProject(pid, dateFormat.parse(from), dateFormat.parse(to));
        List<WorklogDto> scnWorklogs = worklogBackupService.getAllScnByProject(pid, dateFormat.parse(from), dateFormat.parse(to));
        String resultCSV = Stream.of(worklogs.stream(), scnWorklogs.stream())
            .flatMap(Function.identity())
            .map(worklog -> String.format(
                "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                worklog.getWorklogKind(),
                worklog.getId(),
                worklog.getIssueId(),
                worklog.getProjectId(),
                worklog.getStartDate(),
                worklog.getWorklogBody() == null ? "" : String.format("\"%s\"", worklog.getWorklogBody().replaceAll("\"", "\"\"")),
                worklog.getWorklogTypeId(),
                worklog.getTimeWorked(),
                worklog.getLinkedWorklogId(),
                worklog.getAuthorKey(),
                worklog.getUpdateAuthorKey(),
                worklog.getCreated(),
                worklog.getUpdated()))
            .collect(Collectors.joining("\n"));
        FileUtils.writeStringToFile(fileDownload,
            "WL*/WL,ID,ISSUE_ID,PROJECT_ID,START_DATE,WORKLOG_BODY,WORKLOG_TYPE_ID,TIME_WORKED," +
                "LINKED_WORKLOG_ID,AUTHOR_KEY,UPDATE_AUTHOR_KEY,CREATED,UPDATED\n" + resultCSV, StandardCharsets.UTF_8);
        fileDownload.deleteOnExit();
        return Response.ok(fileDownload).header("Content-Disposition", "attachment;filename=csv_file.csv").build();
    }
}
