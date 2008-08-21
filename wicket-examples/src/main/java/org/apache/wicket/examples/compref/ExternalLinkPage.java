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
import org.apache.wicket.markup.html.link.ExternalLink;


/**
 * Page with examples on {@link org.apache.wicket.markup.html.link.ExternalLink}.
 * 
 * @author Eelco Hillenius
 */
public class ExternalLinkPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public ExternalLinkPage()
	{
		// add a link that goes to javalobby
		add(new ExternalLink("externalLink1", "http://www.javalobby.org", "To JavaLobby"));
		// add a link that goes to the server side
		add(new ExternalLink("externalLink2", "http://www.theserverside.com", "To The Server Side"));
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<a href=\"#\" target=\"_new\" wicket:id=\"externalLink1\">this body will be replaced</a>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;add(new ExternalLink(\"externalLink1\", \"http://www.javalobby.org\", \"To JavaLobby\"));";
		add(new ExplainPanel(html, code));
	}
}