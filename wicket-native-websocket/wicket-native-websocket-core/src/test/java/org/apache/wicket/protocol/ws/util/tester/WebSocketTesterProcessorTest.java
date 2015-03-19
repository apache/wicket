package org.apache.wicket.protocol.ws.util.tester;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.ConnectionRejectedException;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class WebSocketTesterProcessorTest extends Assert {

    private static class TestProcessor extends TestWebSocketProcessor {
        private TestProcessor(HttpServletRequest request, WebApplication application) {
            super(request, application);
        }

        @Override
        protected void onOutMessage(String message) {
        }

        @Override
        protected void onOutMessage(byte[] message, int offset, int length) {
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
    }

    @Test(expected = ConnectionRejectedException.class)
    public void onConnectNoOrigin() throws Exception {
        // Given header 'Origin' is missing
        WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);
        webSocketSettings.setAllowedDomains(Arrays.asList(new String[] { "http://www.example.com" }));
        MockHttpServletRequest request = tester.getRequest();
        request.addParameter("resourceName", TestWebSocketResource.TEXT);
        request.addParameter(WebRequest.PARAM_AJAX_BASE_URL, ".");

        // When we open a connection
        TestWebSocketProcessor processor = new TestProcessor(tester.getRequest(), tester.getApplication());
        processor.onOpen(new Object());

        // Then it fails
    }

    @Ignore
    @Test(expected = ConnectionRejectedException.class)
    public void onConnectMultipleOrigins() throws Exception {
        // Given the request contains multiple header 'Origin's
        WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);
        webSocketSettings.setAllowedDomains(Arrays.asList(new String[] { "http://www.example.com" }));
        MockHttpServletRequest request = tester.getRequest();
        request.addHeader("origin", "http://www.example.com");
        request.addHeader("origin", "http://ww2.example.com");
        request.addParameter("resourceName", TestWebSocketResource.TEXT);
        request.addParameter(WebRequest.PARAM_AJAX_BASE_URL, ".");

        // When we open a connection
        TestWebSocketProcessor processor = new TestProcessor(tester.getRequest(), tester.getApplication());
        processor.onOpen(new Object());

        // Then it fails
    }

    @Test
    public void onConnectMatchingOrigin() throws Exception {
        // Given header 'Origin' matches the host origin
        WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);
        webSocketSettings.setAllowedDomains(Arrays.asList(new String[] { "http://www.example.com" }));
        MockHttpServletRequest request = tester.getRequest();
        request.addHeader("origin", "http://www.example.com");
        request.addParameter("resourceName", TestWebSocketResource.TEXT);
        request.addParameter(WebRequest.PARAM_AJAX_BASE_URL, ".");

        // When we open a connection
        TestWebSocketProcessor processor = new TestProcessor(tester.getRequest(), tester.getApplication());
        processor.onOpen(new Object());

        // Then it succeeds
    }

    @Test(expected = ConnectionRejectedException.class)
    public void onConnectMismatchingOrigin() throws Exception {
        // Given header 'Origin' does not match the host origin
        WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);
        webSocketSettings.setAllowedDomains(Arrays.asList(new String[] { "http://www.example.com" }));
        MockHttpServletRequest request = tester.getRequest();
        request.addHeader("origin", "http://ww2.example.com");
        request.addParameter("resourceName", TestWebSocketResource.TEXT);
        request.addParameter(WebRequest.PARAM_AJAX_BASE_URL, ".");

        // When we open a connection
        TestWebSocketProcessor processor = new TestProcessor(tester.getRequest(), tester.getApplication());
        processor.onOpen(new Object());

        // Then it fails
    }
}
