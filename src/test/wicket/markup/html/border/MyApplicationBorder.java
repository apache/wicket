/*
 * $Id: BorderComponent1.java 3557 2006-01-01 07:34:05Z jonathanlocke $
 * $Revision$ $Date: 2006-01-01 08:34:05 +0100 (So, 01 Jan 2006) $
 * 
 * ========================================================================
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
package wicket.markup.html.border;

import wicket.MarkupContainer;


/**
 * Test the component: PageView
 * 
 * @author Juergen Donnerstag
 */
public class MyApplicationBorder extends Border
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 */
	public MyApplicationBorder(MarkupContainer parent, final String id)
	{
		super(parent, id);

		Border border = new BoxBorder(this, "boxBorder");
	}
}