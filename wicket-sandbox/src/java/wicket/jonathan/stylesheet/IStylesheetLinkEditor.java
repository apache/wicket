/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.jonathan.stylesheet;

/**
 * Allows components which implement this interface an opportunity to edit a
 * list of stylesheet links. Normally, this will amount to just adding a link to
 * the component's stylesheet resource, but the editor can do anythign it wants.
 * 
 * @author Jonathan Locke
 */
public interface IStylesheetLinkEditor
{
	/**
	 * @param links
	 *            The links to edit
	 */
	public void edit(StylesheetLinks links);
}
