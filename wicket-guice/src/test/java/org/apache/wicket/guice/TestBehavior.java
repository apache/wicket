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
package org.apache.wicket.guice;

import com.google.inject.Inject;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.junit.Assert;

/**
 * A behavior that will be use injected services
 * 
 * https://issues.apache.org/jira/browse/WICKET-4149
 */
public class TestBehavior extends Behavior
{
	private static final long serialVersionUID = 1L;

	@Inject
	@Blue
	private ITestService injectedFieldBlue;

	@Override
	public void bind(Component component)
	{
		super.bind(component);

		Assert.assertNotNull(injectedFieldBlue);
		Assert.assertEquals("blue", injectedFieldBlue.getString());
	}

}
