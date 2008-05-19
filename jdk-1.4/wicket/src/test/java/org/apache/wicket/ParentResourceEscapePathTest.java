package org.apache.wicket;

import org.apache.wicket.protocol.http.MockHttpServletRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;

public class ParentResourceEscapePathTest extends WicketTestCase
{
	public void testParentEscapeSequenceInRenderedHtml() throws Exception
	{
		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("-updir-");
		parentEscapeSequenceInRenderedHtml();

		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("$up$");
		parentEscapeSequenceInRenderedHtml();
	}

	private void parentEscapeSequenceInRenderedHtml()
	{
		tester.setupRequestAndResponse();
		tester.startPage(ParentResourceEscapePathTestPage.class);
		tester.assertRenderedPage(ParentResourceEscapePathTestPage.class);
		tester.assertNoErrorMessage();

		final StringBuilder expectedHtml = new StringBuilder();
		expectedHtml.append("<html><head><wicket:link><script src=\"");
		expectedHtml.append(expectedResourceUrl());
		expectedHtml.append("\" type=\"text/javascript\"></script></wicket:link></head></html>");

		assertEquals(expectedHtml.toString(), tester.getServletResponse().getDocument());
	}

	public void testResourceUrlGeneratedByResourceReference()
	{
		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("-updir-");
		resourceUrlGeneratedByResourceReference();

		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("$up$");
		resourceUrlGeneratedByResourceReference();
	}

	private void resourceUrlGeneratedByResourceReference()
	{
		final ResourceReference ref = new ResourceReference(ParentResourceEscapePathTestPage.class,
			"../../../ParentResourceTest.js");

		assertEquals(expectedResourceUrl(), tester.createRequestCycle().urlFor(ref).toString());
	}

	public void testRequestHandlingOfResourceUrlWithEscapeStringInside()
	{
		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("-updir-");
		requestHandlingOfResourceUrlWithEscapeStringInside();

		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("$up$");
		requestHandlingOfResourceUrlWithEscapeStringInside();
	}

	private void requestHandlingOfResourceUrlWithEscapeStringInside()
	{
		final WebRequestCycle cycle = tester.setupRequestAndResponse();
		final MockHttpServletRequest request = tester.getServletRequest();
		request.setMethod("GET");
		request.setURL("http://localhost/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/" +
			expectedResourceUrl());
		tester.processRequestCycle(cycle);
		tester.assertNoErrorMessage();
		assertEquals("// ParentResourceTest.js", tester.getServletResponse().getDocument());
	}

	private String expectedResourceUrl()
	{
		final CharSequence escapeSequence = tester.getApplication()
			.getResourceSettings()
			.getParentFolderPlaceholder();

		final StringBuilder url = new StringBuilder();
		url.append("resources/org.apache.wicket.ParentResourceEscapePathTestPage/");

		for (int i = 0; i < 3; i++)
		{
			url.append(escapeSequence);
			url.append('/');
		}
		url.append("ParentResourceTest.js");

		return url.toString();
	}
}
