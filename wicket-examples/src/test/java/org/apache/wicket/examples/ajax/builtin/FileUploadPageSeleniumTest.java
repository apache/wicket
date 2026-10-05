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
package org.apache.wicket.examples.ajax.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.wicket.examples.AjaxEngineSelector.Engine;
import org.apache.wicket.examples.JettyTestCaseDecorator;
import org.apache.wicket.examples.SeleniumBrowser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Uploads files with {@link FileUploadPage} in Chrome and in Firefox, with either Ajax engine, and
 * checks that the upload progress bar shows the progress of the slowed down upload.
 * <p>
 * Needs the browsers, so it only runs with {@code -Dwicket.selenium=true}, see
 * {@link SeleniumBrowser}.
 */
@EnabledIfSystemProperty(named = "wicket.selenium", matches = "true")
class FileUploadPageSeleniumTest extends JettyTestCaseDecorator
{
	/**
	 * Logs the value of the first upload progress bar and its status text whenever they change,
	 * into the session storage, which keeps the log across the navigation of a regular submit.
	 */
	private static final String RECORD_PROGRESS = """
		sessionStorage.setItem('progress', '[]');
		var bar = document.querySelector('.wupb-progressBar');
		var status = document.querySelector('.wupb-uploadStatus');
		new MutationObserver(function () {
			var log = JSON.parse(sessionStorage.getItem('progress'));
			log.push({
				value: bar.hidden ? 'hidden' : String(bar.querySelector('progress').value),
				status: status.textContent
			});
			sessionStorage.setItem('progress', JSON.stringify(log));
		}).observe(bar.parentNode, { attributes: true, childList: true, subtree: true,
			characterData: true });
		""";

	@TempDir
	Path temp;

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
	@MethodSource("browsersAndEngines")
	void anAjaxUploadShowsItsProgress(SeleniumBrowser browser, Engine engine) throws IOException
	{
		open(browser, engine);
		selectFile(800 * 1024);
		js().executeScript(RECORD_PROGRESS);

		js().executeScript("window.notReloaded = true;");

		button("Ajax Submit").click();

		wait.until(ExpectedConditions.textToBePresentInElementLocated(
			By.cssSelector(".feedbackPanel"), "This request was processed using AJAX"));
		wait.until(driver -> "hidden".equals(last(progress()).get("value")));
		assertShowedProgress(progress());
		assertEquals(Boolean.TRUE, js().executeScript("return window.notReloaded;"),
			"the form was submitted besides the Ajax request");
		assertButtonsEnabled(true);
	}

	@ParameterizedTest
	@MethodSource("browsersAndEngines")
	void theBrowserDoesNotRestoreTheFileFieldWhenGoingBack(SeleniumBrowser browser, Engine engine)
		throws IOException
	{
		open(browser, engine);
		selectFile(100 * 1024);

		driver.findElement(By.partialLinkText("go back to Ajax Examples")).click();
		wait.until(ExpectedConditions.urlMatches("/ajax/?(\\?.*)?$"));
		driver.navigate().back();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("form")));

		assertEquals("", fileField().getDomProperty("value"));
		assertButtonsEnabled(false);
		selectFile(100 * 1024);
	}

	@ParameterizedTest
	@MethodSource("browsersAndEngines")
	void aRegularUploadShowsItsProgress(SeleniumBrowser browser, Engine engine) throws IOException
	{
		open(browser, engine);
		selectFile(800 * 1024);
		js().executeScript(RECORD_PROGRESS);
		WebElement submit = button("Regular Submit");

		submit.click();

		wait.until(ExpectedConditions.stalenessOf(submit));
		wait.until(ExpectedConditions.textToBePresentInElementLocated(
			By.cssSelector(".feedbackPanel"), "File-Name: upload.bin"));
		assertShowedProgress(progress());
		assertButtonsEnabled(false);
	}

	@ParameterizedTest
	@MethodSource("browsersAndEngines")
	void aFileBeyondTheMaximumSizeLeavesTheButtonsDisabled(SeleniumBrowser browser, Engine engine)
		throws IOException
	{
		open(browser, engine);
		assertButtonsEnabled(false);

		fileField().sendKeys(newFile(20 * 1024 * 1024).toString());

		wait.until(ExpectedConditions.textToBePresentInElementLocated(
			By.cssSelector("form"), "File exceeds max allowed size."));
		assertButtonsEnabled(false);
	}

	static Stream<Arguments> browsersAndEngines()
	{
		return Stream.of(SeleniumBrowser.values()).flatMap(
			browser -> Stream.of(Engine.values()).map(engine -> Arguments.of(browser, engine)));
	}

	private void open(SeleniumBrowser browser, Engine engine)
	{
		driver = browser.newDriver(1280, 1024);
		wait = new WebDriverWait(driver, Duration.ofSeconds(15), Duration.ofMillis(20));
		wait.ignoring(StaleElementReferenceException.class);
		driver.get(String.format("http://localhost:%d/wicket-examples/ajax/upload", localPort));
		if (isJQueryLoaded() != (engine == Engine.JQUERY))
		{
			WebElement toggle = driver.findElement(By.partialLinkText("switch to"));
			toggle.click();
			wait.until(ExpectedConditions.stalenessOf(toggle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("form")));
		}
		assertEquals(engine == Engine.JQUERY, isJQueryLoaded(), "the Ajax engine");
	}

	private void selectFile(int size) throws IOException
	{
		assertButtonsEnabled(false);

		fileField().sendKeys(newFile(size).toString());
		wait.until(driver -> button("Ajax Submit").isEnabled());
		assertButtonsEnabled(true);

		driver.findElement(By.cssSelector("form input[type=text]")).sendKeys("hello");
	}

	private static void assertShowedProgress(List<Map<String, Object>> progress)
	{
		assertTrue(progress.stream().anyMatch(entry -> {
			String value = (String)entry.get("value");
			return !"hidden".equals(value) && Integer.parseInt(value) > 0 &&
				Integer.parseInt(value) < 100 &&
				((String)entry.get("status")).contains("% finished");
		}), "the bar showed no progress: " + progress);
	}

	private void assertButtonsEnabled(boolean enabled)
	{
		for (String text : new String[] { "Regular Submit", "Ajax Submit" })
		{
			assertEquals(enabled, button(text).isEnabled(), text);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> progress()
	{
		return (List<Map<String, Object>>)js().executeScript(
			"return JSON.parse(sessionStorage.getItem('progress'));");
	}

	private static Map<String, Object> last(List<Map<String, Object>> progress)
	{
		return progress.isEmpty() ? Map.of() : progress.get(progress.size() - 1);
	}

	private WebElement fileField()
	{
		return driver.findElement(By.cssSelector("form input[type=file]"));
	}

	private WebElement button(String text)
	{
		return driver.findElement(
			By.xpath("(//form)[1]//button[contains(normalize-space(), '" + text + "')]"));
	}

	private Path newFile(int size) throws IOException
	{
		Path file = temp.resolve("upload.bin");
		Files.write(file, new byte[size]);
		return file;
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
