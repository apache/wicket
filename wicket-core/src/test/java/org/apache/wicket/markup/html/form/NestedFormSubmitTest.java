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

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NestedFormSubmitTest extends WicketTestCase
{

	public class TestForm<S> extends Form<S>
	{
		private final IModel<Boolean> submitted;
		private final Button submit;
		private final boolean wantInclusion;
		private final boolean wantExclusion;

		TestForm(String id, IModel<Boolean> submitted, boolean wantInclusion, boolean wantExclusion)
		{
			super(id);
			this.submitted = submitted;
			this.wantInclusion = wantInclusion;
			this.wantExclusion = wantExclusion;

			submit = new Button("submit");
			add(submit);
		}

		@Override
		protected void onSubmit()
		{
			super.onSubmit();
			submitted.setObject(Boolean.TRUE);
		}

		@Override
		public boolean wantSubmitOnNestedFormSubmit()
		{
			return wantInclusion;
		}

		@Override
		public boolean wantSubmitOnParentFormSubmit()
		{
			return wantExclusion;
		}
	}

	public class TestPage extends WebPage
	{
		private final TestForm<?> outer;
		private final TestForm<?> middle;
		private final TestForm<?> inner;

		public TestPage(IModel<Boolean> submittedOuter, boolean outerWantsInclusion,
			IModel<Boolean> submittedMiddle, boolean middleWantsInclusion,
			boolean middleWantsExclusion, IModel<Boolean> submittedInner)
		{
			outer = new TestForm<Void>("outer", submittedOuter, outerWantsInclusion, true);
			this.add(outer);
			middle = new TestForm<Void>("middle", submittedMiddle, middleWantsInclusion,
				middleWantsExclusion);
			outer.add(middle);
			inner = new TestForm<Void>("inner", submittedInner, false, true);
			middle.add(inner);
		}
	}

	private Model<Boolean> submittedOuter;
	private Model<Boolean> submittedMiddle;
	private Model<Boolean> submittedInner;
	private TestPage page;

	@Before
	public void setUp() throws Exception
	{
		submittedOuter = Model.of(false);
		submittedMiddle = Model.of(false);
		submittedInner = Model.of(false);
	}

	@After
	public void tearDown() throws Exception
	{
		submittedInner.setObject(false);
		submittedMiddle.setObject(false);
		submittedOuter.setObject(false);
	}

	private void assertFormSubmitOuter(boolean expectSubmittedOuter, boolean expectSubmittedMiddle,
		boolean expectSubmittedInner)
	{
		FormTester form = tester.newFormTester(page.outer.getPageRelativePath());
		form.submit("submit");
		assertFormsAreSubmitted(expectSubmittedOuter, expectSubmittedMiddle, expectSubmittedInner);
	}

	private void assertFormSubmitMiddle(boolean expectSubmittedOuter,
		boolean expectSubmittedMiddle, boolean expectSubmittedInner)
	{
		FormTester form = tester.newFormTester(page.outer.getPageRelativePath());
		form.submit("middle:submit");
		assertFormsAreSubmitted(expectSubmittedOuter, expectSubmittedMiddle, expectSubmittedInner);
	}

	private void assertFormSubmitInner(boolean expectSubmittedOuter, boolean expectSubmittedMiddle,
		boolean expectSubmittedInner)
	{
		FormTester form = tester.newFormTester(page.outer.getPageRelativePath());
		form.submit("middle:inner:submit");
		assertFormsAreSubmitted(expectSubmittedOuter, expectSubmittedMiddle, expectSubmittedInner);
	}

	private void assertFormsAreSubmitted(boolean expectSubmittedOuter,
		boolean expectSubmittedMiddle, boolean expectSubmittedInner)
	{
		assertEquals("outer", expectSubmittedOuter, submittedOuter.getObject().booleanValue());
		assertEquals("middle", expectSubmittedMiddle, submittedMiddle.getObject().booleanValue());
		assertEquals("inner", expectSubmittedInner, submittedInner.getObject().booleanValue());
	}

	@Test
	public void testDefaultOuterSubmitShouldSubmitAll() throws Exception
	{
		startPage(false, false, true);
		assertFormSubmitOuter(true, true, true);
	}

	@Test
	public void testDefaultMiddleSubmitShouldSubmitMiddleAndInner() throws Exception
	{
		startPage(false, false, true);
		assertFormSubmitMiddle(false, true, true);
	}

	@Test
	public void testDefaultInnerSubmitShouldSubmitOnlyInner() throws Exception
	{
		startPage(false, false, true);
		assertFormSubmitInner(false, false, true);
	}

	@Test
	public void testWithOuterInclusionOuterIsSubmittedOnMiddleSubmit() throws Exception
	{
		startPage(true, false, true);
		assertFormSubmitMiddle(true, true, true);
	}

	@Test
	public void testWithOuterInclusionOuterIsSubmittedOnInnerSubmit() throws Exception
	{
		startPage(true, false, true);
		assertFormSubmitInner(true, true, true);
	}

	@Test
	public void testWithMiddleInclusionMiddleIsSubmittedOnInnerSubmit() throws Exception
	{
		startPage(false, true, true);
		assertFormSubmitInner(false, true, true);
	}

	@Test
	public void testWithMiddleAndOuterInclusionMiddleAndOuterIsSubmittedOnInnerSubmit()
		throws Exception
	{
		startPage(true, true, true);
		assertFormSubmitInner(true, true, true);
	}

	@Test
	public void testWithMiddleExclusionAndOuterIsSubmitted() throws Exception
	{
		startPage(false, false, false);
		assertFormSubmitOuter(true, false, false);
	}

	private void startPage(boolean outerWantsInclusion, boolean middleWantsInclusion,
		boolean middleWantsExclusion)
	{
		page = (TestPage)tester.startPage(new TestPage(submittedOuter, outerWantsInclusion,
			submittedMiddle, middleWantsInclusion, middleWantsExclusion, submittedInner));
	}
}
