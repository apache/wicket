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
import java.util.Collection;

import wicket.markup.ComponentTag;
import wicket.model.IModel;

/**
 * Essentially a drop down choice that doesn't drop down. Instead, it scrolls and displays
 * a given number of rows.
 *
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Eelco Hillenius
 */
public final class ListChoice extends DropDownChoice implements FormComponent.ICookieValue
{ // TODO finalize javadoc
    /** Serial Version ID */
	private static final long serialVersionUID = 1227773600645861006L;

	/** The default maximum number of rows to display. */
    private static int defaultMaxRows = 8;

    /** The maximum number of rows to display. */
    private int maxRows;

    /**
     * Constructor that uses the provided {@link IModel}as its model. All components have
     * names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the model
     * @param values the list values
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public ListChoice(String name, IModel model, final Collection values)
    {
        this(name, model, values, defaultMaxRows);
    }

    /**
     * Constructor that uses the provided instance of {@link IModel}as a dynamic model.
     * This model will be wrapped in an instance of {@link wicket.model.PropertyModel}using the
     * provided expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(myIModel, expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the instance of {@link IModel}from which the model object will be
     *            used as the subject for the given expression
     * @param values the list values
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public ListChoice(String name, IModel model, String expression,
            final Collection values)
    {
        this(name, model, values, defaultMaxRows);
    }

    /**
     * Constructor that uses the provided object as a simple model. This object will be
     * wrapped in an instance of {@link wicket.model.Model}. All components have names. A
     * component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as a simple model
     * @param values the list values
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public ListChoice(String name, Serializable object, final Collection values)
    {
        this(name, object, values, defaultMaxRows);
    }

    /**
     * Constructor that uses the provided object as a dynamic model. This object will be
     * wrapped in an instance of {@link wicket.model.Model}that will be wrapped in an instance
     * of {@link wicket.model.PropertyModel}using the provided expression. Thus, using this
     * constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(new Model(object), expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @param values the list values
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public ListChoice(String name, Serializable object, String expression,
            final Collection values)
    {
        this(name, object, expression, values, defaultMaxRows);
    }

    /**
     * Constructor that uses the provided {@link IModel}as its model. All components have
     * names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the model
     * @param values the list values
     * @param maxRows The maximum number of rows to display
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public ListChoice(String name, IModel model, final Collection values, final int maxRows)
    {
        super(name, model, values);
        setRenderNullOption(false);
        this.maxRows = maxRows;
    }

    /**
     * Constructor that uses the provided instance of {@link IModel}as a dynamic model.
     * This model will be wrapped in an instance of {@link wicket.model.PropertyModel}using the
     * provided expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(myIModel, expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the instance of {@link IModel}from which the model object will be
     *            used as the subject for the given expression
     * @param values the list values
     * @param expression the OGNL expression that works on the given object
     * @param maxRows The maximum number of rows to display
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public ListChoice(String name, IModel model, String expression,
            final Collection values, final int maxRows)
    {
        super(name, model, expression, values);
        setRenderNullOption(false);
        this.maxRows = maxRows;
    }

    /**
     * Constructor that uses the provided object as a simple model. This object will be
     * wrapped in an instance of {@link wicket.model.Model}. All components have names. A
     * component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as a simple model
     * @param values the list values
     * @param maxRows The maximum number of rows to display
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public ListChoice(String name, Serializable object,
            final Collection values, final int maxRows)
    {
        super(name, object, values);
        setRenderNullOption(false);
        this.maxRows = maxRows;
    }

    /**
     * Constructor that uses the provided object as a dynamic model. This object will be
     * wrapped in an instance of {@link wicket.model.Model}that will be wrapped in an instance
     * of {@link wicket.model.PropertyModel}using the provided expression. Thus, using this
     * constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(new Model(object), expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @param values the list values
     * @param maxRows The maximum number of rows to display
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public ListChoice(String name, Serializable object, String expression,
            final Collection values, final int maxRows)
    {
        super(name, object, expression, values);
        setRenderNullOption(false);
        this.maxRows = maxRows;
    }

    /**
     * @see wicket.Component#handleComponentTag(ComponentTag)
     */
    protected void handleComponentTag(final ComponentTag tag)
    {
        super.handleComponentTag(tag);
        tag.put("size", Math.min(maxRows, getValues().size()));
    }
    
    /**
     * Gets the default maximum number of rows to display.
     * @return Returns the defaultMaxRows.
     */
    protected static int getDefaultMaxRows()
    {
        return defaultMaxRows;
    }

    /**
     * Sets the default maximum number of rows to display.
     * @param defaultMaxRows The defaultMaxRows to set.
     */
    protected static void setDefaultMaxRows(final int defaultMaxRows)
    {
        ListChoice.defaultMaxRows = defaultMaxRows;
    }

    /**
     * Gets the maximum number of rows to display.
     * @return the maximum number of rows to display
     */
    public int getMaxRows()
    {
        return maxRows;
    }

    /**
     * Sets the maximum number of rows to display.
     * @param maxRows the maximum number of rows to display
     * @return This
     */
    public ListChoice setMaxRows(int maxRows)
    {
        this.maxRows = maxRows;
        return this;
    }
}
