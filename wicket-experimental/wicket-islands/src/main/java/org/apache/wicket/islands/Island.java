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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import org.apache.wicket.IGenericComponent;
import org.apache.wicket.islands.serializer.IIslandSerializer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.io.IClusterable;
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

	private static final Pattern TYPE_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

	private record Endpoint(Class<?> requestType, IslandCallHandler<Object, Object> handler)
		implements IClusterable
	{
	}

	private final String type;

	private final String key;

	private final Map<String, Endpoint> endpoints = new LinkedHashMap<>();

	private final IslandEndpointBehavior endpointBehavior;

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
		Args.notNull(type, "type");
		if (!TYPE_PATTERN.matcher(type).matches())
		{
			throw new IllegalArgumentException("Invalid island type '" + type +
				"': use letters, digits, '-' and '_'");
		}
		this.type = type;
		key = Long.toString(ThreadLocalRandom.current().nextLong() & Long.MAX_VALUE, 36);
		setOutputMarkupId(true);
		endpointBehavior = new IslandEndpointBehavior();
		add(endpointBehavior);
	}

	/**
	 * @return the island type
	 */
	public final String getType()
	{
		return type;
	}

	/**
	 * @return the key identifying this island instance across re-renders
	 */
	public final String getKey()
	{
		return key;
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
	@SuppressWarnings("unchecked")
	public final <Q, R> Island<P> onCall(String name, Class<Q> requestType, Class<R> resultType,
		IslandCallHandler<Q, R> handler)
	{
		Args.notEmpty(name, "name");
		Args.notNull(requestType, "requestType");
		Args.notNull(resultType, "resultType");
		Args.notNull(handler, "handler");
		endpoints.put(name, new Endpoint(requestType, (IslandCallHandler<Object, Object>)handler));
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
		Args.notNull(handler, "handler");
		return onCall(name, requestType, Void.class, (request, call) -> {
			handler.handle(request, call);
			return null;
		});
	}

	Object invoke(String name, String payload, IslandCall call)
	{
		Endpoint endpoint = endpoints.get(name);
		if (endpoint == null)
		{
			throw new IslandCallRejectedException("Unknown call '" + name + "'.");
		}

		Object request = null;
		if (endpoint.requestType() != Void.class)
		{
			if (payload != null)
			{
				request = getSerializer().fromJson(payload, endpoint.requestType());
			}
			if (request == null)
			{
				throw new IslandCallRejectedException("The call has no payload.");
			}
		}
		return endpoint.handler().handle(request, call);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		if (tag.isOpenClose())
		{
			tag.setType(TagType.OPEN);
		}
		tag.put(ATTR_TYPE, type);
		tag.put(ATTR_KEY, key);
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		IIslandSerializer serializer = getSerializer();
		String state = "{\"props\":" + serializer.toJson(getModelObject()) + ",\"calls\":" +
			serializer.toJson(endpoints.keySet()) + ",\"ajax\":" +
			endpointBehavior.renderAttributes() + "}";

		replaceComponentTagBody(markupStream, openTag,
			"<script type=\"application/json\" " + ATTR_STATE + ">" +
				IslandJson.escapeForScript(state) + "</script><div " + ATTR_ROOT + "></div>");
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		IslandSettings.get().getModuleLoader().renderHead(type, response);
	}

	private IIslandSerializer getSerializer()
	{
		return IslandSettings.get().getSerializer();
	}
}
