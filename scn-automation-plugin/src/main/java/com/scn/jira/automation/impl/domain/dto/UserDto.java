package com.scn.jira.automation.impl.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonAutoDetect;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect
public class UserDto {
    private String key;
    private String name;
    private String username;
}
