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
package org.apache.wicket.markup.html.border;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Fragment;


/**
 * Mock page for testing.
 */
public class BoxBorderTestPage_8 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public BoxBorderTestPage_8()
	{
		Border border1 = new BorderComponent1("border1");
		add(border1);

		Fragment panel1 = new Fragment("panel1", "frag1", border1.getBodyContainer());
		border1.add(panel1);

		Fragment panel2 = new Fragment("panel2", "frag2", this);
		border1.add(panel2);
	}
}
