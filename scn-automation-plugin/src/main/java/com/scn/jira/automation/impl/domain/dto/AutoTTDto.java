package com.scn.jira.automation.impl.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonAutoDetect
public class AutoTTDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    @NotNull
    @Valid
    private UserDto user;
    @NotNull
    @Valid
    private ProjectDto project;
    @NotNull
    @Valid
    private IssueDto issue;
    @NotNull
    @Valid
    private WorklogTypeDto worklogType;
    @NotBlank
    private String ratedTime;
    private boolean active;
    private UserDto author;
    private UserDto updateAuthor;
    private Timestamp created;
    private Timestamp updated;
}
