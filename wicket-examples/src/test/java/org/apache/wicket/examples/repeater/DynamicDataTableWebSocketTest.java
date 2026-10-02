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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.examples.JettyTestCaseDecorator;
import org.apache.wicket.examples.SeleniumBrowser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Drives {@link DynamicDataTablePage} in Chrome and in Firefox. Runs with {@code -Pselenium}, see
 * {@link SeleniumBrowser}.
 */
@EnabledIfSystemProperty(named = "wicket.selenium", matches = "true")
class DynamicDataTableWebSocketTest extends JettyTestCaseDecorator
{
	private static final Pattern ITEM_COUNT = Pattern.compile("of (\\d+)");

	private WebDriver driver;

	private WebDriverWait wait;

	@AfterEach
	void stopBrowser()
	{
		if (driver != null)
		{
			driver.quit();
		}
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void addingAContactRefreshesTheTableViaWebSockets(SeleniumBrowser browser)
	{
		driver = browser.newDriver(1280, 1024);
		wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		wait.ignoring(StaleElementReferenceException.class);
		driver.get("http://localhost:" + localPort +
			"/wicket-examples/repeater/wicket/bookmarkable/" + DynamicDataTablePage.class.getName());
		wait.until(d -> !bodyRows().isEmpty());
		wait.until(d -> (Boolean)((JavascriptExecutor)d).executeScript(
			"return !!(Wicket.WebSocket.INSTANCE && Wicket.WebSocket.INSTANCE.ws && " +
				"Wicket.WebSocket.INSTANCE.ws.readyState === 1);"));
		int itemCount = itemCount();

		driver.findElement(By.linkText("Add a contact and refresh the table via web sockets"))
			.click();

		wait.until(d -> itemCount() == itemCount + 1);
		wait.until(d -> "Aaron".equals(firstNameOfFirstRow()));
		String feedback = driver.findElement(By.className("feedbackPanel")).getText();
		assertTrue(feedback.contains("the table was refreshed via web sockets"), feedback);
	}

	private List<WebElement> bodyRows()
	{
		return driver.findElements(
			By.cssSelector("table.dynamic-dataview .dynamic-data-table-rows > tbody > tr"));
	}

	private String firstNameOfFirstRow()
	{
		return bodyRows().get(0).findElements(By.tagName("td")).get(4).getText();
	}

	private int itemCount()
	{
		String label = driver.findElement(By.cssSelector("table.dynamic-dataview .navigatorLabel"))
			.getText();
		Matcher matcher = ITEM_COUNT.matcher(label);
		return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
	}
}
