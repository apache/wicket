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
import org.apache.wicket.devutils.inspector.LiveSessionsPage;
import org.apache.wicket.devutils.inspector.SessionSizeModel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Bytes;

/**
 * A panel for the debug bar that shows the session size and links to the page that shows more
 * information about sessions.
 * 
 * @author Jeremy Thomerson <jthomerson@apache.org>
 */
public class SessionSizeDebugPanel extends StandardDebugPanel
{
	private static final long serialVersionUID = 1L;

	/** */
	public static final IDebugBarContributor DEBUG_BAR_CONTRIB = new IDebugBarContributor()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Component createComponent(final String id, final DebugBar debugBar)
		{
			return new SessionSizeDebugPanel(id);
		}

	};

	/**
	 * Construct.
	 * 
	 * @param id
	 *      the component id
	 */
	public SessionSizeDebugPanel(final String id)
	{
		super(id);
	}

	@Override
	protected Class<? extends Page> getLinkPageClass()
	{
		return LiveSessionsPage.class;
	}

	@Override
	protected ResourceReference getImageResourceReference()
	{
		// TODO: need better image for this:
		return new PackageResourceReference(SessionSizeDebugPanel.class, "harddrive.png");
	}

	@Override
	protected IModel<String> getDataModel()
	{
		return new AbstractReadOnlyModel<String>()
		{
			private static final long serialVersionUID = 1L;

			private final IModel<Bytes> size = new SessionSizeModel();

			@Override
			public String getObject()
			{
				Bytes sessionSizeInBytes = size.getObject();
				String sessionSizeAsString = sessionSizeInBytes != null
					? sessionSizeInBytes.toString() : "unknown";

				return "Session: " + sessionSizeAsString;
			}

			@Override
			public void detach()
			{
				super.detach();
				size.detach();
			}
		};
	}

}
