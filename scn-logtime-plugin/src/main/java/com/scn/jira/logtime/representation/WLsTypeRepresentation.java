package com.scn.jira.logtime.representation;

import java.util.Collection;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

@Immutable
@XmlRootElement
public class WLsTypeRepresentation {
	@XmlElement
	private String id;

	@XmlElement
	private String wlTypeId;

	@XmlElement
	private String wlTypeName;

	@XmlElement
	private Collection<WLsRepresentation> wlsRepresentation;

	@XmlElement
	private Map<String, WLsRepresentation> wlsRepresentationMap;

	// This private constructor isn't used by any code, but JAXB requires any
	// representation class to have a no-args constructor.
	public WLsTypeRepresentation() {
		id = null;
		wlTypeId = null;
		wlTypeName = null;
		wlsRepresentation = null;
	}

	public WLsTypeRepresentation(String id, String wlTypeId, String wlTypeName,
			Collection<WLsRepresentation> wlsRepresentation) {
		super();
		this.id = id;
		this.wlTypeId = wlTypeId;
		this.wlTypeName = wlTypeName;
		this.wlsRepresentation = wlsRepresentation;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Collection<WLsRepresentation> getWlsRepresentation() {
		return wlsRepresentation;
	}

	public void setWlsRepresentation(
			Collection<WLsRepresentation> wlsRepresentation) {
		this.wlsRepresentation = wlsRepresentation;
	}

	public Map<String, WLsRepresentation> getWlsRepresentationMap() {
		return wlsRepresentationMap;
	}

	public void setWlsRepresentationMap(
			Map<String, WLsRepresentation> wlsRepresentationMap) {
		this.wlsRepresentationMap = wlsRepresentationMap;
	}

}