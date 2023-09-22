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
package org.apache.wicket;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

/**
 * Test page used for checking the attribute modification functionality of Component.
 * 
 * @see AttributeModifierComponentTest
 * @author Chris Turner
 */
public class AttributeModifierComponentPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public AttributeModifierComponentPage()
	{
		// Label with attribute modifier
		Label label1 = new Label("label1", new Model<String>("Label 1"));
		add(label1);

		// Label with override attribute modifier
		Label label2 = new Label("label2", new Model<String>("Label 2"));
		label2.add(new AttributeModifier("class", new Model<String>("overrideLabel")));
		label2.add(new AttributeModifier("unknown", new Model<String>("invalid")));
		add(label2);

		// Label with attribute inserter
		Label label3 = new Label("label3", new Model<String>("Label 3"));
		label3.add(AttributeModifier.replace("class", new LoadableDetachableModel<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String load()
			{
				return "insertLabel";
			}
		}));
		add(label3);
	}
}
