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
package org.apache.wicket.examples.repeater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.apache.wicket.examples.JettyTestCaseDecorator;
import org.apache.wicket.examples.SeleniumBrowser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Drives {@link ArrangeableDataTablePage} in Chrome and in Firefox: resizing and moving the
 * columns of an ArrangeableDataTable. Runs with {@code -Pselenium}, see {@link SeleniumBrowser}.
 */
@EnabledIfSystemProperty(named = "wicket.selenium", matches = "true")
class ArrangeableDataTableBrowserTest extends JettyTestCaseDecorator
{
	private static final String HEADERS = "table.dataview thead tr:last-child th";

	private WebDriver driver;

	private WebDriverWait wait;

	@Override
	@AfterEach
	public void after() throws Exception
	{
		if (driver != null)
		{
			driver.quit();
		}
		super.after();
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void theMovableColumnsHaveAHandleAndAllAnEdgeToResize(SeleniumBrowser browser)
	{
		open(browser);

		List<WebElement> headers = driver.findElements(By.cssSelector(HEADERS));
		assertTrue(headers.get(0).findElements(By.className("wicket-col-mover")).isEmpty(),
			"the ID column cannot be moved");
		for (int i = 1; i < headers.size(); i++)
		{
			assertEquals(1, headers.get(i).findElements(By.className("wicket-col-mover")).size());
		}
		assertEquals(headers.size(),
			driver.findElements(By.cssSelector(HEADERS + " .wicket-col-resizer")).size());
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void aResizedColumnKeepsItsWidthWhenItIsMoved(SeleniumBrowser browser)
	{
		open(browser);
		double before = width("First Name");

		WebElement edge = header("First Name").findElement(By.className("wicket-col-resizer"));
		js().executeScript("arguments[0].focus();", edge);
		for (int i = 0; i < 10; i++)
		{
			new Actions(driver).sendKeys(Keys.ARROW_RIGHT).perform();
		}
		wait.until(d -> width("First Name") >= before + 95);
		double resized = width("First Name");

		WebElement handle = header("First Name").findElement(By.className("wicket-col-mover"));
		js().executeScript("arguments[0].focus();", handle);
		new Actions(driver).sendKeys(Keys.ARROW_RIGHT).perform();

		wait.until(d -> order().indexOf("First Name") == 2);
		assertEquals(List.of("ID", "Last Name", "First Name", "Home Phone", "Cell Phone",
			"Birthday"), order());
		assertEquals(resized, width("First Name"), 1, "the width after the move");
		wait.until(d -> "First Name".equals(js().executeScript(
			"return document.activeElement.closest('th').textContent.trim();")));
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void aColumnDraggedByItsHandleIsMovedWhereItIsDropped(SeleniumBrowser browser)
	{
		open(browser);

		WebElement handle = header("Birthday").findElement(By.className("wicket-col-mover"));
		WebElement target = header("First Name");
		new Actions(driver).clickAndHold(handle)
			.moveByOffset(-20, 0)
			.moveToElement(target, -target.getSize().getWidth() / 2 + 5, 0)
			.release()
			.perform();

		wait.until(d -> order().indexOf("Birthday") == 1);
		assertEquals(List.of("ID", "Birthday", "First Name", "Last Name", "Home Phone",
			"Cell Phone"), order());
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void noColumnIsMovedInFrontOfTheFixedOne(SeleniumBrowser browser)
	{
		open(browser);

		WebElement handle = header("First Name").findElement(By.className("wicket-col-mover"));
		js().executeScript("arguments[0].focus();", handle);
		new Actions(driver).sendKeys(Keys.ARROW_LEFT).perform();

		assertEquals("ID", order().get(0));
		assertEquals("First Name", order().get(1));
	}

	private void open(SeleniumBrowser browser)
	{
		driver = browser.newDriver(1400, 1000);
		wait = new WebDriverWait(driver, Duration.ofSeconds(10), Duration.ofMillis(20));
		wait.ignoring(StaleElementReferenceException.class);
		driver.get("http://localhost:" + localPort +
			"/wicket-examples/repeater/arrangeable-data-table");
		wait.until(
			d -> !driver.findElements(By.cssSelector(HEADERS + " .wicket-col-mover")).isEmpty());
	}

	private List<String> order()
	{
		return driver.findElements(By.cssSelector(HEADERS))
			.stream()
			.map(header -> header.getText().trim())
			.toList();
	}

	private WebElement header(String text)
	{
		return driver.findElements(By.cssSelector(HEADERS))
			.stream()
			.filter(header -> header.getText().trim().equals(text))
			.findFirst()
			.orElseThrow();
	}

	private double width(String text)
	{
		return ((Number)js().executeScript("return arguments[0].getBoundingClientRect().width;",
			header(text))).doubleValue();
	}

	private JavascriptExecutor js()
	{
		return (JavascriptExecutor)driver;
	}
}
