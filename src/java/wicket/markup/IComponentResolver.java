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
package wicket.markup;

import wicket.Container;
import wicket.RequestCycle;

/**
 * ApplicationSettings maintains a list of IComponentNameResolvers.
 * IComponentNameResolvers are responsible to map component names
 * with Wicket components. E.g. [autolink] is such a special case.
 *  
 * @author Juergen Donnerstag
 */
public interface IComponentResolver
{
    /**
     * Try to resolve the component name, than create a component,
     * add it to the container and render the compoent.
     * 
     * @param cycle The current RequestCycle 
     * @param markupStream The current markupStream
     * @param tag The current component tag while parsing the markup
     * @param container The container parsing its markup
     * @return true, if componentName was handle by the resolver. False, otherwise  
     */
	public boolean resolve(
	        final RequestCycle cycle, 
	        final MarkupStream markupStream, 
	        final ComponentTag tag, 
	        final Container container);
}