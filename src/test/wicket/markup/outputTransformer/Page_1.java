/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:52:19 +0200 (vr, 26 mei 2006) $
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
package wicket.markup.outputTransformer;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.basic.SimpleBorder;
import wicket.markup.html.border.Border;
import wicket.markup.html.panel.Panel;
import wicket.markup.transformer.AbstractOutputTransformerContainer;
import wicket.markup.transformer.NoopOutputTransformerContainer;
import wicket.markup.transformer.XsltOutputTransformerContainer;
import wicket.model.Model;


/**
 * Mock page for testing.
 * 
 * @author Chris Turner
 */
public class Page_1 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 */
	public Page_1()
	{
		new Label(this, "myLabel", "Test Label");

		MarkupContainer container = new NoopOutputTransformerContainer(this, "test");

		new Label(container, "myLabel2", "Test Label2");

		MarkupContainer panelContainer = new AbstractOutputTransformerContainer(this, "test2")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public CharSequence transform(Component component, String output)
			{
				// replace the generated String
				return "Whatever";
			}
		};

		Panel panel = new Panel_1(panelContainer, "myPanel");
		panel.setRenderBodyOnly(true);

		MarkupContainer borderContainer = new AbstractOutputTransformerContainer(this, "test3")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public CharSequence transform(Component component, String output)
			{
				// Convert all text to uppercase
				return output.toUpperCase();
			}
		};

		Border border = new SimpleBorder(borderContainer, "myBorder");

		MarkupContainer xsltContainer = new XsltOutputTransformerContainer(this, "test4");

		Border border2 = new SimpleBorder(xsltContainer, "myBorder2");
		border2.setRenderBodyOnly(false);
		border2.add(new AttributeModifier("testAttr", true, new Model<String>("myValue")));

		MarkupContainer xsltContainer2 = new XsltOutputTransformerContainer(this, "test5", null,
				"wicket/markup/outputTransformer/anyName.xsl");

		Border border3 = new SimpleBorder(xsltContainer2, "myBorder3");
		border3.setRenderBodyOnly(false);
		border3.add(new AttributeModifier("testAttr", true, new Model<String>("myValue")));
	}
}
