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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.atmosphere.tester.AtmosphereTester;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class AtmosphereTesterTest extends Assert
{
	final AtomicBoolean updateTimeCalled = new AtomicBoolean(false);
	final AtomicBoolean receiveMessageCalled = new AtomicBoolean(false);

	@Test
	public void atmospherePush()
	{
		final String updateTimeIsExecuted = "updateTime is executed!";

		WicketTester tester = new WicketTester();
		HomePage page = new HomePage(new PageParameters())
		{
			@Subscribe
			public void updateTime(AjaxRequestTarget target, Date event)
			{
				super.updateTime(target, event);

				updateTimeCalled.set(true);

				target.appendJavaScript(updateTimeIsExecuted);
			}

			@Subscribe(contextAwareFilter = ReceiverFilter.class)
			public void receiveMessage(AjaxRequestTarget target, ChatMessage message)
			{
				super.receiveMessage(target, message);
				receiveMessageCalled.set(true);
			}
		};

		AtmosphereTester waTester = new AtmosphereTester(tester, page);

		assertThat(updateTimeCalled.get(), is(false));
		assertThat(receiveMessageCalled.get(), is(false));

		Date payload = new Date();
		waTester.post(payload);

		assertThat(updateTimeCalled.get(), is(true));
		assertThat(receiveMessageCalled.get(), is(false));

		tester.assertContains(updateTimeIsExecuted);

		final FormTester form = tester.newFormTester("form");

		form.setValue("input", "Atmosphere rocks!");

		form.submit("send");

		assertThat(updateTimeCalled.get(), is(true));
		assertThat(receiveMessageCalled.get(), is(true));

		// get the the collected so far content of the suspended response
		// Note: it may contain several <ajax-response>s.
		// use waTester.resetResponse() to remove the collected data
		String atmosphereResponse = waTester.getPushedResponse();
//		System.out.println("RES:" + atmosphereResponse);

		// assert
		assertThat(atmosphereResponse,
				is(not(equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ajax-response></ajax-response>"))));

		waTester.switchOnTestMode();
		// now the assertions are against the Atmosphere's suspended response data
		tester.assertComponentOnAjaxResponse("message");
		waTester.switchOffTestMode();
		// now the assertions will be the real last response

		tester.assertLabel("message", "Atmosphere rocks!");

		tester.destroy();
	}
}
