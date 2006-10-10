/*
 * $Id: WicketTagComponentResolver.java,v 1.4 2005/01/18 08:04:29 jonathanlocke
 * Exp $ $Revision$ $Date$
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
package wicket.markup.html;

import wicket.MarkupContainer;
import wicket.markup.IScopedComponent;
import wicket.markup.html.link.Link;

/**
 * 
 * @author Juergen Donnerstag
 */
public abstract class ScopedLink extends Link implements IScopedComponent
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param parent 
	 * @param id
	 */
	public ScopedLink(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * 
	 * @see IScopedComponent#isRenderableInSubContainers()
	 */
	public boolean isRenderableInSubContainers()
	{
		return true;
	}
}
