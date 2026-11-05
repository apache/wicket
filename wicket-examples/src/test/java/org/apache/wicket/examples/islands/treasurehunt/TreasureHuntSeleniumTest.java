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
package org.apache.wicket.examples.islands.treasurehunt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Plays the Treasure Hunt in a headless Chrome, with either Ajax engine.
 * <p>
 * Needs Chrome, so it only runs with {@code -Dwicket.selenium=true}, and the examples' JavaScript,
 * so it is skipped unless the build ran with {@code -Pjs-test}. Every test also checks that the
 * browser logged no errors, which includes violations of the content security policy.
 */
@EnabledIfSystemProperty(named = "wicket.selenium", matches = "true")
class TreasureHuntSeleniumTest extends JettyTestCaseDecorator
{
	private static final String BOARD = "[data-wicket-island=treasure-board]";

	private static final String BOARD_ROOT = BOARD + " [data-wicket-island-root]";

	private static final String CELLS = BOARD + " button.cell";

	private static final String HISTORY = ".probe-history";

	private static final String PLAYER_ENTRY = ".player-entry";

	private static final String HIGH_SCORES = ".highscore-list";

	/**
	 * Marks the element so a later check can tell whether it is still the same one.
	 */
	private static final String MARK =
		"document.querySelector(arguments[0]).__mark = arguments[1];";

	private static final String READ_MARK = """
		var element = document.querySelector(arguments[0]);
		return element && element.__mark ? element.__mark : null;
		""";

	private boolean started;

	private WebDriver driver;

	private WebDriverWait wait;

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
		options.addArguments("--headless=new", "--window-size=1280,1024");
		options.setCapability(ChromeOptions.LOGGING_PREFS, logging);
		driver = new ChromeDriver(options);
		wait = new WebDriverWait(driver, Duration.ofSeconds(10), Duration.ofMillis(20));
		// Wicket replaces the labels on every probe, possibly between finding and reading one
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

	@ParameterizedTest
	@EnumSource(Engine.class)
	void aProbeUpdatesTheBoardTheHistoryAndTheWicketComponents(Engine engine)
	{
		open(engine);
		assertEquals(100, cells().size());
		assertEquals("20", probesLeft());
		mark(BOARD_ROOT, "first");

		probe(0, 0);

		assertEquals("19", probesLeft());
		assertEquals(1, driver.findElements(By.cssSelector(HISTORY + " li")).size());
		assertEquals("first", readMark(BOARD_ROOT), "the board was mounted again");
		assertNoLeakedIslands();
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void theBoardAndTheHistoryShareTheHoveredCell(Engine engine)
	{
		open(engine);
		probe(3, 4);

		hover(HISTORY + " li[data-x='3'][data-y='4']");
		wait.until(ExpectedConditions.attributeContains(cell(3, 4), "class", "hover"));

		hover(BOARD + " button.cell[data-x='5'][data-y='5']");
		wait.until(ExpectedConditions.not(
			ExpectedConditions.attributeContains(cell(3, 4), "class", "hover")));
		hover(BOARD + " button.cell[data-x='3'][data-y='4']");
		wait.until(ExpectedConditions.attributeContains(
			By.cssSelector(HISTORY + " li[data-x='3'][data-y='4']"), "class", "hover"));
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void aNewGameKeepsTheIslandsMounted(Engine engine)
	{
		open(engine);
		probe(0, 0);
		String key = boardKey();
		mark(BOARD_ROOT, "first");

		driver.findElement(By.linkText("New game")).click();
		wait.until(driver -> driver.findElements(By.cssSelector(CELLS + ".probed, " + CELLS +
			".found")).isEmpty());

		assertEquals("20", probesLeft());
		assertEquals(key, boardKey());
		assertEquals("first", readMark(BOARD_ROOT), "the board was mounted again");
		assertNoLeakedIslands();
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void aNewBoardSizeMountsANewBoard(Engine engine)
	{
		open(engine);
		String key = boardKey();
		mark(BOARD_ROOT, "first");

		selectSize("SMALL");

		assertNotEquals(key, boardKey());
		assertEquals(null, readMark(BOARD_ROOT), "the old board was kept");
		probe(1, 1);
		assertEquals("15", probesLeft());
		assertNoLeakedIslands();
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void aProbeFromAStaleTabIsAnsweredAsExpired(Engine engine)
	{
		open(engine);
		String staleTab = driver.getWindowHandle();
		String url = driver.getCurrentUrl();

		// rendering the page in a second tab makes the first one stale
		driver.switchTo().newWindow(WindowType.TAB);
		driver.get(url);
		awaitIslands();
		driver.switchTo().window(staleTab);

		cell(2, 2).click();

		WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.cssSelector(BOARD + " .message")));
		assertTrue(message.getText().contains("Reload"), message.getText());
		assertEquals("20", probesLeft());
		assertTrue(cells().stream()
			.allMatch(cell -> "true".equals(cell.getDomAttribute("aria-disabled"))),
			"the stale board can still be played");

		message.findElement(By.tagName("button")).click();
		wait.until(ExpectedConditions.stalenessOf(message));
		awaitIslands();
		probe(2, 2);
		assertEquals("19", probesLeft());
		assertNoBrowserErrors();
	}

	@ParameterizedTest
	@EnumSource(Engine.class)
	void aScoreIsValidatedSavedAndListed(Engine engine)
	{
		open(engine);
		selectSize("SMALL");
		wait.until(ExpectedConditions.textToBePresentInElementLocated(
			By.cssSelector(HIGH_SCORES), "No scores yet"));

		playUntilTheGameIsOver();
		WebElement name = wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.cssSelector(PLAYER_ENTRY + " input")));

		save();
		wait.until(ExpectedConditions.presenceOfElementLocated(
			By.cssSelector(PLAYER_ENTRY + " .errors li")));
		List<String> islandErrors = texts(PLAYER_ENTRY + " .errors li");
		assertEquals(1, islandErrors.size(), islandErrors.toString());
		assertEquals(islandErrors, texts(".feedbackPanel li"));

		mark("[data-wicket-island=player-entry] [data-wicket-island-root]", "entry");
		name.sendKeys("A");
		save();
		wait.until(ExpectedConditions.textToBePresentInElementLocated(
			By.cssSelector(PLAYER_ENTRY + " .errors"), "2 to 20"));
		assertEquals("A", name.getDomProperty("value"), "the typed name was lost");
		assertEquals("entry",
			readMark("[data-wicket-island=player-entry] [data-wicket-island-root]"),
			"the field was mounted again");

		name.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE, "</script><b>Ann</b>");
		WebElement colour = driver.findElement(
			By.cssSelector(PLAYER_ENTRY + " .swatch:nth-child(4)"));
		colour.click();
		wait.until(ExpectedConditions.attributeToBe(colour, "aria-checked", "true"));
		String chosenColour = colour.getCssValue("background-color");
		save();
		wait.until(ExpectedConditions.textToBePresentInElementLocated(
			By.cssSelector(".treasure-score"), "Saved for </script><b>Ann</b>."));
		wait.until(ExpectedConditions.numberOfElementsToBe(
			By.cssSelector(HIGH_SCORES + " li"), 1));
		assertEquals("</script><b>Ann</b>",
			driver.findElement(By.cssSelector(HIGH_SCORES + " li .name")).getText());
		assertEquals(chosenColour, driver.findElement(By.cssSelector(HIGH_SCORES + " li .dot"))
			.getCssValue("background-color"));
		assertTrue(driver.findElements(By.cssSelector(".treasure-score b, " + HIGH_SCORES + " b"))
			.isEmpty(), "the name was rendered as markup");
		assertNoLeakedIslands();
		assertNoBrowserErrors();
	}

	private void open(Engine engine)
	{
		driver.get(String.format("http://localhost:%d/wicket-examples/treasurehunt", localPort));
		if (isJQueryLoaded() != (engine == Engine.JQUERY))
		{
			WebElement toggle = driver.findElement(By.partialLinkText("switch to"));
			toggle.click();
			wait.until(ExpectedConditions.stalenessOf(toggle));
		}
		assertEquals(engine == Engine.JQUERY, isJQueryLoaded(), "the Ajax engine");
		awaitIslands();
	}

	private void awaitIslands()
	{
		wait.until(driver -> !cells().isEmpty() &&
			!driver.findElements(By.cssSelector(HISTORY)).isEmpty() &&
			!driver.findElements(By.cssSelector(HIGH_SCORES)).isEmpty());
	}

	private boolean isJQueryLoaded()
	{
		return (Boolean)js().executeScript("return typeof window.jQuery === 'function';");
	}

	private List<WebElement> cells()
	{
		return driver.findElements(By.cssSelector(CELLS));
	}

	private By cellSelector(int x, int y)
	{
		return By.cssSelector(CELLS + "[data-x='" + x + "'][data-y='" + y + "']");
	}

	private WebElement cell(int x, int y)
	{
		return driver.findElement(cellSelector(x, y));
	}

	/**
	 * Probes a cell and waits for the answer, which marks the cell as probed or found.
	 */
	private void probe(int x, int y)
	{
		String before = probesLeft();
		cell(x, y).click();
		wait.until(driver -> !before.equals(probesLeft()));
		wait.until(driver -> {
			String classes = driver.findElement(cellSelector(x, y)).getDomAttribute("class");
			return classes.contains("probed") || classes.contains("found");
		});
	}

	private void playUntilTheGameIsOver()
	{
		for (int i = 0; i < 64 && status().contains("left to find"); i++)
		{
			probe(i % 8, i / 8);
		}
		assertFalse(status().contains("left to find"), "the game did not end");
	}

	private void selectSize(String size)
	{
		String key = boardKey();
		new Select(driver.findElement(By.cssSelector("select"))).selectByValue(size);
		wait.until(driver -> !key.equals(boardKey()));
		awaitIslands();
	}

	private void save()
	{
		driver.findElement(By.cssSelector(".treasure-score button[name]")).click();
	}

	private void hover(String selector)
	{
		new Actions(driver).moveToElement(driver.findElement(By.cssSelector(selector))).perform();
	}

	private String status()
	{
		return driver.findElement(By.cssSelector(".treasure-status")).getText();
	}

	private String probesLeft()
	{
		return driver.findElement(By.cssSelector(".treasure-probes-left")).getText();
	}

	private String boardKey()
	{
		return driver.findElement(By.cssSelector(BOARD)).getDomAttribute("data-wicket-island-key");
	}

	private List<String> texts(String selector)
	{
		return driver.findElements(By.cssSelector(selector))
			.stream()
			.map(WebElement::getText)
			.collect(Collectors.toList());
	}

	private void mark(String selector, String mark)
	{
		js().executeScript(MARK, selector, mark);
	}

	private Object readMark(String selector)
	{
		return js().executeScript(READ_MARK, selector);
	}

	/**
	 * Every island the runtime has mounted still has its host in the document.
	 */
	private void assertNoLeakedIslands()
	{
		Object counts = js().executeScript("""
			return [globalThis.__wicketIslandsRuntime.mounted.size,
				document.querySelectorAll('[data-wicket-island]').length];
			""");
		List<?> mountedAndHosts = (List<?>)counts;
		assertEquals(mountedAndHosts.get(1), mountedAndHosts.get(0),
			"mounted islands versus hosts in the page");
	}

	private void assertNoBrowserErrors()
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

	private JavascriptExecutor js()
	{
		return (JavascriptExecutor)driver;
	}
}
