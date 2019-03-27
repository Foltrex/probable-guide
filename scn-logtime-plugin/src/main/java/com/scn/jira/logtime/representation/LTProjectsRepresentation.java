package com.scn.jira.logtime.representation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

/**
 * JAXB representation of a group of projects.
 */
@Immutable
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class LTProjectsRepresentation {
	@XmlElement
	private Collection<LTProjectRepresentation> projects;

	@XmlElement
	private String userName;

	@XmlElement
	private String userKey;

	@XmlElement
	private String scnProjectsTotal;

	@XmlElement
	private String extProjectsTotal;

	@XmlElement
	private List<String> scnWlTotal;

	@XmlElement
	private List<String> extWlTotal;
	
	@XmlElement
	private List<WeekRepresentation> weekRepresentations;

	// This private constructor isn't used by any code, but JAXB requires any
	// representation class to have a no-args constructor.
	public LTProjectsRepresentation() {
		this.projects = null;
		this.scnWlTotal = new ArrayList<String>();
		this.extWlTotal = new ArrayList<String>();
		this.weekRepresentations = new ArrayList<WeekRepresentation>();
		this.extProjectsTotal = "00:00";
		this.scnProjectsTotal = "00:00";
	}

	/**
	 * Stores the specified {@code Project}s in this representation.
	 * 
	 * @param projects
	 *            the projects to store
	 */
	public LTProjectsRepresentation(Iterable<LTProjectRepresentation> projects) {
		this.projects = new HashSet<LTProjectRepresentation>();
		for (LTProjectRepresentation representation : projects) {
			this.projects.add(representation);
		}
	}

	public Collection<LTProjectRepresentation> getProjects() {
		return projects;
	}

	public void setProjects(Collection<LTProjectRepresentation> projects) {
		this.projects = projects;
	}

	public List<String> getScnWlTotal() {
		return scnWlTotal;
	}

	public void setScnWlTotal(List<String> scnWlTotal) {
		this.scnWlTotal = scnWlTotal;
	}

	public List<String> getExtWlTotal() {
		return extWlTotal;
	}

	public void setExtWlTotal(List<String> extWlTotal) {
		this.extWlTotal = extWlTotal;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getScnProjectsTotal() {
		return scnProjectsTotal;
	}

	public void setScnProjectsTotal(String scnProjectsTotal) {
		this.scnProjectsTotal = scnProjectsTotal;
	}

	public String getExtProjectsTotal() {
		return extProjectsTotal;
	}

	public void setExtProjectsTotal(String extProjectsTotal) {
		this.extProjectsTotal = extProjectsTotal;
	}

	public List<WeekRepresentation> getWeekRepresentations() {
		return weekRepresentations;
	}

	public void setWeekRepresentations(List<WeekRepresentation> weekRepresentations) {
		this.weekRepresentations = weekRepresentations;
	}
	
	

}
