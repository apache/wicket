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
package org.apache.wicket.request.handler.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ImageButton;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Exceptions;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * A resource a component was handed as an {@link IResource} is served from the listener URL of that
 * component rather than from a resource URL of its own, and the {@link IAuthorizationStrategy} has
 * to authorize it there just as it does when the same instance is requested through a
 * {@link ResourceReference}.
 * <p>
 * {@link ResourceAuthorizationTest} covers the decision itself on the handler that makes it; this
 * covers the components that have to reach that handler.
 */
public class ComponentResourceAuthorizationTest extends WicketTestCase
{
	/** Every resource the strategy was asked about, in the order it was asked. */
	private final List<IResource> asked = new ArrayList<>();

	/** The parameters of the last question put to the strategy. */
	private PageParameters askedWith;

	/**
	 * Refuses the given resources, allows every other one, and records every question asked.
	 */
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
					asked.add(resource);
					askedWith = parameters;
					return refused.contains(resource) == false;
				}
			});
	}

	/**
	 * The URL Wicket itself rendered for the component with the given wicket:id, which for a
	 * component holding an {@link IResource} is the listener URL of that component. It carries the
	 * identifier of the page that rendered it, so the request reaches that very instance and the
	 * listener gate of the component is in full effect.
	 */
	private String urlInMarkup(String wicketId, String attribute)
	{
		TagTester tag = tester.getTagByWicketId(wicketId);
		assertNotNull("no tag with wicket:id '" + wicketId + "' was rendered", tag);

		String url = tag.getAttribute(attribute);
		assertNotNull("the tag with wicket:id '" + wicketId + "' has no " + attribute, url);
		assertTrue("expected a listener URL carrying the page identifier, was " + url,
			url.contains("?" + tester.getLastRenderedPage().getPageId() + "-"));

		return url;
	}

	private void assertRefused(RecordingResource... resources)
	{
		assertEquals("the request should have been refused",
			HttpServletResponse.SC_FORBIDDEN,
			tester.getLastResponse().getStatus());

		for (RecordingResource resource : resources)
		{
			assertFalse("resource '" + resource + "' streamed although the strategy refused it",
				resource.responded);
			assertFalse("the content of resource '" + resource + "' reached the response",
				tester.getLastResponseAsString().contains(resource.name));
		}
	}

	private void assertRefusalMessage(RecordingResource resource)
	{
		assertEquals("The request to resource '" + resource
			+ "' with parameters '' cannot be authorized.",
			tester.getLastResponse().getErrorMessage());
	}

	private void assertStreamed(String content, RecordingResource resource)
	{
		assertEquals(HttpServletResponse.SC_OK, tester.getLastResponse().getStatus());
		assertTrue("resource '" + resource + "' did not stream", resource.responded);
		assertEquals(content, tester.getLastResponseAsString());
	}

	@Test
	public void resourceLinkWithDeniedResourceIsRefused()
	{
		ResourceLinkPage page = new ResourceLinkPage();
		deny(page.denied);
		tester.startPage(page);
		assertFalse(page.isPageStateless());

		tester.clickLink("denied", false);

		assertRefused(page.denied);
		assertRefusalMessage(page.denied);
		assertFalse("ResourceLink#onClick() ran although the resource request was refused",
			page.deniedClicked);
	}

	/**
	 * Control: the fix must not refuse a resource the strategy allows.
	 */
	@Test
	public void resourceLinkWithAllowedResourceIsStillServed()
	{
		ResourceLinkPage page = new ResourceLinkPage();
		deny(page.denied);
		tester.startPage(page);

		tester.clickLink("allowed", false);

		assertStreamed("allowed", page.allowed);
	}

	@Test
	public void resourceLinkWithDeniedResourceReferenceIsStillRefused()
	{
		ResourceLinkPage page = new ResourceLinkPage();
		deny(page.referenced);
		tester.startPage(page);

		tester.clickLink("reference", false);

		assertRefused(page.referenced);
	}

	/**
	 * Control: the other constructor of the same component renders a resource URL, which is the
	 * route that was authorized all along.
	 */
	@Test
	public void resourceLinkWithAllowedResourceReferenceIsStillServed()
	{
		ResourceLinkPage page = new ResourceLinkPage();
		deny(page.denied);
		tester.startPage(page);

		String href = tester.getTagByWicketId("reference").getAttribute("href");
		assertTrue("expected a resource URL, was " + href, href.contains("resource/"));

		tester.clickLink("reference", false);

		assertStreamed("referenced", page.referenced);
	}

	@Test
	public void imageWithDeniedResourceIsRefused()
	{
		ImagePage page = new ImagePage();
		deny(page.src);
		tester.startPage(page);

		tester.executeUrl(urlInMarkup("src", "src"));

		assertRefused(page.src);
		assertRefusalMessage(page.src);
	}

	/**
	 * Control.
	 */
	@Test
	public void imageWithAllowedResourceIsStillServed()
	{
		ImagePage page = new ImagePage();
		deny(page.fromModel);
		tester.startPage(page);

		tester.executeUrl(urlInMarkup("src", "src"));

		assertStreamed("src", page.src);
	}

	@Test
	public void imageWithDeniedResourceFromItsModelIsRefused()
	{
		ImagePage page = new ImagePage();
		deny(page.fromModel);
		tester.startPage(page);

		tester.executeUrl(urlInMarkup("model", "src"));

		assertRefused(page.fromModel);
	}

	@Test
	public void deniedSrcSetEntryOfAnImageIsRefused()
	{
		ImagePage page = new ImagePage();
		deny(page.setEntry);
		tester.startPage(page);

		tester.executeUrl(urlInMarkup("srcset", "src"));

		assertEquals("the request should have been refused",
			HttpServletResponse.SC_FORBIDDEN,
			tester.getLastResponse().getStatus());
		assertFalse("the refused srcset entry streamed", page.setEntry.responded);
	}

	/**
	 * Control.
	 */
	@Test
	public void allowedSrcSetOfAnImageIsStillServed()
	{
		ImagePage page = new ImagePage();
		deny(page.src);
		tester.startPage(page);

		tester.executeUrl(urlInMarkup("srcset", "src"));

		assertEquals(HttpServletResponse.SC_OK, tester.getLastResponse().getStatus());
		assertTrue("the src of the image did not stream", page.setSrc.responded);
		assertTrue("the srcset entry of the image did not stream", page.setEntry.responded);
	}

	@Test
	public void theStrategyIsAskedAboutEverySrcSetEntry()
	{
		ImagePage page = new ImagePage();
		deny();
		tester.startPage(page);

		tester.executeUrl(urlInMarkup("srcset", "src"));

		assertEquals("the strategy must be asked about the src and about every srcset entry",
			Arrays.asList(page.setSrc, page.setEntry),
			asked);
	}

	@Test
	public void imageButtonWithDeniedResourceIsRefused()
	{
		ImageButtonPage page = new ImageButtonPage();
		deny(page.denied);
		tester.startPage(page);

		tester.executeUrl(urlInMarkup("denied", "src"));

		assertRefused(page.denied);
		assertRefusalMessage(page.denied);
	}

	/**
	 * Control.
	 */
	@Test
	public void imageButtonWithAllowedResourceIsStillServed()
	{
		ImageButtonPage page = new ImageButtonPage();
		deny(page.denied);
		tester.startPage(page);

		tester.executeUrl(urlInMarkup("allowed", "src"));

		assertStreamed("buttonAllowed", page.allowed);
	}

	@Test
	public void theStrategyIsAskedAboutTheInstanceTheComponentHoldsWithEmptyParameters()
	{
		ResourceLinkPage page = new ResourceLinkPage();
		deny();
		tester.startPage(page);

		tester.clickLink("allowed", false);

		assertEquals(Arrays.asList(page.allowed), asked);
		assertNotNull("the strategy must not be asked with null parameters", askedWith);
		assertTrue("a listener URL carries no resource parameters", askedWith.isEmpty());
	}

	@Test
	public void theUnauthorizedResourceRequestListenerOfTheApplicationIsUsed()
	{
		ResourceLinkPage page = new ResourceLinkPage();
		deny(page.denied);
		tester.getApplication()
			.getSecuritySettings()
			.setUnauthorizedResourceRequestListener((resource, parameters) -> {
				throw new IllegalStateException("refused " + resource);
			});
		tester.startPage(page);

		try
		{
			tester.clickLink("denied", false);
			fail("the listener should have refused the request");
		}
		catch (RuntimeException e)
		{
			assertEquals("refused denied",
				Exceptions.findCause(e, IllegalStateException.class).getMessage());
		}

		assertFalse("the resource streamed although the listener refused the request",
			page.denied.responded);
	}

	/** A stateful page carrying both constructors of {@link ResourceLink}. */
	public static class ResourceLinkPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		final RecordingResource denied = new RecordingResource("denied");

		final RecordingResource allowed = new RecordingResource("allowed");

		final RecordingResource referenced = new RecordingResource("referenced");

		boolean deniedClicked;

		ResourceLinkPage()
		{
			setStatelessHint(false);

			add(new ResourceLink<Void>("denied", denied)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					deniedClicked = true;
				}
			});
			add(new ResourceLink<Void>("allowed", allowed));
			add(new ResourceLink<Void>("reference",
				new ResourceReference(ResourceLinkPage.class, "referenced")
				{
					private static final long serialVersionUID = 1L;

					@Override
					public IResource getResource()
					{
						return referenced;
					}
				}));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" //
				+ "<a wicket:id=\"denied\">denied</a>" //
				+ "<a wicket:id=\"allowed\">allowed</a>" //
				+ "<a wicket:id=\"reference\">reference</a>" //
				+ "</body></html>");
		}
	}

	/** A stateful page whose images all hold an {@link IResource} rather than a reference. */
	public static class ImagePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		final RecordingResource src = new RecordingResource("src");

		final RecordingResource setSrc = new RecordingResource("setSrc");

		final RecordingResource setEntry = new RecordingResource("setEntry");

		final RecordingResource fromModel = new RecordingResource("fromModel");

		ImagePage()
		{
			setStatelessHint(false);

			add(new Image("src", src));
			add(new Image("srcset", setSrc, setEntry));
			add(new Image("model", Model.of(fromModel)));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" //
				+ "<img wicket:id=\"src\"/>" //
				+ "<img wicket:id=\"srcset\"/>" //
				+ "<img wicket:id=\"model\"/>" //
				+ "</body></html>");
		}
	}

	/** A stateful page carrying two image buttons, each holding an {@link IResource}. */
	public static class ImageButtonPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		final RecordingResource denied = new RecordingResource("buttonDenied");

		final RecordingResource allowed = new RecordingResource("buttonAllowed");

		ImageButtonPage()
		{
			setStatelessHint(false);

			Form<Void> form = new Form<>("form");
			add(form);
			form.add(new ImageButton("denied", denied));
			form.add(new ImageButton("allowed", allowed));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" //
				+ "<form wicket:id=\"form\">" //
				+ "<input type=\"image\" wicket:id=\"denied\"/>" //
				+ "<input type=\"image\" wicket:id=\"allowed\"/>" //
				+ "</form></body></html>");
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
