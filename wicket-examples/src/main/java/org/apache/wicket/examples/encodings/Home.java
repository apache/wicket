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
package org.apache.wicket.examples.encodings;

import java.util.Locale;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * Example of configuring locale encodings.
 * 
 * @author Jonathan Locke
 */
public class Home extends WicketExamplePage
{
	/**
	 * Constructor
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public Home(final PageParameters parameters)
	{
		// the non-printable characters are: &#65533;&#65533;&#65533;
		add(new Label("message", "Hello world! Test: ���"));
	}

	/**
	 * Because only servlet 2.4 supports web.xml locale-encoding-mapping-list deployment
	 * descriptors, this is a workaround for servlet 2.3
	 */
	@Override
	protected void configureResponse(final WebResponse response)
	{
		final Locale originalLocale = getSession().getLocale();
		getSession().setLocale(Locale.GERMANY);

		super.configureResponse(response);

		// This is no longer useful in many cases, since we now forward the
		// <?xml ..encoding=".." ?> from the Page's markup and use it explicitly
		// set the responses encoding (see super class implementation).
		// It is however not completely useless, as many html (not xhtml) pages
		// might not have that xml declaration <?xml ..?> string.
		/*
		 * final String encoding = "text/" + getMarkupType() + "; charset=" +
		 * CharSetUtil.getEncoding(getRequestCycle());
		 * 
		 * getResponse().setContentType(encoding);
		 */
		getSession().setLocale(originalLocale);
	}
}
