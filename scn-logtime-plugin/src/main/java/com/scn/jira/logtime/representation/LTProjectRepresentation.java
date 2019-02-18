package com.scn.jira.logtime.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

import com.atlassian.jira.project.Project;

/**
 * JAXB representation of a project's information. This can be marshalled as
 * either JSON or XML, depending on what the client asks for.
 */
@Immutable
@XmlRootElement
public class LTProjectRepresentation {
	@XmlElement
	private Long id;

	@XmlElement
	private String key;

	@XmlElement
	private String name;

	@XmlElement
	private Long rowspan;

	@XmlElement
	private List<LTIssueRepresentation> issues;

	@XmlElement
	private String scnPrTotal;

	@XmlElement
	private String extPrTotal;
	
	@XmlElement
	private Boolean permission;

	@XmlElement
	private Map<String, Integer> scnWlTotal;

	@XmlElement
	private Map<String, Integer> extWlTotal;

	// This private constructor isn't used by any code, but JAXB requires any
	// representation class to have a no-args constructor.
	public LTProjectRepresentation() {
		issues = null;
	}

	/**
	 * Initializes the representation's values to those in the specified
	 * {@code Project}.
	 * 
	 * @param project
	 *            the project to use for initialization
	 */
	public LTProjectRepresentation(Project project,
			Iterable<LTIssueRepresentation> issues) {
		this.id = project.getId();
		this.key = project.getKey();
		this.name = project.getName();
		this.scnWlTotal = new HashMap<String, Integer>();
		this.extWlTotal = new HashMap<String, Integer>();
		this.extPrTotal = "00:00";
		this.extPrTotal = "00:00";
		this.permission=false;

		this.issues = new ArrayList<LTIssueRepresentation>();
		for (LTIssueRepresentation representation : issues) {
			this.issues.add(representation);
		}
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

	public List<LTIssueRepresentation> getIssues() {
		return issues;
	}

	public void setIssues(List<LTIssueRepresentation> issues) {
		this.issues = issues;
	}

	public Long getRowspan() {
		return rowspan;
	}

	public void setRowspan(Long rowspan) {
		this.rowspan = rowspan;
	}

	public Map<String, Integer> getScnWlTotal() {
		return scnWlTotal;
	}

	public void setScnWlTotal(Map<String, Integer> scnWlTotal) {
		this.scnWlTotal = scnWlTotal;
	}

	public Map<String, Integer> getExtWlTotal() {
		return extWlTotal;
	}

	public void setExtWlTotal(Map<String, Integer> extWlTotal) {
		this.extWlTotal = extWlTotal;
	}

	public String getScnPrTotal() {
		return scnPrTotal;
	}

	public void setScnPrTotal(String scnPrTotal) {
		this.scnPrTotal = scnPrTotal;
	}

	public String getExtPrTotal() {
		return extPrTotal;
	}

	public void setExtPrTotal(String extPrTotal) {
		this.extPrTotal = extPrTotal;
	}

	public Boolean getPermission() {
		return permission;
	}

	public void setPermission(Boolean permission) {
		this.permission = permission;
	}
	
	

}
