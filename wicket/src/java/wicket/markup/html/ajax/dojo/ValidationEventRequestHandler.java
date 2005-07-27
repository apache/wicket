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
package wicket.markup.html.ajax.dojo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.IEventRequestListener;
import wicket.markup.ComponentTag;
import wicket.markup.html.HtmlHeaderContainer;
import wicket.markup.html.form.FormComponent;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.StringBufferResourceStream;
import wicket.util.value.ValueMap;

/**
 * Handles event requests, like AJAX (XmlHttp) requests.
 *
 * @author Eelco Hillenius
 */
public final class ValidationEventRequestHandler extends DojoEventRequestHandler
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
	 * Gets the name of the event to attach to.
	 * @return the name of the event to attach to
	 */
	public final String getEventName()
	{
		return eventName;
	}

	/**
	 * @see wicket.markup.html.ajax.AbstractEventRequestHandler#doPrintHead(wicket.markup.html.HtmlHeaderContainer)
	 */
	public final void doPrintHead(HtmlHeaderContainer container)
	{
		String s =

			"\t<script language=\"JavaScript\" type=\"text/javascript\">\n" +
			"\tfunction validate(componentUrl, componentPath, field) { \n" +
			"\t\tdojo.io.bind({\n" +
			"\t\t\turl: componentUrl + '&' + componentPath + '=' + field.value,\n" +
			"\t\t\tmimetype: \"text/plain\",\n" +
			"\t\t\tload: function(type, data, evt) {\n" +
			"\t\t\t\talert(data);\n" +
			"\t\t\t}\n" +
			"\t\t});\n" +
			"\t}\n" +
			"\t</script>\n";

		container.getResponse().write(s);
	}

	/**
	 * @see wicket.markup.html.ajax.AbstractEventRequestHandler#bind(wicket.Component)
	 */
	public void bind(Component component)
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
	public final void onComponentTag(final Component component, final ComponentTag tag)
	{
		final ValueMap attributes = tag.getAttributes();
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
	protected final IResourceStream getResponse()
	{
		StringBufferResourceStream s = new StringBufferResourceStream();

		formComponent.validate();

		// there's a lot that should happen here. For starters, we need to update all components
		// that are affected by feedback messages (like FeedbackPanel and FormComponentFeedbackBorder,
		// but it might actually be anything custom too

		// So, for an ajax handler like this it pays of to have a generic ajax rendering cycle,
		// though for some other cases, that might just be too much.

//		if (!formComponent.isValid())
//		{
//			s.append(formComponent.getFeedbackMessage().getMessage());
//		}

		// for now, just display a simple message
		s.append("ajax validation executed");

		return s;
	}
}
