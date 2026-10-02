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

import org.apache.wicket.Component;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.github.openjson.JSONObject;

/**
 * Veils a single component while an Ajax request fired from inside it is in flight, see
 * {@link AbstractVeilBehavior}.
 * <p>
 * Requests fired by the component itself or by any component nested in it are covered; when
 * local veils are nested, the innermost one takes the request. Such a request does not veil the
 * page, even when it has a {@link PageVeilBehavior}. Requests fired from elsewhere leave the
 * component alone.
 * <p>
 * The veil is appended to the component's element, so the component has to render an element
 * that can hold a {@code div}. For the duration, the element gets the class
 * {@code wicket-veil-host}, which isolates it so the veil stays within its stacking context, and,
 * unless it is positioned already, {@code wicket-veil-host-static} with
 * {@code position: relative}. When the element scrolls, the veil covers its visible part. If an
 * Ajax update or a push replaces the element while it is veiled, the veil moves onto the new
 * element. The behavior makes the component output its markup id.
 * <p>
 * The server can raise the veil too, for an update it is about to push to the component, for
 * example through a WebSocket connection: send {@link #getVeilMessage()} as a text message when
 * the work for the component starts, and call {@link #unveil(IPartialPageRequestHandler)} with
 * the handler that pushes the updated component. The spinner follows the same timings as for an
 * Ajax request. When the work fails and no update follows, send {@link #getUnveilMessage()}
 * instead. The text messages are understood by the client of Wicket's native WebSocket support,
 * which hands them to {@code Wicket.Veil}:
 *
 * <pre>
 * // while handling a request, e.g. the click that schedules the work
 * String veilMessage = veil.getVeilMessage();
 *
 * // when the work starts, from any thread
 * connection.sendMessage(veilMessage);
 *
 * // when the result is pushed, e.g. in onEvent() for a WebSocketPushPayload
 * handler.add(component);
 * veil.unveil(handler);
 * </pre>
 *
 * A behavior instance belongs to a single component.
 *
 * @since 11.0.0
 */
public class LocalVeilBehavior extends AbstractVeilBehavior
{
	private static final long serialVersionUID = 1L;

	private Component component;

	@Override
	public void bind(Component component)
	{
		super.bind(component);

		if (this.component != null && this.component != component)
		{
			throw new IllegalStateException(LocalVeilBehavior.class.getSimpleName() +
				" is already bound to " + this.component.getPageRelativePath() +
				" and cannot be bound to another component");
		}
		this.component = component;
		component.setOutputMarkupId(true);
	}

	@Override
	public void unbind(Component component)
	{
		this.component = null;

		super.unbind(component);
	}

	/**
	 * A text message that raises this veil on the client when it is sent through a WebSocket
	 * connection of the component's page. Each one has to be matched by
	 * {@link #unveil(IPartialPageRequestHandler)} or {@link #getUnveilMessage()}.
	 * <p>
	 * Like any component access, call it while handling a request; the message itself is a plain
	 * string that can then be sent from any thread.
	 *
	 * @return the message
	 * @throws IllegalStateException
	 *             if the behavior is not bound to a component
	 */
	public String getVeilMessage()
	{
		return message("show");
	}

	/**
	 * A text message that lowers this veil on the client when it is sent through a WebSocket
	 * connection of the component's page, for when no update follows a
	 * {@link #getVeilMessage()}.
	 *
	 * @return the message
	 * @throws IllegalStateException
	 *             if the behavior is not bound to a component
	 */
	public String getUnveilMessage()
	{
		return message("hide");
	}

	/**
	 * Lowers this veil on the client once the given handler's update has been applied, matching
	 * an earlier {@link #getVeilMessage()}.
	 *
	 * @param handler
	 *            the handler pushing the update of the component
	 * @throws IllegalStateException
	 *             if the behavior is not bound to a component
	 */
	public void unveil(IPartialPageRequestHandler handler)
	{
		handler.appendJavaScript("Wicket.Veil.hide(" + quotedMarkupId() + ");");
	}

	private String message(String command)
	{
		return "{\"wicketVeil\":\"" + command + "\",\"id\":" + quotedMarkupId() + "}";
	}

	private String quotedMarkupId()
	{
		if (component == null)
		{
			throw new IllegalStateException(
				LocalVeilBehavior.class.getSimpleName() + " is not bound to a component");
		}
		return JSONObject.quote(component.getMarkupId());
	}

	@Override
	protected CharSequence getInitScript(Component component)
	{
		return "Wicket.Veil.local(" + JSONObject.quote(component.getMarkupId()) + ", " +
			getOptions() + ");";
	}
}
