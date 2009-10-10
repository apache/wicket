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
import org.apache.wicket.markup.html.basic.Label;


/**
 * Page with examples on {@link org.apache.wicket.markup.html.border.Border}.
 * 
 * @author Eelco Hillenius
 */
public class BorderPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public BorderPage()
	{
		MyBorder border = new MyBorder("border");
		add(border);

		border.add(new Label("label", "I am the label"));
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<span wicket:id=\"border\" class=\"mark\">\n"
			+ "<span wicket:id=\"label\" class=\"mark\">label contents here</span>\n" + "</span>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;public BorderPage()\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;{\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Label label = new Label(\"label\", \"I am the label\");\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;MyBorder border = new MyBorder(\"border\");\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;border.add(label);\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;add(border);\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;}";
		add(new ExplainPanel(html, code));
	}
}