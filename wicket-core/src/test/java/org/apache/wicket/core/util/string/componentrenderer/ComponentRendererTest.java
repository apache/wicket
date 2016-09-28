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
package org.apache.wicket.core.util.string.componentrenderer;

import org.apache.wicket.core.util.string.ComponentRenderer;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for ComponentRenderer
 */
public class ComponentRendererTest extends WicketTestCase
{

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5209
	 *
	 * Tests that the page and the components in it are initialized.
	 * Enclosure won't know its child component without being initialized
	 */
	@Test
	public void componentsAreInitialized()
	{
		ComponentRenderer.renderComponent(new EnclosurePanel("anyId"));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5325
	 *
	 * Tests that ComponentRenderer#renderComponent() will use the component's
	 * markup if available
	 */
	@Test
	public void renderListViewItems()
	{
		PageWithRepeater page = new PageWithRepeater();
		CharSequence listViewOutput = ComponentRenderer.renderComponent(page.get("listView"));
		assertEquals("<div wicket:id=\"listView\"><span wicket:id=\"label\">one</span></div>" +
				"<div wicket:id=\"listView\"><span wicket:id=\"label\">two</span></div>", listViewOutput.toString());
	}
}
