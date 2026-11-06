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
package org.apache.wicket.islands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;

import org.apache.wicket.RuntimeConfigurationType;
import org.junit.jupiter.api.Test;

/**
 * Island calls in deployment mode.
 */
class IslandDeploymentModeTest extends IslandTestCase
{
	@Override
	protected RuntimeConfigurationType configurationType()
	{
		return RuntimeConfigurationType.DEPLOYMENT;
	}

	@Test
	@SuppressWarnings("unchecked")
	void failingHandlerDoesNotRevealDetails()
	{
		tester.startPage(IslandTestPage.class);

		Map<String, Object> error = error(call("boom", "1", null));

		assertEquals("error", error.get("kind"));
		assertFalse(((List<String>)error.get("messages")).get(0).contains("secret detail"));
	}
}
