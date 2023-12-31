package it.com.scn.jira.integration;

import com.scn.jira.cloneproject.rest.CloneProjectResourceModel;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CloneProjectResourceFuncTest {
    private String baseUrl;

    @Before
    public void setup() {
        baseUrl = System.getProperty("baseurl");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void doCloneGETWorks() {
        String resourceUrl = baseUrl + "/rest/cloneproject/1.0/doclone";

        Client client2 = Client.create();
        String response = client2.resource(resourceUrl).get(String.class);

        assertEquals("validation 1", "Do POST instead of GET", response);
    }

    @Test
    public void doClonePOSTWorks() {
        // TMP project and other data should be pre-created according to pom/productDataPath
        String resourceUrl = baseUrl + String.format("/rest/cloneproject/1.0/doclone?pkey=%s&pname=%s&templatekey=%s&lead=%s&url=%s",
            "ERPDEV", "ERP%20Development", "TEST", "admin", "https://www.google.com");

        Client client2 = Client.create();
        client2.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
        ClientResponse response = client2.resource(resourceUrl).post(ClientResponse.class);

        // check return text should be "OK"
        assertEquals("doclone should return not OK", "OK", response.getEntity(String.class));

        // validate all schemes are set properly - unfortunately its impossible via the standard API.
        CloneProjectResourceModel data = client2.resource(baseUrl + String.format("/rest/cloneproject/1.0/project?pkey=%s", "ERPDEV")).get(CloneProjectResourceModel.class);

        assertEquals("message", data.message, "OK");
        assertEquals("pkey", data.pkey, "ERPDEV");
        assertEquals("pname", data.pname, "ERP Development");
        assertEquals("lead", data.lead, "admin");
        assertEquals("description", data.descr, "");
        assertEquals("url", data.url, "https://www.google.com");
        assertEquals("assignee type", data.assigneetype.longValue(), 3);
        assertEquals("category", data.category, "ERP");
        assertEquals("workflowscheme", data.workflowscheme, "TEST: Project Management Workflow Scheme");
        assertEquals("issuetypescheme", data.issuetypescheme, "TEST: Project Management Issue Type Scheme");
        assertEquals("issuetypesceeenscheme", data.issuetypesceeenscheme, "TEST: Project Management Issue Type Screen Scheme");
        assertEquals("fieldconfigurationscheme", data.fieldconfigurationscheme, "Test field configuration scheme");
        assertEquals("permissionscheme", data.permissionscheme, "Default Permission Scheme");
        assertEquals("notificationscheme", data.notificationscheme, "Default Notification Scheme");

        // delete newly created project - shock! there is no such a REST method in standard API
        String response2 = client2.resource(baseUrl + String.format("/rest/cloneproject/1.0/project?pkey=%s", "ERPDEV")).delete(String.class);

        assertEquals("Project DELETE should return OK", "OK", response2);
    }

    @Test
    public void noAuthenticatedPOST() {
        // TMP project and other data should be pre-created according to pom/productDataPath
        String resourceUrl = baseUrl + String.format("/rest/cloneproject/1.0/doclone?pkey=%s&pname=%s&templatekey=%s&lead=%s&url=%s",
            "ATLASSUPP", "ATLASSUPP", "TEST", "admin", "http://newUrl.com");

        Client client2 = Client.create();
        ClientResponse response = client2.resource(resourceUrl).post(ClientResponse.class);
        assertEquals("Should return 401 error code", 401, response.getStatus());
    }

    @Test
    public void insufficientPermissionPOST() {
        // TMP project and other data should be pre-created according to pom/productDataPath
        String resourceUrl = baseUrl + String.format("/rest/cloneproject/1.0/doclone?pkey=%s&pname=%s&templatekey=%s&lead=%s",
            "ATLASSUPP", "ATLASSUPP", "TEST", "admin");

        Client client2 = Client.create();
        client2.addFilter(new HTTPBasicAuthFilter("user", "user"));
        String response = client2.resource(resourceUrl).post(String.class);

        // check return text should be "Failed to create a project ..."
        assertEquals("Failed to create a project", response.substring(0, 26));
    }
}
