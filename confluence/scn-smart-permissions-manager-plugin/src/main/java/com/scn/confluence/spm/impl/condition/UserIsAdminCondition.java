package com.scn.confluence.spm.impl.condition;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.scn.confluence.spm.impl.security.SecurityUtils;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class UserIsAdminCondition implements Condition {
    private final SecurityUtils securityUtils;

    @Override
    public void init(Map<String, String> map) throws PluginParseException {

    }

    @Override
    public boolean shouldDisplay(Map<String, Object> map) {
        return securityUtils.isLoggedInUserSuperAdmin();
    }
}
