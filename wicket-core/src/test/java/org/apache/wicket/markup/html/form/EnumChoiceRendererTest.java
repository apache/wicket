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
package org.apache.wicket.markup.html.form;

import org.junit.Test;

/**
 * Tests {@link EnumChoiceRenderer}
 * 
 * @author igor.vaynberg
 */
public class EnumChoiceRendererTest extends org.junit.Assert
{

	/**
	 * 
	 */
	@Test
	public void testResourceKeyGenerationForAnonymousEnums()
	{
		final EnumChoiceRenderer<TestEnum> renderer = new EnumChoiceRenderer<TestEnum>();
		assertEquals("TestEnum.ANONYMOUS", renderer.resourceKey(TestEnum.ANONYMOUS));
	}

	/**
	 * Enum for testing
	 * 
	 * @author igor.vaynberg
	 */
	public enum TestEnum {
		/** an anonymous enum value */
		ANONYMOUS {
		// anonymous enum value
		}
	}

}
