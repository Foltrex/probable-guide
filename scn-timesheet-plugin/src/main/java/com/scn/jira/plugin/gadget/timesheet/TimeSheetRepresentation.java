package com.scn.jira.plugin.gadget.timesheet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import net.jcip.annotations.Immutable;

@Immutable
@XmlRootElement
public class TimeSheetRepresentation
{

  @XmlElement
  private String html;

  private TimeSheetRepresentation()
  {
  }

  public TimeSheetRepresentation(String html)
  {
    this.html = html;
  }
}