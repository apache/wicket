/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 07:46:36 +0200 (vr, 26 mei 2006) $
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
package wicket.markup.html.basic;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * A Label component replaces its body with the String version of its model
 * object returned by getModelObjectAsString().
 * <p>
 * Exactly what is displayed as the body, depends on the model. The simplest
 * case is a Label with a static String model, which can be constructed like
 * this:
 * 
 * <pre>
 * add(new Label(&quot;myLabel&quot;, &quot;the string to display&quot;))
 * </pre>
 * 
 * A Label with a dynamic model can be created like this:
 * 
 * <pre>
 *       
 *             add(new Label(&quot;myLabel&quot;, new PropertyModel(person, &quot;name&quot;));
 *        
 * </pre>
 * 
 * In this case, the Label component will replace the body of the tag it is
 * attached to with the 'name' property of the given Person object, where Person
 * might look like:
 * 
 * <pre>
 * public class Person
 * {
 * 	private String name;
 * 
 * 	public String getName()
 * 	{
 * 		return name;
 * 	}
 * 
 * 	public void setName(String name)
 * 	{
 * 		this.name = name;
 * 	}
 * }
 * </pre>
 * 
 * @param <T>
 *            The type
 * 
 * @author Jonathan Locke
 */
public class Label<T> extends WebComponent<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 */
	public Label(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * Convenience constructor. Same as Label(String, new Model(String))
	 * 
	 * @param parent
	 *            The parent of this component The parent component
	 * 
	 * @param id
	 *            See Component
	 * @param label
	 *            The label text
	 * 
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public Label(MarkupContainer parent, final String id, String label)
	{
		this(parent, id, new Model<String>(label));
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer, String, IModel)
	 */
	@SuppressWarnings("unchecked")
	public Label(MarkupContainer parent, final String id, IModel model)
	{
		super(parent, id, model);
	}

	/**
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		replaceComponentTagBody(markupStream, openTag, getModelObjectAsString());
	}
}