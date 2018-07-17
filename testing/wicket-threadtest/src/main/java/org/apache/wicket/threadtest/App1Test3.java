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
import java.util.List;

import org.apache.wicket.threadtest.apps.app1.ResourceTestPage;
import org.apache.wicket.threadtest.tester.SimpleGetCommand;
import org.apache.wicket.threadtest.tester.Tester;

/**
 * @author almaw
 */
public class App1Test3
{
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
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
