package com.scn.confluence.spm.impl.action;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.oscache.web.filter.CacheHttpServletResponseWrapper;
import com.opensymphony.webwork.ServletActionContext;
import lombok.RequiredArgsConstructor;
import org.apache.velocity.VelocityContext;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DynamicPermissionAction extends ConfluenceActionSupport {

    private final SpaceManager spaceManager;

    private Collection<String> spaceKeys;

    @Override
    public String execute() throws Exception {
        spaceKeys = spaceManager.getAllSpaces()
            .stream()
            .map(Space::getKey)
            .collect(Collectors.toList());
        return super.execute();
    }

    public Collection<String> getSpaceKeys() {
        return spaceKeys;
    }
}
