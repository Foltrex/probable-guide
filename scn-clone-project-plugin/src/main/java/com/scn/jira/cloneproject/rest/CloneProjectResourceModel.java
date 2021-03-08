package com.scn.jira.cloneproject.rest;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "status")
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@AllArgsConstructor
public class CloneProjectResourceModel {
    @XmlElement
    public String message;
    @XmlElement
    public String pkey;
    @XmlElement
    public String pname;
    @XmlElement
    public String lead;
    @XmlElement
    public String descr;
    @XmlElement
    public String url;
    @XmlElement
    public Long assigneetype;
    @XmlElement
    public String category;
    // user roles ???
    @XmlElement
    public String workflowscheme;
    @XmlElement
    public String issuetypescheme;
    @XmlElement
    public String issuetypesceeenscheme;
    @XmlElement
    public String fieldconfigurationscheme;
    @XmlElement
    public String permissionscheme;
    @XmlElement
    public String notificationscheme;
    @XmlElement
    public String issuesecurityscheme;
}
