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
package org.apache.wicket.markup.html.form;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

/**
 * Test case for https://issues.apache.org/jira/browse/WICKET-5883
 */
public class FormValidationDisabledFormComponentTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();

				// make feedback messages not to disappear after page refresh (F5)
				getApplicationSettings().setFeedbackMessageCleanupFilter(IFeedbackMessageFilter.NONE);
			}
		};
	}

	@Override
	protected WicketTester newWicketTester(WebApplication app)
	{
		return new WicketTester(app)
		{
			@Override
			public void clearFeedbackMessages()
			{
				// preserve the configured filter
				cleanupFeedbackMessages(getApplication().getApplicationSettings().getFeedbackMessageCleanupFilter());
			}
		};
	}

	@Test
	public void formSubmitsEvenWithInvalidButInvisibleFormComponent()
	{
		TestPage page = tester.startPage(TestPage.class);
		assertFalse(page.onSubmitCalled.get());
		assertFalse(page.onErrorCalled.get());

		FormTester formTester = tester.newFormTester(page.form.getPageRelativePath());
		formTester.submit();
		assertTrue(page.field1.hasErrorMessage());
		assertFalse(page.onSubmitCalled.get());
		assertTrue(page.onErrorCalled.get());
		page.onErrorCalled.set(false);

		page.field1.setVisible(false);
		formTester = tester.newFormTester(page.form.getPageRelativePath());
		formTester.submit();
		assertTrue(page.field1.hasErrorMessage());
		assertTrue(page.onSubmitCalled.get());
		assertFalse(page.onErrorCalled.get());
	}

	@Test
	public void formSubmitsEvenWithInvalidButDisabledFormComponent()
	{
		TestPage page = tester.startPage(TestPage.class);
		assertFalse(page.onSubmitCalled.get());
		assertFalse(page.onErrorCalled.get());

		FormTester formTester = tester.newFormTester(page.form.getPageRelativePath());
		formTester.submit();
		assertTrue(page.field1.hasErrorMessage());
		assertFalse(page.onSubmitCalled.get());
		assertTrue(page.onErrorCalled.get());
		page.onErrorCalled.set(false);

		page.field1.setEnabled(false);
		formTester = tester.newFormTester(page.form.getPageRelativePath());
		formTester.submit();
		assertTrue(page.field1.hasErrorMessage());
		assertTrue(page.onSubmitCalled.get());
		assertFalse(page.onErrorCalled.get());
	}

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public final AtomicBoolean onSubmitCalled = new AtomicBoolean(false);
		public final AtomicBoolean onErrorCalled = new AtomicBoolean(false);
		public final TextField field1;
		public final Form form;

		public TestPage()
		{
			form = new Form("form")
			{
				@Override
				protected void onSubmit()
				{
					super.onSubmit();
					onSubmitCalled.set(true);
				}

				@Override
				protected void onError()
				{
					super.onError();
					onErrorCalled.set(true);
				}
			};
			add(form);
			form.add(field1 = new TextField<String>("field1", Model.of("")));
			field1.setRequired(true);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
		                                               Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>"//
					+ "<form wicket:id='form'><input wicket:id='field1' type='text'/></form>" //
					+ "</body></html>");
		}
	}

}
