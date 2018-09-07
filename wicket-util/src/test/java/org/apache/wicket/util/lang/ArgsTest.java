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

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link Args}
 */
public class ArgsTest
{


	/**
	 * Test for {@link Args#notEmpty(java.util.Collection, String, Object...)}
	 */
	@Test
	public void notNullCollection()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			Args.notEmpty((Collection<?>)null, "col");
		});

	}

	/**
	 * Test for {@link Args#notEmpty(java.util.Collection, String, Object...)}
	 */
	@Test
	public void notEmptyCollection()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			Args.notEmpty(Collections.emptySet(), "col");
		});
	}

}
