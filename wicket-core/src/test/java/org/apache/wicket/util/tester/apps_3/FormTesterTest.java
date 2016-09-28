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
package org.apache.wicket.util.tester.apps_3;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.apps_1.Book;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Ingram Chen
 */
public class FormTesterTest extends WicketTestCase
{
	private Book[] books;

	private ChoicePage choicePage;

	private FormTester formTester;

	/**
	 * 
	 */
	@Before
	public void before()
	{
		books = new Book[] { new Book("1", "book1"), new Book("2", "book2"),
				new Book("3", "book3"), new Book("4", "book4") };

		choicePage = tester.startPage(new ChoicePage(Arrays.asList(books)));
		formTester = tester.newFormTester("choiceForm");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void singleChoice() throws Exception
	{
		assertSame(books[1], choicePage.dropDownChoice);
		assertSame(books[3], choicePage.listChoice);
		assertSame(books[2], choicePage.radioChoice);
		assertSame(null, choicePage.radioGroup);
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
	@Test
	public void singleChoice_toggle() throws Exception
	{
		assertSame(books[1], choicePage.dropDownChoice);
		assertSame(null, choicePage.radioGroup);
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
	@Test
	public void singleChoiceComponentNotAllowSelectMuliple() throws Exception
	{
		try
		{
			formTester.selectMultiple("dropDownChoice", new int[] { 0 });
			throw new RuntimeException("WicketRuntimeException expected");
		}
		catch (WicketRuntimeException expected)
		{
		}

		try
		{
			formTester.selectMultiple("radioGroup", new int[] { 2, 1 });
			throw new RuntimeException("WicketRuntimeException expected");
		}
		catch (WicketRuntimeException expected)
		{
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void selectMultiple() throws Exception
	{
		assertBooksEquals(new Book[0], choicePage.listMultipleChoice);
		assertBooksEquals(new Book[0], choicePage.checkBoxMultipleChoice);
		assertBooksEquals(new Book[0], choicePage.checkGroup);
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
	@Test
	public void multipleChoiceComponent_cumulate() throws Exception
	{
		assertBooksEquals(new Book[0], choicePage.listMultipleChoice);
		assertBooksEquals(new Book[0], choicePage.checkGroup);
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

	private void assertBooksEquals(Book[] expectBooks, List<Book> actualBooks)
	{
		assertEquals(expectBooks.length, actualBooks.size());
		assertTrue(Arrays.asList(expectBooks).containsAll(actualBooks));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void multipleButtonSubmit() throws Exception
	{
		formTester.submit();

		assertFalse(choicePage.buttonPressed);
		assertFalse(choicePage.anotherButtonPressed);

		formTester = tester.newFormTester("choiceForm");
		formTester.submit("anotherButton");

		assertFalse(choicePage.buttonPressed);
		assertTrue(choicePage.anotherButtonPressed);
	}

	/**
	 * Tests proper initialization.
	 */
	@Test
	public void initialValues()
	{
		assertInitialValues();
		formTester.submit();
		assertInitialValues();
	}

	private void assertInitialValues()
	{
		assertSame(books[1], choicePage.dropDownChoice);
		assertSame(books[3], choicePage.listChoice);
		assertSame(books[2], choicePage.radioChoice);
		assertEquals(true, choicePage.checkBox);
		assertBooksEquals(new Book[] { books[2], books[1] }, choicePage.initialListMultipleChoice);
		assertBooksEquals(new Book[] { books[3], books[0] },
			choicePage.initialCheckBoxMultipleChoice);
		assertBooksEquals(new Book[] { books[3], books[2] }, choicePage.initialCheckGroup);
	}
}
