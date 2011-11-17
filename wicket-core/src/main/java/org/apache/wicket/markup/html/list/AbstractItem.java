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
package org.apache.wicket.markup.html.list;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * A very simple Item. Usually it is used as based class for more advanced Items.
 * 
 * @author Juergen Donnerstag
 */
public class AbstractItem extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            model for this item
	 */
	public AbstractItem(final String id, final IModel<?> model)
	{
		super(id.intern(), model);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 */
	public AbstractItem(final String id)
	{
		super(id.intern());
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            model for this item
	 */
	public AbstractItem(final long id, final IModel<?> model)
	{
		this(Long.toString(id), model);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 */
	public AbstractItem(final long id)
	{
		this(Long.toString(id));
	}
}