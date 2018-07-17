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
package org.apache.wicket.threadtest;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.threadtest.tester.SimpleGetCommand;
import org.apache.wicket.threadtest.tester.Tester;
import org.junit.Test;

/**
 * @author eelcohillenius
 */
public class App2Test
{

	@Test
	public void test1() throws Exception
	{

		List<String> gets = Arrays.asList("/app2/wicket/bookmarkable/org.apache.wicket.threadtest.apps.app2.Home");

		SimpleGetCommand getCmd = new SimpleGetCommand(gets, 5);

		// getCmd.setPrintResponse(true);

		// AS OF OCTOBER 9 2006, THIS TYPICALLY RESULTS IN A DEADLOCK
		// FIXED now... keep this test
		Tester tester = new Tester(getCmd, 50, false);

		// new Tester(.., .., false) would not give a deadlock, as then
		// all threads point to seperate sessions

		tester.run();
	}
}
