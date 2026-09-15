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

import org.apache.wicket.examples.source.SourcesPage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.string.Strings;

/**
 * Base class for all example pages.
 * <p>
 * Shows the title of the example as the page heading and in the browser title. The title is
 * looked up in the resource bundles under the key {@code <simple class name>.title}, usually in the
 * {@code wicket-package.properties} of the example's package, so that an index page linking to
 * the example can use the very same text. A page without such a key gets no heading.
 *
 * @author Jonathan Locke
 */
public class WicketExamplePage extends WebPage
{
	private static final long serialVersionUID = 1L;

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
	}

	/**
	 * @param page
	 *            an example page
	 * @return the resource key of the title of the given example page
	 */
	public static String titleKey(Class<? extends WicketExamplePage> page)
	{
		return page.getSimpleName() + ".title";
	}

	/**
	 * @return the resource key of the title of this example, by default the one given by
	 *         {@link #titleKey(Class)} for this page's class
	 */
	protected String getExampleTitleKey()
	{
		return titleKey(getClass());
	}

	private String getExampleTitle()
	{
		return getLocalizer().getStringIgnoreSettings(getExampleTitleKey(), this, null, null);
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
