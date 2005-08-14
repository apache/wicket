/*
 * $Id$
 * $Revision$ $Date$
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


/**
 * THIS INTERFACE IS NOT PART OF WICKETS PUBLIC API. DO NOT USE IT YOURSELF.
 * 
 * An interface to be implemented by components which are able to render
 * header sections. Usually this is only Page. However, Border implements it
 * as well to handle bordered pages (common page layout).
 * 
 * @author Juergen Donnerstag
 */
public interface IHeaderRenderer
{
	/**
	 * Visit all components of the component hierarchie and ask if they have
	 * something to contribute to the header section of the page. If yes, child
	 * components will return a MarkupContainer of there header section which
	 * gets (auto) added to the component hierarchie and immediately rendered.
	 * 
	 * @param container
	 *            The current html header container
	 */
	public void renderHeaderSections(final HtmlHeaderContainer container);
}
