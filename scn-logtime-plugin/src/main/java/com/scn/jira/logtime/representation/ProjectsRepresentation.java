package com.scn.jira.logtime.representation;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

import net.jcip.annotations.Immutable;

/**
 * JAXB representation of a group of projects.
 */
@Immutable
@XmlRootElement
public class ProjectsRepresentation
{
    @XmlElement
    private Collection<ProjectRepresentation> projects;

    // This private constructor isn't used by any code, but JAXB requires any
    // representation class to have a no-args constructor.
    public ProjectsRepresentation() {
        projects = null;
    }

    /**
     * Stores the specified {@code Project}s in this representation.
     * @param projects the projects to store
     */
    public ProjectsRepresentation(Iterable<ProjectRepresentation> projects) {
        this.projects = new ArrayList<ProjectRepresentation>();
        for (ProjectRepresentation representation : projects)
        {
            this.projects.add(representation);
        }
    }
    public int getSize() {
		return projects.size();
	}

    
	public Collection<ProjectRepresentation> getProjects() {
		return projects;
	}

	public void setProjects(Collection<ProjectRepresentation> projects) {
		this.projects = projects;
	}
    
    
}
