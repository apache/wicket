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
package org.apache.wicket.ajax.markup.html.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;

/**
 * A CheckBox which is updated via ajax when the user changes its value
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AjaxCheckBox extends CheckBox
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public AjaxCheckBox(final String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public AjaxCheckBox(final String id, final IModel<Boolean> model)
	{
		super(id, model);

		setOutputMarkupId(true);

		add(new AjaxFormComponentUpdatingBehavior("click")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				AjaxCheckBox.this.updateAjaxAttributes(attributes);
			}

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				AjaxCheckBox.this.onUpdate(target);
			}
		});
	}

	/**
	 * @param attributes
	 *      the attributes to use for the Ajax request
	 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#updateAjaxAttributes(org.apache.wicket.ajax.attributes.AjaxRequestAttributes)
	 */
	protected void updateAjaxAttributes(final AjaxRequestAttributes attributes)
	{
	}

	/**
	 * Listener method invoked on an ajax update call
	 * 
	 * @param target
	 */
	protected abstract void onUpdate(AjaxRequestTarget target);
}
