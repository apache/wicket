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
package wicket;

import java.util.ArrayList;
import java.util.List;

import wicket.markup.MarkupStream;
import wicket.markup.markupFinder.BorderMarkupFinder;
import wicket.markup.markupFinder.DefaultMarkupFinder;
import wicket.markup.markupFinder.FragmentMarkupFinder;
import wicket.markup.markupFinder.IMarkupFinder;

/**
 * Responding to an AJAX request requires that we position the markup stream at
 * the component associated with the AJAX request. That is straight forward in
 * most cases except for "transparent" components and for components which
 * implement their own IComponentResolver.
 * <p>
 * As users may implement and register there own resolvers, they also have a
 * need to implement and register there own IMarkupFinder. IMarkupFinder can be
 * registered with MarkupFragmentFinder which is accessible through
 * IMarkupSettings.getMarkupFragmentFinder().
 * 
 * @author Juergen Donnerstag
 */
public final class MarkupFragmentFinder
{
	/** List of registered markup finders */
	private List<IMarkupFinder> markupFinders = new ArrayList<IMarkupFinder>();

	/**
	 * Construct
	 */
	public MarkupFragmentFinder()
	{
		registerMarkupFilter(new DefaultMarkupFinder());
		registerMarkupFilter(new BorderMarkupFinder());
		registerMarkupFilter(new FragmentMarkupFinder());
	}

	/**
	 * Register an additional IMarkupFinder for find the markup associated with
	 * a Component. All finders registered are executed in sequence until the
	 * markup has been found.
	 * 
	 * @param finder
	 */
	public void registerMarkupFilter(final IMarkupFinder finder)
	{
		this.markupFinders.add(finder);
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
		MarkupStream markupStream = null;
		for (IMarkupFinder finder : this.markupFinders)
		{
			markupStream = finder.find(component);
			if (markupStream != null)
			{
				return markupStream;
			}
		}

		throw new WicketRuntimeException("Unable to find the markup for the component: "
				+ component.getId());
	}
}