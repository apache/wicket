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

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.junit.Test;

/**
 * Test the dynamic prefix/suffix feature introduced with
 * https://issues.apache.org/jira/browse/WICKET-3478
 * 
 * @author Carl-Eric Menzel <cmenzel@wicketbuch.de> <carl-eric.menzel@c1-setcon.de>
 */
public class CheckBoxMultipleChoiceTest extends WicketTestCase
{
	/** */
	public static class TestPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param show1
		 * @param show2
		 * @param show3
		 */
		public TestPage(final boolean show1, final boolean show2, final boolean show3, boolean show4)
		{
			List<? extends String> choices = Arrays.asList("a", "b", "c");
			add(new CheckBoxMultipleChoice<String>("checkWithoutPrefix", choices)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible()
				{
					return show1;
				}
			});
			add(new CheckBoxMultipleChoice<String>("checkWithFixedPrefix", choices)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible()
				{
					return show2;
				}
			}.setPrefix("pre").setSuffix("suf"));
			add(new CheckBoxMultipleChoice<String>("checkWithDynamicPrefix", choices)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible()
				{
					return show3;
				}

				@Override
				protected String getPrefix(int index, String choice)
				{
					return "pre" + index + choice;
				}

				@Override
				protected String getSuffix(int index, String choice)
				{
					return "suf" + index + choice;
				}
			});

			WebMarkupContainer container = new WebMarkupContainer("container");
			container.setVisibilityAllowed(show4);
			container.setEnabled(false);
			add(container);
			container.add(new CheckBoxMultipleChoice<String>("disabled", choices));
		}

		@Override
		public IMarkupFragment getMarkup()
		{
			return Markup.of("<html><body>" //
				+ "<div wicket:id='checkWithoutPrefix'></div>" //
				+ "<div wicket:id='checkWithFixedPrefix'></div>" //
				+ "<div wicket:id='checkWithDynamicPrefix'></div>" //
				+ "<div wicket:id='container'><div wicket:id='disabled'></div></div>" //
				+ "</body></html>");
		}
	}

	/** */
	@Test
	public void noPrefix()
	{
		tester.startPage(new TestPage(true, false, false, false));
		tester.assertContains("<div wicket:id=\"checkWithoutPrefix\"><input name=\"checkWithoutPrefix\"");
	}

	/** */
	@Test
	public void fixedPrefix()
	{
		tester.startPage(new TestPage(false, true, false, false));
		tester.assertContains("<div wicket:id=\"checkWithFixedPrefix\">pre<input name=\"checkWithFixedPrefix\"");
		tester.assertContains("</label>sufpre<input name=\"checkWithFixedPrefix\"");
		tester.assertContains("</label>suf</div>");
	}

	/** */
	@Test
	public void dynamicPrefix()
	{
		tester.startPage(new TestPage(false, false, true, false));
		tester.assertContains("<div wicket:id=\"checkWithDynamicPrefix\">pre0a<input name=\"checkWithDynamicPrefix\"");
		tester.assertContains("</label>suf0apre1b<input name=\"checkWithDynamicPrefix\"");
		tester.assertContains("</label>suf2c</div>");
	}

	@Test
	public void disabledInHierarchy()
	{
		tester.startPage(new TestPage(false, false, false, true));
		tester.assertContains("disabled=\"disabled\"");
	}

	@Test
	public void defaultLabelPositionIsAfter() throws Exception
	{
		CheckBoxMultipleChoice<Integer> radioChoice = new CheckBoxMultipleChoice<Integer>("testid", Arrays.asList(1));
		tester.startComponentInPage(radioChoice);

		tester.assertResultPage("<span wicket:id=\"testid\"><input name=\"testid\" type=\"checkbox\" value=\"0\" id=\"testid1-testid_0\"/><label for=\"testid1-testid_0\">1</label></span>");
	}

	@Test
	public void labelPositionBefore() throws Exception
	{
		CheckBoxMultipleChoice<Integer> radioChoice = new CheckBoxMultipleChoice<Integer>("testid", Arrays.asList(1));
		radioChoice.setLabelPosition(AbstractChoice.LabelPosition.BEFORE);
		tester.startComponentInPage(radioChoice);

		tester.assertResultPage("<span wicket:id=\"testid\"><label for=\"testid1-testid_0\">1</label><input name=\"testid\" type=\"checkbox\" value=\"0\" id=\"testid1-testid_0\"/></span>");
	}

	@Test
	public void labelPositionWrapBefore() throws Exception
	{
		CheckBoxMultipleChoice<Integer> radioChoice = new CheckBoxMultipleChoice<Integer>("testid", Arrays.asList(1));
		radioChoice.setLabelPosition(AbstractChoice.LabelPosition.WRAP_BEFORE);
		tester.startComponentInPage(radioChoice);

		tester.assertResultPage("<span wicket:id=\"testid\"><label>1 <input name=\"testid\" type=\"checkbox\" value=\"0\" id=\"testid1-testid_0\"/></label></span>");
	}

	@Test
	public void labelPositionWrapAfter() throws Exception
	{
		CheckBoxMultipleChoice<Integer> radioChoice = new CheckBoxMultipleChoice<Integer>("testid", Arrays.asList(1));
		radioChoice.setLabelPosition(AbstractChoice.LabelPosition.WRAP_AFTER);
		tester.startComponentInPage(radioChoice);

		tester.assertResultPage("<span wicket:id=\"testid\"><label><input name=\"testid\" type=\"checkbox\" value=\"0\" id=\"testid1-testid_0\"/> 1</label></span>");
	}
}
