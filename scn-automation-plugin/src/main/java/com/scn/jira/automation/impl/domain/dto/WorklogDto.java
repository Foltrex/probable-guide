package com.scn.jira.automation.impl.domain.dto;

import com.scn.jira.automation.impl.domain.entity.ScnWorklog;
import com.scn.jira.automation.impl.domain.entity.Worklog;

import javax.annotation.Nonnull;
import java.sql.Timestamp;

public class WorklogDto {
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

    public WorklogDto() {
    }

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

    public WorklogKind getWorklogKind() {
        return worklogKind;
    }

    public void setWorklogKind(WorklogKind worklogKind) {
        this.worklogKind = worklogKind;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getWorklogBody() {
        return worklogBody;
    }

    public void setWorklogBody(String worklogBody) {
        this.worklogBody = worklogBody;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Long getTimeWorked() {
        return timeWorked;
    }

    public void setTimeWorked(Long timeWorked) {
        this.timeWorked = timeWorked;
    }

    public String getWorklogTypeId() {
        return worklogTypeId;
    }

    public void setWorklogTypeId(String worklogTypeId) {
        this.worklogTypeId = worklogTypeId;
    }

    public String getAuthorKey() {
        return authorKey;
    }

    public void setAuthorKey(String authorKey) {
        this.authorKey = authorKey;
    }

    public String getUpdateAuthorKey() {
        return updateAuthorKey;
    }

    public void setUpdateAuthorKey(String updateAuthorKey) {
        this.updateAuthorKey = updateAuthorKey;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Timestamp getUpdated() {
        return updated;
    }

    public void setUpdated(Timestamp updated) {
        this.updated = updated;
    }

    public Long getLinkedWorklogId() {
        return linkedWorklogId;
    }

    public void setLinkedWorklogId(Long linkedWorklogId) {
        this.linkedWorklogId = linkedWorklogId;
    }
}
