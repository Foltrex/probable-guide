package it.com.scn.jira.integration;

import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.scn.jira.integration.IntegrationPluginComponent;
import com.scn.jira.integration.TestComponent;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
@RequiredArgsConstructor
public class PluginNameWiredTest {
    private final IntegrationPluginComponent integrationPluginComponent;
    private final TestComponent testComponent;
    private final ApplicationProperties applicationProperties;
    private final AvatarManager avatarManager;

    @Test
    public void integrationPlugin() {
        assertEquals("Integration Plugin: " + applicationProperties.getDisplayName(), integrationPluginComponent.getName());
        assertEquals("Test", testComponent.getTest());
    }

    @Test
    public void cloneProjectPlugin() {
        assertEquals("Clone Project Plugin", integrationPluginComponent.getCloneProjectPluginName());
    }
}
