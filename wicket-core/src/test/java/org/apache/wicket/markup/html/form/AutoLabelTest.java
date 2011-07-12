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

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

/**
 * Tests {@code wicket:for} attribute functionality
 * 
 * @author igor
 */
public class AutoLabelTest extends WicketTestCase
{
	/** */
	public void testLabelIntoMarkupInsertion()
	{
		class MyTestPage extends TestPage
		{
			private static final long serialVersionUID = 1L;

			public MyTestPage(String labelMarkup)
			{
				super("<label wicket:for='t'>" + labelMarkup + "</label>");
				field.setLabel(Model.of("t"));
			}
		}

		// simple insertion
		assertRendered(new MyTestPage("<span class='label-text'>text</span>"),
			"<span class='label-text'>t</span>");

		// preserves markup before and after
		assertRendered(new MyTestPage(" <div> a </div> <span class='label-text'>text</span> b "),
			" <div> a </div> <span class='label-text'>t</span> b ");

		// embedded span tags
		assertRendered(
			new MyTestPage(" a <div> b <span class='label-text'>text</span> c </div> d"),
			" a <div> b <span class='label-text'>t</span> c </div> d");

		// double text span tags - only the first one is touched
		assertRendered(new MyTestPage(
			"<span class='label-text'>text</span><span class='label-text'>text</span>"),
			"<span class='label-text'>t</span><span class='label-text'>text</span>");

		// no span - no insertion
		assertRendered(new MyTestPage(" text "), " text ");

		// empty label tag
		assertRendered(new MyTestPage(""), "></label>");

		// empty span tag
		assertRendered(new MyTestPage("<span class='label-text'></span>"),
			"<span class='label-text'>t</span>");

		// open/close span tag
		assertRendered(new MyTestPage("<span class='label-text'/>"),
			"<span class='label-text'>t</span>");

		// test additional classes on the span are preserved
		assertRendered(new MyTestPage("<span class='foo label-text bar'/>"),
			"<span class='foo label-text bar'>t</span>");
	}

	/** */
	public void testMarkupIntoLabelInsertion()
	{
		class MyTestPage extends TestPage
		{
			private static final long serialVersionUID = 1L;

			public MyTestPage(String labelMarkup)
			{
				super("<label wicket:for='t'>" + labelMarkup + "</label>");
			}
		}

		// test form component label is defaulted to the contents of span class='label-text'

		MyTestPage page = new MyTestPage("<span class='label-text'>text</span>");
		tester.startPage(page);
		assertEquals("text", ((MyTestPage)tester.getLastRenderedPage()).field.getLabel()
			.getObject());
	}

	/** */
	public void testLabelTagClasses()
	{
		class MyTestPage extends TestPage
		{
			private static final long serialVersionUID = 1L;

			public MyTestPage()
			{
				super("<label wicket:for='t'><span class='label-text'>field</span></label>");
			}
		}

		class MyErrorTestPage extends MyTestPage
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				field.error("too short");
			}
		}

		// test required class
		TestPage page = new MyTestPage();
		assertNotRendered(page, "class='required'");
		page.field.setRequired(true);
		assertRendered(page, "class='required'");

		// test error class
		page = new MyTestPage();
		assertNotRendered(page, "class='error'");
		page = new MyErrorTestPage();
		assertRendered(page, "class='error'");

		// test classes are appended and not overridden
		page = new MyErrorTestPage();
		page.field.setRequired(true);
		tester.startPage(page);
		String markup = tester.getLastResponse().getDocument();
		assertTrue(markup.contains("class=\"required error\"") ||
			markup.contains("class=\"error required\""));

		// test existing classes are preserved
		class MyTestPage2 extends TestPage
		{
			private static final long serialVersionUID = 1L;

			public MyTestPage2()
			{
				super(
					"<label class='long' wicket:for='t'><span class='label-text'>field</span></label>");
			}
		}

		MyTestPage2 page2 = new MyTestPage2();
		page2.field.setRequired(true);
		tester.startPage(page2);
		markup = tester.getLastResponse().getDocument();
		assertTrue(markup.contains("class=\"required long\"") ||
			markup.contains("class=\"long required\""));

	}

	private void assertRendered(Page page, String markupFragment)
	{
		tester.startPage(page);
		String markup = tester.getLastResponse().getDocument();
		markup = markup.replace("'", "\"");
		assertTrue("fragment: [" + markupFragment + "] not found in generated markup: [" + markup +
			"]", markup.contains(markupFragment.replace("'", "\"")));
	}

	private void assertNotRendered(Page page, String markupFragment)
	{
		tester.startPage(page);
		String markup = tester.getLastResponse().getDocument();
		markup = markup.replace("'", "\"");
		assertFalse("fragment: [" + markupFragment + "] not found in generated markup: [" + markup +
			"]", markup.contains(markupFragment.replace("'", "\"")));
	}

	private static class TestPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		TextField<String> field;

		private final String labelMarkup;

		public TestPage(String labelMarkup)
		{
			this.labelMarkup = labelMarkup;
			Form<?> form = new Form<Void>("f");
			add(form);
			form.add(field = new TextField<String>("t", Model.of("")));
		}

		@Override
		public IMarkupFragment getMarkup()
		{
			return Markup.of("<html><body><form wicket:id='f'>\n" + labelMarkup +
				"\n<input type='text' wicket:id='t'/>\n</form></body></html>");
		}
	}
}