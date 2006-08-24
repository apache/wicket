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
import wicket.markup.IMarkup;
import wicket.markup.MarkupStream;
import wicket.markup.html.panel.Fragment;

/**
 * Handle Fragments properly.
 * 
 * @author Juergen Donnerstag
 */
public class FragmentMarkupFinder extends DefaultMarkupFinder
{
	/**
	 * Construct.
	 */
	public FragmentMarkupFinder()
	{
	}

	/**
	 * 
	 * @see wicket.markup.markupFinder.IMarkupFinder#find(wicket.Component,
	 *      wicket.markup.MarkupStream, wicket.MarkupContainer)
	 */
	public <T> MarkupStream find(final Component<T> component, MarkupStream markupStream,
			final MarkupContainer parentWithAssociatedMarkup)
	{
		// if it is a child of a fragement. First find the fragement
		MarkupContainer mc = component.findParent(Fragment.class);
		if (mc == null)
		{
			return null;
		}

		final Fragment fragment = (Fragment)mc;
		final MarkupContainer markupProvider = fragment.getMarkupProvider();
		if (markupProvider != null)
		{
			markupStream = markupProvider.getMarkupStream();
			if (markupStream == null)
			{
				markupStream = markupProvider.getAssociatedMarkupStream(true);
			}
		}

		String relativePath = fragment.getFragmentMarkupId() + IMarkup.TAG_PATH_SEPARATOR
				+ getComponentRelativePath(component, mc);

		// If the component is defined in the markup
		int index = markupStream.positionAt(relativePath, false);
		return (index != -1 ? markupStream : null);
	}
}