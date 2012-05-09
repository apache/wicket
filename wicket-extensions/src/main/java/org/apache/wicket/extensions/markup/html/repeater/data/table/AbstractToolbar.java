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
package org.apache.wicket.extensions.markup.html.repeater.data.table;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * A base class for data table toolbars
 * 
 * @see DefaultDataTable
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AbstractToolbar extends Panel
{
	private static final long serialVersionUID = 1L;

	private static int counter = 0;

	private final DataTable<?, ?> table;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            model
	 * @param table
	 *            data table this toolbar will be attached to
	 */
	public AbstractToolbar(final IModel<?> model, final DataTable<?, ?> table)
	{
		super("" + (counter++), model);
		this.table = table;
	}

	/**
	 * Constructor
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 */
	public AbstractToolbar(final DataTable<?, ?> table)
	{
		this(null, table);
	}

	/**
	 * @return DataTable this toolbar is attached to
	 */
	protected DataTable<?, ?> getTable()
	{
		return table;
	}
}
