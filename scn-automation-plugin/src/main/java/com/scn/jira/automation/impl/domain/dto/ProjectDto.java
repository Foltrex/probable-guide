package com.scn.jira.automation.impl.domain.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ProjectDto {
    @XmlElement
    private Long id;
    @XmlElement
    private String key;
    @XmlElement
    private String name;

    private ProjectDto() {
    }

    public ProjectDto(Long id, String key, String name) {
        this();
        this.id = id;
        this.key = key;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ProjectDto{" +
            "id=" + id +
            ", key='" + key + '\'' +
            ", name='" + name + '\'' +
            '}';
    }
}
