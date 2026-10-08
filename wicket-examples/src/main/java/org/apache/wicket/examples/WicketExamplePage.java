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
package org.apache.wicket.examples;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.examples.homepage.HomePage;
import org.apache.wicket.examples.source.SourcesPage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.apache.wicket.util.string.Strings;

/**
 * Base class for all example pages.
 * <p>
 * Every example describes itself in a bundle of its own, named after it - {@code ClockPage} in
 * {@code ClockPage.properties} - under three keys:
 * <ul>
 * <li>{@code title} - shown as the page heading and in the browser title, and used as the link
 * text by an index listing the example. A page without one gets no heading.</li>
 * <li>{@code description} - shown by an index after the link, optional.</li>
 * <li>{@code explanation} - what the example shows, rendered under the heading, optional.</li>
 * <li>{@code order} - where the example comes in its index, optional.</li>
 * </ul>
 * Saying it once in the example's own bundle is what lets {@link ExampleIndexPanel} build an index
 * out of whatever examples it finds, rather than out of a list someone has to keep up to date, and
 * it is what makes the prose of an example translatable at all.
 * <p>
 * <b>{@code explanation} is written into the page as it stands, without escaping</b>, so that it
 * can carry the markup the prose needs - a {@code <br/>}, a list, a {@code <tt>}. That is safe
 * only because these bundles are authored with the example, in this source tree. Never render
 * anything a user supplies through it.
 *
 * @author Jonathan Locke
 * @see ExampleIndexPanel
 */
public class WicketExamplePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Reads an example's own bundle, and only that one: no superclass of it, no package it sits
	 * in. An example that says nothing about itself is meant to say nothing, and must not end up
	 * borrowing the title of the base page it happens to extend.
	 */
	/** Where the main menu is, relative to the context root. */
	private static final String MAIN_INDEX = "index.html";

	private static final ComponentStringResourceLoader OWN_BUNDLE = new ComponentStringResourceLoader()
	{
		@Override
		protected boolean isStopResourceSearch(Class<?> clazz)
		{
			return true;
		}
	};

	/**
	 * Constructor
	 */
	public WicketExamplePage()
	{
		this(new PageParameters());
	}

	/**
	 * Constructor
	 * 
	 * @param pageParameters
	 */
	public WicketExamplePage(final PageParameters pageParameters)
	{
		super(pageParameters);

		BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("sources",
			SourcesPage.class, SourcesPage.generatePageParameters(this));
		add(link);
		
		link.setVisible(showSourceButton());
		
		PopupSettings settings = new PopupSettings("sources", PopupSettings.RESIZABLE);
		settings.setWidth(800);
		settings.setHeight(600);
		link.setPopupSettings(settings);
		
		add(buildHeader("pageHeader"));
		
		explain();
	}

	protected boolean showSourceButton() 
	{
		return true;
	}

	protected Panel buildHeader(String id) 
	{
		return new WicketExampleHeader(id);
	}


	/**
	 * Construct.
	 * 
	 * @param model
	 */
	public WicketExamplePage(IModel<?> model)
	{
		super(model);
	}

	/**
	 * Override base method to provide an explanation
	 */
	protected void explain()
	{
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		add(new Label("pageTitle", this::getPageTitle));
		add(new Label("exampleTitle", this::getExampleTitle)
		{
			@Override
			protected void onConfigure()
			{
				super.onConfigure();

				setVisible(Strings.isEmpty(getExampleTitle()) == false);
			}
		});
		add(new Label("explanation", this::getExampleExplanation)
		{
			@Override
			protected void onConfigure()
			{
				super.onConfigure();

				setVisible(Strings.isEmpty(getExampleExplanation()) == false);
			}
		}.setEscapeModelStrings(false));
		add(new BackToIndexPanel("navigation", getIndexPage(), getIndexPath()));
	}

	/**
	 * @return whether to show the way back at all, {@code true} for every example but the frameset
	 *         one, which is not a page a reader navigates away from
	 */
	protected boolean showNavigation()
	{
		return true;
	}

	/**
	 * @return the index this example is listed on beyond the main menu, {@code null} when the main
	 *         menu lists it directly
	 */
	protected Class<? extends WicketExamplePage> getIndexPage()
	{
		return null;
	}

	/**
	 * @return where {@link #getIndexPage()} is, relative to the context root, when it is a page of
	 *         another of the example applications; {@code null} when it is a page of this one and
	 *         can be linked to by class
	 */
	protected String getIndexPath()
	{
		return null;
	}

	/**
	 * @return what this example shows, by default the {@code explanation} its own bundle gives
	 */
	protected String getExampleExplanation()
	{
		return string(getClass(), "explanation", this);
	}

	/**
	 * @param example
	 *            the example to read the title of
	 * @param context
	 *            the component reading it
	 * @return a model of the example's title, for use as the body of a link to it
	 */
	public static IModel<String> title(Class<?> example, Component context)
	{
		return () -> string(example, "title", context);
	}

	/**
	 * Reads what an example says about itself, for the locale, style and variation of the
	 * component asking.
	 *
	 * @param example
	 *            the example to read from, a page or - as the wizard example does it - a component
	 *            a page is named after
	 * @param key
	 *            {@code title}, {@code description} or {@code order}
	 * @param context
	 *            the component reading it
	 * @return the value, or {@code null} when the example's own bundle does not have the key
	 */
	public static String string(Class<?> example, String key, Component context)
	{
		return string(example, key, context.getLocale(), context.getStyle(),
			context.getVariation());
	}

	/**
	 * @param example
	 *            the example to read from
	 * @param key
	 *            {@code title}, {@code description} or {@code order}
	 * @param locale
	 *            the locale to read it for
	 * @param style
	 *            the style to read it for, may be {@code null}
	 * @param variation
	 *            the variation to read it for, may be {@code null}
	 * @return the value, or {@code null} when the example's own bundle does not have the key
	 */
	public static String string(Class<?> example, String key, Locale locale, String style,
		String variation)
	{
		return OWN_BUNDLE.loadStringResource(example, key, locale, style, variation);
	}

	/**
	 * The way back out of an example: to the index of all the examples first, then to the one the
	 * example itself is listed on when that is somewhere else. Each link names where it goes, so
	 * that a reader two levels deep can tell the two apart.
	 * <p>
	 * Every link is rendered as a plain URL rather than as a link to a page class, because most
	 * of these indexes belong to another of the example applications: the main menu is the home
	 * page of its own application, and a group index is a page of that same one. Linking to such a
	 * page by class would ask the application the reader is currently in to render it, under that
	 * application's own mount, and every relative link on it would then point into the wrong
	 * application.
	 */
	private static final class BackToIndexPanel extends Panel
	{
		private static final long serialVersionUID = 1L;

		private final Class<? extends WicketExamplePage> index;

		private final String path;

		private BackToIndexPanel(final String id, final Class<? extends WicketExamplePage> index,
			final String path)
		{
			super(id);

			this.index = index;
			this.path = path;

			IModel<List<Step>> trail = this::trail;

			add(new ListView<>("indexes", trail)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(final ListItem<Step> item)
				{
					Step step = item.getModelObject();

					IModel<String> label = new StringResourceModel("back",
						BackToIndexPanel.this).setParameters(step.label());

					item.add(step.page() == null
						? new ExternalLink("back", step.href()).setBody(label)
						: new BookmarkablePageLink<Void>("back", step.page()).setBody(label));
				}
			});
		}

		@Override
		protected void onConfigure()
		{
			super.onConfigure();

			setVisible(trail().isEmpty() == false);
		}

		private List<Step> trail()
		{
			WicketExamplePage page = (WicketExamplePage)getPage();
			if (page.showNavigation() == false)
			{
				return List.of();
			}
			Class<?> here = page.getClass();
			List<Step> trail = new ArrayList<>();
			if (here != HomePage.class)
			{
				trail.add(new Step(getString("mainIndex"), null, url(MAIN_INDEX)));
			}
			if (index != null && index != here)
			{
				trail.add(path == null
					? new Step(string(index, "title", this), index, null)
					: new Step(string(index, "title", this), null, url(path)));
			}
			return trail;
		}

		private String url(final String path)
		{
			return RequestCycle.get().getUrlRenderer().renderContextRelativeUrl(path);
		}
	}

	/**
	 * One step of the way back: what to call it, and where it is - as a page when it belongs to
	 * the same application, and as a plain URL when it does not.
	 */
	private record Step(String label, Class<? extends WicketExamplePage> page, String href)
		implements Serializable
	{
	}

	/**
	 * @return the title of this example, by default the one its own bundle gives
	 */
	protected String getExampleTitle()
	{
		return string(getClass(), "title", this);
	}

	private String getPageTitle()
	{
		String exampleTitle = getExampleTitle();
		if (Strings.isEmpty(exampleTitle))
		{
			return getString("examplesTitle");
		}
		return new StringResourceModel("pageTitle", this).setParameters(exampleTitle).getString();
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(WicketExamplePage.class, "fonts/source-code-pro/stylesheet.css"), "screen"));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(WicketExamplePage.class, "fonts/source-sans-pro/stylesheet.css"), "screen"));
		response.render(CssHeaderItem.forReference(new CssResourceReference(WicketExamplePage.class, "style.css"),"screen"));
	}
}
