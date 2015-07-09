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
package org.apache.wicket.extensions.ajax.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;

/**
 * A behavior that adds a span with wicket's default indicator gif to the end of the component's
 * markup. This span can be used as an ajax busy indicator. For an example usage see
 * {@link IndicatingAjaxLink}
 * <p>
 * Instances of this behavior must not be shared between components.
 * 
 * @see IndicatingAjaxLink
 * @see IAjaxIndicatorAware
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class AjaxIndicatorAppender extends Behavior
{
	/**
	 * Component instance this behavior is bound to
	 */
	private Component component;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public AjaxIndicatorAppender()
	{
	}

	@Override
	public void renderHead(final Component component, final IHeaderResponse response)
	{
		super.renderHead(component, response);

		IPartialPageRequestHandler target = component.getRequestCycle().find(IPartialPageRequestHandler.class);
		if (target != null)
		{
			final String javascript = "var e = Wicket.$('" + getMarkupId() +
				"'); if (e != null && typeof(e.parentNode) != 'undefined') e.parentNode.removeChild(e);";

			target.prependJavaScript(javascript);
		}
	}


	@Override
	public void afterRender(final Component component)
	{
		super.afterRender(component);
		final Response r = component.getResponse();

		r.write("<span style=\"display:none;\" class=\"");
		r.write(getSpanClass());
		r.write("\" ");
		r.write("id=\"");
		r.write(getMarkupId());
		r.write("\">");
		r.write("<img src=\"");
		r.write(getIndicatorUrl());
		r.write("\" alt=\"\"/></span>");

	}

	/**
	 * @return url of the animated indicator image
	 */
	protected CharSequence getIndicatorUrl()
	{
		IRequestHandler handler = new ResourceReferenceRequestHandler(
			AbstractDefaultAjaxBehavior.INDICATOR);
		return RequestCycle.get().urlFor(handler);
	}

	/**
	 * @return css class name of the generated outer span
	 */
	protected String getSpanClass()
	{
		return "wicket-ajax-indicator";
	}

	/**
	 * Returns the markup id attribute of the outer most span of this indicator. This is the id of
	 * the span that should be hidden or show to hide or show the indicator.
	 * 
	 * @return markup id of outer most span
	 */
	public String getMarkupId()
	{
		return component.getMarkupId() + "--ajax-indicator";
	}

	/**
	 * @see org.apache.wicket.behavior.Behavior#bind(org.apache.wicket.Component)
	 */
	@Override
	public final void bind(final Component component)
	{
		this.component = component;
	}
}
