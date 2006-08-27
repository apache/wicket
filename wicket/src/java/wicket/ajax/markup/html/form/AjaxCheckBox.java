/*
 * $Id$ $Revision:
 * 1.3 $ $Date$
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
import wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import wicket.markup.html.form.CheckBox;
import wicket.model.IModel;

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
	public AjaxCheckBox(final String id, final IModel model)
	{
		super(id, model);
		
		setOutputMarkupId(true);

		add(new AjaxFormComponentUpdatingBehavior("onclick")
		{
			private static final long serialVersionUID = 1L;

			protected void onUpdate(AjaxRequestTarget target)
			{
				AjaxCheckBox.this.onUpdate(target);
			}
		});
	}

	/**
	 * Listener method invoked on an ajax update call
	 * 
	 * @param target
	 */
	protected abstract void onUpdate(AjaxRequestTarget target);
}
