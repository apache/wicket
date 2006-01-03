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

import wicket.behavior.AttributeModifier;
import wicket.markup.ComponentTag;
import wicket.markup.html.IBodyTagContributor;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.model.Model;

/**
 * Handle &lt;body&gt; tags. The reason why this is a component is because of
 * JavaScript and CSS support which requires to append body onload attributes
 * from child component markup to the page's body tag.
 * 
 * @author Juergen Donnerstag
 */
public class BodyOnLoadContainer extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor used by BodyOnLoadContainer. The id is fix "_body" and the
	 * markup stream will be provided by the parent component.
	 * 
	 * @param id
	 *            Componen id
	 */
	public BodyOnLoadContainer(final String id)
	{
		super(id);
	}

	/**
	 * If parent is WebPage append onload attribute values from all components
	 * in the hierarchie with onload attribute in there own markup.
	 * 
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		// If WebPage ...
		// If not WebPage, than just be a WebMarkupContainer
		if (getParent() instanceof WebPage)
		{
			// The consolidated onload of all child components
			String onLoad = ((WebPage)this.getPage()).getBodyOnLoad();

			// Get the page's onLoad attribute value. Null if not given
			String pageOnLoad = tag.getAttributes().getString("onload");

			// Add an AttributeModifier if onLoad must be changed
			if ((pageOnLoad != null) && (onLoad != null))
			{
				onLoad = pageOnLoad + onLoad;
			}

			if (onLoad != null)
			{
				add(new AttributeModifier("onload", true, new Model(onLoad)));
			}

			// <wicket:head> and panels are able to contribute <body
			// onLoad="xxx"> attributes to the page. In order to
			// support that feature, a BodyOnLoadContainer is automatically
			// created and associated with the body tag of the page (in case of
			// an exception, on the exception page you can see a _body component
			// being a child of the Page). That allways happens because wicket
			// does not know upfront if there will be a panel contributing to
			// the body or not (That will only change in wicket 2). This
			// BodyOnLoadContainer however is transparent, it delegates all
			// events to its parent (the page) and is not directly accessible
			// by the user. However in order to allow the user to attach an 
			// AttributeModifier to body tag, the page might implement the
			// IBodyTagContributor interface.
			if (this.getPage() instanceof IBodyTagContributor)
			{
				IBodyTagContributor bodyContributor = (IBodyTagContributor)this.getPage();
				AttributeModifier[] modifiers = bodyContributor.getBodyAttributeModifiers();
				for (int i = 0; i < modifiers.length; i++)
				{
					add(modifiers[i]);
				}
			}
		}

		// go on with default implementation
		super.onComponentTag(tag);
	}

	/**
	 * @see wicket.MarkupContainer#isTransparent()
	 */
	public boolean isTransparent()
	{
		return true;
	}
}
