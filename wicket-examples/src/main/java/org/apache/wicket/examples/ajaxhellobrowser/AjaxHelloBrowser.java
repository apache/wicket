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
package org.apache.wicket.examples.ajaxhellobrowser;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.ajax.AjaxClientInfoBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.settings.IRequestCycleSettings;


/**
 * A demo usage of AjaxBrowserInfoForm
 */
public class AjaxHelloBrowser extends WicketExamplePage
{
	/**
	 * Constructor.
	 */
	public AjaxHelloBrowser()
	{
		final MultiLineLabel clientInfo = new MultiLineLabel("clientinfo", new AbstractReadOnlyModel<String>()
		{
			@Override
			public String getObject()
			{
				ClientProperties properties = getClientProperties();
				return properties.toString();
			}
		});
		clientInfo.setOutputMarkupPlaceholderTag(true);
		clientInfo.setVisible(false);

		IModel<String> clientTimeModel = new AbstractReadOnlyModel<String>()
		{
			@Override
			public String getObject()
			{
				ClientProperties properties = getClientProperties();
				TimeZone timeZone = properties.getTimeZone();
				if (timeZone != null)
				{
					Calendar cal = Calendar.getInstance(timeZone);
					Locale locale = getLocale();
					DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.LONG, locale);
					String calAsString = dateFormat.format(cal.getTime());
					StringBuilder b = new StringBuilder("Based on your settings, your time is: ");
					b.append(calAsString);
					b.append(" (and your time zone is ");
					b.append(timeZone.getDisplayName(getLocale()));
					b.append(')');
					return b.toString();
				}
				return "Unfortunately, we were not able to figure out what your time zone is, so we have"
						+ " no idea what your time is";
			}
		};
		final Label clientTime = new Label("clienttime", clientTimeModel);
		clientTime.setOutputMarkupPlaceholderTag(true);
		clientTime.setVisible(false);

		add(new AjaxClientInfoBehavior()
		{
			@Override
			protected void onClientInfo(AjaxRequestTarget target, WebClientInfo info)
			{
				super.onClientInfo(target, info);

				clientInfo.setVisible(true);
				clientTime.setVisible(true);
				target.add(clientInfo, clientTime);
			}
		});

		add(clientInfo, clientTime);
	}

	/**
	 * A helper function that makes sure that gathering of extended browser info
	 * is not enabled when reading the ClientInfo's properties
	 *
	 * @return the currently available client info
	 */
	private ClientProperties getClientProperties()
	{
		IRequestCycleSettings requestCycleSettings = getApplication().getRequestCycleSettings();
		boolean gatherExtendedBrowserInfo = requestCycleSettings.getGatherExtendedBrowserInfo();
		ClientProperties properties = null;
		try
		{
			requestCycleSettings.setGatherExtendedBrowserInfo(false);
			WebClientInfo clientInfo = (WebClientInfo) getSession().getClientInfo();
			properties = clientInfo.getProperties();
		}
		finally
		{
			requestCycleSettings.setGatherExtendedBrowserInfo(gatherExtendedBrowserInfo);
		}
		return properties;
	}
}
