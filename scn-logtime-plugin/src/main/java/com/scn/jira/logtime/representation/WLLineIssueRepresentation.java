package com.scn.jira.logtime.representation;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

@Immutable
@XmlRootElement
public class WLLineIssueRepresentation {
	@XmlElement
	private Long issueId;

	@XmlElement
	private Collection<WLsTypeRepresentation> wlsRepresentation;

	// This private constructor isn't used by any code, but JAXB requires any
	// representation class to have a no-args constructor.
	public WLLineIssueRepresentation() {
		issueId = null;
		wlsRepresentation = null;
	}

	public Long getIssueId() {
		return issueId;
	}

	public void setIssueId(Long issueId) {
		this.issueId = issueId;
	}

	public Collection<WLsTypeRepresentation> getWlsRepresentation() {
		return wlsRepresentation;
	}

	public void setWlsRepresentation(
			Collection<WLsTypeRepresentation> wlsRepresentation) {
		this.wlsRepresentation = wlsRepresentation;
	}

}