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
package org.apache.wicket.markup.html.header.testing3;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * 
 * @author Juergen Donnerstag
 */
public class TestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private final Panel panel1 = new Panel1("panel");
	private final Panel panel2 = new Panel2("panel");
	private Panel current;

	/**
	 * Construct.
	 */
	public TestPage()
	{
		current = panel1;
		add(current);

		add(new Link<Void>("link")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				replacePanel();
				setResponsePage(TestPage.this);
			}
		});
	}

	/**
	 * 
	 */
    private void replacePanel()
	{
		current = (current == panel1 ? panel2 : panel1);
		replace(current);
	}
}
