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

import java.util.Map;

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.wicket.cdi.testapp.TestAppScope;
import org.apache.wicket.cdi.testapp.TestCdiApplication;
import org.apache.wicket.cdi.testapp.TestConversationBean;
import org.apache.wicket.cdi.util.tester.CdiWicketTester;
import org.apache.wicket.cdi.util.tester.FilterConfigProducer;
import org.apache.wicket.cdi.util.tester.TestCdiConfiguration;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * @author jsarman
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({
		CdiWicketTester.class,
		BehaviorInjector.class,
		TestCdiConfiguration.class,
		CdiShutdownCleaner.class,
		ComponentInjector.class,
		ConversationExpiryChecker.class,
		ConversationPropagator.class,
		ConversationManager.class,
		DetachEventEmitter.class,
		NonContextualManager.class,
		SessionInjector.class,
		MockCdiContainer.class,
		TestAppScope.class,
		TestConversationBean.class,
		FilterConfigProducer.class,
		TestCdiApplication.class,
		CdiWebApplicationFactory.class})
public abstract class WicketCdiTestCase extends Assert
{
	@Inject
	private Instance<CdiWicketTester> testers;

	private CdiWicketTester instantiatedTester;

	@Inject
	Conversation conversation;

	@Inject
	FilterConfigProducer filterConfigProducer;

	public CdiWicketTester getTester()
	{
		if (instantiatedTester == null)
		{
			instantiatedTester = testers.get();
		}
		return instantiatedTester;
	}

	public CdiWicketTester getTester(boolean newTest)
	{
		if (newTest)
		{
			return testers.get();
		}
		return getTester();
	}

	public CdiWicketTester getTester(Map<String, String> customParamters)
	{
		if (instantiatedTester != null)
		{
			throw new IllegalStateException("The Wicket Tester is already initialized.");
		}
		filterConfigProducer.addParameters(customParamters);
		return getTester();
	}

	public CdiWicketTester getTester(boolean newTest, Map<String, String> customParamters)
	{
		if (newTest)
		{
			filterConfigProducer.addParameters(customParamters);
			return testers.get();
		}
		return getTester(customParamters);
	}

	@Before
	public void init()
	{
		getTester();
	}

	@After
	public void end()
	{
		if (instantiatedTester != null)
		{
			if (!conversation.isTransient())
			{
				conversation.end();
			}
		}
	}

}
