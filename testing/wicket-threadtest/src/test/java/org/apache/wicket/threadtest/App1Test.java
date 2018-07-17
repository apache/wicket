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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.threadtest.apps.app1.ResourceTestPage;
import org.apache.wicket.threadtest.tester.SimpleGetCommand;
import org.apache.wicket.threadtest.tester.Tester;
import org.junit.Test;

/**
 * @author eelcohillenius
 */
public class App1Test
{

	@Test
	public void test1() throws Exception
	{

		List<String> gets = Arrays.asList(
			"/app1/wicket/bookmarkable/org.apache.wicket.threadtest.apps.app1.Home",
			"/app1/wicket/page?${id}-1.-link");

		// you can turn this on if you e.g. want to attach to a profiler
// Thread.sleep(5000);

		SimpleGetCommand getCmd = new SimpleGetCommand(gets, 10) {
			@Override
			protected String getValue(String name, int iteration) {
				if ("id".equals(name)) {
					// page id increases by one on each render
					return "" + (iteration * 2);
				}
				return super.getValue(name, iteration);
			}
		};
		// getCmd.setPrintResponse(true);
		Tester tester = new Tester(getCmd, 100, true);
		tester.run();
	}
	
	@Test
	public void test2() throws Exception
	{

		List<String> gets = Arrays.asList("/app1/wicket/bookmarkable/org.apache.wicket.threadtest.apps.app1.Home");

		SimpleGetCommand getCmd = new SimpleGetCommand(gets, 5);

		// getCmd.setPrintResponse(true);
		Tester tester = new Tester(getCmd, 100, false);
		tester.run();
	}
	
	@Test
	public void test3() throws Exception
	{

		List<String> gets = new ArrayList<String>();
		gets.add("/app1/wicket/bookmarkable/org.apache.wicket.threadtest.apps.app1.ResourceTestPage");
		for (int i = 0; i < ResourceTestPage.IMAGES_PER_PAGE; i++)
		{
			gets.add("/app1/wicket/page?${iteration}--listView-${iteration}-image");
		}

		SimpleGetCommand getCmd = new SimpleGetCommand(gets, 5);

		// getCmd.setPrintResponse(true);
		Tester tester = new Tester(getCmd, 100, false);
		tester.run();
	}
}
