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
package org.apache.wicket.extensions.ajax.veil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Locale;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PageVeilBehavior} and {@link LocalVeilBehavior}.
 */
class VeilBehaviorTest extends WicketTestCase
{
	@Test
	void pageVeilContributesResourcesAndInitScript()
	{
		tester.startPage(new TestPage(new PageVeilBehavior(), null, false));

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("wicket-veil.js"), response);
		assertTrue(response.contains("wicket-veil.css"), response);
		assertTrue(response.contains("Wicket.Veil.page({\"delay\":300,\"minimum\":500});"),
			response);
	}

	@Test
	void localVeilContributesInitScriptForItsComponent()
	{
		TestPage page = tester.startPage(new TestPage(null, new LocalVeilBehavior(), false));

		String response = tester.getLastResponseAsString();
		String markupId = page.container.getMarkupId();
		assertTrue(response.contains("id=\"" + markupId + "\""), response);
		assertTrue(response.contains("wicket-veil.js"), response);
		assertTrue(response.contains(
			"Wicket.Veil.local(\"" + markupId + "\", {\"delay\":300,\"minimum\":500});"),
			response);
	}

	@Test
	void timingsCanBeOverridden()
	{
		tester.startPage(new TestPage(new PageVeilBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Duration getSpinnerDelay()
			{
				return Duration.ofMillis(100);
			}

			@Override
			protected Duration getMinimumSpinnerTime()
			{
				return Duration.ofSeconds(1);
			}
		}, null, false));

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("Wicket.Veil.page({\"delay\":100,\"minimum\":1000});"),
			response);
	}

	@Test
	void localVeilTimingsCanBeSet()
	{
		LocalVeilBehavior veil = new LocalVeilBehavior();
		veil.setSpinnerDelay(Duration.ZERO).setMinimumSpinnerTime(Duration.ofSeconds(2));
		TestPage page = tester.startPage(new TestPage(null, veil, false));

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("Wicket.Veil.local(\"" + page.container.getMarkupId() +
			"\", {\"delay\":0,\"minimum\":2000});"), response);
	}

	@Test
	void timingsDoNotDependOnTheDefaultLocale()
	{
		Locale defaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.forLanguageTag("th-TH-u-nu-thai"));
		try
		{
			tester.startPage(new TestPage(new PageVeilBehavior(), null, false));
		}
		finally
		{
			Locale.setDefault(defaultLocale);
		}

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("Wicket.Veil.page({\"delay\":300,\"minimum\":500});"),
			response);
	}

	@Test
	void negativeTimingsAreRejected()
	{
		LocalVeilBehavior veil = new LocalVeilBehavior();

		assertThrows(IllegalArgumentException.class,
			() -> veil.setSpinnerDelay(Duration.ofMillis(-1)));
		assertThrows(IllegalArgumentException.class,
			() -> veil.setMinimumSpinnerTime(Duration.ofMillis(-1)));
	}

	@Test
	void veilMessagesNameTheComponent()
	{
		LocalVeilBehavior veil = new LocalVeilBehavior();
		TestPage page = tester.startPage(new TestPage(null, veil, false));
		String markupId = page.container.getMarkupId();

		assertEquals("{\"wicketVeil\":\"show\",\"id\":\"" + markupId + "\"}",
			veil.getVeilMessage());
		assertEquals("{\"wicketVeil\":\"hide\",\"id\":\"" + markupId + "\"}",
			veil.getUnveilMessage());
	}

	@Test
	void unveilLowersTheVeilAfterTheUpdate()
	{
		LocalVeilBehavior veil = new LocalVeilBehavior();
		TestPage page = tester.startPage(new TestPage(null, veil, false));
		page.onClick = target -> {
			target.add(page.container);
			veil.unveil(target);
		};

		tester.clickLink("container:link");

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("Wicket.Veil.hide(\"" + page.container.getMarkupId() + "\");"),
			response);
	}

	@Test
	void anUnboundLocalVeilHasNoMessages()
	{
		LocalVeilBehavior veil = new LocalVeilBehavior();

		assertThrows(IllegalStateException.class, veil::getVeilMessage);
	}

	@Test
	void aLocalVeilBelongsToOneComponent()
	{
		LocalVeilBehavior veil = new LocalVeilBehavior();
		new WebMarkupContainer("first").add(veil);

		assertThrows(IllegalStateException.class,
			() -> new WebMarkupContainer("second").add(veil));
	}

	@Test
	void pageVeilRejectsNonPageComponents()
	{
		WebMarkupContainer container = new WebMarkupContainer("container");

		assertThrows(IllegalArgumentException.class, () -> container.add(new PageVeilBehavior()));
	}

	@Test
	void noVeilAddsTheExtraParameter()
	{
		tester.startPage(new TestPage(new PageVeilBehavior(), null, true));

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("\"ep\":[{\"name\":\"wicket_nb\",\"value\":\"true\"}]"),
			response);
	}

	@Test
	void requestsAreNotOptedOutByDefault()
	{
		tester.startPage(new TestPage(new PageVeilBehavior(), null, false));

		assertFalse(tester.getLastResponseAsString().contains("wicket_nb"));
	}

	/**
	 * A page with a container holding an Ajax link.
	 */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		final WebMarkupContainer container;

		SerializableConsumer<AjaxRequestTarget> onClick = target -> {
		};

		TestPage(Behavior pageBehavior, Behavior containerBehavior, boolean noVeil)
		{
			if (pageBehavior != null)
			{
				add(pageBehavior);
			}

			add(container = new WebMarkupContainer("container"));
			if (containerBehavior != null)
			{
				container.add(containerBehavior);
			}

			container.add(new AjaxLink<Void>("link")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
				{
					super.updateAjaxAttributes(attributes);
					if (noVeil)
					{
						PageVeilBehavior.noVeil(attributes);
					}
				}

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					onClick.accept(target);
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><head></head><body>" +
				"<div wicket:id=\"container\"><a wicket:id=\"link\">link</a></div>" +
				"</body></html>");
		}
	}
}
