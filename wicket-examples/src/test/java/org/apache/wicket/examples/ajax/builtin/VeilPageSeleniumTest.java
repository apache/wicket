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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.examples.AjaxEngineSelector.Engine;
import org.apache.wicket.examples.JettyTestCaseDecorator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Drives {@link VeilPage} in a headless Chrome, with either Ajax engine.
 * <p>
 * Needs Chrome, so it only runs with {@code -Dwicket.selenium=true}. Selenium Manager resolves
 * the driver, and the browser too when none is installed.
 */
@EnabledIfSystemProperty(named = "wicket.selenium", matches = "true")
class VeilPageSeleniumTest extends JettyTestCaseDecorator
{
	private static final String PAGE_VEIL = "body > .wicket-veil";

	private static final String OUTER_VEIL = ".veil-outer > .wicket-veil";

	private static final String INNER_VEIL = ".veil-inner > .wicket-veil";

	/**
	 * Logs when a veil is added, gets its spinner and is removed, with the time of each, into
	 * {@code window.veilLog}.
	 */
	private static final String RECORD_VEILS = """
		window.veilLog = [];
		function isVeil(node) {
			return node.classList && node.classList.contains('wicket-veil');
		}
		function log(event, veil, host) {
			window.veilLog.push({
				event: event,
				target: host === document.body ? 'page' :
					host.classList.contains('veil-inner') ? 'inner' : 'outer',
				time: performance.now()
			});
		}
		new MutationObserver(function (records) {
			records.forEach(function (record) {
				if (record.type === 'childList') {
					record.addedNodes.forEach(function (node) {
						if (isVeil(node)) { log('added', node, record.target); }
					});
					record.removedNodes.forEach(function (node) {
						if (isVeil(node)) { log('removed', node, record.target); }
					});
				} else if (isVeil(record.target) &&
					record.target.classList.contains('wicket-veil-busy') &&
					(record.oldValue || '').indexOf('wicket-veil-busy') < 0) {
					log('busy', record.target, record.target.parentNode);
				}
			});
		}).observe(document.body, { childList: true, subtree: true, attributes: true,
			attributeOldValue: true, attributeFilter: ['class'] });
		""";

	/**
	 * Calls back once no veil has been on the page for 300 ms, so the requests the page fires by
	 * itself on load are over.
	 */
	private static final String AWAIT_QUIET = """
		var callback = arguments[arguments.length - 1];
		var quietSince = null;
		(function poll() {
			var now = Date.now();
			if (document.querySelector('.wicket-veil')) {
				quietSince = null;
			} else if (quietSince === null) {
				quietSince = now;
			} else if (now - quietSince >= 300) {
				callback(true);
				return;
			}
			setTimeout(poll, 20);
		})();
		""";

	/**
	 * Tells whether the page veil is the topmost element at the centre of the given element, and
	 * describes the veil's geometry when it is not.
	 */
	private static final String HIT_TEST = """
		var target = arguments[0];
		target.scrollIntoView({ block: 'center' });
		var rect = target.getBoundingClientRect();
		var x = rect.left + rect.width / 2;
		var y = rect.top + rect.height / 2;
		var hit = document.elementFromPoint(x, y);
		var veil = document.querySelector('body > .wicket-veil');
		var style = getComputedStyle(veil);
		var veilRect = veil.getBoundingClientRect();
		return {
			veiled: hit === veil,
			hit: hit ? hit.tagName + '.' + hit.className : null,
			point: x + ',' + y,
			position: style.position,
			zIndex: style.zIndex,
			veilRect: [veilRect.left, veilRect.top, veilRect.width, veilRect.height].join(','),
			stylesheet: Array.prototype.some.call(document.styleSheets, function (sheet) {
				return (sheet.href || '').indexOf('wicket-veil') >= 0;
			})
		};
		""";

	private WebDriver driver;

	private WebDriverWait wait;

	@Override
	@BeforeEach
	public void before() throws Exception
	{
		super.before();

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless=new", "--window-size=1280,1024");
		driver = new ChromeDriver(options);
		driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(10));
		wait = new WebDriverWait(driver, Duration.ofSeconds(10), Duration.ofMillis(20));
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
	void slowRequestVeilsThePageAndShowsTheSpinnerAfterTheDelay(Engine engine)
	{
		open(engine);

		click("Slow request");
		wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(PAGE_VEIL)));
		wait.until(ExpectedConditions.presenceOfElementLocated(
			By.cssSelector(PAGE_VEIL + ".wicket-veil-busy")));
		awaitPageCounter("1");
		awaitNoVeil();

		List<Map<String, Object>> log = veilLog();
		assertEquals(List.of("added", "busy", "removed"), events(log), log.toString());
		assertEquals("page", log.get(0).get("target"));
		double spinnerDelay = time(log, "busy") - time(log, "added");
		assertTrue(spinnerDelay >= 290, "the spinner showed after " + spinnerDelay + " ms");
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void theVeilSwallowsClicks(Engine engine)
	{
		open(engine);

		click("Slow request");
		wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(PAGE_VEIL)));
		WebElement fastRequest = driver.findElement(By.linkText("Fast request"));
		@SuppressWarnings("unchecked")
		Map<String, Object> hit = (Map<String, Object>)js().executeScript(HIT_TEST, fastRequest);
		assertEquals(Boolean.TRUE, hit.get("veiled"), "the veil does not cover the link: " + hit);
		// WebElement.click() would wait for the veil to come down before clicking
		new Actions(driver).moveToElement(fastRequest).click().perform();

		awaitPageCounter("1");
		awaitNoVeil();
		awaitQuiet();
		assertEquals("1", pageCounter(), "the click on the veiled page went through");
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void fastRequestShowsNoSpinner(Engine engine)
	{
		open(engine);

		click("Fast request");
		awaitPageCounter("1");
		awaitNoVeil();

		List<Map<String, Object>> log = veilLog();
		assertEquals(List.of("added", "removed"), events(log), log.toString());
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void theSpinnerStaysForItsMinimumTime(Engine engine)
	{
		open(engine);

		click("Medium request");
		awaitPageCounter("1");
		awaitNoVeil();

		List<Map<String, Object>> log = veilLog();
		assertEquals(List.of("added", "busy", "removed"), events(log), log.toString());
		double shown = time(log, "removed") - time(log, "busy");
		assertTrue(shown >= 480, "the spinner was shown for " + shown + " ms only");
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void anOptedOutRequestLeavesThePageUsable(Engine engine)
	{
		open(engine);

		click("Unveiled request");
		sleep(Duration.ofMillis(400));
		assertTrue(veilLog().isEmpty(), "the opted-out request was veiled: " + veilLog());

		click("Fast request");
		awaitPageCounter("2");
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void aLocalVeilCoversOnlyItsComponentAndShowsTheSpinner(Engine engine)
	{
		open(engine);

		click("Slow request from the outer panel");
		wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(OUTER_VEIL)));
		assertTrue(outerPanel().getDomAttribute("class").contains("wicket-veil-host"));
		assertTrue(driver.findElements(By.cssSelector(PAGE_VEIL)).isEmpty(),
			"the page was veiled too");
		assertTrue(driver.findElements(By.cssSelector(INNER_VEIL)).isEmpty(),
			"the nested panel got a veil of its own");
		wait.until(ExpectedConditions.presenceOfElementLocated(
			By.cssSelector(OUTER_VEIL + ".wicket-veil-busy")));

		click("Fast request");

		wait.until(ExpectedConditions.textToBe(By.cssSelector(".outer-counter"), "1"));
		awaitPageCounter("1");
		awaitNoVeil();
		assertFalse(outerPanel().getDomAttribute("class").contains("wicket-veil-host"),
			"the host class stayed");

		List<Map<String, Object>> outerLog = veilLog().stream()
			.filter(entry -> "outer".equals(entry.get("target")))
			.collect(Collectors.toList());
		assertEquals(List.of("added", "busy", "removed"), events(outerLog), outerLog.toString());
		double spinnerDelay = time(outerLog, "busy") - time(outerLog, "added");
		assertTrue(spinnerDelay >= 290, "the spinner showed after " + spinnerDelay + " ms");
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void aNestedLocalVeilTakesTheRequestWithItsOwnTimings(Engine engine)
	{
		open(engine);

		click("Short request from the inner panel");
		wait.until(ExpectedConditions.textToBe(By.cssSelector(".inner-counter"), "1"));
		awaitNoVeil();

		List<Map<String, Object>> log = veilLog();
		assertEquals(List.of("added", "busy", "removed"), events(log), log.toString());
		assertTrue(log.stream().allMatch(entry -> "inner".equals(entry.get("target"))),
			"a veil other than the inner one was involved: " + log);
		double spinnerDelay = time(log, "busy") - time(log, "added");
		assertTrue(spinnerDelay >= 90 && spinnerDelay < 290,
			"the spinner showed after " + spinnerDelay + " ms instead of the configured 100 ms");
		double shown = time(log, "removed") - time(log, "busy");
		assertTrue(shown >= 980,
			"the spinner was shown for " + shown + " ms instead of the configured 1 s");
	}

	private WebElement outerPanel()
	{
		return driver.findElement(By.cssSelector(".veil-outer"));
	}

	private void open(Engine engine)
	{
		driver.get(String.format("http://localhost:%d/wicket-examples/ajax/veil", localPort));
		if (isJQueryLoaded() != (engine == Engine.JQUERY))
		{
			WebElement toggle = driver.findElement(By.partialLinkText("switch to"));
			toggle.click();
			wait.until(ExpectedConditions.stalenessOf(toggle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".veil-outer")));
		}
		assertEquals(engine == Engine.JQUERY, isJQueryLoaded(), "the Ajax engine");

		awaitQuiet();
		js().executeScript(RECORD_VEILS);
	}

	private boolean isJQueryLoaded()
	{
		return (Boolean)js().executeScript("return typeof window.jQuery === 'function';");
	}

	private void click(String linkText)
	{
		driver.findElement(By.linkText(linkText)).click();
	}

	private String pageCounter()
	{
		return driver.findElement(By.cssSelector(".page-counter")).getText();
	}

	private void awaitPageCounter(String value)
	{
		wait.until(ExpectedConditions.textToBe(By.cssSelector(".page-counter"), value));
	}

	private void awaitNoVeil()
	{
		wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".wicket-veil"), 0));
	}

	private void awaitQuiet()
	{
		js().executeAsyncScript(AWAIT_QUIET);
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> veilLog()
	{
		return (List<Map<String, Object>>)js().executeScript("return window.veilLog;");
	}

	private static List<Object> events(List<Map<String, Object>> log)
	{
		return log.stream().map(entry -> entry.get("event")).collect(Collectors.toList());
	}

	private static double time(List<Map<String, Object>> log, String event)
	{
		return log.stream()
			.filter(entry -> event.equals(entry.get("event")))
			.map(entry -> ((Number)entry.get("time")).doubleValue())
			.findFirst()
			.orElseThrow(() -> new AssertionError("no '" + event + "' in " + log));
	}

	private JavascriptExecutor js()
	{
		return (JavascriptExecutor)driver;
	}

	private static void sleep(Duration duration)
	{
		try
		{
			Thread.sleep(duration.toMillis());
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}
}
