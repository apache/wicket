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
package org.apache.wicket.examples.hellobrowser;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.Session;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;


/**
 * Client snooping page.
 * 
 * @author Eelco Hillenius
 */
public class HelloBrowser extends WicketExamplePage
{
	/**
	 * Construct.
	 */
	public HelloBrowser()
	{
		// Add a label that outputs the client info object; it will result in
		// the calls Session.getClientInfo.

		// don't use a property model here or anything else that is resolved
		// during rendering, as changing the request target during rendering
		// is not allowed.
		final ClientProperties properties = ((WebClientInfo)Session.get().getClientInfo()).getProperties();

		add(new MultiLineLabel("clientinfo", properties.toString()));

		IModel<String> clientTimeModel = new AbstractReadOnlyModel<String>()
		{
			/**
			 * @see org.apache.wicket.model.AbstractReadOnlyModel#getObject()
			 */
			@Override
			public String getObject()
			{
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
		add(new Label("clienttime", clientTimeModel));
	}
}
