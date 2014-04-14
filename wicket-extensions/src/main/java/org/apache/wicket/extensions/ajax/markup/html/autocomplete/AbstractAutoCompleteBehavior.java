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
package org.apache.wicket.extensions.ajax.markup.html.autocomplete;


import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.IWrappedHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.Strings;

/**
 * @since 1.2
 * 
 * @author Janne Hietam&auml;ki (jannehietamaki)
 */
public abstract class AbstractAutoCompleteBehavior extends AbstractDefaultAjaxBehavior
{
	/**
	 * A wrapper for the auto-complete DOM-ready event handler.
	 * <p>
	 * A plain OnDomReadyItem would be aggregated by {@link ResourceAggregator}, possible coming
	 * after the event registration of other behaviors.
	 */
	private static final class WrappedHeaderItem extends HeaderItem implements IWrappedHeaderItem
	{
		private final OnDomReadyHeaderItem item;

		private WrappedHeaderItem(OnDomReadyHeaderItem onDomReady)
		{
			item = onDomReady;
		}

		@Override
		public void render(Response response)
		{
			item.render(response);
		}

		@Override
		public Iterable<?> getRenderTokens()
		{
			return item.getRenderTokens();
		}

		@Override
		public HeaderItem getWrapped()
		{
			return item;
		}

		@Override
		public HeaderItem wrap(HeaderItem item)
		{
			if (item instanceof OnDomReadyHeaderItem)
				return new WrappedHeaderItem((OnDomReadyHeaderItem)item);
			return item;
		}

		@Override
		public List<HeaderItem> getDependencies()
		{
			ResourceReference wicketAjaxReference = Application.get().
					getJavaScriptLibrarySettings().getWicketAjaxReference();
			return Arrays.<HeaderItem>asList(JavaScriptHeaderItem.forReference(wicketAjaxReference));
		}
	}

	public static final ResourceReference AUTOCOMPLETE_JS = new JavaScriptResourceReference(
		AutoCompleteBehavior.class, "wicket-autocomplete.js");

	private static final long serialVersionUID = 1L;

	protected AutoCompleteSettings settings;

	/**
	 * Constructor that creates an default {@link AutoCompleteSettings}
	 */
	public AbstractAutoCompleteBehavior()
	{
		this(new AutoCompleteSettings());
	}

	/**
	 * Constructor
	 * 
	 * @param settings
	 *            settings for the autocomplete list
	 */
	public AbstractAutoCompleteBehavior(AutoCompleteSettings settings)
	{
		if (settings == null)
		{
			settings = new AutoCompleteSettings();
		}
		this.settings = settings;
	}

	@Override
	public void renderHead(final Component component, final IHeaderResponse response)
	{
		super.renderHead(component, response);

		renderAutocompleteHead(response);
	}

	/**
	 * Render autocomplete init javascript and other head contributions
	 * 
	 * @param response
	 */
	private void renderAutocompleteHead(final IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forReference(AUTOCOMPLETE_JS));
		final String id = getComponent().getMarkupId();

		String indicatorId = findIndicatorId();
		if (Strings.isEmpty(indicatorId))
		{
			indicatorId = "null";
		}
		else
		{
			indicatorId = "'" + indicatorId + "'";
		}

		String initJS = String.format("new Wicket.AutoComplete('%s','%s',%s,%s);", id,
			getCallbackUrl(), constructSettingsJS(), indicatorId);

		final OnDomReadyHeaderItem onDomReady = OnDomReadyHeaderItem.forScript(initJS);

		response.render(new WrappedHeaderItem(onDomReady));
	}

	/**
	 * 
	 * @return JS settings
	 */
	protected final String constructSettingsJS()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("{preselect: ").append(settings.getPreselect());
		sb.append(",maxHeight: ").append(settings.getMaxHeightInPx());
		sb.append(",adjustInputWidth: ").append(settings.isAdjustInputWidth());
		sb.append(",useSmartPositioning: ").append(settings.getUseSmartPositioning());
		sb.append(",showListOnEmptyInput: ").append(settings.getShowListOnEmptyInput());
		sb.append(",ignoreBordersWhenPositioning: ").append(
			settings.getIgnoreBordersWhenPositioning());
		sb.append(",showListOnFocusGain: ").append(settings.getShowListOnFocusGain());
		sb.append(",throttleDelay: ").append(settings.getThrottleDelay());
		sb.append(",minInputLength: ").append(settings.getMinInputLength());
		sb.append(",parameterName: '").append(settings.getParameterName()).append('\'');
		sb.append(",showCompleteListOnFocusGain: ").append(
			settings.getShowCompleteListOnFocusGain());
		if (settings.getCssClassName() != null)
		{
			sb.append(",className: '").append(settings.getCssClassName()).append('\'');
		}
		sb.append('}');
		return sb.toString();
	}

	/**
	 * Callback for the ajax event generated by the javascript. This is where we need to generate
	 * our response.
	 * 
	 * @param input
	 *            the input entered so far
	 * @param requestCycle
	 *            current request cycle
	 */
	protected abstract void onRequest(String input, RequestCycle requestCycle);

	@Override
	protected void respond(final AjaxRequestTarget target)
	{
		final RequestCycle requestCycle = RequestCycle.get();
		final String val = requestCycle.getRequest()
			.getRequestParameters()
			.getParameterValue(settings.getParameterName())
			.toOptionalString();

		onRequest(val, requestCycle);
	}

}
