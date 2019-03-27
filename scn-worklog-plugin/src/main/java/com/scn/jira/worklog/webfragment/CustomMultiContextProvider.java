package com.scn.jira.worklog.webfragment;

import com.atlassian.jira.plugin.webfragment.contextproviders.MultiContextProvider;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.web.WebFragmentHelper;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CustomMultiContextProvider extends MultiContextProvider {

    @Inject
    public CustomMultiContextProvider(@ComponentImport PluginAccessor pluginAccessor,
                                      @ComponentImport WebFragmentHelper webFragmentHelper) {
        super(pluginAccessor, webFragmentHelper);
    }
}
