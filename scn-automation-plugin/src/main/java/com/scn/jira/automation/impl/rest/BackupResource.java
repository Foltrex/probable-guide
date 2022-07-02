package com.scn.jira.automation.impl.rest;

import com.scn.jira.automation.api.domain.service.PermissionProvider;
import com.scn.jira.automation.api.domain.service.WorklogBackupService;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;
import com.scn.jira.common.exception.ErrorResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/worklog/backup")
@Provider
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class BackupResource extends BaseResource {
    private final WorklogBackupService worklogBackupService;
    private final PermissionProvider permissionProvider;

    @POST
    public Response doBackup(@QueryParam("pid") Long pid,
                             @QueryParam("from") String from,
                             @QueryParam("to") String to) throws ParseException {
        if (!this.permissionProvider.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorResult("No permissions to backup.")).build();
        }
        worklogBackupService.makeBackup(pid,
            from == null ? null : this.parseDate(from),
            to == null ? null : this.parseDate(to));
        return Response.ok().build();
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
        List<WorklogDto> worklogs = worklogBackupService.getAllByProject(
            pid, from == null ? null : this.parseDate(from), to == null ? null : this.parseDate(to)
        );
        List<WorklogDto> scnWorklogs = worklogBackupService.getAllScnByProject(
            pid, from == null ? null : this.parseDate(from), to == null ? null : this.parseDate(to)
        );
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
