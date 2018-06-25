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
import org.apache.wicket.devutils.diskstore.PageStorePage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.pageStore.IPersistentPageStore;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A panel that adds a link to persisted pages to the debug bar.
 */
public class PageStoreDebugPanel extends StandardDebugPanel
{
	private static final long serialVersionUID = 1L;

	/** */
	public static final IDebugBarContributor DEBUG_BAR_CONTRIB = new IDebugBarContributor()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Component createComponent(final String id, final DebugBar debugBar)
		{
			return new PageStoreDebugPanel(id);
		}
	};

	/**
	 * Construct.
	 * 
	 * @param id
	 *          The component id
	 */
	public PageStoreDebugPanel(final String id)
	{
		super(id);
	}

	@Override
	protected Class<? extends Page> getLinkPageClass()
	{
		return PageStorePage.class;
	}

	@Override
	protected ResourceReference getImageResourceReference()
	{
		return new PackageResourceReference(PageStoreDebugPanel.class, "harddrive.png");
	}

	@Override
	protected IModel<String> getDataModel()
	{
		return new IModel<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				IPersistentPageStore store = PageStorePage.getPersistentPageStore();
				return String.format("Persisted pages: %s", store == null ? "N/A" : store.getTotalSize());
			}
		};
	}
}