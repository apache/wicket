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
package org.apache.wicket.markup.html.internal;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;

import junit.framework.Assert;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.internal.HeaderResponse;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link IHeaderResponse}'s methods
 */
public class HeaderResponseTest
{
    private static final String RESOURCE_NAME = "resource.name";

    private IHeaderResponse headerResponse;

    private ResourceReference reference;

    /**
     * Prepare
     */
    @Before
    public void before()
    {
        final Response realResponse = new StringResponse();

        headerResponse = new HeaderResponse()
        {
            @Override
            protected Response getRealResponse()
            {
                return realResponse;
            }
        };

        reference = new ResourceReference("testReference")
        {
            private static final long serialVersionUID = 1L;

            @Override
            public IResource getResource()
            {
                return null;
            }
        };

        RequestCycle requestCycle = mock(RequestCycle.class);
        when(requestCycle.urlFor(any(IRequestHandler.class))).thenReturn(RESOURCE_NAME);

        Request request = mock(Request.class);
        when(request.getCharset()).thenReturn(Charset.defaultCharset());
        when(requestCycle.getRequest()).thenReturn(request);

        UrlRenderer urlRenderer = mock(UrlRenderer.class);
        when(urlRenderer.renderContextRelativeUrl((any(String.class)))).thenReturn(RESOURCE_NAME);
        when(requestCycle.getUrlRenderer()).thenReturn(urlRenderer);

        ThreadContext.setRequestCycle(requestCycle);
    }

    /**
     * Tear down
     */
    @After
    public void after()
    {
        ThreadContext.setRequestCycle(null);
    }

    /**
     * Tests the creation of a proper IE conditional comment
     */
    @Test
    public void conditionalRenderCSSReference()
    {
        headerResponse.render(CssHeaderItem.forReference(reference, null, "screen", "lt IE 8"));
        String expected = "<!--[if lt IE 8]><link rel=\"stylesheet\" type=\"text/css\" href=\"" +
            RESOURCE_NAME + "\" media=\"screen\" /><![endif]-->\n";
        String actual = headerResponse.getResponse().toString();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests the creation of a proper IE conditional comment
     */
    @Test
    public void conditionalRenderCSSReferenceWithUrl()
    {
        headerResponse.render(CssHeaderItem.forUrl("resource.css", "screen", "lt IE 8"));
        String expected = "<!--[if lt IE 8]><link rel=\"stylesheet\" type=\"text/css\" href=\""+RESOURCE_NAME+"\" media=\"screen\" /><![endif]-->\n";
        String actual = headerResponse.getResponse().toString();
        Assert.assertEquals(expected, actual);
    }


	/**
	 * Tests the creation of a proper IE conditional comment
	 */
	@Test
	public void conditionalRenderCSSContent()
	{
		headerResponse.render(CssHeaderItem.forCSS(".className { font-size: 10px}", "id", "lt IE 8"));
		String expected = "<!--[if lt IE 8]><style type=\"text/css\" id=\"id\"><!--\n" +
				".className { font-size: 10px}--></style>\n" +
				"<![endif]-->\n";
		String actual = headerResponse.getResponse().toString();
		Assert.assertEquals(expected, actual);
	}

    /**
     * Tests setting of 'defer' attribute
     * <p>
     * WICKET-3661
     */
    @Test
    public void deferJavaScriptReference()
    {
        boolean defer = true;
        headerResponse.render(JavaScriptHeaderItem.forUrl("js-resource.js", "some-id", defer));
        String expected = "<script type=\"text/javascript\" id=\"some-id\" defer=\"defer\" src=\"" +
            RESOURCE_NAME + "\"></script>\n";
        String actual = headerResponse.getResponse().toString();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests non-setting of 'defer' attribute
     * <p>
     * WICKET-3661
     */
    @Test
    public void deferFalseJavaScriptReference()
    {
        boolean defer = false;
        headerResponse.render(JavaScriptHeaderItem.forUrl("js-resource.js", "some-id", defer));
        String expected = "<script type=\"text/javascript\" id=\"some-id\" src=\"" + RESOURCE_NAME +
            "\"></script>\n";
        String actual = headerResponse.getResponse().toString();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests setting of 'charset' attribute
     * <p>
     * WICKET-3909
     */
    @Test
    public void charsetSetJavaScriptReference()
    {
        String charset = "foo";
        headerResponse.render(JavaScriptHeaderItem.forUrl("js-resource.js", "some-id", false,
            charset));
        String expected = "<script type=\"text/javascript\" id=\"some-id\" charset=\"" + charset +
            "\" src=\"" + RESOURCE_NAME + "\"></script>\n";
        String actual = headerResponse.getResponse().toString();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests non-setting of 'charset' attribute
     * <p>
     * WICKET-3909
     */
    @Test
    public void charsetNotSetJavaScriptReference()
    {
        headerResponse.render(JavaScriptHeaderItem.forUrl("js-resource.js", "some-id", false, null));
        String expected = "<script type=\"text/javascript\" id=\"some-id\" src=\"" + RESOURCE_NAME +
            "\"></script>\n";
        String actual = headerResponse.getResponse().toString();
        Assert.assertEquals(expected, actual);
    }

	/**
	 * Tests the creation of a proper IE conditional comment
	 */
	@Test
	public void conditionalRenderJSReference()
	{
		headerResponse.render(
				JavaScriptHeaderItem.forReference(reference, new PageParameters(), "id", false, null, "lt IE 8"));

		String expected = "<!--[if lt IE 8]><script type=\"text/javascript\" id=\"id\" src=\""+RESOURCE_NAME+"\"></script>\n<![endif]-->\n";

		String actual = headerResponse.getResponse().toString();

		Assert.assertEquals(expected, actual);
	}

	/**
	 * Tests the creation of a proper IE conditional comment
	 */
	@Test
	public void conditionalRenderJSReferenceWithUrl()
	{
		headerResponse.render(JavaScriptHeaderItem.forUrl("js-resource.js", "id", true, "cp1251", "lt IE 8"));

		String expected = "<!--[if lt IE 8]><script type=\"text/javascript\" id=\"id\" defer=\"defer\" charset=\"cp1251\" src=\""+RESOURCE_NAME+"\"></script>\n" +
				"<![endif]-->\n";

		String actual = headerResponse.getResponse().toString();

		Assert.assertEquals(expected, actual);
	}


	/**
	 * Tests the creation of a proper IE conditional comment
	 */
	@Test
	public void conditionalRenderJSContent()
	{
		headerResponse.render(JavaScriptHeaderItem.forScript("someJSMethod();", "id", "lt IE 8"));

		String expected = "<!--[if lt IE 8]><script type=\"text/javascript\" id=\"id\">\n" +
				"/*<![CDATA[*/\n" +
				"someJSMethod();\n" +
				"/*]]>*/\n" +
				"</script>\n" +
				"<![endif]-->\n";

		String actual = headerResponse.getResponse().toString();

		Assert.assertEquals(expected, actual);
	}
}
