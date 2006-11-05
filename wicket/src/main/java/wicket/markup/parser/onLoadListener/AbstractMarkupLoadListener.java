/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision$
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
package wicket.markup.parser.onLoadListener;

import wicket.MarkupContainer;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupFragment;

/**
 * 
 * @author Juergen Donnerstag
 */
public abstract class AbstractMarkupLoadListener implements IMarkupLoadListener
{
	/**
	 * @see wicket.markup.parser.onLoadListener.IMarkupLoadListener#onAssociatedMarkupLoaded(wicket.MarkupContainer,
	 *      wicket.markup.MarkupFragment)
	 */
	public void onAssociatedMarkupLoaded(final MarkupContainer container,
			final MarkupFragment fragment)
	{
		fragment.visitChildren(MarkupFragment.class, new MarkupFragment.IVisitor()
		{
			/**
			 * @see wicket.markup.MarkupFragment.IVisitor#visit(wicket.markup.MarkupElement,
			 *      wicket.markup.MarkupFragment)
			 */
			public Object visit(final MarkupElement element, final MarkupFragment parent)
			{
				final MarkupFragment frag = (MarkupFragment)element;
				return AbstractMarkupLoadListener.this.visit(container, frag);
			}
		});
	}

	/**
	 * @see MarkupFragment.IVisitor#visit(MarkupElement, MarkupFragment)
	 * 
	 * @param container
	 *            The container loading the associated markup
	 * @param fragment
	 * @return Object
	 */
	protected abstract Object visit(final MarkupContainer container, final MarkupFragment fragment);
}
