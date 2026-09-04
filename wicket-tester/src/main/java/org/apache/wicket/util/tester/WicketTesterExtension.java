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
package org.apache.wicket.util.tester;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

/**
 * Manages {@link WicketTester} instance
 *
 * @author igor
 */
public class WicketTesterExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, TestExecutionExceptionHandler
{
	private WicketTester tester;

	/**
	 * Allows setup of the tester instance
	 *
	 * @return tester
	 */
	protected WicketTester create()
	{
		return new WicketTester();
	}

	/**
	 * Gets the tester instance.
	 *
	 * @return tester instance or {@code null} if called outside the rule's scope
	 */
	public WicketTester getTester()
	{
		return tester;
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) {
		tester = create();
	}

	@Override
	public void afterTestExecution(ExtensionContext context) {
		tester.destroy();
		tester = null;
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		tester.destroy();
		tester = null;

		throw throwable;
	}
}
