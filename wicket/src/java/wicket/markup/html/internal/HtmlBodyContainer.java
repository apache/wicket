/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.markup.html.internal;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.parser.filter.BodyOnLoadHandler;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * This is basically a transparent WebMarkupContainer with the ability to get
 * the markup stream positioned correctly where the component begins from the
 * parent (page) container.
 * <p>
 * It is transparent in the sense that all requests to access a child component
 * are forwarded to the parent (page) container.
 * <p>
 * Because this container is usually automatically created, it can accessed by
 * WebPage.getBodyContainer().
 * <p>
 * Though it is automatically created it may be replaced by adding you own
 * Component with id == BodyOnLoadHandler.BODY_ID to the Page.
 * <p>
 * Components and Behaviors which wish to modify e.g. the body onLoad attribute
 * may attach an AttributeModifier by means of <code>addOnLoadModifier()</code>
 * or <code>add(new AttributeModifier())</code>.
 * 
 * @author Juergen Donnerstag
 */
public class HtmlBodyContainer extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct
	 * 
	 * @see Component#Component(String)
	 */
	public HtmlBodyContainer()
	{
		super(BodyOnLoadHandler.BODY_ID);
	}

	/**
	 * @see wicket.MarkupContainer#isTransparentResolver()
	 */
	public boolean isTransparentResolver()
	{
		return true;
	}

	/**
	 * Add a new AttributeModifier to the body container which appends the
	 * 'value' to the onLoad attribute of the body tag.
	 * 
	 * @param value
	 *            The value to append to 'onLoad'
	 * @return The HtmlBodyContainer
	 */
	public final Component addOnLoadModifier(final String value)
	{
		return addOnLoadModifier(new Model(value));
	}

	/**
	 * Add a new AttributeModifier to the body container which appends the
	 * 'value' to the onLoad attribute of the body tag.
	 * 
	 * @param model
	 *            The value to append to 'onLoad'
	 * @return The HtmlBodyContainer
	 */
	public final Component addOnLoadModifier(final IModel model)
	{
		return add(new AttributeModifier("onLoad", true, model)
		{
			private static final long serialVersionUID = 1L;

			protected String newValue(String currentValue, String replacementValue)
			{
				return (currentValue == null ? replacementValue : currentValue + replacementValue);
			}
		});
	}
}
