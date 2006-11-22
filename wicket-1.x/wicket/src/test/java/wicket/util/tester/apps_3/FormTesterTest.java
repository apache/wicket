/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision$ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.util.tester.apps_3;

import java.util.Arrays;
import java.util.List;

import junit.framework.AssertionFailedError;
import wicket.Page;
import wicket.WicketTestCase;
import wicket.util.tester.FormTester;
import wicket.util.tester.ITestPageSource;
import wicket.util.tester.apps_1.Book;

/**
 * @author Ingram Chen
 */
public class FormTesterTest extends WicketTestCase
{
	private Book[] books;

	private ChoicePage choicePage;

	private FormTester formTester;

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public FormTesterTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		super.setUp();

		books = new Book[] { new Book("1", "book1"), new Book("2", "book2"),
				new Book("3", "book3"), new Book("4", "book4") };

		choicePage = (ChoicePage)application.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return new ChoicePage(Arrays.asList(books));
			}
		});
		formTester = application.newFormTester("choiceForm");
	}

	/**
	 * @throws Exception
	 */
	public void testSingleChoice() throws Exception
	{
		formTester.select("dropDownChoice", 0);
		formTester.select("listChoice", 2);
		formTester.select("radioChoice", 1);
		formTester.select("radioGroup", 3);
		formTester.submit();
		assertSame(books[0], choicePage.dropDownChoice);
		assertSame(books[2], choicePage.listChoice);
		assertSame(books[1], choicePage.radioChoice);
		assertSame(books[3], choicePage.radioGroup);
	}

	/**
	 * @throws Exception
	 */
	public void testSingleChoice_toggle() throws Exception
	{
		formTester.select("dropDownChoice", 0);
		formTester.select("dropDownChoice", 1);// toggle to 1
		formTester.select("radioGroup", 3);
		formTester.select("radioGroup", 2);// toggle to 2
		formTester.submit();
		assertSame(books[1], choicePage.dropDownChoice);
		assertSame(books[2], choicePage.radioGroup);
	}

	/**
	 * @throws Exception
	 */
	public void testSingleChoiceComponentNotAllowSelectMuliple() throws Exception
	{
		try
		{
			formTester.selectMultiple("dropDownChoice", new int[] { 0 });
			throw new RuntimeException("AssertionFailedError expected");
		}
		catch (AssertionFailedError expected)
		{
		}

		try
		{
			formTester.selectMultiple("radioGroup", new int[] { 2, 1 });
			throw new RuntimeException("AssertionFailedError expected");
		}
		catch (AssertionFailedError expected)
		{
		}
	}

	/**
	 * @throws Exception
	 */
	public void testSelectMultiple() throws Exception
	{
		formTester.selectMultiple("listMultipleChoice", new int[] { 0, 3 });
		formTester.selectMultiple("checkBoxMultipleChoice", new int[] { 1, 0, 3 });
		formTester.selectMultiple("checkGroup", new int[] { 0, 1, 2, 3 });
		formTester.submit();

		assertBooksEquals(new Book[] { books[0], books[3] }, choicePage.listMultipleChoice);
		assertBooksEquals(new Book[] { books[0], books[1], books[3] },
				choicePage.checkBoxMultipleChoice);
		assertBooksEquals(books, choicePage.checkGroup);
	}

	/**
	 * @throws Exception
	 */
	public void testMultipleChoiceComponent_cumulate() throws Exception
	{
		formTester.select("listMultipleChoice", 0);
		formTester.selectMultiple("listMultipleChoice", new int[] { 0, 3 });
		formTester.selectMultiple("listMultipleChoice", new int[] { 1 });

		formTester.selectMultiple("checkGroup", new int[] { 2 });
		formTester.selectMultiple("checkGroup", new int[] { 2, 3 });
		formTester.select("checkGroup", 0);
		formTester.submit();

		assertBooksEquals(new Book[] { books[0], books[1], books[3] },
				choicePage.listMultipleChoice);
		assertBooksEquals(new Book[] { books[0], books[2], books[3] }, choicePage.checkGroup);
	}

	private void assertBooksEquals(Book[] expectBooks, List actualBooks)
	{
		assertEquals(expectBooks.length, actualBooks.size());
		assertTrue(Arrays.asList(expectBooks).containsAll(actualBooks));
	}

	/**
	 * @throws Exception
	 */
	public void testMultipleButtonSubmit() throws Exception
	{
		formTester.submit();
		assertFalse(choicePage.anotherButtonPressed);

		formTester = application.newFormTester("choiceForm");
		formTester.submit("anotherButton");
		assertTrue(choicePage.anotherButtonPressed);
	}
}
