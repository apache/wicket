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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.threadtest.tester.SimpleGetCommand;
import org.apache.wicket.threadtest.tester.Tester;
import org.apache.wicket.util.io.WicketObjectStreamFactory;
import org.apache.wicket.util.lang.Objects;


/**
 * @author eelcohillenius
 */
public class App1Test1 {

	private static final Log log = LogFactory.getLog(App1Test1.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		List<String> gets = Arrays.asList(new String[] {
				"/app1/?org.apache.wicket:bookmarkablePage=:org.apache.wicket.threadtest.apps.app1.Home",
				"/app1/?org.apache.wicket:interface=:${iteration}:link::ILinkListener:",
				"/app1/?org.apache.wicket:interface=:${iteration}:link:1:ILinkListener:",
				"/app1/?org.apache.wicket:interface=:${iteration}:link:2:ILinkListener:" });

		// you can turn this on if you e.g. want to attach to a profiler
		// Thread.sleep(5000);

		Objects.setObjectStreamFactory(new WicketObjectStreamFactory());
		SimpleGetCommand getCmd = new SimpleGetCommand(gets, 10);
		// getCmd.setPrintResponse(true);
		Tester tester = new Tester(getCmd, 1000, true);
		tester.run();
	}
}
