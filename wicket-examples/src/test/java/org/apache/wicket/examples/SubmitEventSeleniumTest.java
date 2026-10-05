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
package org.apache.wicket.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.Duration;

import org.apache.wicket.examples.AjaxEngineSelector.Engine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Checks that the forms which fire a submit event from a script are submitted exactly once, in a
 * real browser and with either Ajax engine. Browsers differ on such an event: Firefox submits the
 * form for it unless it is cancelled, Chrome never does.
 * <p>
 * Needs a browser, so it only runs with {@code -Dwicket.selenium=true}. It drives a headless
 * Chrome by default; {@code -Dwicket.selenium.browser=firefox} picks Firefox. With
 * {@code -Dwicket.selenium.remote=<url>} the browser runs on that Selenium grid instead, and
 * {@code -Dwicket.selenium.host=<host>} names the host under which the grid reaches this machine.
 */
@EnabledIfSystemProperty(named = "wicket.selenium", matches = "true")
class SubmitEventSeleniumTest extends JettyTestCaseDecorator
{
	/**
	 * Counts the {@code formdata} events of the given form in the session storage, so the count
	 * survives a page load. The Ajax engines build their form data without the form, so only a
	 * submission by the browser fires one.
	 */
	private static final String RECORD_SUBMISSIONS = """
		sessionStorage.setItem('submissions', '0');
		arguments[0].addEventListener('formdata', function () {
			sessionStorage.setItem('submissions',
				String(Number(sessionStorage.getItem('submissions')) + 1));
		});
		""";

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
			options = new FirefoxOptions().addArguments("-headless");
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

	/**
	 * The Ajax button of the upload example triggers a submit event to start the progress bar.
	 */
	@ParameterizedTest
	@EnumSource(Engine.class)
	void anAjaxButtonTriggeringTheSubmitEventSubmitsOnlyThroughAjax(Engine engine)
	{
		open(engine, "ajax/upload");

		WebElement button = driver.findElements(By.cssSelector("input[value='Ajax Submit']"))
			.get(0);
		js().executeScript(RECORD_SUBMISSIONS, button.findElement(By.xpath("./ancestor::form")));
		button.click();

		wait.until(d -> submissions() > 0 ||
			d.getPageSource().contains("This request was processed using AJAX"));
		assertEquals(0, submissions(), "the browser submitted the form besides the Ajax request");
	}

	/**
	 * A {@code FormComponentUpdatingBehavior} submits its form through
	 * {@code Form#getJsForListenerUrl()}.
	 */
	@ParameterizedTest
	@EnumSource(Engine.class)
	void aFormComponentUpdatingBehaviorSubmitsTheForm(Engine engine)
	{
		open(engine, "forminput");

		WebElement locale = driver.findElement(By.cssSelector("select[name$='localeSelect']"));
		int other = index(new Select(locale).getFirstSelectedOption()) == 0 ? 1 : 0;
		new Select(locale).selectByIndex(other);

		wait.until(ExpectedConditions.stalenessOf(locale));
		WebElement reloaded = driver.findElement(By.cssSelector("select[name$='localeSelect']"));
		assertEquals(other, index(new Select(reloaded).getFirstSelectedOption()),
			"the chosen locale is in effect");
	}

	private void open(Engine engine, String path)
	{
		String host = System.getProperty("wicket.selenium.host", "localhost");
		driver.get(String.format("http://%s:%d/wicket-examples/%s", host, localPort, path));
		if (isJQueryLoaded() != (engine == Engine.JQUERY))
		{
			WebElement toggle = driver.findElement(By.partialLinkText("switch to"));
			toggle.click();
			wait.until(ExpectedConditions.stalenessOf(toggle));
		}
		assertEquals(engine == Engine.JQUERY, isJQueryLoaded(), "the Ajax engine");
		assertTrue((Boolean)js().executeScript("return typeof Wicket.Event === 'object';"),
			"Wicket's JavaScript is loaded");
	}

	private boolean isJQueryLoaded()
	{
		return (Boolean)js().executeScript("return typeof window.jQuery === 'function';");
	}

	private long submissions()
	{
		return (Long)js().executeScript(
			"return Number(sessionStorage.getItem('submissions'));");
	}

	private static int index(WebElement option)
	{
		return Integer.parseInt(option.getDomProperty("index"));
	}

	private JavascriptExecutor js()
	{
		return (JavascriptExecutor)driver;
	}
}
