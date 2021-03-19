package com.scn.jira.timesheet.gadget.timesheet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import net.jcip.annotations.Immutable;

@Immutable
@XmlRootElement
public class TimeSheetRepresentation {

	@XmlElement
	private String html;

	public TimeSheetRepresentation(String html) {
		this.html = html;
	}
}
