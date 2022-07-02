package com.scn.jira.automation.impl.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect
public class UserDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String key;
    private String name;
    private String username;
    private boolean active;
}
