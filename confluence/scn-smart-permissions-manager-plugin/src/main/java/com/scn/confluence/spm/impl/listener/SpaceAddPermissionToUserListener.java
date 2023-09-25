package com.scn.confluence.spm.impl.listener;


import com.atlassian.confluence.api.service.content.ContentService;
import com.atlassian.confluence.content.Content;
import com.atlassian.confluence.content.render.xhtml.migration.ContentDao;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPermissionManager;
import com.atlassian.confluence.core.SpaceContentEntityObject;
import com.atlassian.confluence.core.persistence.ContentEntityObjectDao;
import com.atlassian.confluence.event.events.content.ContentEvent;
import com.atlassian.confluence.event.events.security.ContentPermissionEvent;
import com.atlassian.confluence.event.events.space.SpaceEvent;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.security.*;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.user.User;
import com.atlassian.user.search.page.Pager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SpaceAddPermissionToUserListener implements InitializingBean, DisposableBean {
    private final EventPublisher eventPublisher;
    private final ContentPermissionManager contentPermissionManager;
    private final UserAccessor userAccessor;
    private final ContentEntityObjectDao<ContentEntityObject> contentEntityObjectDao;

    private final List<String> superAdminNames = Arrays.asList("admin", "some");

    @Override
    public void afterPropertiesSet() {
        eventPublisher.register(this);
    }

    @Override
    public void destroy() {
        eventPublisher.unregister(this);
    }

//    space id and user id for permission configuration
//    dynamic super permission
    @PostConstruct
    public void init() {
        contentEntityObjectDao.findAll().forEach(ceo -> {
            ContentEntityObject contentEntityObject = (ContentEntityObject) ceo;
            superAdminNames.forEach(superAdminName -> {
                ConfluenceUser user = userAccessor.getUserByName(superAdminName);
                if (!contentPermissionManager.hasContentLevelPermission(user, ContentPermission.VIEW_PERMISSION, contentEntityObject)) {
                    ContentPermission permission = ContentPermission.createUserPermission(ContentPermission.VIEW_PERMISSION, user);
                    contentPermissionManager.addContentPermission(permission, contentEntityObject);
                }
            });
        });
    }



    @EventListener
    public void onContentPermissionEvent(ContentPermissionEvent contentPermissionEvent) {
        for (String superAdminName : superAdminNames) {
            ConfluenceUser user = userAccessor.getUserByName(superAdminName);
            boolean isSuperUser = userAccessor.hasMembership(UserAccessor.GROUP_CONFLUENCE_ADMINS, superAdminName);
            if (!contentPermissionManager.hasContentLevelPermission(user, ContentPermission.VIEW_PERMISSION, contentPermissionEvent.getContent())
                && user != null
            ) {
                ContentPermission permission = ContentPermission.createUserPermission(ContentPermission.VIEW_PERMISSION, user);
                contentPermissionManager.addContentPermission(permission, contentPermissionEvent.getContent());
            }
        }
        superAdminNames.forEach(superAdminName -> {
            ConfluenceUser user = userAccessor.getUserByName(superAdminName);
            boolean isSuperUser = userAccessor.hasMembership(UserAccessor.GROUP_CONFLUENCE_ADMINS, superAdminName);
            if (!contentPermissionManager.hasContentLevelPermission(user, ContentPermission.VIEW_PERMISSION, contentPermissionEvent.getContent())
                && user != null
            ) {
                ContentPermission permission = ContentPermission.createUserPermission(ContentPermission.VIEW_PERMISSION, user);
                contentPermissionManager.addContentPermission(permission, contentPermissionEvent.getContent());
            }
        });
    }
}
