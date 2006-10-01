/*
 * $Id: HelloBrowser.java 3375 2005-12-06 14:18:14 -0800 (Tue, 06 Dec 2005)
 * eelco12 $ $Revision$ $Date: 2005-12-06 14:18:14 -0800 (Tue, 06 Dec
 * 2005) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.hellobrowser;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;
import wicket.protocol.http.ClientProperties;
import wicket.protocol.http.request.WebClientInfo;

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
		// add a label that outputs a the client info object; it will result in
		// the calls RequestCycle.getClientInfo -> Session.getClientInfo ->
		// RequestCycle.newClientInfo. this is done once by default and
		// afterwards cached in the session object. This application uses
		// a custom requestcycle that overrides newClientInfo to not only
		// look at the user-agent request header, but also snoops javascript
		// properties by redirecting to a special page.

		// don't use a property model here or anything else that is resolved
		// during rendering, as changing the request target during rendering
		// is not allowed.
		WebClientInfo clientInfo = getRequestCycle().getClientInfo();
		final ClientProperties properties = clientInfo.getProperties();

		new Label(this, "clientinfo", properties.toString());

		IModel clientTimeModel = new AbstractReadOnlyModel()
		{
			/**
			 * @see wicket.model.AbstractReadOnlyModel#getObject()
			 */
			@Override
			public Object getObject()
			{
				TimeZone timeZone = properties.getTimeZone();
				if (timeZone != null)
				{
					Calendar cal = Calendar.getInstance(timeZone);
					Locale locale = getLocale();
					DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.LONG, locale);
					String calAsString = dateFormat.format(cal.getTime());
					StringBuffer b = new StringBuffer("Based on your settings, your time is: ");
					b.append(calAsString);
					b.append(" (and your time zone is ");
					b.append(timeZone.getDisplayName(getLocale()));
					b.append(")");
					return b.toString();
				}
				return "Unfortunately, we were not able to figure out what your time zone is, so we have"
						+ "no idea what your time is";
			}
		};
		new Label(this, "clienttime", clientTimeModel);
	}
}