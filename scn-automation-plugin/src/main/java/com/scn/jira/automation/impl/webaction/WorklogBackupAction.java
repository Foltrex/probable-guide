package com.scn.jira.automation.impl.webaction;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;

@Named
@RequiredArgsConstructor
public class WorklogBackupAction extends JiraWebActionSupport {
    private final PageBuilderService pageBuilderService;

    @Override
    public String execute() throws Exception {
        pageBuilderService.assembler().resources()
            .requireWebResource("com.scn.jira.automation.scn-automation-plugin:entrypoint-worklog-backup");

        return super.execute();
    }
}
