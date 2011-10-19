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
package org.apache.wicket.util.lang;

import java.util.Collection;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@link Args}
 */
public class ArgsTest
{
	/**
	 * A rule for expecting exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Test for {@link Args#notEmpty(java.util.Collection, String, Object...)}
	 */
	@Test
	public void notNullCollection()
	{
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Collection 'col' may not be null or empty");

		Args.notEmpty((Collection<?>)null, "col");
	}

	/**
	 * Test for {@link Args#notEmpty(java.util.Collection, String, Object...)}
	 */
	@Test
	public void notEmptyCollection()
	{
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Collection 'col' may not be null or empty");

		Args.notEmpty(Collections.emptySet(), "col");
	}

}
