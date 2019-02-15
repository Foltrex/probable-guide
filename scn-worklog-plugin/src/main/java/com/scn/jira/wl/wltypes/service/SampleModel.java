package com.scn.jira.wl.wltypes.service;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SampleModel
{
    @XmlElement
    private String firstName;

    @XmlElement
    private String lastName;

    // This private constructor isn't used by any code, but JAXB requires any
    // representation class to have a no-args constructor.
    private SampleModel() { }

    public SampleModel(String firstName, String lastName)
    {
        this.firstName = firstName;
        this.lastName = lastName;
    }

}