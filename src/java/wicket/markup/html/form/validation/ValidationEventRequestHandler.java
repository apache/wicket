/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.markup.html.form.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.EventRequestHandler;
import wicket.IEventRequestListener;
import wicket.markup.ComponentTag;
import wicket.markup.html.HtmlHeaderContainer;
import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.form.FormComponent;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.StringBufferResourceStream;
import wicket.util.value.ValueMap;

/**
 * Handles event requests, like AJAX (XmlHttp) requests.
 *
 * @author Eelco Hillenius
 */
//TODO move dojo stuff to a package
public final class ValidationEventRequestHandler extends EventRequestHandler implements IHeaderContributor
{
	/** log. */
	private static Log log = LogFactory.getLog(ValidationEventRequestHandler.class);

	/** name event, like onblur. */
	private final String eventName;

	/** component this handler is attached to. */
	private FormComponent formComponent;

	/**
	 * Construct.
	 * @param eventName name of the event to attach to, e.g. 'onchange'
	 */
	public ValidationEventRequestHandler(String eventName)
	{
		this.eventName = eventName;
	}

	/**
	 * @see wicket.EventRequestHandler#getEventName()
	 */
	public final String getEventName()
	{
		return eventName;
	}

	/**
	 * @see wicket.markup.html.IHeaderContributor#printHead(wicket.markup.html.HtmlHeaderContainer)
	 */
	public final void printHead(HtmlHeaderContainer container)
	{
		//TODO make this neat using components and/ or templates instead of just writing to out
		String s =
				"\t<script language=\"JavaScript\" type=\"text/javascript\">\n" + 
				"\t\tdjConfig = {\n" + 
				"\t\tbaseRelativePath: \"./dojo/\"\n" +
				"\t};\n" +
				"\t</script>\n" +
				"\t<script language=\"JavaScript\" type=\"text/javascript\" " +
				"src=\"dojo/dojo-io.js\"></script>\n" +
				"\t<script language=\"JavaScript\" type=\"text/javascript\">\n" +
				"\t\tdojo.hostenv.loadModule(\"dojo.io.*\");\n" +
				"\tfunction validate(componentUrl, componentPath, field) { \n" +
				"\t\tdojo.io.bind({\n" +
				"\t\t\turl: componentUrl + '&' + componentPath + '=' + field.value,\n" +
				"\t\t\tmimetype: \"text/xml\",\n" +
				"\t\t\tload: function(type, data, evt) {\n" +
				"\t\t\t\talert(field);\n" +
				"\t\t\t}\n" +
				"\t\t});\n" +
				"\t}\n" +
				"\t</script>\n";
		container.getResponse().write(s);
	}

	/**
	 * @see wicket.EventRequestHandler#bind(wicket.Component)
	 */
	protected void bind(Component component)
	{
		if (!(component instanceof FormComponent))
		{
			throw new IllegalArgumentException("this handler can only be bound to form components");
		}

		if (formComponent != null)
		{
			throw new IllegalStateException("this kind of handler cannot be attached to " +
					"multiple components; it is allready attached to component " + formComponent +
					", but component " + component + " wants to be attached too");

		}

		this.formComponent = (FormComponent)component;
	}

	/**
	 * Attaches the event handler for the given component to the given tag.
	 * 
	 * @param component
	 *            The component
	 * @param tag
	 *            The tag to attache
	 */
	protected final void onRenderComponentTag(final Component component, final ComponentTag tag)
	{
		final ValueMap attributes = tag.getAttributes();

		//TODO loose + id sometime and e.g. put it in urlFor
		final String url = formComponent.urlFor(IEventRequestListener.class) + "&id=" + getId();
		final String attributeValue =
			"javascript:validate('" + url + "', '" + formComponent.getPath() + "', this);";

		attributes.put(getEventName(), attributeValue);
	}

	/**
	 * @see wicket.EventRequestHandler#getResourceStream()
	 */
	/**
	 * Gets the resource to render to the requester.
	 * @return the resource to render to the requester
	 */
	protected final IResourceStream getResourceStream()
	{
		StringBufferResourceStream s = new StringBufferResourceStream();

		formComponent.validate();

		if (!formComponent.isValid())
		{
			s.append(formComponent.getFeedbackMessage().getMessage());
		}

		return s;
	}
}
