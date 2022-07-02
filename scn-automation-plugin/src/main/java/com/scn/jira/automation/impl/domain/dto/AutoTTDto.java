package com.scn.jira.automation.impl.domain.dto;

import com.scn.jira.common.json.FormattedDuration;
import com.scn.jira.common.json.LocalDateDeserializer;
import com.scn.jira.common.json.LocalDateSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;

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
    @FormattedDuration
    private String ratedTime;
    @NotNull
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate startDate;
    private boolean active;
    private UserDto author;
    private UserDto updateAuthor;
    private Timestamp created;
    private Timestamp updated;
}
