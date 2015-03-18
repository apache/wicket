package org.apache.wicket.protocol.ws.util.tester;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.ws.api.ConnectionRejectedException;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WebSocketTesterProcessorTest extends Assert {

    WicketTester tester;

    @Before
    public void before() {
        WebApplication application = new MockApplication() {
            @Override
            protected void init() {
                super.init();

                getSharedResources().add(TestWebSocketResource.TEXT, new TestWebSocketResource("expected"));
            }
        };
        tester = new WicketTester(application);
        WebApplication webApplication = tester.getApplication();
        webApplication.getWicketFilter().setFilterPath("");
        tester.startPage(new WebSocketBehaviorTestPage(""));
    }

    @After
    public void after() {
        tester.destroy();
    }

    @Test
    public void onConnectMatchingOrigin() throws Exception {
        // Given header 'Origin' matches the host origin
        MockHttpServletRequest request = tester.getRequest();
        request.addHeader("origin", "http://www.example.com");
        request.addParameter("resourceName", TestWebSocketResource.TEXT);
        request.addParameter(WebRequest.PARAM_AJAX_BASE_URL, ".");
        Url url = new Url();
        url.setProtocol("http");
        url.setHost("www.example.com");
        request.setUrl(url);

        TestWebSocketProcessor processor = new TestWebSocketProcessor(tester.getRequest(), tester.getApplication()) {

            @Override
            protected void onOutMessage(String message) {
            }

            @Override
            protected void onOutMessage(byte[] message, int offset, int length) {
            }

        };

        // When we open a connection
        processor.onOpen(new Object());

        // Then it succeeds
    }

    @Test(expected = ConnectionRejectedException.class)
    public void onConnectMismatchingOrigin() throws Exception {
        // Given header 'Origin' does not match the host origin
        MockHttpServletRequest request = tester.getRequest();
        request.addHeader("origin", "http://www.example.com");
        request.addParameter("resourceName", TestWebSocketResource.TEXT);
        request.addParameter(WebRequest.PARAM_AJAX_BASE_URL, ".");
        Url url = new Url();
        url.setProtocol("http");
        url.setHost("ww2.example.com");
        request.setUrl(url);

        TestWebSocketProcessor processor = new TestWebSocketProcessor(tester.getRequest(), tester.getApplication()) {

            @Override
            protected void onOutMessage(String message) {
            }

            @Override
            protected void onOutMessage(byte[] message, int offset, int length) {
            }

        };

        // When we open a connection
        processor.onOpen(new Object());

        // Then it fails
    }
}
