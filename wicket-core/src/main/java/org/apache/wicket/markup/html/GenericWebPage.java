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

import org.apache.wicket.IGenericComponent;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * A {@link WebPage} with typesafe getters and setters for the model and its underlying object
 * 
 * @param <T>
 *            the type of the page's model object
 */
public class GenericWebPage<T> extends WebPage implements IGenericComponent<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor. Having this constructor public means that your page is 'bookmarkable' and hence
	 * can be called/ created from anywhere.
	 */
	protected GenericWebPage()
	{
		super();
	}

	/**
	 * @param model
	 *            the page's model
	 * @see Page#Page(IModel)
	 */
	protected GenericWebPage(final IModel<T> model)
	{
		super(model);
	}

	/**
	 * Constructor which receives wrapped query string parameters for a request. Having this
	 * constructor public means that your page is 'bookmarkable' and hence can be called/ created
	 * from anywhere. For bookmarkable pages (as opposed to when you construct page instances
	 * yourself, this constructor will be used in preference to a no-arg constructor, if both exist.
	 * Note that nothing is done with the page parameters argument. This constructor is provided so
	 * that tools such as IDEs will include it their list of suggested constructors for derived
	 * classes.
	 * 
	 * Please call this constructor if you want to remember the pageparameters
	 * {@link #getPageParameters()}. So that they are reused for stateless links.
	 * 
	 * @param parameters
	 *            Wrapped query string parameters.
	 */
	protected GenericWebPage(final PageParameters parameters)
	{
		super(parameters);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final T getModelObject()
	{
		return (T)getDefaultModelObject();
	}

	@Override
	public final void setModelObject(final T modelObject)
	{
		setDefaultModelObject(modelObject);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final IModel<T> getModel()
	{
		return (IModel<T>)getDefaultModel();
	}

	@Override
	public final void setModel(final IModel<T> model)
	{
		setDefaultModel(model);
	}
}
