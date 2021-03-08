package com.scn.jira.automation.impl.domain.dto;

import lombok.*;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonAutoDetect
public class ProjectDto {
    private Long id;
    private String key;
    private String name;
}
