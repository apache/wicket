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
package org.apache.wicket.examples.islands.taskboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;

import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.islands.Island;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskBoardPageTest
{
	private WicketTester tester;

	private TaskBoardPage page;

	@BeforeEach
	void setUp()
	{
		tester = new WicketTester(new TaskBoardApplication());
		page = tester.startPage(TaskBoardPage.class);
	}

	@AfterEach
	void tearDown()
	{
		tester.destroy();
	}

	@Test
	void acceptedMoveUpdatesTheBoardAndTheSummary()
	{
		String response = call("move", move("c1", Column.REVIEW, 0, version()));

		assertTrue(response.contains("\"ok\":true"), response);
		assertTrue(response.contains("\"invalidate\":[\"throughput\"]"), response);
		tester.assertComponentOnAjaxResponse(page.get("summary"));
		tester.assertComponentOnAjaxResponse(page.get("board"));
		assertEquals("c1", board().columns().get(Column.REVIEW).get(0).id());
	}

	@Test
	void moveIntoAFullColumnIsRejected()
	{
		call("move", move("c1", Column.IN_PROGRESS, 0, version()));
		String response = call("move", move("c2", Column.IN_PROGRESS, 0, version()));

		assertTrue(response.contains("\"kind\":\"invalid\""), response);
		assertTrue(response.contains("allows at most 3 cards"), response);
		assertEquals(Column.TODO, columnOf("c2"));
	}

	@Test
	void moveOnAStaleBoardIsRejectedWithTheCurrentBoard()
	{
		long seen = version();
		tester.clickLink("teammate");

		String response = call("move", move("c2", Column.DONE, 0, seen));

		assertTrue(response.contains("A teammate changed the board"), response);
		tester.assertComponentOnAjaxResponse(page.get("board"));
		assertEquals(Column.TODO, columnOf("c2"));
	}

	@Test
	void openingACardOpensTheDialog()
	{
		call("open", "{\"cardId\":\"c1\"}");

		tester.assertComponentOnAjaxResponse(page.get("dialog"));
		assertTrue(tester.getLastResponseAsString().contains("Write the release notes"));
	}

	@Test
	void savedDescriptionIsSanitized()
	{
		call("open", "{\"cardId\":\"c1\"}");
		String form = "dialog:overlay:dialog:content:form";
		FormTester formTester = tester.newFormTester(form);
		formTester.setValue("title", "Release notes");
		formTester.setValue("description",
			"\"<p>ok<img src=x onerror=alert(1)></p><script>alert(2)</script>\"");
		formTester.setValue("due", "\"" + LocalDate.now().plusDays(1) + "\"");
		tester.executeAjaxEvent(form + ":save", "click");

		tester.assertNoErrorMessage();
		assertEquals("<p>ok</p>", board().findCard("c1").orElseThrow().description());
	}

	@Test
	void pastDueDateIsRefused()
	{
		call("open", "{\"cardId\":\"c1\"}");
		String form = "dialog:overlay:dialog:content:form";
		FormTester formTester = tester.newFormTester(form);
		formTester.setValue("due", "\"" + LocalDate.now().minusDays(1) + "\"");
		tester.executeAjaxEvent(form + ":save", "click");

		tester.assertErrorMessages("The due date cannot be in the past.");
	}

	@Test
	void sanitizerKeepsFormattingAndSafeLinks()
	{
		assertEquals("<p><b>bold</b> <a href=\"https://wicket.apache.org\" " +
			"rel=\"noopener noreferrer nofollow\">site</a> x</p>",
			Descriptions.sanitize("<p><b>bold</b> <a href=\"https://wicket.apache.org\">site</a> " +
				"<a href=\"javascript:alert(1)\">x</a></p>"));
		assertFalse(Descriptions.sanitize("<svg onload=alert(1)>").contains("onload"));
	}

	private String call(String name, String payload)
	{
		Island<?> board = (Island<?>)page.get("board");
		Url url = Url.parse(board.getBehaviors(AbstractAjaxBehavior.class).get(0).getCallbackUrl());
		tester.getRequest().setMethod("POST");
		tester.getRequest().getPostParameters().setParameterValue("island-call", name);
		tester.getRequest().getPostParameters().setParameterValue("island-call-id", "1");
		tester.getRequest().getPostParameters().setParameterValue("island-key", board.getKey());
		tester.getRequest().getPostParameters().setParameterValue("island-payload", payload);
		tester.executeAjaxUrl(url);
		return tester.getLastResponseAsString();
	}

	private static String move(String cardId, Column column, int index, long version)
	{
		return "{\"cardId\":\"" + cardId + "\",\"column\":\"" + column + "\",\"index\":" + index +
			",\"version\":" + version + "}";
	}

	private Board board()
	{
		return TaskBoardApplication.get()
			.getRepository()
			.get(TaskBoardApplication.get().getBoardId());
	}

	private long version()
	{
		return board().version();
	}

	private Column columnOf(String cardId)
	{
		return board().columns()
			.entrySet()
			.stream()
			.filter(entry -> entry.getValue().stream().anyMatch(card -> card.id().equals(cardId)))
			.map(Map.Entry::getKey)
			.findFirst()
			.orElseThrow();
	}
}
