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

import javax.enterprise.context.Conversation;
import javax.inject.Inject;

import org.apache.wicket.cdi.testapp.TestAppScope;
import org.apache.wicket.cdi.testapp.TestConversationBean;
import org.apache.wicket.cdi.util.tester.CdiWicketTester;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.After;
import org.junit.Assert;
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
		ConversationManager.class,
		DetachEventEmitter.class,
		NonContextualManager.class,
		SessionInjector.class,
		MockContainer.class,
		TestAppScope.class,
		TestConversationBean.class})
public abstract class CdiBaseTest extends Assert
{
	@Inject
	CdiWicketTester tester;

	@Inject
	Conversation conversation;

	@After
	public void end()
	{

		if (!conversation.isTransient())
		{
			conversation.end();
		}
	}

}
