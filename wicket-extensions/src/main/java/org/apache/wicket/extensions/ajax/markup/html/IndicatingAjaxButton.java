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
package org.apache.wicket.extensions.ajax.markup.html;

import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

/**
 * A variant of the {@link AjaxButton} that displays a busy indicator while the ajax request is in
 * progress.
 * 
 * @author evan
 * 
 */
public abstract class IndicatingAjaxButton extends AjaxButton implements IAjaxIndicatorAware
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender();

	/**
	 * Constructor
	 * 
	 * @param id
	 */
	public IndicatingAjaxButton(final String id)
	{
		this(id, null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 *            model used to set <code>value</code> markup attribute
	 */
	public IndicatingAjaxButton(final String id, final IModel<String> model)
	{
		this(id, model, null);
	}

	/**
	 * 
	 * Constructor
	 * 
	 * @param id
	 * @param form
	 */
	public IndicatingAjaxButton(final String id, final Form<?> form)
	{
		this(id, null, form);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 * @param form
	 */
	public IndicatingAjaxButton(final String id, final IModel<String> model, final Form<?> form)
	{
		super(id, model, form);
		add(indicatorAppender);
	}

	/**
	 * @see IAjaxIndicatorAware#getAjaxIndicatorMarkupId()
	 * @return the markup id of the ajax indicator
	 * 
	 */
	@Override
	public String getAjaxIndicatorMarkupId()
	{
		return indicatorAppender.getMarkupId();
	}

}