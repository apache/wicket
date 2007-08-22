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
package org.apache.wicket.examples.staticpages;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.parser.filter.RelativePathPrefixHandler;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Examples for serving static files. These examples show how to serve files (or
 * more generally data streams) by reusing the powerful Wicket concepts like
 * IResourceStream, IRequestTarget and IRequestTargetUrlCodingStrategy.
 * 
 * XXX Notice the use of a WebMarkupContainer to produce the static links, since
 * with plain <tt>href</tt> attribute Wicket's
 * {@link RelativePathPrefixHandler} would prepend the servlet context path but
 * without Wicket's filter path.
 * 
 * TODO provide an example using a bookmarkable page
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class Home extends WicketExamplePage
{
	public Home()
	{
		// Hello World as a Static Page
		add(new StaticLink("hellostream", new Model("docs/hello.html")));
		// Hello World as a Static Page with XSLT layout
		add(new StaticLink("helloxslt", new Model("xsldocs/hello.html")));
		// Passing URI to a Wicket page
		add(new StaticLink("wicketpage", new Model("pages/path/to/hello.html")));
		// Sending a Wicket page by email
		add(new StaticLink("emailwicketpage", new Model("pages/path/to/hello.html?email=true")));
	}

	private class StaticLink extends WebMarkupContainer
	{
		public StaticLink(String id, IModel model)
		{
			super(id, model);
			add(new AttributeModifier("href", true, model));
		}
	}
}