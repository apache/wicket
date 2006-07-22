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
package wicket.markup.html.form;

import wicket.MarkupContainer;
import wicket.model.IModel;

/**
 * TextField doesn't permit the html <input type='hidden'> so this is a simple
 * subclass to allow this
 * 
 * A HiddenField is useful when you have a javascript based component that
 * updates the form state. Either
 * 
 * 1) add a AttributeModified to set the id attribute, then use
 * document.getElementById(id), or 2) lookup the field name=getPath() within the
 * form
 * 
 * @param <T>
 *            The type
 * 
 * @author Cameron Braid
 */
public class HiddenField<T> extends TextField<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            component id
	 */
	public HiddenField(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            component id
	 * @param type
	 *            the type to use when updating the model for this text field
	 */
	public HiddenField(MarkupContainer parent, String id, Class<? extends T> type)
	{
		super(parent, id, type);
	}

	/**
	 * @param id
	 *            component id
	 * @param model
	 *            the model
	 * @param type
	 *            the type to use when updating the model for this text field
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public HiddenField(MarkupContainer parent, String id, IModel<T> model, Class type)
	{
		super(parent, id, model, type);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            see Component
	 * @param model
	 *            the model
	 */
	public HiddenField(MarkupContainer parent, String id, IModel<T> model)
	{
		super(parent, id, model);
	}


	@Override
	protected String getInputType()
	{
		return "hidden";
	}
}