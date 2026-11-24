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
package org.apache.wicket.examples.islands.twins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.examples.AjaxEngineSelector.Engine;
import org.apache.wicket.examples.islands.IslandSeleniumTestCase;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Uses the React and the Svelte poll in a headless Chrome, with either Ajax engine, and checks
 * that each sees what the other does. Every test also checks that the browser logged no errors,
 * which includes violations of the content security policy.
 */
@EnabledIfSystemProperty(named = "wicket.selenium", matches = "true")
class TwinIslandsSeleniumTest extends IslandSeleniumTestCase
{
	private static final String REACT = "[data-wicket-island=poll-react]";

	private static final String SVELTE = "[data-wicket-island=poll-svelte]";

	private static final String ROOT = " [data-wicket-island-root]";

	@ParameterizedTest
	@EnumSource(Engine.class)
	void aVoteOnOneSideUpdatesBothSides(Engine engine)
	{
		open(engine);
		mark(REACT + ROOT, "react");
		mark(SVELTE + ROOT, "svelte");

		vote(REACT, 2);

		assertEquals("1", count(REACT, 2));
		assertEquals("1 vote, Svelte leads.", summary());
		assertTrue(option(REACT, 2).getDomAttribute("class").contains("leader"));
		assertTrue(option(SVELTE, 2).getDomAttribute("class").contains("leader"));
		assertEquals("Votes in total: 1. Cast here: 1.", footer(REACT));
		assertEquals("Votes in total: 1. Cast here: 0.", footer(SVELTE));

		vote(SVELTE, 1);

		assertEquals("2 votes, no option leads.", summary());
		assertEquals("Votes in total: 2. Cast here: 1.", footer(REACT));
		assertEquals("Votes in total: 2. Cast here: 1.", footer(SVELTE));
		assertEquals("react", readMark(REACT + ROOT), "the React poll was mounted again");
		assertEquals("svelte", readMark(SVELTE + ROOT), "the Svelte poll was mounted again");
		assertNoLeakedIslands();
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void aHalfTypedOptionSurvivesAVoteOnTheOtherSide(Engine engine)
	{
		open(engine);
		input(SVELTE).sendKeys("Vu");
		input(REACT).sendKeys("Li");

		vote(REACT, 1);
		vote(SVELTE, 3);

		assertEquals("Vu", input(SVELTE).getDomProperty("value"));
		assertEquals("Li", input(REACT).getDomProperty("value"));
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void anOptionAddedOnOneSideShowsOnBoth(Engine engine)
	{
		open(engine);

		input(SVELTE).sendKeys("react");
		submit(SVELTE);
		WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.cssSelector(SVELTE + " .poll-message")));
		assertEquals("'react' is an option already.", message.getText());
		assertEquals("react", input(SVELTE).getDomProperty("value"));
		assertTrue(driver.findElements(By.cssSelector(REACT + " .poll-message")).isEmpty());

		input(REACT).sendKeys("Vue");
		submit(REACT);
		wait.until(ExpectedConditions.numberOfElementsToBe(
			By.cssSelector(SVELTE + " .poll-option"), 4));

		assertEquals(texts(REACT + " .poll-label"), texts(SVELTE + " .poll-label"));
		assertEquals("Vue", option(SVELTE, 4).findElement(By.cssSelector(".poll-label")).getText());
		assertEquals("", input(REACT).getDomProperty("value"));
		assertEquals("react", input(SVELTE).getDomProperty("value"));
		assertNoLeakedIslands();
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void bothSidesShareTheHighlight(Engine engine)
	{
		open(engine);

		hover(option(REACT, 3));
		wait.until(ExpectedConditions.attributeContains(optionSelector(SVELTE, 3), "class",
			"highlight"));

		hover(option(SVELTE, 1));
		wait.until(ExpectedConditions.attributeContains(optionSelector(REACT, 1), "class",
			"highlight"));
		assertFalse(option(REACT, 3).getDomAttribute("class").contains("highlight"));
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void theWicketResetLinkUpdatesBothSides(Engine engine)
	{
		open(engine);
		vote(REACT, 1);
		vote(SVELTE, 1);
		mark(REACT + ROOT, "react");
		mark(SVELTE + ROOT, "svelte");

		driver.findElement(By.linkText("Reset the votes")).click();
		wait.until(ExpectedConditions.textToBe(By.cssSelector(".twins-summary"), "No votes yet."));

		wait.until(driver -> "0".equals(count(REACT, 1)) && "0".equals(count(SVELTE, 1)));
		assertEquals("Votes in total: 0. Cast here: 1.", footer(REACT));
		assertEquals("Votes in total: 0. Cast here: 1.", footer(SVELTE));
		assertEquals("react", readMark(REACT + ROOT), "the React poll was mounted again");
		assertEquals("svelte", readMark(SVELTE + ROOT), "the Svelte poll was mounted again");
		assertNoLeakedIslands();
		assertNoBrowserErrors();
	}

	private void open(Engine engine)
	{
		open("twinislands", engine);
		wait.until(driver -> !driver.findElements(By.cssSelector(REACT + " .poll-option")).isEmpty()
			&& !driver.findElements(By.cssSelector(SVELTE + " .poll-option")).isEmpty());
	}

	/**
	 * Votes in one poll and waits until the other shows the vote.
	 */
	private void vote(String side, int id)
	{
		String other = REACT.equals(side) ? SVELTE : REACT;
		int expected = Integer.parseInt(count(other, id)) + 1;
		option(side, id).findElement(By.cssSelector(".poll-vote")).click();
		wait.until(driver -> String.valueOf(expected).equals(count(other, id)) &&
			String.valueOf(expected).equals(count(side, id)));
	}

	private void submit(String side)
	{
		driver.findElement(By.cssSelector(side + " .poll-add button")).click();
	}

	private void hover(WebElement element)
	{
		new Actions(driver).moveToElement(element).perform();
	}

	private By optionSelector(String side, int id)
	{
		return By.cssSelector(side + " .poll-option[data-id='" + id + "']");
	}

	private WebElement option(String side, int id)
	{
		return driver.findElement(optionSelector(side, id));
	}

	private String count(String side, int id)
	{
		return option(side, id).findElement(By.cssSelector(".poll-count")).getText();
	}

	private WebElement input(String side)
	{
		return driver.findElement(By.cssSelector(side + " .poll-add input"));
	}

	private String footer(String side)
	{
		return driver.findElement(By.cssSelector(side + " .poll-footer")).getText();
	}

	private String summary()
	{
		return driver.findElement(By.cssSelector(".twins-summary")).getText();
	}
}
