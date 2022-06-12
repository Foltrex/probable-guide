package com.scn.jira.automation.impl.domain.dto;

import com.scn.jira.automation.impl.domain.entity.ScnWorklog;
import com.scn.jira.automation.impl.domain.entity.Worklog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.sql.Timestamp;

@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonAutoDetect
public class WorklogDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private WorklogKind worklogKind;
    private Long id;
    private Long issueId;
    private Long projectId;
    private String worklogBody;
    private Timestamp startDate;
    private Long timeWorked;
    private String worklogTypeId;
    private String authorKey;
    private String updateAuthorKey;
    private Timestamp created;
    private Timestamp updated;
    private Long linkedWorklogId;

    public WorklogDto(@Nonnull Worklog worklog) {
        setWorklogKind(WorklogKind.WORKLOG);
        setId(worklog.getId());
        setIssueId(worklog.getIssueId());
        setProjectId(worklog.getProjectId());
        setWorklogBody(worklog.getWorklogBody());
        setStartDate(worklog.getStartDate());
        setTimeWorked(worklog.getTimeWorked());
        setWorklogTypeId(worklog.getWorklogTypeId());
        setAuthorKey(worklog.getAuthorKey());
        setUpdateAuthorKey(worklog.getUpdateAuthorKey());
        setCreated(worklog.getCreated());
        setUpdated(worklog.getUpdated());
    }

    public WorklogDto(@Nonnull ScnWorklog worklog) {
        this((Worklog) worklog);
        setWorklogKind(WorklogKind.WORKLOG_SCN);
        setLinkedWorklogId(worklog.getWorklog() == null ? null : worklog.getWorklog().getId());
    }
}
