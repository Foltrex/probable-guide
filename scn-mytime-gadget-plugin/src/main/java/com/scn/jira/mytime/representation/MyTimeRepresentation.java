package com.scn.jira.mytime.representation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

@Immutable
@XmlRootElement
public class MyTimeRepresentation {
	@XmlElement
	private String html;

	public MyTimeRepresentation() {
	}

	public MyTimeRepresentation(String html) {
		this.html = html;
	}
}