/*
 * $Id: ChoiceRenderer.java 5861 2006-05-25 20:55:07 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 20:55:07 +0000 (Thu, 25 May
 * 2006) $
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

import wicket.util.lang.PropertyResolver;

/**
 * Default implementation of {@link wicket.markup.html.form.IChoiceRenderer}.
 * Usage:
 * <p>
 * 
 * <pre>
 * new DropDownChoice(&quot;users&quot;, new Model(selectedUser), listOfUsers)
 * </pre>
 * 
 * creates a DropDownChoice of users and the display value will be toString()
 * and the id the index of the object in the ListOfUsers.
 * </p>
 * <p>
 * 
 * <pre>
 * new DropDownChoice(&quot;users&quot;, new Model(selectedUser), listOfUsers, new ChoiceRenderer(&quot;name&quot;))
 * </pre>
 * 
 * creates a DropDownChoice of users and the display value will be looked up by
 * property expression ("name") and the id the index of the object in the
 * ListOfUsers
 * </p>
 * <p>
 * 
 * <pre>
 * new DropDownChoice(&quot;users&quot;, new Model(selectedUser), listOfUsers, new ChoiceRenderer(&quot;name&quot;, &quot;id&quot;))
 * </pre>
 * 
 * creates a DropDownChoice of users and the display value will be looked up by
 * property expression ("name") and the id will be looked up by the property
 * expression "id"
 * </p>
 * 
 * @param <T>
 *            The type
 * @author jcompagner
 */
public class ChoiceRenderer<T> implements IChoiceRenderer<T>
{
	private static final long serialVersionUID = 1L;

	/** expression for getting the display value. */
	private final String displayExpression;

	/** expression for getting the id. */
	private final String idExpression;

	/**
	 * Construct. When you use this constructor, the display value will be
	 * determined by calling toString() on the list object, and the id will be
	 * based on the list index. the id value will be the index
	 */
	public ChoiceRenderer()
	{
		super();
		this.displayExpression = null;
		this.idExpression = null;
	}

	/**
	 * Construct. When you use this constructor, the display value will be
	 * determined by executing the given property expression on the list object,
	 * and the id will be based on the list index. The display value will be
	 * calculated by the given property expression
	 * 
	 * @param displayExpression
	 *            A property expression to get the display value
	 */
	public ChoiceRenderer(String displayExpression)
	{
		super();
		this.displayExpression = displayExpression;
		this.idExpression = null;
	}

	/**
	 * Construct. When you use this constructor, both the id and the display
	 * value will be determined by executing the given property expressions on
	 * the list object.
	 * 
	 * @param displayExpression
	 *            A property expression to get the display value
	 * @param idExpression
	 *            A property expression to get the id value
	 */
	public ChoiceRenderer(String displayExpression, String idExpression)
	{
		super();
		this.displayExpression = displayExpression;
		this.idExpression = idExpression;
	}


	/**
	 * @see wicket.markup.html.form.IChoiceRenderer#getDisplayValue(Object)
	 */
	public Object getDisplayValue(T object)
	{
		Object returnValue = object;
		if ((displayExpression != null) && (object != null))
		{
			returnValue = PropertyResolver.getValue(displayExpression, object);
		}

		if (returnValue == null)
		{
			return "";
		}

		return returnValue;
	}

	/**
	 * @see wicket.markup.html.form.IChoiceRenderer#getIdValue(java.lang.Object,
	 *      int)
	 */
	public String getIdValue(T object, int index)
	{
		if (idExpression == null)
		{
			return Integer.toString(index);
		}

		if (object == null)
		{
			return "";
		}

		Object returnValue = PropertyResolver.getValue(idExpression, object);
		if (returnValue == null)
		{
			return "";
		}

		return returnValue.toString();
	}
}