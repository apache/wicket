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
package org.apache.wicket.extensions.markup.html.repeater.tree.table;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

/**
 * Test for {@link NodeBorder}.
 */
public class NodeBorderTest
{

	/**
	 * WICKET-5447
	 */
	@Test
	public void properlyClosed() throws Exception
	{
		WicketTester tester = new WicketTester();

		Label label = new Label("label");
		label.add(new NodeBorder(new boolean[] { true, false, true }));

		tester.startComponentInPage(label);

		tester
			.assertResultPage("<div class=\"tree-branch tree-branch-mid\"><div class=\"tree-subtree\"><div class=\"tree-branch tree-branch-last\"><div class=\"tree-subtree\"><div class=\"tree-branch tree-branch-mid\"><span wicket:id=\"label\" class=\"tree-node\"></span></div></div></div></div></div>");
	}
}
