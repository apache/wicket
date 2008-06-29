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
package org.apache.wicket.examples.compref;

import org.apache.wicket.examples.WicketExamplePage;

/**
 * Page with examples on {@link org.apache.wicket.markup.html.panel.Panel}.
 * 
 * @author Eelco Hillenius
 */
public class PanelPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public PanelPage()
	{
		add(new MyPanel("panel"));
	}

	@Override
	protected void explain()
	{
		String html = "<span wicket:id=\"panel\">panel contents come here</span>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;public PanelPage()\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;{\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;add(new MyPanel(\"panel\"));\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;}";
		add(new ExplainPanel(html, code));
	}
}