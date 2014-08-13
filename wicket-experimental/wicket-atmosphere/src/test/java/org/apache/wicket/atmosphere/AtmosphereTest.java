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
package org.apache.wicket.atmosphere;

import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.atmosphere.tester.AtmosphereTester;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class AtmosphereTest extends Assert
{
	@Test
	public void atmospherePush()
	{
		WicketTester tester = new WicketTester();
		HomePage page = new HomePage(new PageParameters())
		{
			@Subscribe
			public void updateTime(AjaxRequestTarget target, Date event)
			{
				super.updateTime(target, event);

				System.err.println("updateTime");
				target.appendJavaScript("updateTime is executed");
			}

			@Subscribe(contextAwareFilter = ReceiverFilter.class)
			public void receiveMessage(AjaxRequestTarget target, ChatMessage message)
			{
				super.receiveMessage(target, message);

				System.err.println("receiveMessage");
			}
		};

		AtmosphereTester waTester = new AtmosphereTester(tester, page);

		Date payload = new Date();
		waTester.post(payload);

		System.err.println(tester.getLastResponseAsString());

		tester.destroy();
	}
}
