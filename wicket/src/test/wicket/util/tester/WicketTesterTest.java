/*
 * $Id: WicketTesterTest.java 5336 2006-04-11 09:42:12 +0000 (Tue, 11 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-11 09:42:12 +0000 (Tue, 11 Apr
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.tester;

import java.util.Locale;

import junit.framework.TestCase;
import wicket.Component;
import wicket.MockPageWithLink;
import wicket.MockPageWithOneComponent;
import wicket.Page;
import wicket.Session;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.ClientEvent;
import wicket.ajax.markup.html.AjaxLink;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.util.tester.apps_1.Book;
import wicket.util.tester.apps_1.CreateBook;
import wicket.util.tester.apps_1.MyMockApplication;
import wicket.util.tester.apps_1.SuccessPage;
import wicket.util.tester.apps_1.ViewBook;



/**
 * 
 * @author Juergen Donnerstag
 */
public class WicketTesterTest extends TestCase
{
	private boolean eventExecuted;
		
	@Override
	protected void setUp() throws Exception
	{
		eventExecuted = false;
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testViewBook() throws Exception
	{
		MyMockApplication tester = new MyMockApplication();

		// for WebPage without default constructor, I define a TestPageSource to
		// let the page be instatiated lately.
		tester.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				Book mockBook = new Book("xxId", "xxName");
				return new ViewBook(mockBook);
			}
		});

		// assertion
		tester.assertRenderedPage(ViewBook.class);
		tester.assertLabel("id", "xxId");
		tester.assertLabel("name", "xxName");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testCreateBook_validateFail() throws Exception
	{
		MyMockApplication tester = new MyMockApplication();
		Session.get().setLocale(Locale.US); // fix locale
		tester.startPage(CreateBook.class);

		FormTester formTester = tester.newFormTester("createForm");

		formTester.setValue("id", "");
		formTester.setValue("name", "");
		formTester.submit();

		tester.assertRenderedPage(CreateBook.class);

		// assert error message from validation
		tester.assertErrorMessages(new String[] { "id is required", "name is required" });
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testCreateBook_validatePass() throws Exception
	{
		MyMockApplication tester = new MyMockApplication();
		tester.startPage(CreateBook.class);

		FormTester formTester = tester.newFormTester("createForm");

		formTester.setValue("id", "xxId");
		formTester.setValue("name", "xxName");
		formTester.submit();

		tester.assertRenderedPage(SuccessPage.class);

		// assert info message present.
		tester.assertInfoMessages(new String[] { "book 'xxName' created" });

		// assert previous page expired.
		// TODO Post 1.2: General: No longer a valid test
		// tester.assertExpirePreviousPage();
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testBookmarkableLink() throws Exception
	{
		MyMockApplication tester = new MyMockApplication();

		// for WebPage without default constructor, I define a TestPageSource to
		// let the page be instatiated lately.
		tester.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				Book mockBook = new Book("xxId", "xxName");
				return new ViewBook(mockBook);
			}
		});

		// assertion
		tester.assertRenderedPage(ViewBook.class);
		tester.clickLink("link");
		tester.assertRenderedPage(CreateBook.class);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testPageConstructor() throws Exception
	{
		MyMockApplication tester = new MyMockApplication();
		Book mockBook = new Book("xxId", "xxName");
		Page page = new ViewBook(mockBook);
		tester.startPage(page);

		// assertion
		tester.assertRenderedPage(ViewBook.class);
		tester.clickLink("link");
		tester.assertRenderedPage(CreateBook.class);
	}
	
	/**
	 * 
	 */
	public void testAssertComponentOnAjaxResponse()
	{
		// Start the tester
		WicketTester tester = new WicketTester();
		
		final Page page = new MockPageWithLink();
		AjaxLink ajaxLink = new AjaxLink(page, MockPageWithLink.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				// Replace the link with a normal Link
				Link link = new Link(page, MockPageWithLink.LINK_ID) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick()
					{
						// Do nothing
					}
				};
				link.setOutputMarkupId(true);
				
				target.addComponent(link);
			}
		};
		ajaxLink.setOutputMarkupId(true);
		
		tester.startPage(new ITestPageSource() {
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return page;
			}
		});
		
		
		// Click the link
		tester.clickLink(MockPageWithLink.LINK_ID);

		// The link must be a Link :)
		tester.assertComponent(MockPageWithLink.LINK_ID, Link.class);
		
		// Get the new link component
		Component component = tester.getComponentFromLastRenderedPage(MockPageWithLink.LINK_ID);
		
		// This must not fail
		tester.assertComponentOnAjaxResponse(component);
	}

	/**
	 * Test that the executeAjaxEvent on the WicketTester works.
	 */
	public void testExecuteAjaxEvent()
	{
		// Start the tester
		WicketTester tester = new WicketTester();

		// Setup mocks
		final MockPageWithOneComponent page = new MockPageWithOneComponent();

		Label label = new Label(page, "component", "Dblclick This To See Magick");
		label.add(new AjaxEventBehavior(ClientEvent.DBLCLICK)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				eventExecuted = true;
			}
		});

		// Start the page
		tester.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return page;
			}
		});

		// Execute the event
		tester.executeAjaxEvent(label, ClientEvent.DBLCLICK);

		assertTrue(eventExecuted);
	}
}
