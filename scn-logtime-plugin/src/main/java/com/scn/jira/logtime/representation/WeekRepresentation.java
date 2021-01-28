package com.scn.jira.logtime.representation;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

/**
 * JAXB representation of a project's information. This can be marshalled as
 * either JSON or XML, depending on what the client asks for.
 */
@Immutable
@XmlRootElement
public class WeekRepresentation {

	@XmlElement
	private String weekNumber;

	@XmlElement
	private List<DayRepresentation> dayRepresentations;

	public WeekRepresentation(String weekNumber,
			List<DayRepresentation> dayRepresentations) {
		super();
		this.weekNumber = weekNumber;
		this.dayRepresentations = dayRepresentations;
	}

	public String getWeekNumber() {
		return weekNumber;
	}

	public void setWeekNumber(String weekNumber) {
		this.weekNumber = weekNumber;
	}

	public List<DayRepresentation> getDayRepresentations() {
		return dayRepresentations;
	}

	public void setDayRepresentations(List<DayRepresentation> dayRepresentations) {
		this.dayRepresentations = dayRepresentations;
	}

}