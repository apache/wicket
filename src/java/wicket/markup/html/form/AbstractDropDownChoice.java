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
package wicket.markup.html.form;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import wicket.IModel;
import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;

/**
 * Abstract base class for all Choice (html select) options
 * {@link wicket.markup.html.form.IOnChangeListener}to implement
 * onChange behaviour of the HTML select element.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
public abstract class AbstractDropDownChoice extends FormComponent implements FormComponent.ICookieValue
{
	/** Serial Version ID */
	private static final long serialVersionUID = -8334966481181600604L;

	/** Index value for null choice. */
	public static final int NULL_VALUE = -1;

	static
	{
		// allow optional use of the IOnChangeListener interface
		RequestCycle.registerListenerInterface(IOnChangeListener.class);
	}

	/** The list of values. */
	private List values;

	/** whether the null option must be rendered if current selection == null. */
	private boolean renderNullOption = true;
	
	/**
	 * Constructor that uses the provided {@link IModel}as its model. All
	 * components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *           The non-null name of this component
	 * @param model
	 *           the model
	 * @param values
	 *           the drop down values
	 * @throws wicket.RenderException
	 *            Thrown if the component has been given a null name.
	 */
	public AbstractDropDownChoice(String name, IModel model, final Collection values)
	{
		super(name, model);
		setValues(values);
	}

	/**
	 * Constructor that uses the provided instance of {@link IModel}as a dynamic
	 * model. This model will be wrapped in an instance of
	 * {@link wicket.PropertyModel}using the provided expression.
	 * Thus, using this constructor is a short-hand for:
	 * 
	 * <pre>
	 * new MyComponent(name, new PropertyModel(myIModel, expression));
	 * </pre>
	 * 
	 * All components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *           The non-null name of this component
	 * @param model
	 *           the instance of {@link IModel}from which the model object will
	 *           be used as the subject for the given expression
	 * @param values
	 *           the drop down values
	 * @param expression
	 *           the OGNL expression that works on the given object
	 * @throws wicket.RenderException
	 *            Thrown if the component has been given a null name.
	 */
	public AbstractDropDownChoice(String name, IModel model, String expression, final Collection values)
	{
		super(name, model, expression);
		setValues(values);
	}

	/**
	 * Constructor that uses the provided object as a simple model. This object
	 * will be wrapped in an instance of {@link wicket.Model}.
	 * All components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *           The non-null name of this component
	 * @param object
	 *           the object that will be used as a simple model
	 * @param values
	 *           the drop down values
	 * @throws wicket.RenderException
	 *            Thrown if the component has been given a null name.
	 */
	public AbstractDropDownChoice(String name, Serializable object, final Collection values)
	{
		super(name, object);
		setValues(values);
	}

	/**
	 * Constructor that uses the provided object as a dynamic model. This object
	 * will be wrapped in an instance of {@link wicket.Model}that
	 * will be wrapped in an instance of
	 * {@link wicket.PropertyModel}using the provided expression.
	 * Thus, using this constructor is a short-hand for:
	 * 
	 * <pre>
	 * new MyComponent(name, new PropertyModel(new Model(object), expression));
	 * </pre>
	 * 
	 * All components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *           The non-null name of this component
	 * @param object
	 *           the object that will be used as the subject for the given
	 *           expression
	 * @param expression
	 *           the OGNL expression that works on the given object
	 * @param values
	 *           the drop down values
	 * @throws wicket.RenderException
	 *            Thrown if the component has been given a null name.
	 */
	public AbstractDropDownChoice(String name, Serializable object, String expression, final Collection values)
	{
		super(name, object, expression);
		setValues(values);
	}

	/**
	 * Set values.
	 * 
	 * @param values
	 *           values to set
	 * @return dropdown choice
	 */
	public AbstractDropDownChoice setValues(final Collection values)
	{
		if (values == null)
		{
			this.values = Collections.EMPTY_LIST;
		}
		else if(values instanceof List)
		{
			this.values = (List)values;
		}
		else
		{
			this.values = new ArrayList(values);
		}
		return this;
	}

	/**
	 * Gets the list of values.
	 * @return the list of values
	 */
	public List getValues()
	{
		if(values instanceof IIdList)
		{
			((IIdList)values).attach(RequestCycle.get());
		}
		return this.values;
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#updateModel(wicket.RequestCycle)
	 */
	public abstract void updateModel(final RequestCycle cycle);


	/**
	 * @see wicket.Component#handleComponentTag(RequestCycle,
	 *      wicket.markup.ComponentTag)
	 */
	protected void handleComponentTag(final RequestCycle cycle, final ComponentTag tag)
	{
		checkTag(tag, "select");

		if (this instanceof IOnChangeListener)
		{
			// if a user subclasses this class and implements IOnChangeListener
			// an onChange scriptlet is added
			String url = cycle.urlFor(this, IOnChangeListener.class);

			tag.put("onChange", "location.href='" + url
					+ "&selected=' + this.options[this.selectedIndex].value;");
		}

		super.handleComponentTag(cycle, tag);
	}

	/**
	 * @see wicket.Component#handleBody(wicket.RequestCycle,
	 *      wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	protected final void handleBody(final RequestCycle cycle, final MarkupStream markupStream,
			final ComponentTag openTag)
	{
		final StringBuffer options = new StringBuffer();
		final Object selected = getModelObject();
		final List list = getValues();

		if (selected == null && isRenderNullOption())
		{
			final String chooseOne = getLocalizer().getString(getPath() + ".null", this, "Choose One");

			options.append("\n<option selected value=\"" + NULL_VALUE + "\">" + chooseOne + "</option>");
		}

		for (int i = 0; i < list.size(); i++)
		{
			final Object value = list.get(i);

			if (value != null)
			{
				options.append("\n<option " + (isSelected(value) ? "selected " : "") + "value=\"");
				options.append((list instanceof IIdList) ? ((IIdList)list).getIdValue(i) : Integer.toString(i));
				options.append("\">");

				final String label = (list instanceof IIdList) ? ((IIdList)list).getDisplayValue(i) : value.toString();

				options.append(getLocalizer().getString(getPath() + "." + label, this, label));
				options.append("</option>");
			}
			else
			{
				throw new IllegalArgumentException(
						"Dropdown choice contains null value in values collection at index " + i);
			}
		}

		options.append("\n");
		replaceBody(cycle, markupStream, openTag, options.toString());
		
		// Deattach the list after this. Check if this is the right place!
		if(list instanceof IIdList)
		{
			((IIdList)list).detach(cycle);
		}
	}
	
	protected boolean isSelected(Object currentValue)
	{
		return currentValue.equals(getModelObject());
	}

	/**
	 * called when a selection changed.
	 * 
	 * @param cycle
	 *           the request cycle
	 */
	public final void selectionChanged(RequestCycle cycle)
	{
		int index = Integer.parseInt(cycle.getRequest().getParameter("selected"));
		Object value = getValues().get(index);

		updateModel(cycle);
		selectionChanged(cycle, value);
	}

	/**
	 * Template method that can be overriden by clients that implement
	 * IOnChangeListener to be notified by onChange events of a select element.
	 * This method does nothing by default.
	 * 
	 * @param cycle
	 *           the request cycle
	 * @param newSelection
	 *           the newly selected object
	 * @see wicket.markup.html.form.IOnChangeListener#selectionChanged(wicket.RequestCycle,java.lang.Object)
	 */
	public void selectionChanged(RequestCycle cycle, Object newSelection)
	{
		// no nada
	}

	/**
	 * Should a null value be rendered if the selection is null like "Choose One".
	 * The default is true.
	 * @return boolean
	 */
	public boolean isRenderNullOption()
	{
		return renderNullOption;
	}
	
	/**
	 * Should a null value be rendered if the selection is null like "Choose One".
	 * @param renderNullOption boolean
	 */
	public void setRenderNullOption(boolean renderNullOption)
	{
		this.renderNullOption = renderNullOption;
	}
}

// /////////////////////////////// End of File /////////////////////////////////
