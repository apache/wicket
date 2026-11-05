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

import org.apache.wicket.islands.serializer.IIslandSerializer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Args;

/**
 * What every island has, whatever component it is: its type and key, its calls and the behavior
 * receiving them, and the rendering of its host element.
 */
final class IslandSupport implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private static final Pattern TYPE_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

	private record Endpoint(Class<?> requestType, IslandCallHandler<Object, Object> handler)
		implements IClusterable
	{
	}

	private final String type;

	private final String key;

	private final Map<String, Endpoint> endpoints = new LinkedHashMap<>();

	private final Map<String, String> pageless = new LinkedHashMap<>();

	private final IslandEndpointBehavior behavior = new IslandEndpointBehavior();

	IslandSupport(String type)
	{
		Args.notNull(type, "type");
		if (!TYPE_PATTERN.matcher(type).matches())
		{
			throw new IllegalArgumentException("Invalid island type '" + type +
				"': use letters, digits, '-' and '_'");
		}
		this.type = type;
		key = Long.toString(ThreadLocalRandom.current().nextLong() & Long.MAX_VALUE, 36);
	}

	String getType()
	{
		return type;
	}

	String getKey()
	{
		return key;
	}

	IslandEndpointBehavior getBehavior()
	{
		return behavior;
	}

	@SuppressWarnings("unchecked")
	<Q, R> void addEndpoint(String name, Class<Q> requestType, Class<R> resultType,
		IslandCallHandler<Q, R> handler)
	{
		Args.notEmpty(name, "name");
		Args.notNull(requestType, "requestType");
		Args.notNull(resultType, "resultType");
		Args.notNull(handler, "handler");
		endpoints.put(name, new Endpoint(requestType, (IslandCallHandler<Object, Object>)handler));
	}

	void addPageless(String name, String endpointId)
	{
		Args.notEmpty(name, "name");
		Args.notEmpty(endpointId, "endpointId");
		pageless.put(name, endpointId);
	}

	static <Q> IslandCallHandler<Q, Void> withoutResult(IslandCallConsumer<Q> consumer)
	{
		Args.notNull(consumer, "handler");
		return (request, call) -> {
			consumer.handle(request, call);
			return null;
		};
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
				request = serializer().fromJson(payload, endpoint.requestType());
			}
			if (request == null)
			{
				throw new IslandCallRejectedException("The call has no payload.");
			}
		}
		return endpoint.handler().handle(request, call);
	}

	void onComponentTag(ComponentTag tag)
	{
		if (tag.isOpenClose())
		{
			tag.setType(TagType.OPEN);
		}
		tag.put(Island.ATTR_TYPE, type);
		tag.put(Island.ATTR_KEY, key);
	}

	/**
	 * @param props
	 *            the props
	 * @param form
	 *            the JSON of the form state, {@code null} for an island that is no form component
	 * @return the body of the host element after any markup of its own: the state and the root
	 */
	String renderState(Object props, String form)
	{
		IIslandSerializer serializer = serializer();
		String state = "{\"props\":" + serializer.toJson(props) + ",\"calls\":" +
			serializer.toJson(endpoints.keySet()) + ",\"ajax\":" + behavior.renderAttributes() +
			",\"pageless\":" + serializer.toJson(pagelessUrls()) +
			(form != null ? ",\"form\":" + form : "") + "}";
		return "<script type=\"application/json\" " + Island.ATTR_STATE + ">" +
			IslandJson.escapeForScript(state) + "</script><div " + Island.ATTR_ROOT + "></div>";
	}

	private Map<String, String> pagelessUrls()
	{
		Map<String, String> urls = new LinkedHashMap<>();
		pageless.forEach((name, id) -> {
			PageParameters parameters = new PageParameters();
			parameters.set(0, id);
			urls.put(name, RequestCycle.get()
				.urlFor(IslandSettings.get().getEndpointReference(), parameters)
				.toString());
		});
		return urls;
	}

	void renderHead(IHeaderResponse response)
	{
		IslandSettings.get().getModuleLoader().renderHead(type, response);
	}

	static IIslandSerializer serializer()
	{
		return IslandSettings.get().getSerializer();
	}
}
