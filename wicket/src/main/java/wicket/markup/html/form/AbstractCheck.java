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
package wicket.markup.html.form;


import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;

/**
 * Base class for {@link Check}. This is mostly here to encapsulate some gritty
 * details in order to lessen a chance of programmer error in {@link Check}
 * implementation.
 * 
 * @author ivaynberg
 * @param <T>
 */
class AbstractCheck<T> extends WebMarkupContainer<T>
{
	private static final long serialVersionUID = 1L;

	private short value = -1;

	private CheckGroup group;

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(MarkupContainer,String)
	 */
	public AbstractCheck(MarkupContainer parent, String id, CheckGroup group)
	{
		this(parent, id, null, group);
	}

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(MarkupContainer,String,
	 *      IModel)
	 */
	public AbstractCheck(MarkupContainer parent, String id, IModel<T> model, CheckGroup group)
	{
		super(parent, id, model);
		this.group = group;
	}


	/**
	 * @see WebMarkupContainer#WebMarkupContainer(MarkupContainer,String)
	 */
	public AbstractCheck(MarkupContainer parent, String id)
	{
		this(parent, id, null, null);
	}

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(MarkupContainer,String,
	 *      IModel)
	 */
	public AbstractCheck(MarkupContainer parent, String id, IModel<T> model)
	{
		this(parent, id, model, null);
	}

	/**
	 * Form submission value used for this radio component. This string will
	 * appear as the value of the <code>value</code> html attribute for the
	 * <code>input</code> tag.
	 * 
	 * @return form submission value
	 */
	public final String getValue()
	{
		if (value < 0)
		{
			value = getPage().getAutoIndex();
		}
		return "check" + value;
	}

	protected final CheckGroup getGroup()
	{
		if (group == null)
		{
			group = findParent(CheckGroup.class);
			if (group == null)
			{
				throw new WicketRuntimeException(
						"Check component ["
								+ getPath()
								+ "] cannot find its parent CheckGroup. All Check components must be a child of or below in the hierarchy of a CheckGroup component.");
			}
		}
		return group;
	}


}