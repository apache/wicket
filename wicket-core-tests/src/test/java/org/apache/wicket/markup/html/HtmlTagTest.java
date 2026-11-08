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
package org.apache.wicket.markup.html;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupType;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests the rendering of the &lt;html&gt; tag of a page through {@link WebPage#onHtmlTag}.
 */
class HtmlTagTest extends WicketTestCase
{
	private static final String BODY = "<body><span wicket:id=\"label\"></span></body></html>";

	@Test
	void langIsTheLanguageTagOfTheSessionLocale()
	{
		tester.getSession().setLocale(Locale.forLanguageTag("nl-NL"));

		tester.startPage(new InlinePage("<html>" + BODY));

		assertResponseContains("<html lang=\"nl-NL\" dir=\"ltr\">");
	}

	@Test
	void langFollowsTheLocaleOfEachRender()
	{
		tester.getSession().setLocale(Locale.forLanguageTag("nl-NL"));
		tester.startPage(new InlinePage("<html>" + BODY));
		assertResponseContains("<html lang=\"nl-NL\" dir=\"ltr\">");

		tester.getSession().setLocale(Locale.GERMANY);
		tester.startPage(new InlinePage("<html>" + BODY));
		assertResponseContains("<html lang=\"de-DE\" dir=\"ltr\">");
	}

	@Test
	void otherAttributesOfTheHtmlTagAreKept()
	{
		tester.startPage(new InlinePage(
			"<html class=\"no-js\" xmlns:wicket=\"http://wicket.apache.org\">" + BODY));

		assertResponseContains(
			"<html class=\"no-js\" xmlns:wicket=\"http://wicket.apache.org\" lang=\"en-US\"" +
				" dir=\"ltr\">");
	}

	@Test
	void langInTheMarkupIsKept()
	{
		tester.getSession().setLocale(Locale.forLanguageTag("nl-NL"));

		tester.startPage(new InlinePage("<html lang=\"en\">" + BODY));

		assertResponseContains("<html lang=\"en\">");
	}

	@Test
	void xmlLangInTheMarkupIsKept()
	{
		tester.getSession().setLocale(Locale.forLanguageTag("nl-NL"));

		tester.startPage(new InlinePage(
			"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">" + BODY));

		assertResponseContains("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">");
	}

	@Test
	void dirIsRtlForARightToLeftLanguage()
	{
		tester.getSession().setLocale(Locale.forLanguageTag("ar-EG"));

		tester.startPage(new InlinePage("<html>" + BODY));

		assertResponseContains("<html lang=\"ar-EG\" dir=\"rtl\">");
	}

	@Test
	void ltrForALanguageWrittenInLatinScript()
	{
		tester.getSession().setLocale(Locale.forLanguageTag("az-Latn"));

		tester.startPage(new InlinePage("<html>" + BODY));

		assertResponseContains("<html lang=\"az-Latn\" dir=\"ltr\">");
	}

	@Test
	void dirInTheMarkupIsKept()
	{
		tester.getSession().setLocale(Locale.forLanguageTag("he-IL"));

		tester.startPage(new InlinePage("<html dir=\"ltr\">" + BODY));

		assertResponseContains("<html dir=\"ltr\" lang=\"he-IL\">");
	}

	@Test
	void noDirWhenTheMarkupDeclaresItsLanguage()
	{
		tester.getSession().setLocale(Locale.forLanguageTag("ar"));

		tester.startPage(new InlinePage("<html lang=\"en\">" + BODY));

		assertResponseContains("<html lang=\"en\">");
	}

	@Test
	void subclassOptsOutOfDir()
	{
		tester.getSession().setLocale(Locale.forLanguageTag("ar"));

		tester.startPage(new InlinePage("<html>" + BODY)
		{
			@Override
			protected void onHtmlTag(ComponentTag tag)
			{
				super.onHtmlTag(tag);
				tag.remove("dir");
			}
		});

		assertResponseContains("<html lang=\"ar\">");
	}

	@Test
	void noLangForALocaleWithoutLanguage()
	{
		tester.getSession().setLocale(Locale.ROOT);

		tester.startPage(new InlinePage("<html>" + BODY));

		assertResponseContains("<html>");
	}

	@Test
	void namespacedHtmlTagIsLeftAlone()
	{
		tester.startPage(new InlinePage(
			"<x:html xmlns:x=\"urn:x\"><body><span wicket:id=\"label\"></span></body></x:html>"));

		assertResponseContains("<x:html xmlns:x=\"urn:x\">");
	}

	@Test
	void pageThatIsNotAWebPageWritesTheHtmlTagUnchanged()
	{
		tester.startPage(new PlainPage());

		assertResponseContains("<html><body><span wicket:id=\"label\">text</span></body></html>");
	}

	@Test
	void htmlTagBoundToAComponentIsLeftToTheComponent()
	{
		InlinePage page = new InlinePage("<html wicket:id=\"html\">" + BODY);
		page.add(new TransparentWebMarkupContainer("html")
			.add(AttributeModifier.replace("lang", "fr")));

		tester.startPage(page);

		assertResponseContains("<html wicket:id=\"html\" lang=\"fr\">");
	}

	@Test
	void subclassAddsAttributes()
	{
		tester.startPage(new InlinePage("<html>" + BODY)
		{
			@Override
			protected void onHtmlTag(ComponentTag tag)
			{
				super.onHtmlTag(tag);
				tag.put("data-theme", "dark\"");
			}
		});

		assertResponseContains("<html lang=\"en-US\" dir=\"ltr\" data-theme=\"dark&quot;\">");
	}

	@Test
	void htmlTagOfTheBasePageIsRenderedWithMarkupInheritance()
	{
		tester.getSession().setLocale(Locale.forLanguageTag("nl-NL"));

		tester.startPage(new DerivedPage());

		assertResponseContains("<html lang=\"nl-NL\" dir=\"ltr\">");
		assertResponseContains("<p>derived</p>");
	}

	@Test
	void queuedComponentsInsideTheHtmlTagAreDequeued()
	{
		tester.startPage(new QueuingPage());

		assertResponseContains("<html lang=\"en-US\" dir=\"ltr\">");
		assertResponseContains("<span wicket:id=\"label\">queued</span>");
	}

	@Test
	void ajaxUpdateFindsTheMarkupOfComponentsInsideTheHtmlTag()
	{
		tester.startPage(new AjaxPage());

		tester.clickLink("link", true);

		tester.assertComponentOnAjaxResponse("label");
		assertResponseContains("clicked");
	}

	private void assertResponseContains(String expected)
	{
		String response = tester.getLastResponseAsString();
		assertTrue(response.contains(expected),
			"Expected <" + expected + "> in response:\n" + response);
	}

	private static class InlinePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private final String markup;

		InlinePage(String markup)
		{
			this.markup = markup;
			add(new Label("label", "text"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(markup);
		}
	}

	private static class PlainPage extends Page implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		PlainPage()
		{
			add(new Label("label", "text"));
		}

		@Override
		public MarkupType getMarkupType()
		{
			return MarkupType.HTML_MARKUP_TYPE;
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html>" + BODY);
		}
	}

	private static class BasePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			if (containerClass == BasePage.class)
			{
				return new StringResourceStream("<html><body><wicket:child/></body></html>");
			}
			return new StringResourceStream(
				"<html lang=\"ignored\"><body><wicket:extend><p>derived</p></wicket:extend>" +
					"</body></html>");
		}
	}

	private static class DerivedPage extends BasePage
	{
		private static final long serialVersionUID = 1L;
	}

	private static class QueuingPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		QueuingPage()
		{
			queue(new Label("label", "queued"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html>" + BODY);
		}
	}

	private static class AjaxPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		AjaxPage()
		{
			Label label = new Label("label", "text");
			label.setOutputMarkupId(true);
			add(label);
			add(new AjaxLink<Void>("link")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					label.setDefaultModelObject("clicked");
					target.add(label);
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id=\"link\"></a><span wicket:id=\"label\"></span>" +
					"</body></html>");
		}
	}
}
