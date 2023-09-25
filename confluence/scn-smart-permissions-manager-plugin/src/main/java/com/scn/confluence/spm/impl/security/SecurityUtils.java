package com.scn.confluence.spm.impl.security;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    private final UserAccessor userAccessor;

    public boolean isLoggedInUserSuperAdmin() {
        ConfluenceUser currentLoggedInUser = AuthenticatedUserThreadLocal.get();
        return userAccessor.hasMembership(UserAccessor.GROUP_CONFLUENCE_ADMINS, currentLoggedInUser.getName());
    }
}
