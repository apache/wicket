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
package org.apache.wicket.request.resource;

import static org.apache.wicket.request.resource.ResourceSandboxConfiguration.CONTENT_SECURITY_POLICY_HEADER;
import static org.apache.wicket.request.resource.ResourceSandboxConfiguration.CONTENT_TYPE_OPTIONS_HEADER;
import static org.apache.wicket.request.resource.ResourceSandboxConfiguration.NOSNIFF;
import static org.apache.wicket.request.resource.ResourceSandboxConfiguration.SANDBOX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.ajax.WicketAjaxJQueryResourceReference;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests the headers {@link AbstractResource} sets for a {@link ResourceSandboxConfiguration}.
 */
class ResourceSandboxTest extends WicketTestCase
{
	private static final String HTML = "<html><body><script>alert(1)</script></body></html>";

	private static final String OWN_POLICY = "default-src 'self'";

	private Boolean enabled;
	private String[] exemptedPaths = { };
	private Class<? extends IResource> exemptedResource;

	@Test
	void resourceHasNoHeadersByDefault()
	{
		buildApp();

		tester.startResourceReference(WicketAjaxJQueryResourceReference.get());
		assertHeaders(null, null);

		tester.startResource(new HtmlResource());
		assertHeaders(null, null);
	}

	@Test
	void resourceHasNoHeadersWhenDisabled()
	{
		enabled = false;
		buildApp();

		tester.startResource(new HtmlResource());
		assertHeaders(null, null);
	}

	@Test
	void pageIsTheSameWhetherEnabledOrNot()
	{
		buildApp();
		tester.startPage(LinkPage.class);
		assertNull(tester.getLastResponse().getHeader(CONTENT_TYPE_OPTIONS_HEADER));
		assertFalse(String.valueOf(policies()).contains(SANDBOX));

		enabled = true;
		buildApp();
		tester.startPage(LinkPage.class);
		assertNull(tester.getLastResponse().getHeader(CONTENT_TYPE_OPTIONS_HEADER));
		assertFalse(String.valueOf(policies()).contains(SANDBOX));
	}

	@Test
	void packageResourceHasBothHeaders()
	{
		enabled = true;
		buildApp();

		tester.startResourceReference(WicketAjaxJQueryResourceReference.get());

		assertHeaders(NOSNIFF, "[sandbox]");
	}

	@Test
	void resourceStreamResourceHasBothHeaders()
	{
		enabled = true;
		buildApp();

		tester.startResource(
			new ResourceStreamResource(new StringResourceStream(HTML, "text/html")));

		assertHeaders(NOSNIFF, "[sandbox]");
	}

	@Test
	void resourceRequestedThroughComponentHasBothHeaders()
	{
		enabled = true;
		buildApp();

		tester.startPage(LinkPage.class);
		tester.clickLink("link");

		assertEquals(HTML, tester.getLastResponseAsString());
		assertHeaders(NOSNIFF, "[sandbox]");
	}

	@Test
	void resourceBelowExemptedPathHasNoHeaders()
	{
		enabled = true;
		exemptedPaths = new String[] { "/files" };
		buildApp();

		tester.executeUrl("files/a.html");
		assertEquals("text/html", tester.getLastResponse().getContentType());
		assertHeaders(null, null);

		tester.executeUrl("../files2");
		assertEquals("text/html", tester.getLastResponse().getContentType());
		assertHeaders(NOSNIFF, "[sandbox]");
	}

	@Test
	void exemptedPathIsComparedBySegment()
	{
		buildApp();
		ResourceSandboxConfiguration sandbox = new ResourceSandboxConfiguration(true, "/files/");
		IResource resource = new HtmlResource();

		assertTrue(sandbox.isExempted(resource, new MockWebRequest(Url.parse("files"))));
		assertTrue(sandbox.isExempted(resource, new MockWebRequest(Url.parse("files/a.html"))));
		assertFalse(sandbox.isExempted(resource, new MockWebRequest(Url.parse("files2"))));
		assertFalse(sandbox.isExempted(resource, new MockWebRequest(Url.parse("files/../files2"))));
		assertFalse(sandbox.isExempted(resource, new MockWebRequest(Url.parse("other/files"))));
	}

	@Test
	void resourceOfExemptedClassHasNoHeaders()
	{
		enabled = true;
		exemptedResource = HtmlResource.class;
		buildApp();

		tester.startResource(new HtmlResource());
		assertHeaders(null, null);

		tester.startResourceReference(WicketAjaxJQueryResourceReference.get());
		assertHeaders(NOSNIFF, "[sandbox]");
	}

	@Test
	void resourceKeepsItsOwnPolicy()
	{
		enabled = true;
		buildApp();

		tester.startResource(new HtmlResource()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes)
			{
				ResourceResponse response = super.newResourceResponse(attributes);
				response.getHeaders().addHeader("content-security-policy", OWN_POLICY);
				return response;
			}
		});

		// the mock response keeps header names as they were written
		assertEquals(OWN_POLICY, tester.getLastResponse().getHeader("content-security-policy"));
		assertHeaders(NOSNIFF, null);
	}

	@Test
	void resourceWithoutContentTypeIsNotMarkedNosniff()
	{
		enabled = true;
		buildApp();

		tester.startResource(new HtmlResource()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes)
			{
				return super.newResourceResponse(attributes).setContentType(null);
			}
		});

		assertNull(tester.getLastResponse().getContentType());
		assertHeaders(null, "[sandbox]");
	}

	@Test
	void notModifiedResponseHasNoHeaders()
	{
		enabled = true;
		buildApp();

		Instant nextWeek = Instant.now().plus(7, ChronoUnit.DAYS);
		tester.getRequest().addDateHeader("If-Modified-Since", nextWeek.toEpochMilli());
		tester.startResourceReference(WicketAjaxJQueryResourceReference.get());

		assertEquals(HttpServletResponse.SC_NOT_MODIFIED, tester.getLastResponse().getStatus());
		assertHeaders(null, null);
	}

	private void assertHeaders(String contentTypeOptions, String policies)
	{
		assertEquals(contentTypeOptions,
			tester.getLastResponse().getHeader(CONTENT_TYPE_OPTIONS_HEADER));
		assertEquals(policies, policies());
	}

	/**
	 * @return all policies as the mock response keeps them, a list in brackets, or {@code null}
	 */
	private String policies()
	{
		MockHttpServletResponse response = tester.getLastResponse();
		return response.containsHeader(CONTENT_SECURITY_POLICY_HEADER)
			? response.getHeaders(CONTENT_SECURITY_POLICY_HEADER).iterator().next() : null;
	}

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				if (enabled != null)
				{
					ResourceSandboxConfiguration sandbox = getSecuritySettings()
						.setResourceSandboxConfiguration(enabled, exemptedPaths)
						.getResourceSandboxConfiguration();
					if (exemptedResource != null)
					{
						sandbox.addExemptedResource(exemptedResource);
					}
				}
				mountResource("/files/a.html", new HtmlResourceReference("files"));
				mountResource("/files2", new HtmlResourceReference("files2"));
			}
		};
	}

	// the application depends on fields the tests set, so it is built by them
	@Override
	public void commonBefore()
	{
	}

	private void buildApp()
	{
		ThreadContext.detach();

		tester = newWicketTester(newApplication());
	}

	private static class HtmlResource extends AbstractResource
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected ResourceResponse newResourceResponse(Attributes attributes)
		{
			return new ResourceResponse().setContentType("text/html")
				.setWriteCallback(new WriteCallback()
				{
					@Override
					public void writeData(Attributes attributes)
					{
						attributes.getResponse().write(HTML);
					}
				});
		}
	}

	private static class HtmlResourceReference extends ResourceReference
	{
		private static final long serialVersionUID = 1L;

		HtmlResourceReference(String name)
		{
			super(name);
		}

		@Override
		public IResource getResource()
		{
			return new HtmlResource();
		}
	}

	public static class LinkPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		public LinkPage()
		{
			add(new ResourceLink<Void>("link", new HtmlResource()));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id=\"link\">link</a></body></html>");
		}
	}
}
