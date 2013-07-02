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
package org.apache.wicket.cdi;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.cdi.testapp.TestAppScope;
import org.apache.wicket.cdi.testapp.TestApplication;
import org.apache.wicket.cdi.testapp.TestConversationBean;
import org.apache.wicket.util.tester.WicketTester;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.jglue.cdiunit.ContextController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * @author jsarman
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({BehaviorInjector.class,
		CdiConfiguration.class,
		CdiShutdownCleaner.class,
		ComponentInjector.class,
		ConversationExpiryChecker.class,
		ConversationPropagator.class,
		DetachEventEmitter.class,
		NonContextualManager.class,
		SessionInjector.class,
		MockContainer.class,
		TestAppScope.class,
		TestConversationBean.class})
public abstract class CdiBaseTest extends Assert
{

	WicketTester tester;
	@Inject
	ContextController contextController;

	boolean inited;

	@Before
	public void before()
	{
		if (autoInitializeTester())
		{
			initializeTest();
		}
	}

	@After
	public void after()
	{
		if (inited)
		{
			destroyTester();
		}
	}


	protected void initializeTest()
	{
		tester = new WicketTester(new TestApplication());
		prepareRequest(tester.getRequest());
		inited = true;
	}

	protected void destroyTester()
	{
		inited = false;
		tester.destroy();
		tester = null;
	}

	public void prepareRequest(HttpServletRequest request)
	{
		contextController.openRequest(request);
		contextController.openSession(request);
		contextController.openConversation(request);
	}

	public boolean autoInitializeTester()
	{
		return true;
	}
}
