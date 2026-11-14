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
package org.apache.wicket.examples.islands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.wicket.examples.AjaxEngineSelector.Engine;
import org.apache.wicket.examples.JettyTestCaseDecorator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base for the island examples' browser tests: the examples in Jetty, a headless Chrome whose log
 * is checked for errors, and the switch between the Ajax engines.
 * <p>
 * Needs Chrome, so the tests only run with {@code -Dwicket.selenium=true}, and the examples'
 * JavaScript, so they are skipped unless the build ran with {@code -Pjs-test}.
 */
public abstract class IslandSeleniumTestCase extends JettyTestCaseDecorator
{
	private static final String MARK =
		"document.querySelector(arguments[0]).__mark = arguments[1];";

	private static final String READ_MARK = """
		var element = document.querySelector(arguments[0]);
		return element && element.__mark ? element.__mark : null;
		""";

	private boolean started;

	protected WebDriver driver;

	protected WebDriverWait wait;

	@Override
	@BeforeEach
	public void before() throws Exception
	{
		Assumptions.assumeTrue(
			getClass().getResource("/META-INF/wicket-islands/.vite/manifest.json") != null,
			"the island modules are built with -Pjs-test only");

		super.before();
		started = true;

		LoggingPreferences logging = new LoggingPreferences();
		logging.enable(LogType.BROWSER, Level.ALL);
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless=new", "--window-size=1400,1024");
		options.setCapability(ChromeOptions.LOGGING_PREFS, logging);
		driver = new ChromeDriver(options);
		wait = new WebDriverWait(driver, Duration.ofSeconds(10), Duration.ofMillis(20));
		// Wicket replaces components while a test reads them
		wait.ignoring(StaleElementReferenceException.class);
	}

	@Override
	@AfterEach
	public void after() throws Exception
	{
		if (driver != null)
		{
			driver.quit();
		}
		if (started)
		{
			super.after();
		}
	}

	/**
	 * Opens an example with the given Ajax engine.
	 *
	 * @param path
	 *            the path of the example below the context
	 * @param engine
	 *            the Ajax engine
	 */
	protected void open(String path, Engine engine)
	{
		driver.get(String.format("http://localhost:%d/wicket-examples/%s", localPort, path));
		if (isJQueryLoaded() != (engine == Engine.JQUERY))
		{
			WebElement toggle = driver.findElement(By.partialLinkText("switch to"));
			toggle.click();
			wait.until(ExpectedConditions.stalenessOf(toggle));
		}
		assertEquals(engine == Engine.JQUERY, isJQueryLoaded(), "the Ajax engine");
	}

	private boolean isJQueryLoaded()
	{
		return (Boolean)js().executeScript("return typeof window.jQuery === 'function';");
	}

	/**
	 * Marks an element, so a later check can tell whether it is still the same one.
	 */
	protected void mark(String selector, String mark)
	{
		js().executeScript(MARK, selector, mark);
	}

	/**
	 * @return the mark of the element, {@code null} for an element that was never marked
	 */
	protected Object readMark(String selector)
	{
		return js().executeScript(READ_MARK, selector);
	}

	/**
	 * Every island the runtime has mounted still has its host in the document.
	 */
	protected void assertNoLeakedIslands()
	{
		List<?> mountedAndHosts = (List<?>)js().executeScript("""
			return [globalThis.__wicketIslandsRuntime.mounted.size,
				document.querySelectorAll('[data-wicket-island]').length];
			""");
		assertEquals(mountedAndHosts.get(1), mountedAndHosts.get(0),
			"mounted islands versus hosts in the page");
	}

	/**
	 * The browser logged no warnings or errors, violations of the content security policy
	 * included.
	 */
	protected void assertNoBrowserErrors()
	{
		List<String> errors = driver.manage()
			.logs()
			.get(LogType.BROWSER)
			.getAll()
			.stream()
			.filter(entry -> entry.getLevel().intValue() >= Level.WARNING.intValue())
			.map(LogEntry::getMessage)
			.collect(Collectors.toList());
		assertTrue(errors.isEmpty(), errors.toString());
	}

	protected List<String> texts(String selector)
	{
		return driver.findElements(By.cssSelector(selector))
			.stream()
			.map(WebElement::getText)
			.collect(Collectors.toList());
	}

	protected JavascriptExecutor js()
	{
		return (JavascriptExecutor)driver;
	}
}
