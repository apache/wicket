/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.cdapp;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.border.Border;


/**
 * This base class also creates a border for each page subclass, automatically
 * adding children of the page to the border. This accomplishes two important
 * things: (1) subclasses do not have to repeat the code to create the border
 * navigation and (2) since subclasses do not repeat this code, they are not
 * hardwired to page navigation structure details.
 * @author Eelco Hillenius
 */
public abstract class SimpleBorderedPage extends WebPage
{
	/** Border. */
	private Border border;

	/**
	 * Constructor
	 */
	public SimpleBorderedPage()
	{
		// Create border and add it to the page
		border = new SimpleBorder("border");
		super.add(border);
	}

	/**
	 * Adding children to instances of this class causes those children to be
	 * added to the border child instead.
	 * @see wicket.MarkupContainer#add(wicket.Component)
	 */
	public MarkupContainer add(final Component child)
	{
		// Add children of the page to the page's border component
		border.add(child);
		return this;
	}


	/**
	 * Removing children from instances of this class causes those children to be
	 * removed from the border child instead.
	 * @see wicket.MarkupContainer#removeAll()
	 */
	public void removeAll()
	{
		border.removeAll();
	}
}
