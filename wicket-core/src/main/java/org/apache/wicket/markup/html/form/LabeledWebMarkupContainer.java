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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IWrapModel;

/**
 * Default implementation of {@link ILabelProvider}.
 * 
 * @author almaw
 * 
 */
public abstract class LabeledWebMarkupContainer extends WebMarkupContainer
	implements
		ILabelProvider<String>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The value will be made available to the validator property by means of ${label}. It does not
	 * have any specific meaning to FormComponent itself.
	 */
	private IModel<String> labelModel = null;

	@Override
	protected void onDetach()
	{
		super.onDetach();
		if (labelModel != null)
		{
			labelModel.detach();
			if (labelModel instanceof IWrapModel)
			{
				((IWrapModel<?>)labelModel).getWrappedModel().detach();
			}
		}
	}

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public LabeledWebMarkupContainer(final String id)
	{
		super(id);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public LabeledWebMarkupContainer(final String id, final IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.ILabelProvider#getLabel()
	 */
	@Override
	public IModel<String> getLabel()
	{
		return labelModel;
	}

	/**
	 * Provide internal setter. We need this because people want to be able to chain together our
	 * setters in FormComponent, etc. e.g. for .setLabel(foo).setRequired()
	 * 
	 * @param labelModel
	 * @return {@code this}
	 */
	public LabeledWebMarkupContainer setLabel(IModel<String> labelModel)
	{
		this.labelModel = wrap(labelModel);
		return this;
	}
}
