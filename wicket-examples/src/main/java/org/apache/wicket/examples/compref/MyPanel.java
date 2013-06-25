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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * A sample panel component.
 * 
 * @author Eelco Hillenius
 */
class MyPanel extends Panel
{
	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 */
	public MyPanel(String id)
	{
		super(id);
		add(new Label("label", "yep, this is from a component proper"));
		add(new AnotherPanel("otherPanel"));

		// create a list with sublists
		List<Object> l1 = new ArrayList<>();
		l1.add("test 1.1");
		l1.add("test 1.2");
		List<Object> l2 = new ArrayList<>();
		l2.add("test 2.1");
		l2.add("test 2.2");
		l2.add("test 2.3");
		List<String> l3 = new ArrayList<>();
		l3.add("test 3.1");
		l2.add(l3);
		l2.add("test 2.4");
		l1.add(l2);
		l1.add("test 1.3");

		// construct the panel
		add(new RecursivePanel("recursive", l1));
	}
}
