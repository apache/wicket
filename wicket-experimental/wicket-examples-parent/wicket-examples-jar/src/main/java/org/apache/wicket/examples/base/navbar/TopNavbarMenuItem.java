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
package org.apache.wicket.examples.base.navbar;

import org.apache.wicket.examples.base.markup.ClassValue;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class TopNavbarMenuItem extends Panel
{
	private static final long serialVersionUID = 1L;

	private BookmarkablePageLink<Void> link;

	public TopNavbarMenuItem(String id, String label, Class<? extends WebPage> page,
		PageParameters parameters)
	{
		super(id);

		add(link = new BookmarkablePageLink<Void>("link", page, parameters));
		link.setBody(Model.of(label));
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		if (link.linksTo(getPage()))
		{
			tag.put("class", ClassValue.of(tag).with("active"));
		}
	}
}
