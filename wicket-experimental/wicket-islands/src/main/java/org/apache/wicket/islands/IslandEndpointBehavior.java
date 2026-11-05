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

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.islands.serializer.IIslandSerializer;
import org.apache.wicket.islands.serializer.IslandSerializationException;
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

	static final String DATA_NAME = "wicket-islands";

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);
		attributes.setMethod(AjaxRequestAttributes.Method.POST);
		attributes.setChannel(new AjaxChannel("wicket-island-" + getComponent().getMarkupId()));
	}

	CharSequence renderAttributes()
	{
		return renderAjaxAttributes(getComponent());
	}

	@Override
	protected void respond(AjaxRequestTarget target)
	{
		Island<?> island = (Island<?>)getComponent();
		IRequestParameters parameters = RequestCycle.get().getRequest().getPostParameters();
		String callId = parameters.getParameterValue(PARAM_CALL_ID).toString("");
		String name = parameters.getParameterValue(PARAM_CALL).toString("");
		String payload = parameters.getParameterValue(PARAM_PAYLOAD).toOptionalString();

		IIslandSerializer serializer = IslandSettings.get().getSerializer();
		IslandCall call = new IslandCall(island, target);

		String outcome;
		try
		{
			Object result = island.invoke(name, payload, call);
			outcome = "\"ok\":true,\"value\":" + serializer.toJson(result);
		}
		catch (IslandCallRejectedException e)
		{
			outcome = failure(serializer, "invalid", e.getMessages());
		}
		catch (IslandSerializationException e)
		{
			LOG.debug("Island call '{}' on {} failed to (de)serialize", name, island.getPath(), e);
			outcome = failure(serializer, "invalid",
				List.of(detail(e, "The call's payload or result could not be converted.")));
		}
		catch (AuthorizationException e)
		{
			outcome = failure(serializer, "forbidden", List.of("The call is not allowed."));
		}
		catch (ReplaceHandlerException e)
		{
			throw e;
		}
		catch (RuntimeException e)
		{
			LOG.error("Island call '{}' on {} failed", name, island.getPath(), e);
			outcome = failure(serializer, "error",
				List.of(detail(e, "The call failed on the server.")));
		}

		target.addData(DATA_NAME, "{\"call\":" + serializer.toJson(callId) + "," + outcome +
			",\"invalidate\":" + serializer.toJson(call.getInvalidations()) + "}");
	}

	private static String failure(IIslandSerializer serializer, String kind, List<String> messages)
	{
		return "\"ok\":false,\"error\":{\"kind\":" + serializer.toJson(kind) + ",\"messages\":" +
			serializer.toJson(messages) + "}";
	}

	private static String detail(RuntimeException e, String message)
	{
		return Application.get().usesDevelopmentConfig() ? message + " " + e : message;
	}
}
