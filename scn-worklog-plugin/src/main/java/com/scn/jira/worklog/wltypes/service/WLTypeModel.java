package com.scn.jira.worklog.wltypes.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "wltype")
@XmlAccessorType(XmlAccessType.FIELD)
public class WLTypeModel {
	@XmlElement(name = "id")
	public int id;
	@XmlElement(name = "name")
	public String name;
	@XmlElement(name = "description")
	public String description;
	@XmlElement(name = "icon")
	public String icon;
	@XmlElement(name = "sequence")
	public int sequence;

	public WLTypeModel() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIconUri() {
		return icon;
	}

	public void setIconUri(String icon) {
		this.icon = icon;
	}
}
