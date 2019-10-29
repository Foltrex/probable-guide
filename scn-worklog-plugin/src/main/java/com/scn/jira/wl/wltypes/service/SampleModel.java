package com.scn.jira.wl.wltypes.service;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SampleModel {
	@XmlElement
	private String firstName;

	@XmlElement
	private String lastName;

	public SampleModel() {
	}

	public SampleModel(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

}