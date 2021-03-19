package it.com.scn.jira.integration;

import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
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
//    private final CloneProjectPluginComponent cloneProjectPluginComponent;

    @Test
    public void integrationPlugin() {
        assertEquals("Integration plugin", integrationPluginComponent.getName());
        assertEquals("Test", testComponent.getTest());
    }

    @Test
    public void cloneProjectPlugin() {
        assertEquals("Clone Project Plugin", integrationPluginComponent.getCloneProjectPluginName());
    }
}
