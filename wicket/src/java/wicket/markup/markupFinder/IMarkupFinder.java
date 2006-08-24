/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision$ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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

/**
 * Must be registered with a MarkupFragmentFinder which accessible through
 * application.getMarkupSettings().getMarkupFragmentFinder() and allows to
 * implement your own algoritms to find the markup associated with a Component.
 * 
 * @author Juergen Donnerstag
 */
public interface IMarkupFinder
{
	/**
	 * Searches for the markup stream associated with the component and position
	 * the markup stream at the first tag of the component.
	 * 
	 * @param component
	 *            The component search for its markup
	 * @param <T>
	 *            Component type
	 * @return A MarkupStream which is positioned at the component. Null, if
	 *         component markup not found
	 */
	<T> MarkupStream find(final Component<T> component);

	/**
	 * Search for the markup stream associated with the component within the
	 * markup stream provided and position the markup stream at the first tag of
	 * the component.
	 * 
	 * @param <T>
	 *            Component type
	 * @param component
	 *            The component search for its markup
	 * @param stream
	 *            The markup stream to check for the component's markup
	 * @param parentWithAssociatedMarkup
	 *            The container associated with the stream provided
	 * @return Same as the markup stream provided but positioned that component.
	 *         Null, if component markup not found
	 */
	<T> MarkupStream find(final Component<T> component, final MarkupStream stream,
			final MarkupContainer parentWithAssociatedMarkup);
}
