package com.scn.jira.worklog.remote.service.object;

import com.atlassian.jira.rest.Dates;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

@XmlRootElement(name = "project")
@Immutable
public class BlockedProject {
    @XmlElement(name = "projectKey")
    private String projectKey;
    @XmlElement(name = "date")
    @XmlJavaTypeAdapter(value = Dates.DateAdapter.class, type = Date.class)
    private Date date;

    private BlockedProject() {
    }

    public BlockedProject(String projectKey, Date date) {
        this();
        this.projectKey = projectKey;
        this.date = date;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof BlockedProject)) {
            return false;
        }

        return this.getProjectKey().equals(((BlockedProject) obj).getProjectKey());
    }

    @Override
    public int hashCode() {
        return this.projectKey.hashCode();
    }
}
