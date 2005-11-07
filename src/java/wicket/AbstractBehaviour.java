/*
 * $Id$
 * $Revision$
 * $Date$
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

import wicket.markup.ComponentTag;
import wicket.markup.html.HtmlHeaderContainer;

/**
 * Adapter implementation of {@link wicket.IBehaviour}. This class just
 * implements the interface with empty methods; override this class when you
 * don't want to implement the whole interface.
 * 
 * @author Ralf Ebert
 * @author Eelco Hillenius
 */
public abstract class AbstractBehaviour implements IBehaviour
{
	/**
	 * @see wicket.IBehaviour#bind(wicket.Component)
	 */
	public void bind(Component hostComponent)
	{
	}

	/**
	 * @see wicket.IBehaviour#rendered(wicket.Component)
	 */
	public void rendered(Component hostComponent)
	{
	}

	/**
	 * @see wicket.IBehaviour#onComponentTag(wicket.Component,
	 *      wicket.markup.ComponentTag)
	 */
	public void onComponentTag(Component component, ComponentTag tag)
	{
	}

	/**
	 * @see wicket.IBehaviour#detachModel()
	 */
	public void detachModel()
	{
	}

	/**
	 * @see wicket.markup.html.ajax.IBodyOnloadContributor#getBodyOnload()
	 */
	public String getBodyOnload()
	{
		return null;
	}

	/**
	 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.markup.html.HtmlHeaderContainer)
	 */
	public void renderHead(HtmlHeaderContainer container)
	{
	}
}
