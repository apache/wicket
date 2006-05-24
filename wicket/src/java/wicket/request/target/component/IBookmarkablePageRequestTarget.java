/*
 * $Id: IBookmarkablePageRequestTarget.java,v 1.1 2005/12/06 22:17:44 eelco12
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
package wicket.request.target.component;

import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageParameters;
import wicket.request.target.IEventProcessor;

/**
 * Target that denotes a page that is to be created from the provided page
 * class. This is typically used for redirects to bookmarkable pages.
 * 
 * @author Eelco Hillenius
 */
public interface IBookmarkablePageRequestTarget extends IRequestTarget, IEventProcessor
{
	/**
	 * Gets the page class.
	 * 
	 * @return the page class
	 */
	Class<? extends Page> getPageClass();

	/**
	 * Gets the optional page parameters.
	 * 
	 * @return the page parameters or null
	 */
	PageParameters getPageParameters();

	/**
	 * Gets the optional page map name.
	 * 
	 * @return the optional page map name
	 */
	String getPageMapName();
}