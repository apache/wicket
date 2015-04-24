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

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.pages.BrowserInfoForm;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.time.Duration;

/**
 * An behavior that collects the information to populate
 * WebClientInfo's ClientProperties by using Ajax
 *
 * @see #onClientInfo(AjaxRequestTarget, org.apache.wicket.protocol.http.request.WebClientInfo)
 */
public class AjaxClientInfoBehavior extends AbstractAjaxTimerBehavior
{
	/**
	 * Constructor.
	 *
	 * Auto fires after 50 millis.
	 */
	public AjaxClientInfoBehavior()
	{
		this(Duration.milliseconds(50));
	}

	/**
	 * Constructor.
	 *
	 * Auto fires after {@code duration}.
	 */
	public AjaxClientInfoBehavior(Duration duration)
	{
		super(duration);
	}

	@Override
	protected final void onTimer(AjaxRequestTarget target)
	{
		stop(target);

		RequestCycle requestCycle = RequestCycle.get();
		IRequestParameters requestParameters = requestCycle.getRequest().getRequestParameters();
		String navigatorAppName = requestParameters.getParameterValue("navigatorAppName").toString("N/A");
		String navigatorAppVersion = requestParameters.getParameterValue("navigatorAppVersion").toString("N/A");
		String navigatorAppCodeName = requestParameters.getParameterValue("navigatorAppCodeName").toString("N/A");
		boolean navigatorCookieEnabled = requestParameters.getParameterValue("navigatorCookieEnabled").toBoolean(false);
		Boolean navigatorJavaEnabled = requestParameters.getParameterValue("navigatorJavaEnabled").toBoolean(false);
		String navigatorLanguage = requestParameters.getParameterValue("navigatorLanguage").toString("N/A");
		String navigatorPlatform = requestParameters.getParameterValue("navigatorPlatform").toString("N/A");
		String navigatorUserAgent = requestParameters.getParameterValue("navigatorUserAgent").toString("N/A");
		int screenWidth = requestParameters.getParameterValue("screenWidth").toInt(-1);
		int screenHeight = requestParameters.getParameterValue("screenHeight").toInt(-1);
		int screenColorDepth = requestParameters.getParameterValue("screenColorDepth").toInt(-1);
		String utcOffset = requestParameters.getParameterValue("utcOffset").toString("N/A");
		String utcDSTOffset = requestParameters.getParameterValue("utcDSTOffset").toString("N/A");
		int browserWidth = requestParameters.getParameterValue("browserWidth").toInt(-1);
		int browserHeight = requestParameters.getParameterValue("browserHeight").toInt(-1);
		String hostname = requestParameters.getParameterValue("hostname").toString("N/A");

		WebClientInfo clientInfo = new WebClientInfo(requestCycle);
		Session.get().setClientInfo(clientInfo);

		ClientProperties properties = clientInfo.getProperties();
		properties.setNavigatorAppCodeName(navigatorAppCodeName);
		properties.setNavigatorAppName(navigatorAppName);
		properties.setNavigatorAppVersion(navigatorAppVersion);
		properties.setNavigatorCookieEnabled(navigatorCookieEnabled);
		properties.setNavigatorJavaEnabled(navigatorJavaEnabled);
		properties.setNavigatorLanguage(navigatorLanguage);
		properties.setNavigatorPlatform(navigatorPlatform);
		properties.setNavigatorUserAgent(navigatorUserAgent);
		properties.setScreenWidth(screenWidth);
		properties.setScreenHeight(screenHeight);
		properties.setScreenColorDepth(screenColorDepth);
		properties.setUtcOffset(utcOffset);
		properties.setUtcDSTOffset(utcDSTOffset);
		properties.setBrowserWidth(browserWidth);
		properties.setBrowserHeight(browserHeight);
		properties.setHostname(hostname);

		onClientInfo(target, clientInfo);
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

		attributes.getDynamicExtraParameters().add("return Wicket.BrowserInfo.collect()");
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(BrowserInfoForm.JS));
	}
}
