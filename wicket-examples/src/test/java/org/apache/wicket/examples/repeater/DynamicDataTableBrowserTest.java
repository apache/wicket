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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.Map;

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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Drives {@link DynamicDataTablePage} in Chrome and in Firefox: the height of the body, the empty
 * column at the end, the toolbar's buttons and links, the veil over the table while it pages,
 * selects or runs a slow action, and where the edit form is shown. Runs with
 * {@code -Pselenium}, see {@link SeleniumBrowser}.
 */
@EnabledIfSystemProperty(named = "wicket.selenium", matches = "true")
class DynamicDataTableBrowserTest extends JettyTestCaseDecorator
{
	private static final String TABLE = "table.dynamic-dataview";

	private static final String ROWS = rows("");

	private static final String OVERLAY_WINDOW = ".dynamic-data-table-overlay-window";

	/**
	 * The extents of the table, of its scrolling body and of the page, and how far each header
	 * cell is from the cell of the first row below it.
	 */
	private static final String GEOMETRY = """
		var table = document.querySelector('table.dynamic-dataview');
		var body = table.querySelector('.dynamic-data-table-body');
		var box = function (element) { return element.getBoundingClientRect(); };
		var result = {
			body: !!body,
			tableWidth: box(table).width,
			parentWidth: table.parentElement.clientWidth,
			pageScrollWidth: document.documentElement.scrollWidth,
			pageWidth: document.documentElement.clientWidth,
			headTop: box(table.tHead).top,
			viewport: window.innerHeight
		};
		if (body) {
			var headers = table.querySelector('tr.dynamic-data-table-headers').cells;
			var row = body.querySelector('tbody > tr');
			result.height = body.offsetHeight;
			result.clientHeight = body.clientHeight;
			result.scrollHeight = body.scrollHeight;
			result.clientWidth = body.clientWidth;
			result.scrollWidth = body.scrollWidth;
			result.bodyTop = box(body).top;
			result.headBottom = box(table.tHead).bottom;
			result.misaligned = row ? Array.prototype.filter.call(row.cells, function (cell, i) {
				return Math.abs(box(cell).left - box(headers[i]).left) > 1;
			}).length : 0;
		}
		return result;
		""";

	/**
	 * Logs, into {@code window.veilLog}, when a veil is put over the table and when it shows its
	 * spinner.
	 */
	private static final String RECORD_VEILS = """
		window.veilLog = [];
		new MutationObserver(function (records) {
			records.forEach(function (record) {
				record.addedNodes.forEach(function (node) {
					if (node.classList && node.classList.contains('wicket-veil')) {
						window.veilLog.push('veil');
					}
				});
				if (record.type === 'attributes' &&
						record.target.classList.contains('wicket-veil-busy')) {
					window.veilLog.push('spinner');
				}
			});
		}).observe(document.body, { childList: true, subtree: true, attributes: true,
			attributeFilter: ['class'] });
		""";

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
	void onlyTheBodyScrollsWithTheConfiguredHeight(SeleniumBrowser browser)
	{
		open(browser);

		Map<String, Object> geometry = geometry();
		assertTrue((Boolean)geometry.get("body"), "the rows are not in a body of their own");
		assertAbout(800, number(geometry, "height"), "the height of the body " + geometry);
		assertTrue(number(geometry, "scrollHeight") > number(geometry, "clientHeight"),
			"25 rows fit into 800 pixels: " + geometry);
		assertAbout(number(geometry, "headBottom"), number(geometry, "bodyTop"),
			"the body is not right below the head " + geometry);
		assertEquals(0, number(geometry, "misaligned"), "misaligned columns " + geometry);
		double headTop = number(geometry, "headTop");

		js().executeScript("document.querySelector('.dynamic-data-table-body').scrollTop = 400;");

		assertAbout(headTop, number(geometry(), "headTop"), "the head moved with the rows");
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void aWidenedColumnWidensTheTableAndThePageScrollsToIt(SeleniumBrowser browser)
	{
		open(browser);

		WebElement edge = driver.findElements(By.cssSelector(TABLE + " thead .wicket-col-resizer"))
			.get(5);
		js().executeScript("arguments[0].focus();", edge);
		for (int i = 0; i < 40; i++)
		{
			new Actions(driver).sendKeys(Keys.ARROW_RIGHT).perform();
		}
		wait.until(d -> number(geometry(), "tableWidth") > number(geometry(), "parentWidth") + 300);

		Map<String, Object> geometry = geometry();
		assertTrue(number(geometry, "pageScrollWidth") > number(geometry, "pageWidth"),
			"the page does not scroll sideways " + geometry);
		assertEquals(0, number(geometry, "misaligned"), "misaligned columns " + geometry);
		assertAbout(number(geometry, "clientWidth"), number(geometry, "scrollWidth"),
			"the rows are clipped " + geometry);

		js().executeScript(
			"window.scrollTo(document.documentElement.scrollWidth, window.scrollY);");
		double right = number(js().executeScript(
			"var headers = document.querySelectorAll(" +
				"'table.dynamic-dataview tr.dynamic-data-table-headers > th');" +
				"return headers[headers.length - 2].getBoundingClientRect().right;"));
		assertTrue(right <= number(js().executeScript("return window.innerWidth;")),
			"the last column is out of reach: its right edge at " + right);
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void aMovedAndResizedColumnStaysSoAfterAReload(SeleniumBrowser browser)
	{
		open(browser);

		WebElement handle = headerCell("First Name").findElement(By.className("wicket-col-mover"));
		js().executeScript("arguments[0].focus();", handle);
		new Actions(driver).sendKeys(Keys.ARROW_RIGHT).perform();
		wait.until(d -> headerTexts().indexOf("First Name") > headerTexts().indexOf("Last Name"));
		awaitNoVeil();

		WebElement edge = headerCell("Address").findElement(By.className("wicket-col-resizer"));
		double before = headerCell("Address").getRect().getWidth();
		js().executeScript("arguments[0].focus();", edge);
		for (int i = 0; i < 10; i++)
		{
			new Actions(driver).sendKeys(Keys.ARROW_RIGHT).perform();
		}
		wait.until(d -> headerCell("Address").getRect().getWidth() >= before + 95);
		double resized = headerCell("Address").getRect().getWidth();
		List<String> order = headerTexts();

		driver.navigate().refresh();
		wait.until(d -> driver.findElements(By.cssSelector(ROWS)).size() == 25);

		assertEquals(order, headerTexts(), "the order after a reload");
		assertAbout(resized, headerCell("Address").getRect().getWidth(),
			"the width after a reload");
		assertEquals(0, number(geometry(), "misaligned"), "misaligned columns " + geometry());
	}

	private List<String> headerTexts()
	{
		return driver.findElements(By.cssSelector(TABLE + " tr.dynamic-data-table-headers > th"))
			.stream()
			.map(header -> header.getText().replaceAll("[▲▼]", "").trim())
			.toList();
	}

	private WebElement headerCell(String text)
	{
		return driver.findElements(By.cssSelector(TABLE + " tr.dynamic-data-table-headers > th"))
			.stream()
			.filter(header -> header.getText().replaceAll("[▲▼]", "").trim().equals(text))
			.findFirst()
			.orElseThrow();
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void aPageWithFewerRowsKeepsTheHeight(SeleniumBrowser browser)
	{
		open(browser);

		WebElement filter = driver.findElement(By.cssSelector(TABLE + " input[type=search]"));
		filter.sendKeys("nobody is called like this");
		wait.until(d -> driver.findElements(By.cssSelector(ROWS)).isEmpty());
		awaitNoVeil();

		Map<String, Object> geometry = geometry();
		assertAbout(800, number(geometry, "height"), "the height changed " + geometry);
		assertAbout(number(geometry, "clientHeight"), number(geometry, "scrollHeight"),
			"an empty page scrolls " + geometry);
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void theBodyHeightCanBeAPartOfTheWindowOrUnset(SeleniumBrowser browser)
	{
		open(browser);

		choose("options:bodyHeightUnits", "VIEWPORT_HEIGHT");
		type("options:bodyHeight", "50");

		Map<String, Object> geometry = geometry();
		assertAbout(number(geometry, "viewport") / 2, number(geometry, "height"),
			"half the window " + geometry);

		type("options:bodyHeight", "-1");

		assertEquals(Boolean.FALSE, geometry().get("body"), "the rows are still in their own body");
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void rowsEndWithAnEmptyCellAlsoWhenTheColumnsCannotBeResized(SeleniumBrowser browser)
	{
		open(browser);

		choose("options:resize", "NONE");

		assertEquals(1, driver.findElements(By.cssSelector(TABLE +
			" > thead th.dynamic-data-table-filler")).size(), "the header's empty cell");
		// read in one go, as pushes repaint the rows all the time
		@SuppressWarnings("unchecked")
		List<Boolean> endsWithFiller = (List<Boolean>)js().executeScript(
			"return Array.prototype.map.call(document.querySelectorAll(arguments[0]), " +
				"function (row) { return row.lastElementChild.classList.contains(" +
				"'dynamic-data-table-filler'); });", ROWS);
		assertEquals(25, endsWithFiller.size());
		assertTrue(endsWithFiller.stream().allMatch(Boolean::booleanValue),
			"a row without the empty cell " + endsWithFiller);
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void theToolbarActionsAreFivePixelsApart(SeleniumBrowser browser)
	{
		open(browser);

		@SuppressWarnings("unchecked")
		List<Number> gaps = (List<Number>)js().executeScript("""
			var actions = document.querySelectorAll(
				'table.dynamic-dataview .dynamic-data-table-actions')[0].children;
			var gaps = [];
			for (var i = 1; i < actions.length; i++) {
				gaps.push(actions[i].getBoundingClientRect().left -
					actions[i - 1].getBoundingClientRect().right);
			}
			return gaps;
			""");
		assertEquals(3, gaps.size(), "add, the slow action, columns and CSV export");
		for (Number gap : gaps)
		{
			assertAbout(5, gap.doubleValue(), "the gap between two actions " + gaps);
		}
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void thePagingLinksAreApartAndHighlightedUnderThePointer(SeleniumBrowser browser)
	{
		open(browser);

		WebElement page2 = navigatorLink("Go to page 2");
		WebElement page3 = navigatorLink("Go to page 3");
		double gap = number(page3.getRect().getX()) -
			number(page2.getRect().getX() + page2.getRect().getWidth());
		assertTrue(gap >= 1, "the links are " + gap + " pixels apart");

		String background = page2.getCssValue("background-color");
		String color = page2.getCssValue("color");
		new Actions(driver).moveToElement(page2).perform();
		wait.until(d -> !background.equals(page2.getCssValue("background-color")));

		assertNotEquals(color, page2.getCssValue("color"), "the text color under the pointer");
		assertEquals(background, page3.getCssValue("background-color"),
			"a link not under the pointer changed");
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void pagingAndSelectingVeilTheTable(SeleniumBrowser browser)
	{
		open(browser);
		js().executeScript(RECORD_VEILS);

		assertVeiled("paging", () -> navigatorLink("Go to page 2").click());
		assertVeiled("selecting a row",
			() -> click(By.cssSelector(rows(":first-child input[type=checkbox]"))));
		assertVeiled("selecting the page", () -> driver.findElement(
			By.cssSelector(TABLE + " > thead input[type=checkbox]")).click());
		assertVeiled("selecting all", () -> driver.findElement(
			By.cssSelector(TABLE + " tr.dynamic-data-table-selection-toolbar a")).click());
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void theSlowActionShowsTheSpinner(SeleniumBrowser browser)
	{
		open(browser);
		js().executeScript(RECORD_VEILS);

		driver.findElements(By.cssSelector(TABLE +
			" button[title='A slow action, taking two seconds']")).get(0).click();

		wait.until(d -> veilLog().contains("spinner"));
		awaitNoVeil();
		assertTrue(driver.findElement(By.className("feedbackPanel")).getText()
			.contains("The slow action is done."));
	}

	@ParameterizedTest
	@EnumSource(SeleniumBrowser.class)
	void theEditFormCoversThePageOrTheTable(SeleniumBrowser browser)
	{
		open(browser);
		js().executeScript(
			"document.querySelector('.dynamic-data-table-body').scrollTop = 600;");

		Map<String, Object> overlay = editFirstVisibleRow();
		assertAbout(0, number(overlay, "top"), "the veil covers the window " + overlay);
		assertAbout(number(overlay, "viewportHeight"), number(overlay, "height"),
			"the veil covers the window " + overlay);
		assertAbout(number(overlay, "viewportHeight") / 2,
			number(overlay, "windowTop") + number(overlay, "windowHeight") / 2,
			"the form is in the middle of the window " + overlay);
		driver.findElement(By.cssSelector(TABLE + " .wicket-floating-panel-close")).click();
		wait.until(d -> driver.findElements(By.cssSelector(TABLE + " " + OVERLAY_WINDOW))
			.isEmpty());

		choose("options:overlay", "LOCAL");

		overlay = editFirstVisibleRow();
		assertAbout(number(overlay, "tableTop"), number(overlay, "top"),
			"the veil covers the table " + overlay);
		assertAbout(number(overlay, "tableHeight"), number(overlay, "height"),
			"the veil covers the table " + overlay);
	}

	private Map<String, Object> editFirstVisibleRow()
	{
		click(By.cssSelector(rows(" button[title='Edit']")));
		wait.until(d -> !driver.findElements(By.cssSelector(TABLE + " " + OVERLAY_WINDOW))
			.isEmpty());
		awaitNoVeil();
		@SuppressWarnings("unchecked")
		Map<String, Object> overlay = (Map<String, Object>)js().executeScript("""
			var overlay = document.querySelector('caption.dynamic-data-table-overlay');
			var box = overlay.getBoundingClientRect();
			var window = overlay.querySelector('.dynamic-data-table-overlay-window')
				.getBoundingClientRect();
			var table = overlay.closest('table').getBoundingClientRect();
			return { top: box.top, height: box.height, windowTop: window.top,
				windowHeight: window.height, tableTop: table.top, tableHeight: table.height,
				viewportHeight: document.documentElement.clientHeight };
			""");
		return overlay;
	}

	/**
	 * Clicks the first element found, in the middle of the view so that it is not under the sticky
	 * head of the table, again if the rows were repainted by a push before the click.
	 */
	private void click(By locator)
	{
		wait.until(d -> {
			try
			{
				WebElement element = driver.findElements(locator).get(0);
				js().executeScript("arguments[0].scrollIntoView({ block: 'center' });", element);
				element.click();
				return true;
			}
			catch (StaleElementReferenceException repainted)
			{
				return false;
			}
		});
	}

	private void assertVeiled(String action, Runnable click)
	{
		js().executeScript("window.veilLog = [];");
		click.run();
		wait.until(d -> veilLog().contains("veil"));
		awaitNoVeil();
	}

	private List<?> veilLog()
	{
		return (List<?>)js().executeScript("return window.veilLog;");
	}

	/**
	 * @return a selector of the rows, in the scrolling body or in the table itself, followed by the
	 *         given selector
	 */
	private static String rows(String then)
	{
		return TABLE + " .dynamic-data-table-rows > tbody > tr" + then + ", " + TABLE +
			" > tbody > tr:not(.dynamic-data-table-body-row)" + then;
	}

	private void open(SeleniumBrowser browser)
	{
		driver = browser.newDriver(1400, 1100);
		wait = new WebDriverWait(driver, Duration.ofSeconds(10), Duration.ofMillis(20));
		wait.ignoring(StaleElementReferenceException.class);
		driver.get(
			"http://localhost:" + localPort + "/wicket-examples/repeater/dynamic-data-table");
		wait.until(d -> driver.findElements(By.cssSelector(ROWS)).size() == 25);
		awaitNoVeil();
	}

	private void choose(String name, String value)
	{
		WebElement table = driver.findElement(By.cssSelector(TABLE));
		new Select(driver.findElement(By.name(name))).selectByIndex(indexOf(name, value));
		awaitRenderedAgain(table);
	}

	private int indexOf(String name, String value)
	{
		return switch (name)
		{
			case "options:bodyHeightUnits" -> List.of("PIXELS", "PERCENT", "VIEWPORT_HEIGHT")
				.indexOf(value);
			case "options:resize" -> List.of("NONE", "STRETCH").indexOf(value);
			case "options:overlay" -> List.of("LOCAL", "BODY").indexOf(value);
			default -> throw new IllegalArgumentException(name);
		};
	}

	private void type(String name, String value)
	{
		WebElement table = driver.findElement(By.cssSelector(TABLE));
		WebElement field = driver.findElement(By.name(name));
		field.clear();
		field.sendKeys(value, Keys.TAB);
		awaitRenderedAgain(table);
	}

	/**
	 * Waits for the Ajax response replacing the given element of the table after an option
	 * changed, and for the rows of the new table.
	 */
	private void awaitRenderedAgain(WebElement table)
	{
		wait.until(d -> {
			try
			{
				table.isDisplayed();
				return false;
			}
			catch (StaleElementReferenceException replaced)
			{
				return true;
			}
		});
		wait.until(d -> driver.findElements(By.cssSelector(ROWS)).size() == 25);
		awaitNoVeil();
	}

	private void awaitNoVeil()
	{
		wait.until(d -> driver.findElements(By.cssSelector(".wicket-veil")).isEmpty());
	}

	private WebElement navigatorLink(String title)
	{
		return driver.findElements(By.cssSelector(TABLE + " .navigator a[title='" + title + "']"))
			.get(0);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> geometry()
	{
		return (Map<String, Object>)js().executeScript(GEOMETRY);
	}

	private static double number(Map<String, Object> values, String name)
	{
		return number(values.get(name));
	}

	private static double number(Object value)
	{
		return ((Number)value).doubleValue();
	}

	private static void assertAbout(double expected, double actual, String message)
	{
		assertTrue(Math.abs(expected - actual) <= 2, message + ": expected about " + expected +
			" but was " + actual);
	}

	private JavascriptExecutor js()
	{
		return (JavascriptExecutor)driver;
	}
}
