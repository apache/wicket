/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.markup.html.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A radio choice allows the user to select between several options using radio
 * buttons. The options are descendant components of the RadioChoice and come in
 * two flavors. RadioOption, which is attached to an invidual radio input tag,
 * and RadioOptionSet, which automatically generates a list of options from a
 * collection.
 * 
 * @author Jonathan Locke
 */
public class RadioChoice extends FormComponent
{
	/** Index value for null choice */
	private static final int NULL_VALUE = -1;

	/** Serial Version ID */
	private static final long serialVersionUID = -1560593550286375796L;

	/** List of choices attached to this model */
	private final List values = new ArrayList();

	/**
	 * @see wicket.Component#Component(String, Serializable)
	 */
	public RadioChoice(String name, Serializable object)
	{
		super(name, object);
	}

	/**
	 * @see wicket.Component#Component(String, Serializable, String)
	 */
	public RadioChoice(String name, Serializable object, String expression)
	{
		super(name, object, expression);
	}

	/**
	 * @see FormComponent#getValue()
	 */
	public final String getValue()
	{
		final int index = values.indexOf(getModelObject());

		return Integer.toString(index);
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#setValue(java.lang.String)
	 */
	public final void setValue(final String value)
	{
		setModelObject(values.get(Integer.parseInt(value)));
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#supportsPersistence()
	 */
	protected final boolean supportsPersistence()
	{
		return true;
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	public final void updateModel()
	{
		final int index = getRequestInt(NULL_VALUE);

		if (index != NULL_VALUE)
		{
			setModelObject(values.get(index));
		}
	}

	/**
	 * @param choice
	 *            The choice to add to this radio choice
	 * @return The index of the choice
	 */
	final int addRadioOption(final Object choice)
	{
		final int index = values.size();

		values.add(choice);

		return index;
	}
}


