/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
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

import wicket.MarkupContainer;
import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.border.Border;

/**
 * PortletHeaderContainer is a HeaderContainer {@link HeaderContainer} that 
 * works like HtmlHeaderContainer {@link HtmlHeaderContainer}, but since 
 * portlets do not support real header contribution, it tries to achieve 
 * similiar results without doing real contribution.
 * 
 * TODO: this currently adds header content in the body of the page. This is not
 * correct behavior (against the specs), but it would be possible to do a javascript
 * which includes for example css files.
 * 
 * @author Janne Hietam&aumnl;ki
 */
public class PortletHeaderContainer extends HeaderContainer
{

	/**
	 * Construct.
	 * @param parent
	 * @param id
	 */
	public PortletHeaderContainer(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * First render the body of the component. And if it is the header component
	 * of a Page (compared to a Panel or Border), than get the header sections
	 * from all component in the hierachie and render them as well.
	 * 
	 * @see wicket.MarkupContainer#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	@Override
	protected final void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		// In any case, first render the header section directly associated
		// with the markup
		super.onComponentTagBody(markupStream, openTag);

		// If the parent component is a Page (or a bordered Page), we must
		// now include the header sections of all components in the
		// component hierarchie.
		MarkupContainer parent = getParent();

		// If bordered page ...
		while ((parent instanceof Border))
		{
			parent = parent.getParent();
		}

		// must be a Page
		if (parent instanceof Page)
		{
			renderHeaderSections((Page)parent, this);
		}
		else
		{
			throw new WicketRuntimeException(
					"Programming error: 'parent' should be a Page or a Border.");
		}
	}
}