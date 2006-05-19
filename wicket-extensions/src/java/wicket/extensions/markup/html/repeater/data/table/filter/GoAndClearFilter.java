/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.markup.html.form.Button;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.lang.Objects;

/**
 * Filter component that generates a 'go' and 'clear' buttons.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class GoAndClearFilter extends GoFilter
{
	private static final long serialVersionUID = 1L;

	protected static final IModel defaultClearModel = new Model("clear");

	private final Button clear;

	private final Object originalState;

	/**
	 * Constructor
	 * 
	 * This constructor will use default models for the 'clear' and 'go' button
	 * labels
	 * 
	 * @param id
	 *            component id
	 * @param form
	 *            filter form of the filter toolbar
	 */
	public GoAndClearFilter(String id, FilterForm form)
	{
		this(id, form, defaultGoModel, defaultClearModel);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param form
	 *            filter form of the filter toolbar
	 * @param goModel
	 *            model for the label of the 'go' button
	 * @param clearModel
	 *            model for the label of the 'clear' button
	 */
	public GoAndClearFilter(String id, FilterForm form, IModel goModel, IModel clearModel)
	{
		super(id, goModel);

		originalState = Objects.cloneModel(form.getModelObject());

		clear = new Button("clear", clearModel)
		{
			private static final long serialVersionUID = 1L;

			protected void onSubmit()
			{
				onClearSubmit(this);
			}
		};

		clear.setDefaultFormProcessing(false);

		add(clear);
	}

	/**
	 * @return button component representing the clear button
	 */
	protected Button getClearButton()
	{
		return clear;
	}

	/**
	 * This method should be implemented by subclasses to provide behavior for
	 * the clear button.
	 * 
	 * @param button
	 *            the 'clear' button
	 * 
	 */
	protected void onClearSubmit(Button button) {
		button.getForm().setModelObject(Objects.cloneModel(originalState));
	}

}
