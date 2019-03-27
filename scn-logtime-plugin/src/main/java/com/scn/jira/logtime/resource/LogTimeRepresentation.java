package com.scn.jira.logtime.resource;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

@Immutable
@XmlRootElement
public class LogTimeRepresentation {

	@XmlElement
	private String html;

	private LogTimeRepresentation() {
	}

	public LogTimeRepresentation(String html) {
		this.html = html;
	}
}