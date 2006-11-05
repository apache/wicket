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
import wicket.markup.MarkupFragment;
import wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;

/**
 * Markup load listeners can be registered with the application and are invoked
 * after Markup has been loaded from disk. As Wicket will internally cache the
 * markup it is really only called when loaded.
 * <p>
 * IMarkupLoadListener and IComponentResolver seem to be similar at first, but
 * IMarkupLoadListener is signifcantly less powerful. Comparison:
 * <ul>
 * <li>IMarkupLoadListener are executed after the associated markup has been
 * loaded</li>
 * <li>IComponentResolver are executed during the render phase if a component
 * can not be found by simply asking the parent container</li>
 * <li>IMarkupLoadListener can only add new (auto-)components to the container
 * associated with the markup file</li>
 * <li>IComponentResolver will add new (auto-)components to the container
 * associated with markup fragment currently being rendered</li>
 * <li>All (auto-)components are removed after the render phase. Hence,
 * components created via IComponentResolver have a shorter lifecycle</li>
 * <li>Because components created via IComponentResolver during the render
 * phase, there is no need to sync them in a clustered environment</li>
 * </ul>
 * 
 * @see WebMarkupContainerWithAssociatedMarkup#getAssociatedMarkup(boolean)
 * 
 * @author Juergen Donnerstag
 */
public interface IMarkupLoadListener
{
	/**
	 * Invoked after a markup file has been loaded.
	 * 
	 * @param container
	 *            The container which is associated with the markup
	 * 
	 * @param markup
	 *            The markup
	 */
	void onAssociatedMarkupLoaded(final MarkupContainer container, final MarkupFragment markup);
}
