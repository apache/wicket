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

import java.time.Duration;
import java.util.List;

import org.apache.wicket.examples.AjaxEngineSelector.Engine;
import org.apache.wicket.examples.islands.IslandSeleniumTestCase;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Uses the task board in a headless Chrome, with either Ajax engine: dragging cards, the filter,
 * the chart and the edit dialog. Every test also checks that the browser logged no errors, which
 * includes violations of the content security policy.
 */
@EnabledIfSystemProperty(named = "wicket.selenium", matches = "true")
class TaskBoardSeleniumTest extends IslandSeleniumTestCase
{
	private static final String NOTICE = ".task-board [role=alert]";

	private static final String FORM = ".card-form";

	private static final String THIRD_OF_THE_MONTH = "//*[contains(@class,'card-form')]" +
		"//button[starts-with(@aria-label,'3 ')][not(@data-outside)]";

	@ParameterizedTest
	@EnumSource(Engine.class)
	void aMoveIsConfirmedAndUpdatesTheSummary(Engine engine)
	{
		open(engine);
		mark("[data-wicket-island=task-board] [data-wicket-island-root]", "board");

		drag("c1", Column.REVIEW);

		awaitColumn("c1", Column.REVIEW);
		wait.until(driver -> summary().contains("Review 2"));
		assertTrue(driver.findElements(By.cssSelector(NOTICE)).isEmpty());
		assertEquals("board",
			readMark("[data-wicket-island=task-board] [data-wicket-island-root]"),
			"the board was mounted again");
		assertNoLeakedIslands();
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void aMoveIntoAFullColumnIsRolledBack(Engine engine)
	{
		open(engine);
		drag("c1", Column.IN_PROGRESS);
		wait.until(driver -> summary().contains("In progress 3 / 3"));

		drag("c2", Column.IN_PROGRESS);

		WebElement notice = wait.until(
			ExpectedConditions.visibilityOfElementLocated(By.cssSelector(NOTICE)));
		assertTrue(notice.getText().contains("allows at most 3 cards"), notice.getText());
		awaitColumn("c2", Column.TODO);
		assertTrue(summary().contains("In progress 3 / 3"), summary());
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void aMoveOnAStaleBoardShowsTheCurrentBoard(Engine engine)
	{
		open(engine);
		driver.findElement(By.linkText("Let a teammate move a card")).click();
		wait.until(ExpectedConditions.textToBePresentInElementLocated(
			By.cssSelector(".board-teammate"), "teammate moved"));
		assertEquals(Column.TODO, columnOf("c1"), "the board learnt of the teammate's move");

		drag("c2", Column.DONE);

		WebElement notice = wait.until(
			ExpectedConditions.visibilityOfElementLocated(By.cssSelector(NOTICE)));
		assertTrue(notice.getText().contains("A teammate changed the board"), notice.getText());
		awaitColumn("c1", Column.REVIEW);
		awaitColumn("c2", Column.TODO);

		drag("c2", Column.DONE);
		awaitColumn("c2", Column.DONE);
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void theFilterHidesCardsWithoutTheServer(Engine engine)
	{
		open(engine);

		driver.findElement(By.cssSelector(".board-filter input[placeholder='Anything']"))
			.sendKeys("bug");
		wait.until(driver -> visibleCards().equals(List.of("c4")));
		clear(driver.findElement(By.cssSelector(".board-filter input[placeholder='Anything']")));
		wait.until(driver -> visibleCards().size() == 9);

		WebElement assignee = driver.findElement(
			By.cssSelector(".board-filter input[placeholder='Anyone']"));
		assignee.click();
		assignee.sendKeys("Ca");
		WebElement carol = wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.xpath("//*[contains(@class,'board-filter')]//*[@role='option'][.='Carol']")));
		assertEquals(1, driver.findElements(By.cssSelector(".board-filter [role=option]")).size(),
			"the members endpoint did not filter by the prefix");
		carol.click();
		wait.until(driver -> visibleCards().equals(List.of("c4", "c8")));
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void theChartFetchesItsDataAgainAfterAMoveToDone(Engine engine)
	{
		open(engine);
		WebElement thisWeek = wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.cssSelector(".cards-per-week .this-week")));
		int before = cardsDoneThisWeek(thisWeek.getText());

		drag("c7", Column.DONE);

		awaitColumn("c7", Column.DONE);
		wait.until(ExpectedConditions.textToBe(By.cssSelector(".cards-per-week .this-week"),
			"This week: " + (before + 1) + " cards done"));
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void editingACardValidatesTheFormIslandsAndSanitizesTheDescription(Engine engine)
	{
		open(engine);
		new Actions(driver).doubleClick(card("c1")).perform();
		wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.cssSelector(FORM + " .ProseMirror")));
		WebElement title = driver.findElement(By.cssSelector(FORM + " input[type=text]"));
		assertEquals("Write the release notes", title.getDomProperty("value"));

		clear(title);
		driver.findElement(By.cssSelector(FORM + " button.mantine-DatePickerInput-input")).click();
		wait.until(ExpectedConditions.elementToBeClickable(
			By.cssSelector(FORM + " [data-direction=previous]"))).click();
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath(THIRD_OF_THE_MONTH))).click();
		save();
		wait.until(driver -> texts(FORM + " .feedbackPanel li").size() == 2);
		assertEquals(List.of("'Title' is required.", "The due date cannot be in the past."),
			texts(FORM + " .feedbackPanel li"));
		assertTrue(driver.findElement(By.cssSelector(FORM + " .due-date"))
			.getText()
			.contains("The due date cannot be in the past."));

		driver.findElement(By.cssSelector(FORM + " input[type=text]"))
			.sendKeys("Release notes <b>12</b>");
		js().executeScript("""
			document.querySelector('[data-wicket-island=date-picker] [data-wicket-island-value]')
				.value = '';
			document.querySelector('[data-wicket-island=rich-text] [data-wicket-island-value]')
				.value = JSON.stringify('<p>Hello <b>world</b><img src=x onerror="alert(1)">'
					+ '<a href="javascript:alert(2)">x</a></p><script>alert(3)</scr' + 'ipt>');
			""");
		save();
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(FORM)));

		wait.until(ExpectedConditions.textToBe(By.cssSelector("[data-card-id=c1] .title"),
			"Release notes <b>12</b>"));
		WebElement description = driver.findElement(
			By.cssSelector(".card-preview .card-preview-description"));
		assertEquals("<p>Hello <b>world</b>x</p>", description.getDomProperty("innerHTML"));
		assertFalse(driver.findElements(By.cssSelector(".card-preview h4 b")).size() > 0,
			"the title was rendered as markup");
		assertNoLeakedIslands();
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void aCardOpensFromTheKeyboard(Engine engine)
	{
		open(engine);
		js().executeScript("arguments[0].focus()", card("c2"));

		driver.switchTo().activeElement().sendKeys(Keys.TAB);
		assertEquals("Edit Upgrade the build plugins",
			driver.switchTo().activeElement().getDomAttribute("aria-label"));
		driver.switchTo().activeElement().sendKeys(Keys.ENTER);

		wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.cssSelector(FORM + " .ProseMirror")));
		assertEquals("Upgrade the build plugins",
			driver.findElement(By.cssSelector(FORM + " input[type=text]")).getDomProperty("value"));
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void typingInTheDescriptionWhileItIsValidatedLosesNothing(Engine engine)
	{
		open(engine);
		new Actions(driver).doubleClick(card("c1")).perform();
		WebElement editor = wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.cssSelector(FORM + " .ProseMirror")));
		editor.click();
		new Actions(driver).keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL)
			.sendKeys(Keys.BACK_SPACE)
			.perform();

		StringBuilder typed = new StringBuilder();
		for (int i = 0; typed.length() <= EditCardPanel.MAX_DESCRIPTION; i++)
		{
			String chunk = (i == 0 ? "" : " ") + "the quick brown fox jumps over the lazy dog " + i;
			new Actions(driver).sendKeys(chunk).pause(Duration.ofMillis(80)).perform();
			typed.append(chunk);
		}

		wait.until(driver -> texts(FORM + " .rich-text .field-errors li").stream()
			.anyMatch(error -> error.contains("maximum of 1000 characters")));
		wait.until(ExpectedConditions.textToBe(By.cssSelector(FORM + " .ProseMirror"),
			typed.toString()));
		assertEquals("\"<p>" + typed + "</p>\"", driver
			.findElement(By.cssSelector("[data-wicket-island=rich-text] [data-wicket-island-value]"))
			.getDomProperty("value"));
		assertEquals(Boolean.TRUE, js().executeScript(
			"return document.activeElement.closest('.ProseMirror') !== null"),
			"the editor lost the focus");
		assertNoLeakedIslands();
		assertNoBrowserErrors();
	}

	private void open(Engine engine)
	{
		open("taskboard", engine);
		wait.until(driver -> !driver.findElements(By.cssSelector("[data-card-id]")).isEmpty() &&
			!driver.findElements(By.cssSelector(".board-filter")).isEmpty() &&
			!driver.findElements(By.cssSelector(".cards-per-week")).isEmpty());
	}

	/**
	 * Drags a card onto a column's title, which stays in view, with the pointer events dnd-kit
	 * listens to.
	 */
	private void drag(String cardId, Column column)
	{
		WebElement target = driver.findElement(By.cssSelector("[data-column=" + column + "]"));
		new Actions(driver).clickAndHold(card(cardId))
			.moveByOffset(10, 10)
			.pause(Duration.ofMillis(50))
			.moveToElement(target, 0, -target.getSize().getHeight() / 2 + 15)
			.pause(Duration.ofMillis(50))
			.release()
			.perform();
	}

	private WebElement card(String cardId)
	{
		return driver.findElement(By.cssSelector("[data-card-id=" + cardId + "]"));
	}

	private Column columnOf(String cardId)
	{
		return Column.valueOf(card(cardId).findElement(By.xpath("ancestor::*[@data-column]"))
			.getDomAttribute("data-column"));
	}

	private void awaitColumn(String cardId, Column column)
	{
		wait.until(driver -> columnOf(cardId) == column);
	}

	private List<String> visibleCards()
	{
		return driver.findElements(By.cssSelector("[data-card-id]"))
			.stream()
			.map(card -> card.getDomAttribute("data-card-id"))
			.toList();
	}

	private String summary()
	{
		return String.join(", ", texts(".board-summary tr"));
	}

	private static int cardsDoneThisWeek(String caption)
	{
		return Integer.parseInt(caption.replaceAll("\\D", ""));
	}

	private void clear(WebElement input)
	{
		input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
	}

	private void save()
	{
		driver.findElement(By.cssSelector(FORM + " button[name]")).click();
	}
}
