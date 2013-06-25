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
package org.apache.wicket.devutils.debugbar;

import org.apache.wicket.Page;
import org.apache.wicket.devutils.DevUtilsPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A standard looking debug panel with an img (optional) and a string of data, and the whole thing
 * is a link.
 * 
 * @author Jeremy Thomerson <jthomerson@apache.org>
 */
public abstract class StandardDebugPanel extends DevUtilsPanel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public StandardDebugPanel(final String id)
	{
		super(id);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		BookmarkablePageLink<Void> link = createLink("link");
		add(link);
		ResourceReference img = getImageResourceReference();
		if (img == null)
		{
			link.add(new WebMarkupContainer("img").setVisibilityAllowed(false));
		}
		else
		{
			link.add(new Image("img", img));
		}
		link.add(new Label("data", getDataModel()));
	}

	protected BookmarkablePageLink<Void> createLink(final String id)
	{
		return new BookmarkablePageLink<>(id, getLinkPageClass(), getLinkPageParameters());
	}

	protected abstract IModel<String> getDataModel();

	protected abstract ResourceReference getImageResourceReference();

	protected abstract Class<? extends Page> getLinkPageClass();

	protected PageParameters getLinkPageParameters()
	{
		return new PageParameters();
	}
}
