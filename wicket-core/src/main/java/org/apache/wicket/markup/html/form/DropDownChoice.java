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
package org.apache.wicket.markup.html.form;

import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;


/**
 * A choice implemented as a dropdown menu/list.
 * <p>
 * Java:
 * 
 * <pre>
 * List SITES = Arrays.asList(new String[] { &quot;The Server Side&quot;, &quot;Java Lobby&quot;, &quot;Java.Net&quot; });
 * 
 * // Add a dropdown choice component that uses Input's 'site' property to designate the
 * // current selection, and that uses the SITES list for the available options.
 * // Note that when the selection is null, Wicket will lookup a localized string to
 * // represent this null with key: &quot;id + '.null'&quot;. In this case, this is 'site.null'
 * // which can be found in DropDownChoicePage.properties
 * form.add(new DropDownChoice(&quot;site&quot;, SITES));
 * </pre>
 * 
 * HTML:
 * 
 * <pre>
 *   	&lt;select wicket:id=&quot;site&quot;&gt;
 *   		&lt;option&gt;site 1&lt;/option&gt;
 *   		&lt;option&gt;site 2&lt;/option&gt;
 *   	&lt;/select&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 * 
 * @param <T>
 *            The model object type
 */
public class DropDownChoice<T> extends AbstractSingleSelectChoice<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 */
	public DropDownChoice(final String id)
	{
		super(id);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public DropDownChoice(final String id, final List<? extends T> choices)
	{
		super(id, choices);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param renderer
	 *            The rendering engine
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public DropDownChoice(final String id, final List<? extends T> choices,
		final IChoiceRenderer<? super T> renderer)
	{
		super(id, choices, renderer);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public DropDownChoice(final String id, IModel<T> model, final List<? extends T> choices)
	{
		super(id, model, choices);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The drop down choices
	 * @param renderer
	 *            The rendering engine
	 */
	public DropDownChoice(final String id, IModel<T> model, final List<? extends T> choices,
		final IChoiceRenderer<? super T> renderer)
	{
		super(id, model, choices, renderer);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 */
	public DropDownChoice(String id, IModel<? extends List<? extends T>> choices)
	{
		super(id, choices);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The drop down choices
	 */
	public DropDownChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices)
	{
		super(id, model, choices);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param choices
	 *            The drop down choices
	 * @param renderer
	 *            The rendering engine
	 */
	public DropDownChoice(String id, IModel<? extends List<? extends T>> choices,
		IChoiceRenderer<? super T> renderer)
	{
		super(id, choices, renderer);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The drop down choices
	 * @param renderer
	 *            The rendering engine
	 */
	public DropDownChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices,
		IChoiceRenderer<? super T> renderer)
	{
		super(id, model, choices, renderer);
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "select");

		super.onComponentTag(tag);
	}
}
