/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.plugin.versioncontrolmonitor.server;

import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.dto.server.DtoFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Unit tests for VersionControlMonitorService
 *
 * @author Stephane Tournie
 */
@RunWith(MockitoJUnitRunner.class)
public class TestVersionControlMonitorService {

    private final static String REQUEST_HEADER_GITHUB_EVENT = "X-GitHub-Event";

    private VersionControlMonitorService fakeVersionControlMonitorService;

    @Before
    public void setUp() throws Exception {
        // Prepare authConnection
        Token fakeToken = DtoFactory.newDto(Token.class).withValue("fakeToken");
        AuthConnection mockAuthConnection = mock(AuthConnection.class);
        when(mockAuthConnection.authenticateUser("somebody@somemail.com", "somepwd")).thenReturn(fakeToken);

        // Prepare factoryConnection
        URL factoryResource = getClass().getResource("/factory-MKTG-341.json");
        Factory fakeFactory = DtoFactory.getInstance().createDtoFromJson(readFile(factoryResource.getFile(), StandardCharsets.UTF_8),
                                                                         Factory.class);
        FactoryConnection mockFactoryConnection = mock(FactoryConnection.class);
        when(mockFactoryConnection.getFactory("fakeFactoryId", fakeToken)).thenReturn(fakeFactory);
        when(mockFactoryConnection.updateFactory(fakeFactory, fakeToken)).thenReturn(fakeFactory);

        // Prepare VersionControlMonitorService
        fakeVersionControlMonitorService =
                new VersionControlMonitorService(mockAuthConnection, mockFactoryConnection);
    }

    @Test
    public void testGithubWebhookPushEventNoConnector() throws Exception {
        HttpServletRequest mockRequest = prepareRequest("push");
        Response response = fakeVersionControlMonitorService.githubWebhook(mockRequest);
        Assert.assertTrue(response.getStatus() == OK.getStatusCode());
    }

    @Test
    public void testGithubWebhookPullRequestEventNoConnector() throws Exception {
        HttpServletRequest mockRequest = prepareRequest("pull_request");
        Response response = fakeVersionControlMonitorService.githubWebhook(mockRequest);
        Assert.assertTrue(response.getStatus() == OK.getStatusCode());
    }

    protected HttpServletRequest prepareRequest(String eventType) throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        String githubEventString = null;
        switch (eventType) {
            case "pull_request":
                URL urlPR = getClass().getResource("/pull_request_event.json");
                githubEventString = readFile(urlPR.getFile(), StandardCharsets.UTF_8);
                break;
            case "push":
                URL urlP = getClass().getResource("/push_event.json");
                githubEventString = readFile(urlP.getFile(), StandardCharsets.UTF_8);
                break;
            default:
                break;
        }
        ServletInputStream fakeInputStream = null;
        if (githubEventString != null) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(githubEventString.getBytes(StandardCharsets.UTF_8));
            fakeInputStream = new ServletInputStream() {
                public int read() throws IOException {
                    return byteArrayInputStream.read();
                }
            };
        }
        when(mockRequest.getHeader(REQUEST_HEADER_GITHUB_EVENT)).thenReturn(eventType);
        when(mockRequest.getInputStream()).thenReturn(fakeInputStream);

        return mockRequest;
    }

    /**
     * Read file as String
     *
     * @param path path to the file to read
     * @param encoding charset used to encode
     * @return
     * @throws IOException
     */
    protected String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
