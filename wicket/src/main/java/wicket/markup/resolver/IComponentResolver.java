/*
 * $Id: IComponentResolver.java 3576 2006-01-01 23:19:44 +0000 (Sun, 01 Jan
 * 2006) jonathanlocke $ $Revision$ $Date: 2006-01-01 23:19:44 +0000
 * (Sun, 01 Jan 2006) $
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
package wicket.markup.resolver;

import java.io.Serializable;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;

/**
 * ApplicationSettings maintains a list of IComponentResolvers.
 * IComponentResolvers are responsible for mapping component names to Wicket
 * components.
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
 * @author Juergen Donnerstag
 */
public interface IComponentResolver extends Serializable
{
	/**
	 * Try to resolve the tag, then create a component, add it to the container
	 * and render it by calling {@link Component#render(MarkupStream)}
	 * 
	 * @param container
	 *            The container parsing its markup
	 * @param markupStream
	 *            The current markupStream
	 * @param tag
	 *            The current component tag while parsing the markup
	 * @return True if component-id was handled by the resolver, false
	 *         otherwise.
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag);
}
