/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.internal;

import java.util.HashMap;
import java.util.Map;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.markup.html.IHeaderResponse;
import wicket.markup.html.WebMarkupContainer;

/**
 * HeaderContainer is a base class for {@link HtmlHeaderContainer} and
 * {@link PortletHeaderContainer}
 * 
 * @author Juergen Donnerstag
 * @author Janne Hietam&auml;ki
 */
public abstract class HeaderContainer extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * wicket:head tags (components) must only be added once. To allow for a
	 * little bit more control, each wicket:head has an associated scope which
	 * by default is equal to the java class name directly associated with the
	 * markup which contains the wicket:head. It can be modified by means of the
	 * scope attribute.
	 */
	private transient Map<String, Map<Class, MarkupContainer>> componentsPerScope;

	/**
	 * Header response that is responsible for filtering duplicate
	 * contributions.
	 */
	private transient IHeaderResponse headerResponse = null;

	/**
	 * Construct
	 * 
	 * @see Component#Component(MarkupContainer,String)
	 */
	public HeaderContainer(MarkupContainer parent, final String id)
	{
		super(parent, id);

		// We will render the tags manually, because if no component asked to
		// contribute to the header, the tags will not be printed either.
		// No contribution usually only happens if none of the components
		// including the page does have a <head> or <wicket:head> tag.
		setRenderBodyOnly(true);
	}

	/**
	 * Ask all child components of the Page if they have something to contribute
	 * to the &lt;head&gt; section of the HTML output. Every component
	 * interested must subclass Component.renderHead().
	 * <p>
	 * Note: HtmlHeaderContainer will be removed from the component hierachie at
	 * the end of the request (@see #onEndRequest()) and thus can not transport
	 * status from one request to the next. This is true for all components
	 * added to the header as well.
	 * 
	 * @param page
	 *            The page object
	 */
	protected final void renderHeaderSections(final Page page)
	{
		page.renderHead(new HeaderResponse(getResponse()));
	}

	/**
	 * @see wicket.MarkupContainer#isTransparentResolver()
	 */
	@Override
	public boolean isTransparentResolver()
	{
		return true;
	}

	/**
	 * Check if the header component is ok to render within the scope given.
	 * 
	 * @param header
	 *            The header part container to check
	 * @return true, if the component is eligable to create and render
	 */
	public boolean okToRender(final WicketHeadContainer header)
	{
		if (this.componentsPerScope == null)
		{
			this.componentsPerScope = new HashMap<String, Map<Class, MarkupContainer>>();
		}

		String scope = header.getScope();
		Map<Class, MarkupContainer> componentScope = this.componentsPerScope.get(scope);
		if (componentScope == null)
		{
			componentScope = new HashMap<Class, MarkupContainer>();
			this.componentsPerScope.put(scope, componentScope);
		}

		Class markupClass = header.getMarkupFragment().getMarkup().getResource().getMarkupClass();
		Component creator = componentScope.get(markupClass);
		if (creator != null)
		{
			if (creator == header.getParent())
			{
				return true;
			}
			return false;
		}

		componentScope.put(markupClass, header.getParent());
		return true;
	}

	/**
	 * 
	 * @see wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		// not needed anymore, which is why it can be transient
		this.componentsPerScope = null;
	}

	/**
	 * @see wicket.Component#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		this.componentsPerScope = null;
		this.headerResponse = null;
		super.onDetach();
	}
}