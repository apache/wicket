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
package org.apache.wicket.extensions.markup.html.tabs;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * Convenience class that takes care of common ITab functionality
 * 
 * @see ITab
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractTab implements ITab
{
	private static final long serialVersionUID = 1L;

	IModel<String> title;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            IModel used to represent the title of the tab. Must contain a string
	 */
	public AbstractTab(final IModel<String> title)
	{
		this.title = title;
	}

	/**
	 * {@inheritDoc}
	 */
	public IModel<String> getTitle()
	{
		return title;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isVisible()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public abstract WebMarkupContainer getPanel(final String panelId);
}