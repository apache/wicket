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
package wicket.markup.html.basic;

import java.io.Serializable;

import wicket.RenderException;
import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.PropertyModel;
import wicket.util.string.Strings;


/**
 * If you have email addresses or web URLs in the data that you are displaying, 
 * then you can automatically display those pieces of data as hyperlinks, 
 * you will not have to take any action to convert that data.
 * <p>
 * Email addresses will be wrapped with a &lt;a href="mailto:xxx"&gt;xxx&lt;/a&gt; 
 * tag, where "xxx" is the email address that was detected.
 * <p>
 * Web URLs will be wrapped with a &lt;a href="xxx"&gt;xxx&lt;/a&gt; tag, 
 * where "xxx" is the URL that was detected (it can be any valid URL type, 
 * http://, https://, ftp://, etc...)
 *
 * @author Juergen Donnerstag
 */
public final class SmartLinkMultiLineLabel extends MultiLineLabel
{
    /** Serial Version ID */
	private static final long serialVersionUID = 6614585065978596357L;

	/**
     * Constructor that uses the provided {@link IModel}as its model. All components have
     * names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public SmartLinkMultiLineLabel(String name, IModel model)
    {
        super(name, model);
    }

    /**
     * Constructor that uses the provided instance of {@link IModel}as a dynamic model.
     * This model will be wrapped in an instance of {@link PropertyModel}using the
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
     * @param expression the OGNL expression that works on the given object
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public SmartLinkMultiLineLabel(String name, IModel model, String expression)
    {
        super(name, model, expression);
    }

    /**
     * Constructor that uses the provided object as a simple model. This object will be
     * wrapped in an instance of {@link Model}. All components have names. A component's
     * name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as a simple model
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public SmartLinkMultiLineLabel(String name, String object)
    {
        super(name, object);
    }

    /**
     * Constructor that uses the provided object as a dynamic model. This object will be
     * wrapped in an instance of {@link Model}that will be wrapped in an instance of
     * {@link PropertyModel}using the provided expression. Thus, using this constructor
     * is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(new Model(object), expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public SmartLinkMultiLineLabel(String name, Serializable object, String expression)
    {
        super(name, object, expression);
    }

    /**
     * @see wicket.Component#handleBody(wicket.RequestCycle,
     *      wicket.markup.MarkupStream,
     *      wicket.markup.ComponentTag)
     */
    protected void handleBody(final RequestCycle cycle, final MarkupStream markupStream,
            final ComponentTag openTag)
    {
        String body = Strings.toMultilineMarkup(getModelObjectAsString());
        body = SmartLinkLabel.smartLink(body);
        replaceBody(cycle, markupStream, openTag, body);
    }
}

///////////////////////////////// End of File /////////////////////////////////
