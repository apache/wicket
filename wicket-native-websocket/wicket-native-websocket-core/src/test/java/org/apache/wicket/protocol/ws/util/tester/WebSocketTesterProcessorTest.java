/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.protocol.ws.util.tester;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class WebSocketTesterProcessorTest extends Assert {

    final static AtomicBoolean messageReceived = new AtomicBoolean(false);

    private static class TestProcessor extends TestWebSocketProcessor {
        private TestProcessor(HttpServletRequest request, WebApplication application) {
            super(request, application);
        }

        @Override
        protected void onOutMessage(String message) {
            messageReceived.set(true);
        }

        @Override
        protected void onOutMessage(byte[] message, int offset, int length) {
            messageReceived.set(true);
        }
    }

    WicketTester tester;
    WebApplication application = new MockApplication() {
        @Override
        protected void init() {
            super.init();

            getSharedResources().add(TestWebSocketResource.TEXT, new TestWebSocketResource("expected"));
        }
    };

    @Before
    public void before() {
        tester = new WicketTester(application);
        WebApplication webApplication = tester.getApplication();
        webApplication.getWicketFilter().setFilterPath("");
    }

    @After
    public void after() {
        tester.destroy();
        TestWebSocketResource.ON_ABORT_CALLED.set(false);
    }

    @Test
    public void onConnectNoOrigin() throws Exception {
        // Given header 'Origin' is missing
        configureRequest(true, new String[] { "http://www.example.com" }, new String[] {});

        // When we open a connection
        TestWebSocketProcessor processor = new TestProcessor(tester.getRequest(), tester.getApplication());
        processor.onOpen(new Object());

        // Then it fails
        assertEquals(true, TestWebSocketResource.ON_ABORT_CALLED.get());
    }

    @Ignore
    @Test
    public void onConnectMultipleOrigins() throws Exception {
        // Given the request contains multiple header 'Origin's
        configureRequest(true, new String[] { "http://www.example.com" }, new String[] { "http://www.example.com", "http://ww2.example.com" });

        // When we open a connection
        TestWebSocketProcessor processor = new TestProcessor(tester.getRequest(), tester.getApplication());
        processor.onOpen(new Object());

        // Then it fails
        assertEquals(true, TestWebSocketResource.ON_ABORT_CALLED.get());
    }

    @Test
    public void onConnectMatchingOrigin() throws Exception {
        // Given header 'Origin' matches the host origin
        configureRequest(true, new String[] { "http://www.example.com" }, new String[] { "http://www.example.com" });

        // When we open a connection
        TestWebSocketProcessor processor = new TestProcessor(tester.getRequest(), tester.getApplication());
        processor.onOpen(new Object());

        // Then it succeeds
        assertEquals(false, TestWebSocketResource.ON_ABORT_CALLED.get());
    }

    @Test
    public void onConnectMismatchingOrigin() throws Exception {
        // Given header 'Origin' does not match the host origin
        configureRequest(true, new String[] { "http://www.example.com" }, new String[] { "http://ww2.example.com" });

        // When we open a connection
        TestWebSocketProcessor processor = new TestProcessor(tester.getRequest(), tester.getApplication());
        processor.onOpen(new Object());

        // Then it fails
        assertEquals(true, TestWebSocketResource.ON_ABORT_CALLED.get());
    }

    protected void configureRequest(boolean protectionNeeded, String[] allowedDomains, String[] origins) {
        WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);
        webSocketSettings.setHijackingProtectionEnabled(protectionNeeded);
        webSocketSettings.setAllowedDomains(Arrays.asList(allowedDomains));
        MockHttpServletRequest request = tester.getRequest();
        for (String origin : origins) {
            request.addHeader("Origin", origin);
        }
        request.addParameter("resourceName", TestWebSocketResource.TEXT);
        request.addParameter(WebRequest.PARAM_AJAX_BASE_URL, ".");
    }

}
