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
package wicket;

import wicket.PageParameters;
import wicket.markup.ComponentTagAttributeModifier;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.basic.Label;
import wicket.model.IDetachableModel;
import wicket.model.Model;

/**
 * Test page used for checking the attribute modification functionality of Component.
 * @see AttributeModifierComponentTest
 * @author Chris Turner
 */
public class AttributeModifierComponentPage extends HtmlPage
{

	/** Serial Version ID */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * @param parameters
	 */
	public AttributeModifierComponentPage(final PageParameters parameters)
	{
		// Label with attribute modifier
		Label label1 = new Label("label1", new Model("Label 1"));
		add(label1);

		// Lavel with override attribute modifier
		Label label2 = new Label("label2", new Model("Label 2"));
		label2.add(new ComponentTagAttributeModifier("class", new Model("overrideLabel")));
		label2.add(new ComponentTagAttributeModifier("unknown", new Model("invalid")));
		add(label2);

		// Lavel with attribute inserter
		Label label3 = new Label("label3", new Model("Label 3"));
		label3.add(new ComponentTagAttributeModifier("class", true, new IDetachableModel()
		{
			private String text = null;

			public void detach(final Session session)
			{
				System.out.println("ComponentTagAttributeModifier model detached");
				text = null;
			}

			public void attach(final Session session)
			{
				System.out.println("ComponentTagAttributeModifier model attached");
				text = "insertLabel";
			}

			public Object getObject()
			{
				return text;
			}

			public void setObject(Object object)
			{
				text = object.toString();
			}
		}));
		add(label3);
	}

}
