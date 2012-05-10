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
package org.apache.wicket.ajax;

import java.util.UUID;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.string.StringValue;

/**
 * An Ajax behavior that notifies when a new browser window/tab is opened with
 * url to a page instance which is already opened in another window/tab.
 *
 * Note: this behavior may be assigned only to an instance of a WebPage class.
 *
 * @since 6.0
 */
public abstract class NewWindowNotifier extends AbstractDefaultAjaxBehavior
{
	/**
	 * A unique name used for the page window's name
	 */
	private final String windowName;

	/**
	 * The name of the HTTP request parameter that brings the current page window's name.
	 */
	private static final String PARAM_WINDOW_NAME = "windowName";

	/**
	 * A flag whether this behavior has been rendered at least once.
	 */
	private boolean hasBeenRendered;

	/**
	 * Constructor.
	 */
	public NewWindowNotifier()
	{
		this(UUID.randomUUID().toString());
	}

	/**
	 * Constructor.
	 *
	 * @param windowName
	 *      the custom name to use for the page's window
	 */
	public NewWindowNotifier(final String windowName)
	{
		this.windowName = windowName;
	}

	@Override
	protected final void onBind()
	{
		super.onBind();

		Component component = getComponent();
		if (component instanceof WebPage == false)
		{
			throw new WicketRuntimeException(NewWindowNotifier.class.getName() + " can be assigned only to WebPage instances.");
		}
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);
		
		String uuidParam = "return {'"+ PARAM_WINDOW_NAME +"': window.name}";
		attributes.getDynamicExtraParameters().add(uuidParam);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		if (hasBeenRendered == false)
		{
			hasBeenRendered = true;
			response.render(OnDomReadyHeaderItem.forScript(String.format("window.name='%s'", windowName)));
		}
		response.render(OnLoadHeaderItem.forScript("setTimeout(function() {" + getCallbackScript().toString() + "}, 30);"));
	}

	@Override
	protected void respond(AjaxRequestTarget target)
	{
		StringValue uuidParam = getComponent().getRequest().getRequestParameters().getParameterValue(PARAM_WINDOW_NAME);

		if (windowName.equals(uuidParam.toString()) == false)
		{
			onNewWindow(target);
		}
	}

	/**
	 * A callback method when a new window/tab is opened for a page instance
	 * which is already opened in another window/tab.
	 *
	 * @param target
	 *      the current ajax request handler
	 */
	protected abstract void onNewWindow(AjaxRequestTarget target);

}
