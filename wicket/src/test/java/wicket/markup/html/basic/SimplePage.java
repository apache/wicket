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
package wicket.markup.html.basic;

import wicket.AttributeModifier;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.border.Border;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;


/**
 * Mock page for testing.
 * 
 * @author Chris Turner
 */
public class SimplePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public SimplePage()
	{
		new Label(this, "myLabel", "Test Label");

		WebMarkupContainer container = new WebMarkupContainer(this, "test");
		container.setRenderBodyOnly(true);

		new Label(container, "myLabel2", "Test Label2");

		Panel panel = new SimplePanel(this, "myPanel");
		panel.setRenderBodyOnly(true);

		new SimpleBorder(this, "myBorder");

		Border border2 = new SimpleBorder(this, "myBorder2");
		border2.setRenderBodyOnly(false);
		border2.add(new AttributeModifier("testAttr", true, new Model<String>("myValue")));
	}
}
