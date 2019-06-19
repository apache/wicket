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
package org.apache.wicket.extensions.ajax.markup.html.repeater;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link AjaxListPanel}.
 * 
 * @author svenmeier
 */
class AjaxListPanelTest extends WicketTestCase
{

	@Test
	void test()
	{
		final AjaxListPanel list = new AjaxListPanel("list");

		AjaxEventBehavior behavior = new AjaxEventBehavior("click") {
			Label label;
			
			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				if (label == null) {
					label = new Label(list.newChildId());
					list.append(label, target);
				} else {
					list.delete(label, target);
					label = null;
				}
			}
		};
		list.add(behavior);
		
		tester.startComponentInPage(list);
		
		tester.executeBehavior(behavior);
		assertTrue(tester.getLastResponseAsString().contains("Wicket.DOM.add(Wicket.DOM.get('container2'), '<div wicket:id=\\\"repeater\\\" id=\\\"id13\\\"/>');"));

		tester.executeBehavior(behavior);
		assertTrue(tester.getLastResponseAsString().contains("Wicket.DOM.remove(Wicket.DOM.get('id13'));"));
	}
}
