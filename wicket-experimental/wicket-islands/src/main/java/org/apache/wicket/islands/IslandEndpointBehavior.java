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
import java.util.List;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.islands.serializer.IIslandSerializer;
import org.apache.wicket.islands.serializer.IslandSerializationException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.RequestHandlerExecutor.ReplaceHandlerException;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives the page-bound calls of an island and answers each with a data entry carrying the
 * result or the failure.
 */
final class IslandEndpointBehavior extends AbstractDefaultAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(IslandEndpointBehavior.class);

	static final String PARAM_CALL = "island-call";

	static final String PARAM_CALL_ID = "island-call-id";

	static final String PARAM_PAYLOAD = "island-payload";

	static final String PARAM_KEY = "island-key";

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);
		attributes.setMethod(AjaxRequestAttributes.Method.POST);
		attributes.setChannel(channel());
	}

	/**
	 * Keeps the page stateless while the island has no page-bound calls.
	 */
	@Override
	public boolean getStatelessHint(Component component)
	{
		return !supportOf(component).hasEndpoints();
	}

	private AjaxChannel channel()
	{
		return new AjaxChannel("wicket-island-" + getComponent().getMarkupId());
	}

	/**
	 * Renders a fixed set of plain values rather than the behavior's full Ajax attributes: what an
	 * application adds to every behavior, such as call listeners or preconditions, is rendered as
	 * functions, which the island state cannot carry as JSON.
	 *
	 * @return the Ajax attributes of the island's calls
	 */
	Map<String, String> renderAttributes()
	{
		Map<String, String> attributes = new LinkedHashMap<>();
		attributes.put("u", getCallbackUrl().toString());
		attributes.put("m", "POST");
		attributes.put("ch", channel().toString());
		return attributes;
	}

	/**
	 * @return whether the current request is a call to this behavior
	 */
	boolean isRequested()
	{
		IRequestHandler handler = RequestCycle.get().getActiveRequestHandler();
		return handler instanceof ListenerRequestHandler listener &&
			listener.getBehaviorIndex() != null &&
			getComponent().getBehaviorById(listener.getBehaviorIndex()) == this;
	}

	@Override
	protected void respond(AjaxRequestTarget target)
	{
		Component component = getComponent();
		IslandSupport island = supportOf(component);
		IRequestParameters parameters = RequestCycle.get().getRequest().getPostParameters();
		String callId = parameters.getParameterValue(PARAM_CALL_ID).toString("");
		String name = parameters.getParameterValue(PARAM_CALL).toString("");
		String payload = parameters.getParameterValue(PARAM_PAYLOAD).toOptionalString();
		String key = parameters.getParameterValue(PARAM_KEY).toString("");

		IIslandSerializer serializer = IslandSettings.get().getSerializer();
		IslandCall call = new IslandCall(component, target);
		List<String> invalidations = call.getInvalidations();

		String answer;
		if (!island.getKey().equals(key))
		{
			answer = IslandAnswer.failure(serializer, callId, "expired",
				List.of(IslandAnswer.EXPIRED_MESSAGE), invalidations);
			target.addData(IslandAnswer.DATA_NAME, answer);
			return;
		}

		try
		{
			Object result = island.invoke(name, payload, call);
			answer = IslandAnswer.success(serializer, callId, result, invalidations);
		}
		catch (IslandCallRejectedException e)
		{
			answer = IslandAnswer.failure(serializer, callId, "invalid", e.getMessages(),
				invalidations);
		}
		catch (IslandSerializationException e)
		{
			LOG.debug("Island call '{}' on {} has a payload that cannot be converted", name,
				component.getPath(), e);
			answer = IslandAnswer.failure(serializer, callId, "invalid",
				List.of(detail(e, "The call's payload could not be converted.")),
				invalidations);
		}
		catch (AuthorizationException e)
		{
			answer = IslandAnswer.failure(serializer, callId, "forbidden",
				List.of("The call is not allowed."), invalidations);
		}
		catch (ReplaceHandlerException e)
		{
			throw e;
		}
		catch (RuntimeException e)
		{
			LOG.error("Island call '{}' on {} failed", name, component.getPath(), e);
			answer = IslandAnswer.failure(serializer, callId, "error",
				List.of(detail(e, "The call failed on the server.")), invalidations);
		}

		target.addData(IslandAnswer.DATA_NAME, answer);
	}

	private static IslandSupport supportOf(Component component)
	{
		if (component instanceof Island<?> island)
		{
			return island.getIslandSupport();
		}
		return ((FormIsland<?, ?>)component).getIslandSupport();
	}

	private static String detail(RuntimeException e, String message)
	{
		return Application.get().usesDevelopmentConfig() ? message + " " + e : message;
	}
}
