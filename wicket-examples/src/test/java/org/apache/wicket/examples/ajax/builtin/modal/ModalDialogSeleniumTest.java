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
package org.apache.wicket.examples.ajax.builtin.modal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.apache.wicket.examples.AjaxEngineSelector.Engine;
import org.apache.wicket.examples.JettyTestCaseDecorator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Drives {@link ModalDialogPage} with the keyboard, in a real browser and with either Ajax engine:
 * the focus moves into a dialog when it opens, stays inside on Tab and Shift+Tab, and goes back to
 * the element that opened the dialog when Escape closes it.
 * <p>
 * Needs a browser, so it only runs with {@code -Dwicket.selenium=true}. It drives a headless
 * Chrome by default; {@code -Dwicket.selenium.browser=firefox} picks Firefox, and
 * {@code -Dwicket.selenium.firefox=<path>} the Firefox binary to start. With
 * {@code -Dwicket.selenium.remote=<url>} the browser runs on that Selenium grid instead, and
 * {@code -Dwicket.selenium.host=<host>} names the host under which the grid reaches this machine.
 */
@EnabledIfSystemProperty(named = "wicket.selenium", matches = "true")
class ModalDialogSeleniumTest extends JettyTestCaseDecorator
{
	private static final By DIALOG = By.cssSelector(".modal-dialog");

	private WebDriver driver;

	private WebDriverWait wait;

	@Override
	@BeforeEach
	public void before() throws Exception
	{
		super.before();

		driver = createDriver();
		wait = new WebDriverWait(driver, Duration.ofSeconds(10), Duration.ofMillis(20));
	}

	private static WebDriver createDriver() throws Exception
	{
		boolean firefox = "firefox".equals(System.getProperty("wicket.selenium.browser"));
		String remote = System.getProperty("wicket.selenium.remote");

		Capabilities options;
		if (firefox)
		{
			FirefoxOptions firefoxOptions = new FirefoxOptions().addArguments("-headless");
			String binary = System.getProperty("wicket.selenium.firefox");
			if (binary != null)
			{
				firefoxOptions.setBinary(binary);
			}
			options = firefoxOptions;
		}
		else
		{
			options = new ChromeOptions().addArguments("--headless=new");
		}

		if (remote != null)
		{
			return new RemoteWebDriver(URI.create(remote).toURL(), options);
		}
		return firefox ? new FirefoxDriver((FirefoxOptions)options)
			: new ChromeDriver((ChromeOptions)options);
	}

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
	@EnumSource(Engine.class)
	void aDialogTakesTheFocusAndGivesItBackOnEscape(Engine engine)
	{
		open(engine);
		WebElement trigger = driver.findElement(By.linkText("via Ajax"));

		press(trigger, Keys.ENTER);

		WebElement dialog = awaitDialogs(1).get(0);
		assertEquals("dialog", dialog.getDomAttribute("role"));
		assertEquals("true", dialog.getDomAttribute("aria-modal"));
		assertEquals("Dialog 1",
			driver.findElement(By.id(dialog.getDomAttribute("aria-labelledby"))).getText());
		awaitFocus(textField(dialog), "the focus moves to the field with autofocus");

		press(driver.switchTo().activeElement(), Keys.ESCAPE);

		awaitDialogs(0);
		awaitFocus(driver.findElement(By.linkText("via Ajax")),
			"the focus goes back to the link that opened the dialog");
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void escapeClosesOneNestedDialogAtATime(Engine engine)
	{
		open(engine);
		press(driver.findElement(By.linkText("via Ajax")), Keys.ENTER);
		WebElement outer = awaitDialogs(1).get(0);
		awaitFocus(textField(outer), "the focus is in the first dialog");

		press(textField(outer), Keys.ENTER);
		WebElement inner = awaitDialogs(2).get(1);
		awaitFocus(textField(inner), "the focus is in the nested dialog");

		press(driver.switchTo().activeElement(), Keys.ESCAPE);
		outer = awaitDialogs(1).get(0);
		awaitFocus(textField(outer), "the focus goes back to the field that opened the dialog");

		press(driver.switchTo().activeElement(), Keys.ESCAPE);
		awaitDialogs(0);
		awaitFocus(driver.findElement(By.linkText("via Ajax")), "the focus is back on the page");
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void tabAndShiftTabCycleThroughADialogWithTheDefaults(Engine engine)
	{
		open(engine);
		driver.findElement(By.xpath("//label[text()='stacked dialogs']")).click();
		waitForAjax();

		press(driver.findElement(By.linkText("via Ajax")), Keys.ENTER);
		WebElement dialog = awaitDialogs(1).get(0);
		WebElement first = textField(dialog);
		WebElement last = dialog.findElement(By.linkText("Close dialog"));
		awaitFocus(first, "the focus moves into the dialog");

		press(first, Keys.chord(Keys.SHIFT, Keys.TAB));
		awaitFocus(last, "Shift+Tab on the first element goes to the last one");

		press(last, Keys.TAB);
		awaitFocus(first, "Tab on the last element goes to the first one");

		press(first, Keys.TAB);
		awaitFocus(dialog.findElement(By.linkText("Lorem...")), "Tab moves on in the dialog");

		press(driver.switchTo().activeElement(), Keys.ESCAPE);
		awaitDialogs(0);
		awaitFocus(driver.findElement(By.linkText("via Ajax")),
			"the focus goes back to the link that opened the dialog");
	}

	private void open(Engine engine)
	{
		String host = System.getProperty("wicket.selenium.host", "localhost");
		driver.get(String.format("http://%s:%d/wicket-examples/ajax/modal-dialog", host, localPort));
		if (isJQueryLoaded() != (engine == Engine.JQUERY))
		{
			WebElement toggle = driver.findElement(By.partialLinkText("switch to"));
			toggle.click();
			wait.until(ExpectedConditions.stalenessOf(toggle));
		}
		assertEquals(engine == Engine.JQUERY, isJQueryLoaded(), "the Ajax engine");
	}

	private void press(WebElement element, CharSequence keys)
	{
		element.sendKeys(keys);
	}

	private List<WebElement> awaitDialogs(int count)
	{
		wait.until(d -> d.findElements(DIALOG).size() == count);
		waitForAjax();
		return driver.findElements(DIALOG);
	}

	private void awaitFocus(WebElement expected, String message)
	{
		try
		{
			wait.until(d -> expected.equals(d.switchTo().activeElement()));
		}
		catch (RuntimeException e)
		{
			assertEquals(describe(expected), describe(driver.switchTo().activeElement()), message);
		}
	}

	private void waitForAjax()
	{
		wait.until(d -> (Boolean)js().executeScript(
			"return Wicket.channelManager === undefined || " +
				"Object.keys(Wicket.channelManager.channels).every(function (name) {" +
				" return !Wicket.channelManager.channels[name].busy; });"));
	}

	private static WebElement textField(WebElement dialog)
	{
		return dialog.findElements(By.cssSelector("input[type=text]")).get(0);
	}

	private static String describe(WebElement element)
	{
		return element.getTagName() + "#" + element.getDomAttribute("id") + " " +
			element.getText();
	}

	private boolean isJQueryLoaded()
	{
		return (Boolean)js().executeScript("return typeof window.jQuery === 'function';");
	}

	private JavascriptExecutor js()
	{
		return (JavascriptExecutor)driver;
	}
}
