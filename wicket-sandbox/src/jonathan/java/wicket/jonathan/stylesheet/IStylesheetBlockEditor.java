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
 * Interface to code that edits a stylesheet block. Normally, editing will
 * amount to appending text to the stylesheet block, but an implementor can do
 * anything it wants to.
 * 
 * @author Jonathan Locke
 */
public interface IStylesheetBlockEditor
{
	/**
	 * Allows the implementor to edit a stylesheet block.
	 * 
	 * @param block
	 *            The block to edit
	 */
	public void edit(StylesheetBlock block);
}
