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

import org.apache.wicket.model.IModel;

/**
 * A {@link TextField} for HTML5 &lt;input&gt; with type <em>range</em>.
 * 
 * <p>
 * Automatically validates the input against the configured {@link #setMinimum(Number)}  min} and
 * {@link #setMaximum(Number)}  max} attributes. If any of them is <code>null</code> then
 * {@link Double#MIN_VALUE} and {@link Double#MAX_VALUE} are used respectfully.
 * 
 * @param <N>
 *            the number type
 */
public class RangeTextField<N extends Number & Comparable<N>> extends NumberTextField<N>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 */
	public RangeTextField(String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            see Component
	 * @param model
	 *            the model
	 */
	public RangeTextField(String id, IModel<N> model)
	{
		this(id, model, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            the input value
	 * @param type
	 *            The type to use when updating the model for this text field
	 */
	public RangeTextField(String id, IModel<N> model, Class<N> type)
	{
		super(id, model, type);
	}

	@Override
	protected String getInputType()
	{
		return "range";
	}
}
