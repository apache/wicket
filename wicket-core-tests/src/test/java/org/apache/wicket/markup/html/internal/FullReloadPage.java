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
package org.apache.wicket.markup.html.internal;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;


/**
 */
public class FullReloadPage extends WebPage
{
	/** */
	private static final long serialVersionUID = 1L;
	private final WebMarkupContainer toggleable;
	private final Link<Void> link;

	/**
	 * 
	 */
	@SuppressWarnings("serial")
	public FullReloadPage()
	{
		add(toggleable = new WebMarkupContainer("toggleable"));
		add(link = new Link<Void>("link")
		{
			@Override
			public void onClick()
			{
				toggleable.setVisible(!toggleable.isVisible());
			}
		});
	}

	/**
	 * @return the toggleable element
	 */
	public WebMarkupContainer getToggleable()
	{
		return toggleable;
	}

	/**
	 * @return the toggle link
	 */
	public Link<Void> getLink()
	{
		return link;
	}
}