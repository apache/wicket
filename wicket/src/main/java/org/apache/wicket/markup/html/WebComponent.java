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
package org.apache.wicket.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

/**
 * Base class for simple HTML components which do not hold nested components. If you need to support
 * nested components, see WebMarkupContainer or use Panel if the component will have its own
 * associated markup.
 * 
 * @see org.apache.wicket.markup.html.WebMarkupContainer
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public class WebComponent extends Component
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see Component#Component(String)
	 */
	public WebComponent(final String id)
	{
		super(id);
	}

	/**
	 * @see Component#Component(String, IModel)
	 */
	public WebComponent(final String id, final IModel<?> model)
	{
		super(id, model);
	}

	@Override
	protected void onRender()
	{
		internalRenderComponent();
	}
}