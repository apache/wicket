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
package wicket.markup.html.basic;

import java.io.Serializable;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlComponent;
import wicket.model.IModel;

/**
 * A Label component replaces its body with the String version of its model
 * object returned by getModelObjectAsString().
 * <p>
 * Exactly what is displayed as the body, depends on the model. The simplest
 * case is a Label with a static String model, which can be constructed like:
 * 
 * <pre>
 * add(new Label(&quot;myLabel&quot;, &quot;the string to display&quot;))
 * </pre>
 * 
 * A Label with a dynamic model could be created like:
 * 
 * <pre>
 * 
 *     add(new Label(&quot;myLabel&quot;, person, &quot;name&quot;);
 *  
 * </pre>
 * 
 * This component will replace the body of the tag it is attached to with the
 * 'name' property of the given person object, where person might look like:
 * 
 * <pre>
 * public class Person
 * {
 *     private String name;
 * 
 *     public String getName()
 *     {
 *         return name;
 *     }
 * 
 *     public void setName(String name)
 *     {
 *         this.name = name;
 *     }
 * }
 * </pre>
 * 
 * @author Jonathan Locke
 */
public class Label extends HtmlComponent
{
    /** Serial Version ID */
    private static final long serialVersionUID = -2180588252471379004L;

    /**
     * Constructor that uses the provided {@link IModel}as its model. All
     * components have names. A component's name cannot be null.
     * 
     * @param name
     *            The non-null name of this component
     * @param model
     *            the model
     * @throws wicket.WicketRuntimeException
     *             Thrown if the component has been given a null name.
     */
    public Label(String name, IModel model)
    {
        super(name, model);
    }

    /**
     * Constructor that uses the provided instance of {@link IModel}as a
     * dynamic model. This model will be wrapped in an instance of
     * {@link wicket.model.PropertyModel}using the provided expression. Thus,
     * using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(myIModel, expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * 
     * @param name
     *            The non-null name of this component
     * @param model
     *            The instance of {@link IModel}from which the model object
     *            will be used as the subject for the given expression
     * @param expression
     *            The OGNL expression that works on the given object
     * @throws wicket.WicketRuntimeException
     *             Thrown if the component has been given a null name.
     */
    public Label(String name, IModel model, String expression)
    {
        super(name, model, expression);
    }

    /**
     * Constructor that uses the provided object as a simple model. This object
     * will be wrapped in an instance of {@link wicket.model.Model}. All
     * components have names. A component's name cannot be null.
     * 
     * @param name
     *            The non-null name of this component
     * @param object
     *            the object that will be used as a simple model
     * @throws wicket.WicketRuntimeException
     *             Thrown if the component has been given a null name.
     */
    public Label(String name, Serializable object)
    {
        super(name, object);
    }

    /**
     * Constructor that uses the provided object as a dynamic model. This object
     * will be wrapped in an instance of {@link wicket.model.Model}that will be
     * wrapped in an instance of {@link wicket.model.PropertyModel}using the
     * provided expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(new Model(object), expression));
     * </pre>
     * 
     * @param name
     *            The non-null name of this component
     * @param object
     *            The object that will be used as the subject for the given
     *            expression
     * @param expression
     *            The OGNL expression that works on the given object
     * @throws wicket.WicketRuntimeException
     *             Thrown if the component has been given a null name.
     */
    public Label(String name, Serializable object, String expression)
    {
        super(name, object, expression);
    }

    /**
     * @see wicket.Component#handleBody(wicket.markup.MarkupStream,
     *      wicket.markup.ComponentTag)
     */
    protected void handleBody(final MarkupStream markupStream,
            final ComponentTag openTag)
    {
        replaceBody(markupStream, openTag, getModelObjectAsString());
    }
}
