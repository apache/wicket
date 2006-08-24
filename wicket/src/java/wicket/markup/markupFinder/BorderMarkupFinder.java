/*
 * $Id: MarkupStream.java 5090 2006-03-22 21:52:18Z jdonnerstag $ $Revision:
 * 5090 $ $Date: 2006-03-22 22:52:18 +0100 (Mi, 22 Mrz 2006) $
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
package wicket.markup.markupFinder;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.MarkupStream;
import wicket.markup.html.border.Border;

/**
 * Handle Border components properly.
 * 
 * @author Juergen Donnerstag
 */
public class BorderMarkupFinder extends DefaultMarkupFinder
{
	/**
	 * Construct.
	 */
	public BorderMarkupFinder()
	{
	}

	/**
	 * Gets the markup stream and positions it at the component.
	 * 
	 * @param component
	 * @param <T>
	 *            Component type
	 * @return A MarkupStream which is positioned at the component
	 */
	public final <T> MarkupStream find(final Component<T> component)
	{
		// Get the parent's associated markup stream.
		MarkupContainer parentWithAssociatedMarkup = component.findParentWithAssociatedMarkup();
		MarkupStream markupStream = null;

		// Yet another exception for Border in the code base.
		// However if the container with the markup is a Border, than
		// ...
		while (parentWithAssociatedMarkup instanceof Border)
		{
			parentWithAssociatedMarkup = parentWithAssociatedMarkup
					.findParentWithAssociatedMarkup();

			// Might be that we have to walk up the component hierarchy
			markupStream = parentWithAssociatedMarkup.getAssociatedMarkupStream(true);
			markupStream = find(component, markupStream, parentWithAssociatedMarkup);
		}

		return markupStream;
	}
}