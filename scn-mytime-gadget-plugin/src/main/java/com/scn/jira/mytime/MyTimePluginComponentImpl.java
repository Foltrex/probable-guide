package com.scn.jira.mytime;

import org.springframework.stereotype.Component;

@Component
public class MyTimePluginComponentImpl implements MyTimePluginComponent {
    public String getName() {
        return "My time plugin";
    }
}
