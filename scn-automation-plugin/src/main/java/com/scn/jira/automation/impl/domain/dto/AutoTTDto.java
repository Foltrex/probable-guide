package com.scn.jira.automation.impl.domain.dto;

import com.scn.jira.automation.api.domain.service.JiraContextService;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.impl.domain.entity.AutoTT;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;

@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonAutoDetect
public class AutoTTDto {
    private Long id;
    private UserDto user;
    private ProjectDto project;
    private IssueDto issue;
    @Nullable
    private WorklogTypeDto worklogType;
    private String ratedTime;
    private boolean active;
    private UserDto author;
    private UserDto updateAuthor;
    private Timestamp created;
    private Timestamp updated;

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
}
