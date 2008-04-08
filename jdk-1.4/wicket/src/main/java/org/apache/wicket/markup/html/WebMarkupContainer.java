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
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * A container of HTML markup and components. It is very similar to the base class MarkupContainer,
 * except that the markup type is defined to be HTML.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * 
 * @param <T>
 *            The model object type
 */
public class WebMarkupContainer<T> extends MarkupContainer<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see Component#Component(String)
	 */
	public WebMarkupContainer(final String id)
	{
		super(id);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public WebMarkupContainer(final String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * Gets the markup type for this component.
	 * 
	 * @return Markup type of HTML
	 */
	@Override
	public String getMarkupType()
	{
		return getPage().getMarkupType();
	}

	/**
	 * A convenience method to return the WebPage. Same as getPage().
	 * 
	 * @return WebPage
	 */
	public final WebPage getWebPage()
	{
		return (WebPage)getPage();
	}
}