/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.encodings;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.PageParameters;
import wicket.contrib.utils.encoding.CharSetUtil;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;

/**
 * Example of configuring locale encodings.
 * 
 * @author Jonathan Locke
 */
public class Home extends WicketExamplePage
{
	private static Log log = LogFactory.getLog(Home.class);

	/**
	 * Constructor
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public Home(final PageParameters parameters)
	{
		add(new Label("message", "Hello world! Test: הצü"));
	}

	/**
	 * Because only servlet 2.4 supports web.xml locale-encoding-mapping-list
	 * deployment descriptors, this is a workaround for servlet 2.3
	 */
	protected void configureResponse()
	{
		final Locale originalLocale = getSession().getLocale();
		getSession().setLocale(Locale.GERMANY);
		super.configureResponse();

		final String encoding = "text/" + getMarkupType() + "; charset="
				+ CharSetUtil.getEncoding(getRequestCycle());

		getResponse().setContentType(encoding);
		getSession().setLocale(originalLocale);
	}
}


