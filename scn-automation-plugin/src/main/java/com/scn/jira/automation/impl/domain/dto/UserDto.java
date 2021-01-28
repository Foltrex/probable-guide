package com.scn.jira.automation.impl.domain.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserDto {
    @XmlElement
    private String key;
    @XmlElement
    private String name;

    private UserDto() {
    }

    public UserDto(String key, String name) {
        this();
        this.key = key;
        this.name = name;
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
        return "UserDto{" +
            "key='" + key + '\'' +
            ", name='" + name + '\'' +
            '}';
    }
}
