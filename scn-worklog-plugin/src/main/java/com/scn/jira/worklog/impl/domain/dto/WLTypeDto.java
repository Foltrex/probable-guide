package com.scn.jira.worklog.impl.domain.dto;

import lombok.*;
import org.codehaus.jackson.annotate.JsonAutoDetect;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
@JsonAutoDetect
public class WLTypeDto {
    private Long id;
    private String name;
    private int sequence;
    private String description;
    private String iconUri;
    private String statusColor;
}
