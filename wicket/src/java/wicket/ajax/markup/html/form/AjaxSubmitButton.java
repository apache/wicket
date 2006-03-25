/*
 * $Id$ $Revision:
 * 4871 $ $Date$
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
package wicket.ajax.markup.html.form;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.form.AjaxFormSubmitBehavior;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebComponent;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.Form;

/**
 * A button that submits the form via ajax. Since this button takes the form as
 * a constructor argument it does not need to be added to it unlike the
 * {@link Button} component.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AjaxSubmitButton extends WebComponent
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param form
	 */
	public AjaxSubmitButton(String id, Form form)
	{
		super(id);

		add(new AjaxFormSubmitBehavior(form, "onclick")
		{

			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target)
			{
				AjaxSubmitButton.this.onSubmit(target);
			}

		});

	}

	protected void onComponentTag(ComponentTag tag)
	{
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "button");
		super.onComponentTag(tag);
	}

	/**
	 * Listener method invoked on form submit
	 * 
	 * @param target
	 */
	protected abstract void onSubmit(AjaxRequestTarget target);


}
