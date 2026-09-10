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
package org.apache.wicket.extensions.ajax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * An {@link AjaxDownloadBehavior} constructed from an {@link IResource} serves it from the listener
 * URL of the component it is bound to, so the {@link IAuthorizationStrategy} has to authorize it
 * there just as it does for the {@link ResourceReference} the other constructor takes.
 */
class AjaxDownloadResourceAuthorizationTest extends WicketTestCase
{
	private void deny(final IResource... denied)
	{
		final List<IResource> refused = Arrays.asList(denied);

		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy()
			{
				@Override
				public boolean isResourceAuthorized(IResource resource, PageParameters parameters)
				{
					return refused.contains(resource) == false;
				}
			});
	}

	/**
	 * The URL a behavior constructed from an {@link IResource} hands to the browser when it
	 * initiates: the listener URL of the behavior the download added to serve that resource. That
	 * behavior is private, so it can only be recognised as the one behavior of the component that
	 * is not the download itself.
	 */
	private String downloadUrl(Component component, AjaxDownloadBehavior download)
	{
		Page page = component.getPage();
		assertFalse(page.isPageStateless(), "the fixture page must be stateful");

		Behavior resourceBehavior = null;
		for (Behavior behavior : component.getBehaviors())
		{
			if (behavior != download)
			{
				resourceBehavior = behavior;
			}
		}
		assertNotNull(resourceBehavior,
			"the download did not add a behavior to serve its resource");

		IRequestHandler handler = new ListenerRequestHandler(
			new PageAndComponentProvider(page, component),
			component.getBehaviorId(resourceBehavior));

		String url = tester.urlFor(handler).toString();
		assertTrue(url.contains("?" + page.getPageId() + "-"),
			"expected a listener URL carrying the page identifier, was " + url);

		return url;
	}

	@Test
	void downloadOfADeniedResourceIsRefused()
	{
		DownloadPage page = new DownloadPage();
		deny(page.resource);
		tester.startPage(page);

		tester.executeUrl(downloadUrl(page.resourceContainer, page.resourceDownload));

		assertEquals(HttpServletResponse.SC_FORBIDDEN, tester.getLastResponse().getStatus(),
			"the download should have been refused");
		assertEquals(
			"The request to resource 'streamedDownload' with parameters '' cannot be authorized.",
			tester.getLastResponse().getErrorMessage());
		assertFalse(page.resource.responded,
			"the resource streamed although the strategy refused it");
		assertFalse(tester.getLastResponseAsString().contains("streamedDownload"),
			"the content of the resource reached the response");
	}

	/**
	 * Control: the fix must not refuse a download the strategy allows.
	 */
	@Test
	void downloadOfAnAllowedResourceIsStillServed()
	{
		DownloadPage page = new DownloadPage();
		deny(page.referenced);
		tester.startPage(page);

		tester.executeUrl(downloadUrl(page.resourceContainer, page.resourceDownload));

		assertEquals(HttpServletResponse.SC_OK, tester.getLastResponse().getStatus());
		assertTrue(page.resource.responded, "the resource did not stream");
		assertEquals("streamedDownload", tester.getLastResponseAsString());
	}

	/**
	 * The {@link ResourceReference} constructor serves its resource from a resource URL instead, so
	 * it adds no behavior of its own - which is why that route was authorized all along.
	 */
	@Test
	void theResourceReferenceVariantAddsNoBehaviorOfItsOwn()
	{
		DownloadPage page = new DownloadPage();
		tester.startPage(page);

		assertEquals(1, page.referenceContainer.getBehaviors().size(),
			"the ResourceReference variant must not add a behavior that serves the resource");
	}

	@Test
	void downloadOfADeniedResourceReferenceIsStillRefused()
	{
		DownloadPage page = new DownloadPage();
		deny(page.referenced);
		tester.startPage(page);

		tester.startResourceReference(page.reference);

		assertEquals(HttpServletResponse.SC_FORBIDDEN, tester.getLastResponse().getStatus());
		assertFalse(page.referenced.responded,
			"the referenced resource streamed although the strategy refused it");
	}

	/**
	 * Control.
	 */
	@Test
	void downloadOfAnAllowedResourceReferenceIsStillServed()
	{
		DownloadPage page = new DownloadPage();
		deny(page.resource);
		tester.startPage(page);

		tester.startResourceReference(page.reference);

		assertEquals(HttpServletResponse.SC_OK, tester.getLastResponse().getStatus());
		assertTrue(page.referenced.responded, "the referenced resource did not stream");
	}

	/** A stateful page carrying both constructors of {@link AjaxDownloadBehavior}. */
	public static class DownloadPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		final RecordingResource resource = new RecordingResource("streamedDownload");

		final RecordingResource referenced = new RecordingResource("referenced");

		final ResourceReference reference;

		final WebMarkupContainer resourceContainer;

		final WebMarkupContainer referenceContainer;

		final AjaxDownloadBehavior resourceDownload;

		DownloadPage()
		{
			setStatelessHint(false);

			reference = new ResourceReference(DownloadPage.class, "referenced")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public IResource getResource()
				{
					return referenced;
				}
			};

			resourceDownload = new AjaxDownloadBehavior(resource);
			resourceContainer = new WebMarkupContainer("resource");
			resourceContainer.add(resourceDownload);
			add(resourceContainer);

			referenceContainer = new WebMarkupContainer("reference");
			referenceContainer.add(new AjaxDownloadBehavior(reference));
			add(referenceContainer);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" //
				+ "<span wicket:id=\"resource\">resource</span>" //
				+ "<span wicket:id=\"reference\">reference</span>" //
				+ "</body></html>");
		}
	}

	/** Records that it was asked to respond, and writes something recognisable when it does. */
	private static class RecordingResource implements IResource
	{
		private static final long serialVersionUID = 1L;

		private final String name;

		private boolean responded;

		RecordingResource(String name)
		{
			this.name = name;
		}

		@Override
		public void respond(Attributes attributes)
		{
			responded = true;
			attributes.getResponse().write(name);
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
