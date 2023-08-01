package it.com.scn.jira.worklog.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scn.jira.worklog.impl.domain.dto.WLTypeDto;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.BasicAuthSecurityHandler;
import org.apache.wink.common.internal.http.Accept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WLTypeResourceIT {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static final String RESOURCE_URL = "http://localhost:2990/jira/rest/scn-worklog-plugin/1.0/worklog-types";

    private final Gson gson = new Gson();

    private final WLTypeDto firstWlTypeDto = WLTypeDto.builder()
        .id(1L)
        .name("first")
        .description("first description")
        .iconUri("http://first-icon-uri")
        .statusColor("first color")
        .build();

    private final WLTypeDto secondWlTypeDto = WLTypeDto.builder()
        .id(2L)
        .name("second")
        .description("second description")
        .iconUri("http://second-icon-uri")
        .statusColor("second color")
        .build();

    @Before
    public void setUp() {
        initData();
    }

    @After
    public void tearDown() {
        clearDatabase();
    }

    @Test
    public void testGetAll() {
        List<WLTypeDto> expected = Arrays.asList(firstWlTypeDto, secondWlTypeDto);

        String jsonResponse = httpGet(RESOURCE_URL);
        Type wlTypeDtoListType = new TypeToken<ArrayList<WLTypeDto>>(){}.getType();
        List<WLTypeDto> actual = gson.fromJson(jsonResponse, wlTypeDtoListType);

        assertEquals(expected, actual);
    }

    @Test
    public void testGet() {
        WLTypeDto expected = firstWlTypeDto;

        String jsonResponse = httpGet(RESOURCE_URL + "/" + 1);
        WLTypeDto actual = gson.fromJson(jsonResponse, WLTypeDto.class);

        assertEquals(expected, actual);
    }

    @Test
    public void testCreate() {
        WLTypeDto expected = WLTypeDto.builder()
            .id(3L)
            .name("third")
            .description("third description")
            .iconUri("http://third-uri")
            .statusColor("third color")
            .build();

        WLTypeDto actual = httpPost(RESOURCE_URL, expected, WLTypeDto.class);

        assertEquals(expected, actual);
    }

    @Test
    public void testUpdate() {
        WLTypeDto expected = firstWlTypeDto;
        expected.setDescription("asdfsad");

        WLTypeDto actual = httpPut(RESOURCE_URL + "/" + 1, expected, WLTypeDto.class);

        assertEquals(expected, actual);
    }

    @Test
    public void testDeleteAll() {
        int expectedStatusCode = Response.Status.NO_CONTENT.getStatusCode();
        int actualStatusCode = httpDeleteAll(RESOURCE_URL);
        assertEquals(expectedStatusCode, actualStatusCode);

        List<WLTypeDto> expected = Collections.emptyList();
        String jsonResponse = httpGet(RESOURCE_URL);
        Type wlTypeDtoListType = new TypeToken<ArrayList<WLTypeDto>>(){}.getType();
        List<WLTypeDto> actual = gson.fromJson(jsonResponse, wlTypeDtoListType);
        assertEquals(expected, actual);
    }

    @Test
    public void testDelete() {
        int expectedStatusCode = Response.Status.NO_CONTENT.getStatusCode();
        int actualStatusCode = httpDelete(RESOURCE_URL, 1L);
        assertEquals(expectedStatusCode, actualStatusCode);

        List<WLTypeDto> expected = Arrays.asList(secondWlTypeDto);
        String jsonResponse = httpGet(RESOURCE_URL);
        Type wlTypeDtoListType = new TypeToken<ArrayList<WLTypeDto>>(){}.getType();
        List<WLTypeDto> actual = gson.fromJson(jsonResponse, wlTypeDtoListType);
        assertEquals(expected, actual);
    }

    private String httpGet(String url) {
        Resource resource = _httpConfig(url);
        return resource
            .header("Accept", "application/json;q=1.0")
            .get()
            .getEntity(String.class);
    }

    private <T> T httpPost(String url, T t, Class<T> cls) {
        String jsonResponse = _httpConfig(url)
            .contentType(MediaType.APPLICATION_JSON)
            .post(gson.toJson(t))
            .getEntity(String.class);

        return gson.fromJson(jsonResponse, cls);
    }

    private <T> T httpPut(String url, T t, Class<T> cls) {
        String jsonResponse = _httpConfig(url)
            .contentType( MediaType.APPLICATION_JSON)
            .put(gson.toJson(t))
            .getEntity(String.class);

        return gson.fromJson(jsonResponse, cls);
    }

    private int httpDelete(String url, Long id) {
        return _httpConfig(url + "/" + id)
            .header("Accept", "application/json;q=1.0")
            .delete()
            .getStatusCode();
    }

    private int httpDeleteAll(String url) {
        return _httpConfig(url)
            .header("Accept", MediaType.APPLICATION_JSON)
            .delete()
            .getStatusCode();
    }

    private Resource _httpConfig(String url) {
        ClientConfig config = new ClientConfig();

        BasicAuthSecurityHandler basicAuthSecHandler = new BasicAuthSecurityHandler();
        basicAuthSecHandler.setUserName(USERNAME);
        basicAuthSecHandler.setPassword(PASSWORD);
        config.handlers(basicAuthSecHandler);

        RestClient client = new RestClient(config);

        return  client.resource(url);
    }

    private void initData() {
        WLTypeDto actualFirst = httpPost(RESOURCE_URL, firstWlTypeDto, WLTypeDto.class);
        assertEquals(firstWlTypeDto, actualFirst);

        WLTypeDto actualSecond = httpPost(RESOURCE_URL, secondWlTypeDto, WLTypeDto.class);
        assertEquals(secondWlTypeDto, actualSecond);
    }

    private void clearDatabase() {
        int statusCode = httpDeleteAll(RESOURCE_URL);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), statusCode);
    }
}
