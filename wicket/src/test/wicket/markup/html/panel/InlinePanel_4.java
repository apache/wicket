/*
 * $Id: SimplePanel_1.java 3618 2006-01-04 09:28:14Z ivaynberg $ $Revision$
 * $Date: 2006-01-04 10:28:14 +0100 (Mi, 04 Jan 2006) $
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
package wicket.markup.html.panel;

import wicket.MarkupContainer;

/**
 * Mock page for testing.
 * 
 */
public class InlinePanel_4 extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 */
	public InlinePanel_4(MarkupContainer parent, final String id)
	{
		super(parent, id);

		Fragment panel1 = new Fragment(this, "myPanel1", "frag1");

		Fragment panel2 = new Fragment(this, "myPanel2", "frag2");
	}
}
