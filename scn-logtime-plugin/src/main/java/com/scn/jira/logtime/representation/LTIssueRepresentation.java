package com.scn.jira.logtime.representation;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

import net.jcip.annotations.Immutable;

/**
 * JAXB representation of a project's information. This can be marshalled as either JSON or XML, depending on what the client asks
 * for.
 */
@Immutable
@XmlRootElement
public class LTIssueRepresentation {
	@XmlElement
	private Long id;

	@XmlElement
	private String key;

	@XmlElement
	private String name;

	@XmlElement
	private String urlName;

	@XmlElement
	private Long rowspan;

	@XmlElement
	private Collection<WLLineIssueRepresentation> wlTypes;

	public LTIssueRepresentation() {
		id = null;
		key = null;
		name = null;
		rowspan = null;
		wlTypes = new HashSet<WLLineIssueRepresentation>();
		urlName = null;
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

	public Collection<WLLineIssueRepresentation> getWlTypes() {
		return wlTypes;
	}

	public void setWlTypes(Collection<WLLineIssueRepresentation> wlTypes) {
		this.wlTypes = wlTypes;
	}

	public Long getRowspan() {
		return rowspan;
	}

	public void setRowspan(Long rowspan) {
		this.rowspan = rowspan;
	}

	public String getUrlName() {
		return urlName;
	}

	public void setUrlName(String urlName) {
		this.urlName = urlName;
	}

}