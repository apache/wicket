/*
 * $Id: WicketTagComponentResolver.java,v 1.4 2005/01/18 08:04:29 jonathanlocke
 * Exp $ $Revision$ $Date: 2006-10-01 22:46:37 +0200 (So, 01 Okt 2006) $
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
package wicket.markup.parser.onLoadListener;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupFragment;
import wicket.markup.html.WebMarkupContainer;

/**
 * This is a markup load listener which handles &lt;wicket:link&gt; tags.
 * Because autolinks are already detected and handled, the only task of this
 * resolver will be to add a "transparent" WebMarkupContainer to transparently
 * handling child components.
 * 
 * @author Juergen Donnerstag
 */
public class WicketLinkMarkupLoadListener extends AbstractMarkupLoadListener
{
	/**
	 * 
	 * @see wicket.markup.parser.onLoadListener.AbstractMarkupLoadListener#visit(wicket.MarkupContainer,
	 *      wicket.markup.MarkupFragment)
	 */
	@Override
	protected Object visit(final MarkupContainer container, final MarkupFragment fragment)
	{
		final ComponentTag tag = fragment.getTag();

		// If <wicket:link>
		if (tag.isLinkTag())
		{
			new WebMarkupContainer(container, tag.getId())
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see wicket.MarkupContainer#isTransparentResolver()
				 */
				@Override
				public boolean isTransparentResolver()
				{
					return true;
				}
			};
		}

		return MarkupFragment.IVisitor.CONTINUE_TRAVERSAL;
	}
}