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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.wicket.Application;
import org.apache.wicket.csp.ContentSecurityPolicySettings;
import org.apache.wicket.islands.serializer.IIslandSerializer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
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

	private static final int KEY_BYTES = 12;

	private record Endpoint(Class<?> requestType, IslandCallHandler<Object, Object> handler)
		implements IClusterable
	{
	}

	/**
	 * A page-less endpoint the island calls, with the context of its handle and the handle last
	 * issued for it.
	 */
	private static final class Pageless implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		private final String endpointId;

		private final IModel<? extends Serializable> context;

		private Serializable issuedFor;

		private String handle;

		private Pageless(String endpointId, IModel<? extends Serializable> context)
		{
			this.endpointId = endpointId;
			this.context = context;
		}

		private String handle()
		{
			if (context == null)
			{
				return null;
			}
			Serializable current = Args.notNull(context.getObject(), "context");
			IIslandHandleStore store = IslandSettings.get().getHandleStore();
			if (handle == null || !current.equals(issuedFor) ||
				store.resolve(endpointId, handle) == null)
			{
				handle = store.issue(endpointId, current);
				issuedFor = current;
			}
			return handle;
		}
	}

	private final String type;

	private final String key;

	private final Map<String, Endpoint> endpoints = new LinkedHashMap<>();

	private final Map<String, Pageless> pageless = new LinkedHashMap<>();

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
		key = Application.get()
			.getSecuritySettings()
			.getRandomSupplier()
			.getRandomBase64(KEY_BYTES);
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

	void addPageless(String name, String endpointId, IModel<? extends Serializable> context)
	{
		Args.notEmpty(name, "name");
		Args.notEmpty(endpointId, "endpointId");
		if (IslandSettings.get().getPagelessEndpoints().get(endpointId) == null)
		{
			throw new IllegalArgumentException("No page-less endpoint is registered as '" +
				endpointId + "'");
		}
		pageless.put(name, new Pageless(endpointId, context));
	}

	boolean hasEndpoints()
	{
		return !endpoints.isEmpty();
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
			serializer.toJson(endpoints.keySet()) +
			(hasEndpoints() ? ",\"ajax\":" + serializer.toJson(behavior.renderAttributes()) : "") +
			",\"pageless\":" + serializer.toJson(renderPageless()) +
			(form != null ? ",\"form\":" + form : "") + "}";
		return "<script type=\"application/json\" " + Island.ATTR_STATE + ">" +
			IslandJson.escapeForScript(state) + "</script><div " + Island.ATTR_ROOT + "></div>";
	}

	private Map<String, Map<String, String>> renderPageless()
	{
		Map<String, Map<String, String>> rendered = new LinkedHashMap<>();
		pageless.forEach((name, use) -> {
			PageParameters parameters = new PageParameters();
			parameters.set(0, use.endpointId);
			Map<String, String> call = new LinkedHashMap<>();
			call.put("url", RequestCycle.get()
				.urlFor(IslandSettings.get().getEndpointReference(), parameters)
				.toString());
			String handle = use.handle();
			if (handle != null)
			{
				call.put("handle", handle);
			}
			rendered.put(name, call);
		});
		return rendered;
	}

	void renderHead(IHeaderResponse response)
	{
		ContentSecurityPolicySettings csp = WebApplication.get().getCspSettings();
		if (csp.isNonceEnabled())
		{
			response.render(new MetaDataHeaderItem(MetaDataHeaderItem.META_TAG)
				.addTagAttribute("property", "csp-nonce")
				.addTagAttribute("nonce", csp.getNonce(RequestCycle.get())));
		}
		IslandSettings.get().getModuleLoader().renderHead(type, response);
	}

	void detach()
	{
		pageless.values().forEach(use -> {
			if (use.context != null)
			{
				use.context.detach();
			}
		});
	}

	static IIslandSerializer serializer()
	{
		return IslandSettings.get().getSerializer();
	}
}
