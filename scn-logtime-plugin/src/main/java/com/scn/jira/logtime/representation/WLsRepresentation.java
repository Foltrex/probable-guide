package com.scn.jira.logtime.representation;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

/**
 * JAXB representation of a project's information. This can be marshalled as
 * either JSON or XML, depending on what the client asks for.
 */
@Immutable
@XmlRootElement
public class WLsRepresentation {
	@XmlElement
	private String day;

	@XmlElement
	private String wlTypeId;

	@XmlElement
	private String wlTypeName;

	@XmlElement
	private WLRepresentation wlScnRepresentation;

	@XmlElement
	private WLRepresentation wlExtRepresentation;

	@XmlElement
	private Map<String, WLRepresentation> wlScnRepresentationMap;

	@XmlElement
	private Map<String, WLRepresentation> wlExtRepresentationMap;

	// This private constructor isn't used by any code, but JAXB requires any
	// representation class to have a no-args constructor.
	public WLsRepresentation() {
		day = null;
		wlTypeId = null;
		wlTypeName = null;
		wlScnRepresentation = new WLRepresentation();
		wlScnRepresentation.setTimeSpent("0");
		wlExtRepresentation = new WLRepresentation();
		wlExtRepresentation.setTimeSpent("0");
		;
	}
	
	public WLsRepresentation(String date, Integer status) {
		day = null;
		wlTypeId = null;
		wlTypeName = null;
		wlScnRepresentation = new WLRepresentation(date,status);
		wlExtRepresentation = new WLRepresentation(date,status);
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getWlTypeId() {
		return wlTypeId;
	}

	public void setWlTypeId(String wlTypeId) {
		this.wlTypeId = wlTypeId;
	}

	public String getWlTypeName() {
		return wlTypeName;
	}

	public void setWlTypeName(String wlTypeName) {
		this.wlTypeName = wlTypeName;
	}

	public WLRepresentation getWlScnRepresentation() {
		return wlScnRepresentation;
	}

	public void setWlScnRepresentation(WLRepresentation wlScnRepresentation) {
		this.wlScnRepresentation = wlScnRepresentation;
	}

	public WLRepresentation getWlExtRepresentation() {
		return wlExtRepresentation;
	}

	public void setWlExtRepresentation(WLRepresentation wlExtRepresentation) {
		this.wlExtRepresentation = wlExtRepresentation;
	}

	public Map<String, WLRepresentation> getWlScnRepresentationMap() {
		return wlScnRepresentationMap;
	}

	public void setWlScnRepresentationMap(
			Map<String, WLRepresentation> wlScnRepresentationMap) {
		this.wlScnRepresentationMap = wlScnRepresentationMap;
	}

	public Map<String, WLRepresentation> getWlExtRepresentationMap() {
		return wlExtRepresentationMap;
	}

	public void setWlExtRepresentationMap(
			Map<String, WLRepresentation> wlExtRepresentationMap) {
		this.wlExtRepresentationMap = wlExtRepresentationMap;
	}

}