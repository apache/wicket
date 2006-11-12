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
package wicket.extensions.ajax.markup.html;

import wicket.Component;
import wicket.RequestCycle;
import wicket.Response;
import wicket.ajax.AbstractDefaultAjaxBehavior;
import wicket.ajax.IAjaxIndicatorAware;
import wicket.behavior.AbstractBehavior;

/**
 * A behavior that adds a span with wicket's default indicator gif to the end of
 * the component's markup. This span can be used as an ajax busy indicator. For
 * an example usage see {@link IndicatingAjaxLink}
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
public class WicketAjaxIndicatorAppender extends AbstractBehavior
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Component instance this behavior is bound to
	 */
	private Component component;

	/**
	 * Construct.
	 */
	public WicketAjaxIndicatorAppender()
	{
	}

	/**
	 * @see wicket.behavior.AbstractBehavior#onRendered(wicket.Component)
	 */
	@Override
	public void onRendered(Component component)
	{
		final Response r = component.getResponse();
		r.write("<span style=\"display:none;\" class=\"");
		r.write(getSpanClass());
		r.write("\" ");
		r.write("id=\"");
		r.write(getMarkupId());
		r.write("\">");
		r.write("<img src=\"");
		r.write(getIndicatorUrl());
		r.write("\"/></span>");
	}

	/**
	 * @return url of the animated indicator image
	 */
	protected CharSequence getIndicatorUrl()
	{
		return RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR);
	}

	/**
	 * @return css class name of the generated outer span
	 */
	protected String getSpanClass()
	{
		return "wicket-ajax-indicator";
	}

	/**
	 * Returns the markup id attribute of the outer most span of this indicator.
	 * This is the id of the span that should be hidden or show to hide or show
	 * the indicator.
	 * 
	 * @return markup id of outer most span
	 */
	public String getMarkupId()
	{
		return component.getMarkupId() + "--ajax-indicator";
	}

	/**
	 * @see wicket.behavior.AbstractBehavior#bind(wicket.Component)
	 */
	@Override
	public final void bind(Component component)
	{
		this.component = component;
	}
}
