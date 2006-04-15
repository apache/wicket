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
package wicket.markup.html;

import java.io.Serializable;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.model.Model;

/**
 * Container for the page body. This is mostly an internal class that is used
 * for contributions to the body tag's onload event handler.
 * 
 * @author jcompagner
 */
public final class BodyContainer implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** The webpage where the body container is in */
	private final WebPage page;
	
	/** The container id */
	private final String id;

	/**
	 * Construct.
	 * 
	 * @param page
	 *            The webpage where the body container is in
	 * @param id
	 *            The container id
	 */
	public BodyContainer(WebPage page, String id)
	{
		this.page = page;
		this.id = id;
	}

	/**
	 * Add a new AttributeModifier to the body container which appends the
	 * 'value' to the onLoad attribute of the body tag.
	 * 
	 * @param value
	 *            The value to append to 'onLoad'
	 * @return this
	 */
	public final BodyContainer addOnLoadModifier(final String value)
	{
		final Model model = new Model(value);
		final Component bodyContainer = page.get(id);
		
		// TODO Post 1.2: Move all attribute modifier into a separate package
		// and add an AppendingAttributeModifier to that package
		bodyContainer.add(new AttributeModifier("onLoad", true, model)
		{
			private static final long serialVersionUID = 1L;

			protected String newValue(String currentValue, String replacementValue)
			{
				return (currentValue == null ? replacementValue : currentValue + replacementValue);
			}
		});
		return this;
	}
	
	/**
	 * Add a new AttributeModifier to the body container which appends the
	 * 'value' to the onLoad attribute of the body tag.
	 * 
	 * @param model
	 *            The model that holds the value that must be appended to 'onLoad'
	 * @return this
	 */
	public final BodyContainer addOnLoadModifier(final Model model)
	{
		final Component bodyContainer = page.get(id);
		bodyContainer.add(new AttributeModifier("onLoad", true, model)
		{
			private static final long serialVersionUID = 1L;

			protected String newValue(String currentValue, String replacementValue)
			{
				return (currentValue == null ? replacementValue : currentValue + replacementValue);
			}
		});
		return this;
	}	
	
	/**
	 * Add a new AttributeModifier to the body container which appends the
	 * 'value' to the onUnLoad attribute of the body tag.
	 * 
	 * @param value
	 *            The value to append to 'onUnLoad'
	 * @return this
	 */
	public final BodyContainer addOnUnLoadModifier(final String value)
	{
		final Model model = new Model(value);
		final Component bodyContainer = page.get(id);
		bodyContainer.add(new AttributeModifier("onUnLoad", true, model)
		{
			private static final long serialVersionUID = 1L;

			protected String newValue(String currentValue, String replacementValue)
			{
				return (currentValue == null ? replacementValue : currentValue + replacementValue);
			}
		});
		return this;
	}

	/**
	 * Add a new AttributeModifier to the body container which appends the
	 * value of the model to the onUnLoad attribute of the body tag.
	 * 
	 * @param model
	 *            The model which holds the value to be appended to 'onUnLoad'
	 * @return this
	 */
	public final BodyContainer addOnUnLoadModifier(final Model model)
	{
		final Component bodyContainer = page.get(id);
		bodyContainer.add(new AttributeModifier("onUnLoad", true, model)
		{
			private static final long serialVersionUID = 1L;

			protected String newValue(String currentValue, String replacementValue)
			{
				return (currentValue == null ? replacementValue : currentValue + replacementValue);
			}
		});
		return this;
	}
	
}