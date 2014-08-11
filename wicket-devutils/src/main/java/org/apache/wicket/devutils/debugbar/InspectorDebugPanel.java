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
import org.apache.wicket.devutils.inspector.InspectorPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A panel that adds a link to the inspector to the debug bar.
 * 
 * @author Jeremy Thomerson <jthomerson@apache.org>
 */
public class InspectorDebugPanel extends StandardDebugPanel
{
	private static final long serialVersionUID = 1L;

	/** */
	public static final IDebugBarContributor DEBUG_BAR_CONTRIB = new IDebugBarContributor()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Component createComponent(final String id, final DebugBar debugBar)
		{
			return new InspectorDebugPanel(id);
		}

	};

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public InspectorDebugPanel(final String id)
	{
		super(id);
	}

	@Override
	protected Class<? extends Page> getLinkPageClass()
	{
		return InspectorPage.class;
	}

	@Override
	protected ResourceReference getImageResourceReference()
	{
		return new PackageResourceReference(InspectorPage.class, "bug.png");
	}

	@Override
	protected IModel<String> getDataModel()
	{
		return new Model<>("Inspector");
	}

	@Override
	protected PageParameters getLinkPageParameters()
	{
		PageParameters params = new PageParameters();
		params.add("pageId", getPage().getId(), INamedParameters.Type.MANUAL);
		return params;
	}
}
