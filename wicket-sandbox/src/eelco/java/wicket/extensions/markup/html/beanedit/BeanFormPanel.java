/*
 * $Id$ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.beanedit;

import java.io.Serializable;

import wicket.AttributeModifier;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.StringResourceModel;

/**
 * Panel with a form for generic bean displaying/ editing.
 * 
 * @author Eelco Hillenius
 */
public class BeanFormPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param bean
	 *            JavaBean to be edited or displayed
	 */
	public BeanFormPanel(String id, Serializable bean)
	{
		this(id, new BeanModel(bean));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param beanModel
	 *            model with the JavaBean to be edited or displayed
	 */
	public BeanFormPanel(String id, BeanModel beanModel)
	{
		super(id, beanModel);
		setRenderBodyOnly(true);
		add(new BeanForm("form", beanModel));
	}

	/**
	 * Creates a new instance of the bean panel.
	 * 
	 * @param panelId
	 *            component id
	 * @param beanModel
	 *            model with the JavaBean to be edited or displayed
	 * @return a new instance of bean panel
	 */
	protected AbstractBeanPanel newBeanPanel(String panelId, BeanModel beanModel)
	{
		return new BeanPanel(panelId, beanModel);
	}

	/**
	 * Called whenever the user pushed the cancel button.
	 */
	protected void onCancel()
	{
	}

	/**
	 * Called whenever the user pushed the save button.
	 */
	protected void onSave()
	{
	}

	/**
	 * Gets the label model for the cancel button.
	 * 
	 * @return the label model for the cancel button
	 */
	protected IModel getLabelModelForCancelButton()
	{
		return new StringResourceModel("wicket.ext.BeanFormPanel.cancel", this, null);
	}

	/**
	 * Gets the label model for the save button.
	 * 
	 * @return the label model for the save button
	 */
	protected IModel getLabelModelForSaveButton()
	{
		return new StringResourceModel("wicket.ext.BeanFormPanel.save", this, null);
	}

	/**
	 * Gets the css class model for the cancel button.
	 * 
	 * @return the css class model for the cancel button
	 */
	protected IModel getCSSClassModelForCancelButton()
	{
		return new Model("wicketExtCancelButton");
	}

	/**
	 * Gets the css class model for the save button.
	 * 
	 * @return the css class model for the save button
	 */
	protected IModel getCSSClassModelForSaveButton()
	{
		return new Model("wicketExtSaveButton");
	}

	/**
	 * Form for editing the bean.
	 */
	private final class BeanForm extends Form
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 * @param beanModel
		 *            model with the JavaBean to be edited or displayed
		 */
		public BeanForm(String id, BeanModel beanModel)
		{
			super(id, beanModel);
			add(newBeanPanel("beanPanel", beanModel));

			Button cancel = new Button("cancel")
			{
				private static final long serialVersionUID = 1L;

				public void onSubmit()
				{
					onCancel();
				}
			};
			cancel.add(new AttributeModifier("value", true, getLabelModelForCancelButton()));
			cancel.add(new AttributeModifier("class", true, getCSSClassModelForCancelButton()));
			cancel.setDefaultFormProcessing(true);
			add(cancel);

			Button save = new Button("save")
			{
				private static final long serialVersionUID = 1L;

				public void onSubmit()
				{
					onSave();
				}
			};
			save.add(new AttributeModifier("value", true, getLabelModelForSaveButton()));
			save.add(new AttributeModifier("class", true, getCSSClassModelForSaveButton()));
			add(save);
		}
	}
}
