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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Manages {@link WicketTester} instance
 * 
 * @author igor
 */
public class WicketTesterScope implements TestRule
{
	private WicketTester tester;

	@Override
	public Statement apply(final Statement base, Description description)
	{
		return new Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				tester = new WicketTester();
				setup(tester);
				try
				{
					base.evaluate();
				}
				finally
				{
					tester.destroy();
					tester = null;
				}
			}
		};
	}

	/**
	 * Allows setup of the tester instance
	 * 
	 * @param tester
	 */
	protected void setup(WicketTester tester)
	{

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
}
