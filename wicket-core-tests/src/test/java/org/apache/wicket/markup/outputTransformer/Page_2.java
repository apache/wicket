/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.outputTransformer;

import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.SimpleBorder;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.transformer.AbstractOutputTransformerContainer;
import org.apache.wicket.markup.transformer.NoopOutputTransformerContainer;
import org.apache.wicket.markup.transformer.XsltTransformerBehavior;

/**
 * Mock page for testing.
 * 
 * @author Chris Turner
 */
public class Page_2 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public Page_2()
	{
		add(new Label("myLabel", "Test Label"));

		MarkupContainer container = new NoopOutputTransformerContainer("test");

		add(container);
		container.add(new Label("myLabel2", "Test Label2"));

		MarkupContainer panelContainer = new AbstractOutputTransformerContainer("test2")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public CharSequence transform(Component component, CharSequence output)
			{
				// replace the generated String
				return "Whatever";
			}
		};

		add(panelContainer);
		Panel panel = new Panel_1("myPanel");
		panel.setRenderBodyOnly(true);
		panelContainer.add(panel);

		MarkupContainer borderContainer = new AbstractOutputTransformerContainer("test3")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public CharSequence transform(Component component, CharSequence output)
			{
				// Convert all text to uppercase
				return output.toString().toUpperCase(Locale.ROOT);
			}
		};

		add(borderContainer);
		Border border = new SimpleBorder("myBorder");
		borderContainer.add(border);

		Border border2 = new SimpleBorder("myBorder2");
		border2.setRenderBodyOnly(false);
		border2.add(AttributeModifier.replace("testAttr", "myValue"));
		add(border2);

		border2.add(new XsltTransformerBehavior());
	}
}
