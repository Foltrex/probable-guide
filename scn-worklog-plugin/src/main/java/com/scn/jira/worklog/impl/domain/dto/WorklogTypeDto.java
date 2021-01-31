package com.scn.jira.worklog.impl.domain.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WorklogTypeDto {
    @XmlElement
    private String id;
    @XmlElement
    private String name;

    public WorklogTypeDto() {
    }

    public WorklogTypeDto(String id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "WorklogTypeDto{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            '}';
    }
}
