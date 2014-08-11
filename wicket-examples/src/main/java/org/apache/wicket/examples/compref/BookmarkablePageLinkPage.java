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
package org.apache.wicket.examples.compref;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * Page with examples on {@link org.apache.wicket.markup.html.link.BookmarkablePageLink}.
 * 
 * @author Eelco Hillenius
 */
public class BookmarkablePageLinkPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public BookmarkablePageLinkPage()
	{
		// Addbookmarkable page links. A page is bookmarkable when it has a
		// public default
		// constructor and/ or a constructor with a PageParameters argument

		// Here, we add a link to a bookmarkable page without passing any
		// parameters
		add(new BookmarkablePageLink<>("pageLinkNoArgs", BookmarkablePage.class));

		// And here, we add a link to a bookmarkable page with passing a
		// parameter that holds
		// the message that is to be displayed in the page we address.
		// Note that any arguments are passed as request parameters, and should
		// thus be strings
		PageParameters parameters = new PageParameters();
		parameters.set("message",
			"This message was passed as a page parameter argument", INamedParameters.Type.MANUAL);
		add(new BookmarkablePageLink<>("pageLinkWithArgs", BookmarkablePage.class, parameters));
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<a wicket:id=\"pageLinkWithArgs\">go to our bookmarkable page passing a message argument</a>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;// Note that any arguments are passed as request parameters, and should thus be strings\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;PageParameters parameters = new PageParameters();\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;parameters.put(\"message\", \"This message was passed as a page parameter argument\");\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;add(new BookmarkablePageLink<>(\"pageLinkWithArgs\", BookmarkablePage.class, parameters));";
		add(new ExplainPanel(html, code));

	}

}
