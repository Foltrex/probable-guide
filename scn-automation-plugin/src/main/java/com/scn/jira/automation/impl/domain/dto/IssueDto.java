package com.scn.jira.automation.impl.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueDto {
    @XmlElement
    private Long id;
    @XmlElement
    private String key;
    @XmlElement
    private String name;
}

