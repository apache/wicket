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
package org.apache.wicket.islands;

import java.io.Serializable;

import org.apache.wicket.IGenericComponent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * A component whose body is rendered in the browser by a JavaScript framework component, its
 * client-side peer.
 * <p>
 * The island renders an empty host element that names its type. The client-side runtime mounts
 * the framework component registered for that type into it, and hands it the island's props: the
 * object of the island's model, converted to JSON. Calls registered with {@code onCall} can be
 * invoked by the peer; they run on the server in the context of this component, under the page
 * lock, and travel as Wicket Ajax requests, so a handler can update other components through
 * {@link IslandCall#getTarget()}.
 * <p>
 * <strong>The props are written into the page, where the user can read them.</strong> The model
 * should hold an explicit projection made for the client, never a domain object with fields the
 * user must not see. The JSON is escaped for the <code>&lt;script&gt;</code> element it is
 * written into.
 * <p>
 * <strong>Call payloads come from the browser and are untrusted.</strong> A request type should
 * contain only what the interaction may change; the handler validates it and applies it to the
 * server-side state.
 *
 * @param <P>
 *            the type of the props
 * @since 12.0.0
 */
public class Island<P> extends WebComponent implements IGenericComponent<P, Island<P>>
{
	private static final long serialVersionUID = 1L;

	/**
	 * The attribute holding the island type.
	 */
	public static final String ATTR_TYPE = "data-wicket-island";

	/**
	 * The attribute holding the island key, which identifies the island across re-renders.
	 */
	public static final String ATTR_KEY = "data-wicket-island-key";

	/**
	 * The attribute marking the script element holding the island's state.
	 */
	public static final String ATTR_STATE = "data-wicket-island-state";

	/**
	 * The attribute marking the element the client-side peer is mounted into.
	 */
	public static final String ATTR_ROOT = "data-wicket-island-root";

	private final IslandSupport island;

	/**
	 * @param id
	 *            the component id
	 * @param type
	 *            the island type, naming the client-side module; letters, digits, '-' and '_'
	 * @param props
	 *            the model of the props
	 */
	public Island(String id, String type, IModel<P> props)
	{
		super(id, props);
		island = new IslandSupport(type);
		setOutputMarkupId(true);
		add(island.getBehavior());
	}

	/**
	 * @return the island type
	 */
	public final String getType()
	{
		return island.getType();
	}

	/**
	 * @return the key identifying this island instance across re-renders
	 */
	public final String getKey()
	{
		return island.getKey();
	}

	final IslandSupport getIslandSupport()
	{
		return island;
	}

	/**
	 * Registers a call that returns a result.
	 *
	 * @param <Q>
	 *            the request type
	 * @param <R>
	 *            the result type
	 * @param name
	 *            the name the client calls
	 * @param requestType
	 *            the request type, {@link Void} for a call without payload
	 * @param resultType
	 *            the result type
	 * @param handler
	 *            the handler
	 * @return {@code this}
	 */
	public final <Q, R> Island<P> onCall(String name, Class<Q> requestType, Class<R> resultType,
		IslandCallHandler<Q, R> handler)
	{
		island.addEndpoint(name, requestType, resultType, handler);
		return this;
	}

	/**
	 * Registers a call that returns no result.
	 *
	 * @param <Q>
	 *            the request type
	 * @param name
	 *            the name the client calls
	 * @param requestType
	 *            the request type, {@link Void} for a call without payload
	 * @param handler
	 *            the handler
	 * @return {@code this}
	 */
	public final <Q> Island<P> onCall(String name, Class<Q> requestType,
		IslandCallConsumer<Q> handler)
	{
		island.addEndpoint(name, requestType, Void.class, IslandSupport.withoutResult(handler));
		return this;
	}

	/**
	 * Makes a page-less endpoint callable by the client-side peer under the endpoint's id.
	 *
	 * @param endpointId
	 *            the id the endpoint is registered under in {@link PagelessEndpoints}
	 * @return {@code this}
	 */
	public final Island<P> usePageless(String endpointId)
	{
		return usePageless(endpointId, endpointId);
	}

	/**
	 * Makes a page-less endpoint callable by the client-side peer under the given name. The peer
	 * calls it like any other call; it runs outside the page lock and cannot update components.
	 *
	 * @param name
	 *            the name the client calls
	 * @param endpointId
	 *            the id the endpoint is registered under in {@link PagelessEndpoints}
	 * @return {@code this}
	 */
	public final Island<P> usePageless(String name, String endpointId)
	{
		island.addPageless(name, endpointId, null);
		return this;
	}

	/**
	 * Makes a page-less endpoint callable by the client-side peer under the given name, with a
	 * handle for the given context. The context is read when the island renders and kept on the
	 * server, in the {@link IslandSettings#getHandleStore() handle store}; the client only holds the
	 * handle and sends it with each call, and the endpoint reads the context with
	 * {@link PagelessCall#getHandle(Class)}. Use it for context the client must not choose, such as
	 * the record the page shows. The handle is reissued when the context changes.
	 * <p>
	 * <strong>A handle is not an authorization mechanism.</strong> The endpoint still checks that
	 * the session may access the context.
	 *
	 * @param name
	 *            the name the client calls
	 * @param endpointId
	 *            the id the endpoint is registered under in {@link PagelessEndpoints}
	 * @param context
	 *            the context of the calls; small, serializable and with a meaningful
	 *            {@code equals}
	 * @return {@code this}
	 */
	public final Island<P> usePageless(String name, String endpointId,
		IModel<? extends Serializable> context)
	{
		island.addPageless(name, endpointId, Args.notNull(context, "context"));
		return this;
	}

	/**
	 * Lets calls reach a page Wicket recreated after the original expired. The recreated island has
	 * a new key, so the call is answered as expired instead of being handled.
	 */
	@Override
	public boolean canCallListenerAfterExpiry()
	{
		return true;
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		island.onComponentTag(tag);
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		replaceComponentTagBody(markupStream, openTag, island.renderState(getModelObject(), null));
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		island.renderHead(response);
	}

	@Override
	protected void onDetach()
	{
		island.detach();
		super.onDetach();
	}
}
