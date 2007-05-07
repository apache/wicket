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
package wicket.contrib.markup.html.velocity;

import java.util.HashMap;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.UrlResourceStream;
import org.apache.wicket.velocity.markup.html.VelocityPanel;

/**
 * Test page for <code>VelocityPanel</code>
 * 
 * @see org.apache.wicket.velocity.markup.html.VelocityPanel
 */
public class VelocityWithMarkupParsingPage extends WebPage
{
	/**
	 * Adds a VelocityPanel to the page with markup parsing
	 */
	public VelocityWithMarkupParsingPage()
	{
		HashMap values = new HashMap();
		values.put("labelId", "message");
		VelocityPanel velocityPanel = new VelocityPanel("velocityPanel",
				new UrlResourceStream(this.getClass().getResource("testWithMarkup.html")), new Model(values));
		velocityPanel.setParseGeneratedMarkup(true);
		velocityPanel.add(new Label("message", VelocityPage.TEST_STRING));
		add(velocityPanel);
	}
}
