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
package org.apache.wicket.markup.html.internal;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;


/**
 * Mock page for testing.
 * 
 * @author Juergen Donnerstag
 */
public class EnclosurePage_2 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public EnclosurePage_2()
	{
		WebMarkupContainer example3 = new WebMarkupContainer("example3");
		example3.add(new Label("label1", "label 1"));
		example3.add(new Label("label2", "label 2"));
		example3.add(new Label("label3", "label 3"));
		example3.add(new Label("label4", "label 4"));

		add(example3);

		example3.get("label3").setVisible(false);
	}
}
