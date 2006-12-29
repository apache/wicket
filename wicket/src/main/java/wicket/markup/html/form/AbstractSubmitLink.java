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

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.markup.html.link.AbstractLink;
import wicket.model.IModel;
import wicket.util.string.PrependingStringBuffer;
import wicket.version.undo.Change;

/**
 * Abstract class for links that are capable of submitting a form.
 * 
 * @author Matej Knopp
 * @param <T>
 */
public abstract class AbstractSubmitLink<T> extends AbstractLink<T>
		implements
			IFormSubmittingComponent
{
	/**
	 * Target form or null if the form is parent of the link.
	 */
	private Form form;

	/**
	 * If false, all standard processing like validating and model updating is
	 * skipped.
	 */
	private boolean defaultFormProcessing = true;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 */
	public AbstractSubmitLink(MarkupContainer parent, String id, IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * 
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 */
	public AbstractSubmitLink(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 * @param form
	 */
	public AbstractSubmitLink(MarkupContainer parent, String id, IModel<T> model, Form form)
	{
		super(parent, id, model);
		this.form = form;
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param form
	 */
	public AbstractSubmitLink(MarkupContainer parent, String id, Form form)
	{
		super(parent, id);
		this.form = form;
	}

	/**
	 * Sets the defaultFormProcessing property. When false (default is true),
	 * all validation and formupdating is bypassed and the onSubmit method of
	 * that button is called directly, and the onSubmit method of the parent
	 * form is not called. A common use for this is to create a cancel button.
	 * 
	 * TODO: This is a copy & paste from Button
	 * 
	 * @param defaultFormProcessing
	 *            defaultFormProcessing
	 * @return This
	 */
	public final AbstractSubmitLink<T> setDefaultFormProcessing(boolean defaultFormProcessing)
	{
		if (this.defaultFormProcessing != defaultFormProcessing)
		{
			addStateChange(new Change()
			{
				private static final long serialVersionUID = 1L;

				boolean formerValue = AbstractSubmitLink.this.defaultFormProcessing;

				@Override
				public void undo()
				{
					AbstractSubmitLink.this.defaultFormProcessing = formerValue;
				}

				@Override
				public String toString()
				{
					return "DefaultFormProcessingChange[component: " + getPath()
							+ ", default processing: " + formerValue + "]";
				}
			});
		}

		this.defaultFormProcessing = defaultFormProcessing;
		return this;
	}

	/**
	 * @see wicket.markup.html.form.IFormSubmittingComponent#getDefaultFormProcessing()
	 */
	public boolean getDefaultFormProcessing()
	{
		return defaultFormProcessing;
	}

	/**
	 * @see wicket.markup.html.form.IFormSubmittingComponent#getForm()
	 */
	public Form getForm()
	{
		if (form != null)
			return form;
		else
			return findParent(Form.class);
	}

	/**
	 * @see wicket.markup.html.form.IFormSubmittingComponent#getInputName()
	 */
	public String getInputName()
	{
		// TODO: This is a copy & paste from the FormComponent class. 
		String id = getId();
		final PrependingStringBuffer inputName = new PrependingStringBuffer(id.length());
		Component c = this;
		while (true)
		{
			inputName.prepend(id);
			c = c.getParent();
			if (c == null || (c instanceof Form && ((Form)c).isRootForm()) || c instanceof Page)
			{
				break;
			}
			inputName.prepend(Component.PATH_SEPARATOR);
			id = c.getId();
		}
		return inputName.toString();
	}
}
