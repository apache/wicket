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

import jakarta.annotation.Nonnull;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.pages.BrowserInfoForm;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Args;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;

/**
 * A behavior that collects the information to populate
 * WebClientInfo's ClientProperties by using Ajax. Compared to
 * {@link AjaxClientInfoBehavior} this class does not use a timer
 * but the DOM ready "event" to collect browser info.
 *
 * @see #onClientInfo(AjaxRequestTarget, WebClientInfo)
 */
public class AjaxOnDomReadyClientInfoBehavior extends AbstractDefaultAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void respond(AjaxRequestTarget target)
	{
		RequestCycle requestCycle = RequestCycle.get();

		IRequestParameters requestParameters = requestCycle.getRequest().getRequestParameters();
		WebClientInfo clientInfo = newWebClientInfo(requestCycle);
		clientInfo.getProperties().read(requestParameters);
		Session.get().setClientInfo(clientInfo);

		onClientInfo(target, clientInfo);
	}

	protected WebClientInfo newWebClientInfo(RequestCycle requestCycle)
	{
		return new WebClientInfo(requestCycle);
	}

	/**
	 * A callback method invoked when the client info is collected.
	 * 
	 * @param target
	 *          The Ajax request handler
	 * @param clientInfo
	 *          The collected info for the client 
	 */
	protected void onClientInfo(AjaxRequestTarget target, WebClientInfo clientInfo)
	{
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);
		attributes.setEventNames("domready");
		attributes.setMethod(AjaxRequestAttributes.Method.POST);
		attributes.getDynamicExtraParameters().add("return Wicket.BrowserInfo.collect()");
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(BrowserInfoForm.JS));
		response.render(JavaScriptHeaderItem.forScript(getCallbackScript(), "ajaxOnDomReadyClientInfoBehavior"));
	}

	/**
	 * Creates an {@link AjaxOnDomReadyClientInfoBehavior} based on lambda expressions
	 *
	 * @param onClientInfo
	 *            the {@code SerializableBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link WebClientInfo}
	 * @return the {@link AjaxOnDomReadyClientInfoBehavior}
	 */
	public static AjaxOnDomReadyClientInfoBehavior onClientInfo(@Nonnull SerializableBiConsumer<AjaxRequestTarget, WebClientInfo> onClientInfo)
	{
		Args.notNull(onClientInfo, "onClientInfo");

		return new AjaxOnDomReadyClientInfoBehavior()
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void onClientInfo(AjaxRequestTarget target, WebClientInfo clientInfo)
			{
				onClientInfo.accept(target, clientInfo);
			}
		};
	}
}
