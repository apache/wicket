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

import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.core.request.handler.ComponentNotFoundException;
import org.apache.wicket.core.request.handler.ListenerInvocationNotAllowedException;
import org.apache.wicket.core.request.mapper.StalePageException;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

/**
 * Answers an island call that fails before it reaches its island, because the page expired or
 * changed, or the call is not allowed. Without it, Wicket would redirect the browser to an error
 * page or a re-rendered page, and the island would never learn what happened to its call.
 */
final class IslandCallFailureListener implements IRequestCycleListener
{
	@Override
	public IRequestHandler onException(RequestCycle cycle, Exception ex)
	{
		if (!(cycle.getRequest() instanceof WebRequest request) || !request.isAjax())
		{
			return null;
		}
		String callId = request.getPostParameters()
			.getParameterValue(IslandEndpointBehavior.PARAM_CALL_ID)
			.toOptionalString();
		if (callId == null)
		{
			return null;
		}

		String kind;
		String message;
		if (ex instanceof PageExpiredException || ex instanceof StalePageException ||
			ex instanceof ComponentNotFoundException)
		{
			kind = "expired";
			message = IslandAnswer.EXPIRED_MESSAGE;
		}
		else if (ex instanceof ListenerInvocationNotAllowedException ||
			ex instanceof AuthorizationException)
		{
			kind = "forbidden";
			message = "The call is not allowed.";
		}
		else
		{
			return null;
		}

		return new AnswerHandler(IslandAnswer.failure(IslandSettings.get().getSerializer(), callId,
			kind, List.of(message), List.of()));
	}

	/**
	 * Writes an Ajax response that holds nothing but the answer.
	 */
	private record AnswerHandler(String answer) implements IRequestHandler
	{
		@Override
		public void respond(IRequestCycle requestCycle)
		{
			WebResponse response = (WebResponse)requestCycle.getResponse();
			response.setContentType("text/xml; charset=UTF-8");
			response.disableCaching();
			response.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ajax-response><data name=\"" +
				IslandAnswer.DATA_NAME + "\"><![CDATA[" + answer.replace("]]>", "]]]]><![CDATA[>") +
				"]]></data></ajax-response>");
		}
	}
}
