/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.MarkupContainer;
import wicket.markup.html.form.Button;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Filter component that generates a 'go' button that when pressed submits the
 * filter form
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class GoFilter extends Panel
{
	private static final long serialVersionUID = 1L;

	protected static final IModel<String> defaultGoModel = new Model<String>("filter");

	private final Button go;

	/**
	 * @see wicket.Component#Component(MarkupContainer, String)
	 */
	public GoFilter(MarkupContainer parent, final String id)
	{
		this(parent, id, defaultGoModel);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer, String, IModel)
	 */
	public GoFilter(MarkupContainer parent, final String id, IModel goModel)
	{
		super(parent, id);

		go = new Button(this, "go", goModel)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit()
			{
				onGoSubmit(this);
			}
		};
	}

	protected Button getGoButton()
	{
		return go;
	}

	/**
	 * This method can be overridden by subclasses to provide non-standard
	 * behavior for the 'go' button.
	 * 
	 * @param button
	 *            the 'go' button, can be used to get to the Form object and
	 *            through that to the filter state object by retrieving the
	 *            form's model object
	 * 
	 */
	protected void onGoSubmit(Button button)
	{
	}

}
