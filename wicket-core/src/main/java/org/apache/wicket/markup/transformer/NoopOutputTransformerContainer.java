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
package org.apache.wicket.markup.transformer;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

/**
 * An implementation of an output transformer which does nothing. It does not modify the markup at
 * all.
 * 
 * @author Juergen Donnerstag
 */
public class NoopOutputTransformerContainer extends AbstractOutputTransformerContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct
	 * 
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public NoopOutputTransformerContainer(final String id)
	{
		super(id);
	}

	/**
	 * Construct
	 * 
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public NoopOutputTransformerContainer(final String id, final IModel<?> model)
	{
		super(id, model);
	}

	@Override
	public CharSequence transform(final Component component, final CharSequence output)
	{
		return output;
	}
}
