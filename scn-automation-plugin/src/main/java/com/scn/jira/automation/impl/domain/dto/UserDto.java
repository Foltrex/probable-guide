package com.scn.jira.automation.impl.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonAutoDetect;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonAutoDetect
public class UserDto {
    private String key;
    private String name;
}
