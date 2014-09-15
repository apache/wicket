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

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.core.util.lang.WicketObjects;

/**
 * A panel for the debug bar that shows the size of the currently shown page.
 * <p>
 * <strong>Note</strong>: this size includes the size of the debug bar itself too!
 */
public class PageSizeDebugPanel extends StandardDebugPanel
{
	private static final long serialVersionUID = 1L;

	/** */
	public static final IDebugBarContributor DEBUG_BAR_CONTRIB = new IDebugBarContributor()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Component createComponent(final String id, final DebugBar debugBar)
		{
			return new PageSizeDebugPanel(id);
		}

	};

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public PageSizeDebugPanel(final String id)
	{
		super(id);
	}

	@Override
	protected Class<? extends Page> getLinkPageClass()
	{
		// not used
		return WebPage.class;
	}

	// Disable the link because there is no page with more detailed information
	@Override
	protected BookmarkablePageLink<Void> createLink(final String id)
	{
		BookmarkablePageLink<Void> bookmarkablePageLink = super.createLink(id);
		bookmarkablePageLink.setEnabled(false);
		return bookmarkablePageLink;
	}

	@Override
	protected String getIcon()
	{
		return "glyphicon glyphicon-floppy-disk";
	}

	@Override
	protected IModel<String> getDataModel()
	{
		return new AbstractReadOnlyModel<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				Page enclosingPage = getPage();
				long pageSize = WicketObjects.sizeof(enclosingPage);
				Bytes pageSizeInBytes = (pageSize > -1 ? Bytes.bytes(pageSize) : null);
				String pageSizeAsString = pageSizeInBytes != null ? pageSizeInBytes.toString()
					: "unknown";

				return "Page: " + pageSizeAsString;
			}
		};
	}
}
