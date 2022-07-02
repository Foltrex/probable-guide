package com.scn.jira.automation.impl.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonAutoDetect
public class ProjectDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private Long id;
    private String key;
    private String name;
}
