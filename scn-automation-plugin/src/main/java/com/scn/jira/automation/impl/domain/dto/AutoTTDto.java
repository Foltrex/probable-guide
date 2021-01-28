package com.scn.jira.automation.impl.domain.dto;

import com.scn.jira.automation.api.domain.service.JiraContextService;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.impl.domain.entity.AutoTT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;

@XmlRootElement
public class AutoTTDto {
    @XmlElement
    private Long id;
    @XmlElement
    private UserDto user;
    @XmlElement
    private ProjectDto project;
    @XmlElement
    private IssueDto issue;
    @XmlElement
    private WorklogTypeDto worklogType;
    @XmlElement
    private String ratedTime;
    @XmlElement
    private boolean active;
    @XmlElement
    private UserDto author;
    @XmlElement
    private UserDto updateAuthor;
    @XmlElement
    private Timestamp created;
    @XmlElement
    private Timestamp updated;

    private AutoTTDto() {
    }

    public AutoTTDto(@Nonnull JiraContextService contextService, @Nonnull WorklogContextService worklogContextService,
                     @Nonnull AutoTT autoTT) {
        this();
        this.setId(autoTT.getId());
        this.setUser(contextService.getUserDto(autoTT.getUserKey()));
        this.setProject(contextService.getProjectDto(autoTT.getProjectId()));
        this.setIssue(contextService.getIssueDto(autoTT.getIssueId()));
        this.setWorklogType(worklogContextService.getWorklogType(autoTT.getWorklogTypeId()));
        this.setRatedTime(worklogContextService.getFormattedTime(autoTT.getRatedTime()));
        this.setActive(autoTT.getActive());
        this.setAuthor(contextService.getUserDto(autoTT.getAuthorKey()));
        this.setUpdateAuthor(contextService.getUserDto(autoTT.getUpdateAuthorKey()));
        this.setCreated(autoTT.getCreated());
        this.setUpdated(autoTT.getUpdated());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public ProjectDto getProject() {
        return project;
    }

    public void setProject(ProjectDto project) {
        this.project = project;
    }

    public IssueDto getIssue() {
        return issue;
    }

    public void setIssue(IssueDto issue) {
        this.issue = issue;
    }

    @Nullable
    public WorklogTypeDto getWorklogType() {
        return worklogType;
    }

    public void setWorklogType(WorklogTypeDto worklogType) {
        this.worklogType = worklogType;
    }

    public String getRatedTime() {
        return ratedTime;
    }

    public void setRatedTime(String ratedTime) {
        this.ratedTime = ratedTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UserDto getAuthor() {
        return author;
    }

    public void setAuthor(UserDto author) {
        this.author = author;
    }

    public UserDto getUpdateAuthor() {
        return updateAuthor;
    }

    public void setUpdateAuthor(UserDto updateAuthor) {
        this.updateAuthor = updateAuthor;
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

    @Override
    public String toString() {
        return "AutoTTDto{" +
            "id=" + id +
            ", user=" + user +
            ", project=" + project +
            ", issue=" + issue +
            ", worklogType=" + worklogType +
            ", ratedTime=" + ratedTime +
            ", active=" + active +
            ", author=" + author +
            ", updateAuthor=" + updateAuthor +
            ", created=" + created +
            ", updated=" + updated +
            '}';
    }
}
