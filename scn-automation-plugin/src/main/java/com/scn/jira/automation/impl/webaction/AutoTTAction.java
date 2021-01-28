package com.scn.jira.automation.impl.webaction;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;

@Named
public class AutoTTAction extends JiraWebActionSupport {
    private final PageBuilderService pageBuilderService;

    @Autowired
    public AutoTTAction(PageBuilderService pageBuilderService) {
        this.pageBuilderService = pageBuilderService;
    }

    @Override
    public String execute() throws Exception {
        pageBuilderService.assembler().resources()
            .requireWebResource("com.scn.jira.automation.scn-automation-plugin:entrypoint-autotimetracking-table");

        return super.execute();
    }
}
