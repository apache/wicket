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
package wicket.extensions.markup.html.repeater.data.table;

import wicket.model.IModel;

/**
 * This column will display the specified default value if the evaluation of the
 * provided ognl expression fails. Useful for cases where a part of ognl
 * expression can evaluate to null, ie foo.bar with foo being null.
 * 
 * @author igor
 * 
 */
public class LenientPropertyColumn extends PropertyColumn
{
	private static final long serialVersionUID = 1L;

	private Object defaultValue;

	/**
	 * Constructs a sortable column
	 * 
	 * @param displayModel
	 *            column caption model
	 * @param sortProperty
	 *            sort property
	 * @param ognlExpression
	 *            ognl expression
	 * @param defaultValue
	 *            default value
	 */
	public LenientPropertyColumn(IModel displayModel, String sortProperty, String ognlExpression,
			Object defaultValue)
	{
		super(displayModel, sortProperty, ognlExpression);
		this.defaultValue = defaultValue;
	}

	/**
	 * Constructs non sortable column
	 * 
	 * @param displayModel
	 *            column caption model
	 * @param ognlExpression
	 *            ognl expression
	 * @param defaultValue
	 *            default value
	 */
	public LenientPropertyColumn(IModel displayModel, String ognlExpression, Object defaultValue)
	{
		super(displayModel, ognlExpression);
		this.defaultValue = defaultValue;
	}

	protected IModel createLabelModel(IModel embeddedModel)
	{
		IModel model = super.createLabelModel(embeddedModel);
		return new LenientModelWrapper(model, defaultValue);
	}

}
