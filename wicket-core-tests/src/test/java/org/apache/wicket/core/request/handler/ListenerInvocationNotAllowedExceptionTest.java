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
package org.apache.wicket.core.request.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

class ListenerInvocationNotAllowedExceptionTest
{
	@Test
	void smokeTest()
	{
		var tester = new WicketTester(new MockApplication());
		// Arrange
		var label = new Label("id", "Label");
		var cut = new ListenerInvocationNotAllowedException(label,
			new AttributeModifier("class", "test"), "no no no");
		// Act

		//Assert
		assertThat(cut.getMessage()).startsWith("no no noComponent: [Component id = id], Path: id, Behavior:");
	}

}