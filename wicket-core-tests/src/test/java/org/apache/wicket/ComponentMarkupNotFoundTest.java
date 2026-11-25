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
package org.apache.wicket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests the message of the exception thrown when a component has no markup.
 */
class ComponentMarkupNotFoundTest extends WicketTestCase
{
	@Test
	void messageNamesPageAndPath()
	{
		MockPageWithOneComponent page = new MockPageWithOneComponent();
		page.add(new Label(MockPageWithOneComponent.COMPONENT_ID, "text"));
		tester.startPage(page);

		// the component is not in the page markup, like one added to an Ajax update
		Label missing = new Label("missing", "text");
		page.add(missing);

		MarkupNotFoundException e = assertThrows(MarkupNotFoundException.class, missing::render);

		assertThat(e.getMessage()).contains("Markup not found for Component")
			.contains("page: " + MockPageWithOneComponent.class.getName())
			.contains("path: missing");
	}
}
