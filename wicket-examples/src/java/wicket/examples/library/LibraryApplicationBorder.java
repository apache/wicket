/*
 * $Id: LibraryApplicationBorder.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24
 * May 2006) joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed,
 * 24 May 2006) $
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
package wicket.examples.library;

import wicket.MarkupContainer;
import wicket.markup.IAlternateParentProvider;
import wicket.markup.html.border.Border;
import wicket.markup.html.border.BoxBorder;

/**
 * Border component.
 * 
 * @author Jonathan Locke
 */
public class LibraryApplicationBorder extends Border implements IAlternateParentProvider
{
	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent
	 * @param id
	 *            The id of this component
	 */
	public LibraryApplicationBorder(MarkupContainer parent, final String id)
	{
		super(parent, id);
		
		Border boxBorder = new BoxBorder(this, "boxBorder");
		setBorderBodyContainer(boxBorder);
	}

	/**
	 * @see wicket.markup.IAlternateParentProvider#getAlternateParent(java.lang.Class, java.lang.String)
	 */
	public MarkupContainer getAlternateParent(Class childClass, String childId)
	{
		return (getBodyContainer() != null && !"mainNavigation".equals(childId) 
				? getBodyContainer() : this);
	}
}
