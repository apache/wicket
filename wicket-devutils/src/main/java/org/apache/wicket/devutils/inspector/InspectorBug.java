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
package org.apache.wicket.devutils.inspector;

import org.apache.wicket.PageParameters;
import org.apache.wicket.devutils.DevUtilsPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;

/**
 * A page that shows interesting attributes of the Wicket environment, including the current session
 * and the component tree for the current page.
 * 
 * @author Jonathan Locke
 */
public final class InspectorBug extends DevUtilsPanel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            Component id
	 * @param page
	 *            Page to inspect
	 */
	public InspectorBug(final String id, final WebPage page)
	{
		super(id);
		PageParameters parameters = new PageParameters();
		parameters.put("pageId", page.getId());
		parameters.put("version", page.getVersions()-1);
		Link<?> link = new BookmarkablePageLink<Void>("link", InspectorPage.class, parameters);
		link.add(new Image("bug"));
		add(link);
	}
}
