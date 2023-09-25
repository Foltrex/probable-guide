package com.scn.confluence.spm.impl.interceptor;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.scn.confluence.spm.impl.security.SecurityUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AdminRoleInterceptor implements Interceptor {
    private final SecurityUtils securityUtils;

    @Override
    public void destroy() {
    }

    @Override
    public void init() {
    }

    @Override
    public String intercept(ActionInvocation actionInvocation) throws Exception {
        if (securityUtils.isLoggedInUserSuperAdmin()) {
            return actionInvocation.invoke();
        } else {
            return "accessDenied";
        }
    }
}
