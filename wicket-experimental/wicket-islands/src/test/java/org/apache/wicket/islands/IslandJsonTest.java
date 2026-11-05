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

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link IslandJson}.
 */
class IslandJsonTest
{
	@Test
	void escapesWhatCouldEndOrCommentOutTheScriptElement()
	{
		assertEquals("{\"a\":\"\\u003c/script\\u003e\\u003c!-- \\u0026 \\u2028\\u2029\"}",
			IslandJson.escapeForScript("{\"a\":\"</script><!-- & \u2028\u2029\"}"));
	}

	@Test
	void leavesEverythingElseAlone()
	{
		String json = "{\"a\":[1,true,null,\"x\\\"y\"]}";
		assertEquals(json, IslandJson.escapeForScript(json));
	}
}
