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
import java.util.List;

import wicket.model.IModel;

/**
 * A radio choice allows the user to select between several options using radio buttons.
 * The options are descendant components of the RadioChoice and come in two flavors.
 * RadioOption, which is attached to an invidual radio input tag, and RadioOptionSet,
 * which automatically generates a list of options from a collection.
 * @author Jonathan Locke
 */
public final class RadioChoice extends FormComponent
{
	// Index value for null choice
    private static final int NULL_VALUE = -1;
 // TODO finalize javadoc
    /** Serial Version ID */
	private static final long serialVersionUID = -1560593550286375796L;

    // List of choices attached to this model
    private final List values = new ArrayList();

    /**
     * Constructor that uses the provided {@link IModel} as its model. All components have
     * names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the model
     * @throws wicket.WicketRuntimeException Thrown if the component has
     * been given a null name.
     */
    public RadioChoice(String name, IModel model)
    {
        super(name, model);
    }

    /**
     * Constructor that uses the provided instance of {@link IModel} as a dynamic model.
     * This model will be wrapped in an instance of {@link wicket.model.PropertyModel}
     * using the provided expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(myIModel, expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the instance of {@link IModel} from which the model object will be
     *            used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.WicketRuntimeException Thrown if the component has
     * been given a null name.
     */
    public RadioChoice(String name, IModel model, String expression)
    {
        super(name, model, expression);
    }

    /**
     * Constructor that uses the provided object as a simple model. This object will be
     * wrapped in an instance of {@link wicket.model.Model}. All components
     * have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as a simple model
     * @throws wicket.WicketRuntimeException Thrown if the component has
     * been given a null name.
     */
    public RadioChoice(String name, Serializable object)
    {
        super(name, object);
    }

    /**
     * Constructor that uses the provided object as a dynamic model. This object will be
     * wrapped in an instance of {@link wicket.model.Model} that will be
     * wrapped in an instance of {@link wicket.model.PropertyModel} using the
     * provided expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(new Model(object), expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.WicketRuntimeException Thrown if the component has
     * been given a null name.
     */
    public RadioChoice(String name, Serializable object, String expression)
    {
        super(name, object, expression);
    }

    /**
     * @see wicket.markup.html.form.FormComponent#supportsPersistence()
     */
    public boolean supportsPersistence()
    {
        return true;
    }

    /**
     * @see FormComponent#getValue()
     */
    public String getValue()
    {
        final int index = values.indexOf(getModelObject());

        return Integer.toString(index);
    }

    /**
     * @see wicket.markup.html.form.FormComponent#setValue(java.lang.String)
     */
    public void setValue(final String value)
    {
        setModelObject(values.get(Integer.parseInt(value)));
    }

    /**
     * @see wicket.markup.html.form.FormComponent#updateModel()
     */
    public void updateModel()
    {
        final int index = getRequestInt(NULL_VALUE);

        if (index != NULL_VALUE)
        {
            setModelObject(values.get(index));
        }
    }

    /**
     * @param choice The choice to add to this radio choice
     * @return The index of the choice
     */
    int addRadioOption(final Object choice)
    {
        final int index = values.size();

        values.add(choice);

        return index;
    }
}


