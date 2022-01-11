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
import org.apache.wicket.Page;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.danekja.java.util.function.serializable.SerializableConsumer;

/**
 * An Ajax behavior that notifies when a new browser window/tab is opened with url to a page
 * instance which is already opened in another window/tab in the same user session.
 *
 * @since 6.0
 * @see #onNewWindow(AjaxRequestTarget)
 */
public class AjaxNewWindowNotifyingBehavior extends AbstractDefaultAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * The name of the HTTP request parameter that transports the current page window's name.
	 */
	private static final String PARAM_WINDOW_NAME = "windowName";

	/**
	 * The name of the window the page is bound to.
	 */
	private String boundName;

	/**
	 * Returns the window's name.
	 * 
	 * @return name of {@code null} if not yet bound to a window
	 */
	public String getWindowName()
	{
		return boundName;
	}
	
	/**
	 * Overridden to add the current window name to the request.
	 */
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);

		String parameter = "return {'" + PARAM_WINDOW_NAME + "': window.name}";
		attributes.getDynamicExtraParameters().add(parameter);

		if (boundName != null)
		{
			// already bound, send request only when changed
			attributes.getAjaxCallListeners().add(new AjaxCallListener()
			{
				@Override
				public CharSequence getPrecondition(Component component)
				{
					return String.format("return (window.name !== '%s');", boundName);
				}
			});
		}
	}

	/**
	 * Overridden to initiate a request once the page was rendered.
	 */
	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		response.render(OnLoadHeaderItem
			.forScript("setTimeout(function() {" + getCallbackScript().toString() + "}, 30);"));
	}

	@Override
	protected void respond(AjaxRequestTarget target)
	{
		String windowName = getComponent().getRequest().getRequestParameters().getParameterValue(PARAM_WINDOW_NAME).toString();

		if (boundName == null)
		{
			// not bound to any window yet

			if (Strings.isEmpty(windowName))
			{
				// create new name
				windowName = newWindowName();
				target.appendJavaScript(String.format("window.name = '%s';", windowName));
			}

			// now bound to window
			boundName = windowName;
		}
		else if (boundName.equals(windowName) == false)
		{
			onNewWindow(target);
		}
	}

	/**
	 * Create a name for a nameless window, default uses a random {@link UUID}.
	 * 
	 * @return window name
	 */
	protected String newWindowName()
	{
		return UUID.randomUUID().toString();
	}

	/**
	 * A callback method that is called when a new window/tab is opened for a page instance
	 * which is already opened in another window/tab.
	 * <p>
	 * Default implementation redirects to a new page instance with identical page parameters.
	 *
	 * @param target
	 *            the current request handler
	 */
	protected void onNewWindow(AjaxRequestTarget target)
	{
		Page page = getComponent().getPage();

		getComponent().setResponsePage(page.getClass(), page.getPageParameters());
	}

	/**
	 * Creates an {@link AjaxNewWindowNotifyingBehavior} based on lambda expressions
	 * 
	 * @param onNewWindow
	 *            the {@code SerializableConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxNewWindowNotifyingBehavior}
	 */
	public static AjaxNewWindowNotifyingBehavior onNewWindow(SerializableConsumer<AjaxRequestTarget> onNewWindow)
	{
		Args.notNull(onNewWindow, "onNewWindow");

		return new AjaxNewWindowNotifyingBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onNewWindow(AjaxRequestTarget target)
			{
				onNewWindow.accept(target);
			}
		};
	}
}
