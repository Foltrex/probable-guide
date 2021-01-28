package com.scn.jira.logtime.representation;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

import com.atlassian.jira.project.Project;

import net.jcip.annotations.Immutable;

@Immutable
@XmlRootElement
public class ProjectRepresentation {
	@XmlElement
	private Long id;

	@XmlElement
	private String key;

	@XmlElement
	private String name;

	@XmlElement
	private String projectUrl;

	@XmlElement
	private String projectLead;

	@XmlElement
	private String description;

	// This private constructor isn't used by any code, but JAXB requires any
	// representation class to have a no-args constructor.
	public ProjectRepresentation() {
		id = null;
		key = null;
		name = null;
		projectUrl = null;
		projectLead = null;
		description = null;
	}

	public ProjectRepresentation(Long id, String key, String name) {
		super();
		this.id = id;
		this.key = key;
		this.name = name;
	}

	/**
	 * Initializes the representation's values to those in the specified {@code Project}.
	 * 
	 * @param project
	 *            the project to use for initialization
	 */
	public ProjectRepresentation(Project project) {
		this.id = project.getId();
		this.key = project.getKey();
		this.name = project.getName();
		this.projectUrl = project.getUrl();
		this.projectLead = project.getLeadUserName();
		this.description = project.getDescription();

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

	public String getProjectUrl() {
		return projectUrl;
	}

	public void setProjectUrl(String projectUrl) {
		this.projectUrl = projectUrl;
	}

	public String getProjectLead() {
		return projectLead;
	}

	public void setProjectLead(String projectLead) {
		this.projectLead = projectLead;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
