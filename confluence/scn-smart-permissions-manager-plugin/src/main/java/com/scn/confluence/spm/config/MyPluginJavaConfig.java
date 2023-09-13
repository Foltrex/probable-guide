package com.scn.confluence.spm.config;

import com.atlassian.confluence.content.render.xhtml.migration.ContentDao;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPermissionManager;
import com.atlassian.confluence.core.persistence.ContentEntityObjectDao;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.velocity.VelocityManager;
import com.scn.confluence.spm.api.MyPluginComponent;
import com.scn.confluence.spm.impl.MyPluginComponentImpl;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
import com.atlassian.sal.api.ApplicationProperties;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.exportOsgiService;
import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.importOsgiService;

@Configuration
@Import({
        ModuleFactoryBean.class,
        PluginAccessorBean.class
})
public class MyPluginJavaConfig {


    // imports ApplicationProperties from OSGi
    @Bean
    public SpaceManager spaceManager() {
        return importOsgiService(SpaceManager.class);
    }

    @Bean
    public ContentPermissionManager contentPermissionManager() {
        return importOsgiService(ContentPermissionManager.class);
    }

    @Bean
    public UserAccessor userAccessor() {
        return importOsgiService(UserAccessor.class);
    }

    @Bean
    public ContentEntityObjectDao<ContentEntityObject> contentEntityObjectDao() {
        return importOsgiService(ContentEntityObjectDao.class);
    }

    @Bean
    public VelocityManager velocityManager() {
        return importOsgiService(VelocityManager.class);
    }

    @Bean
    public PageManager pageManager() {
        return importOsgiService(PageManager.class);
    }

    @Bean
    public ApplicationProperties applicationProperties() {
        return importOsgiService(ApplicationProperties.class);
    }

    @Bean
    public EventPublisher eventPublisher() {
        return importOsgiService(EventPublisher.class);
    }

    @Bean
    public MyPluginComponent myPluginComponent(ApplicationProperties applicationProperties) {
        return new MyPluginComponentImpl(applicationProperties);
    }

    // Exports MyPluginComponent as an OSGi service
    @Bean
    public FactoryBean<ServiceRegistration> registerMyDelegatingService(
            final MyPluginComponent mypluginComponent) {
        return exportOsgiService(mypluginComponent, null, MyPluginComponent.class);
    }
}
