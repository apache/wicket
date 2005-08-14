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
package wicket.markup.html.ajax.rico;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.IEventRequestListener;
import wicket.markup.ComponentTag;
import wicket.markup.html.HtmlHeaderContainer;
import wicket.markup.html.ajax.rico.RicoEventRequestHandler;
import wicket.markup.html.form.FormComponent;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.StringBufferResourceStream;
import wicket.util.value.ValueMap;

/**
 * Handles event requests, like AJAX (XmlHttp) requests.
 *
 * @author Eelco Hillenius
 */
public final class ValidationEventRequestHandler extends RicoEventRequestHandler
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
		String id = "f" + String.valueOf(formComponent.hashCode());
		String handlerId = getId();
		String url = formComponent.urlFor(IEventRequestListener.class) + "&id=" + handlerId;
		String path = formComponent.getPath();
		String s =
			"<script language=\"JavaScript\">\n" +
			"\tonloads.push(" + id + ");\n" +
			"\tfunction " + id + "() {\n" +
			"\t\tajaxEngine.registerRequest( '" + id + "', '" + url + "' );\n" +
			"\t}\n" +
			"\tfunction validate(field) {\n" +
			"\t\tajaxEngine.sendRequest('" + id + "',\"" + path + "=\"+" + "field.value);\n" +
			//"\t\tajaxEngine.sendRequest('" + id + "');\n" +
			"\t}\n" +
			"</script>";
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
		final String attributeValue = "javascript:validate(this)";
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

		if (!formComponent.isValid())
		{
			s.append(formComponent.getFeedbackMessage().getMessage());
		}

		return s;
	}
}
