// (c)2004 Templemore Technologies Limited. All Rights Reserved
// http://www.templemore.co.uk/copyright.html
package wicket.examples.hangman;

import wicket.examples.helloworld.HelloWorldTest;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import net.sourceforge.jwebunit.WebTestCase;
import nl.openedge.util.jetty.JettyDecorator;

/**
 * Testcase for the <code>Hangman</code> class.
 * 
 * @author Chris Turner
 * @version 1.0
 */
public class HangManTest extends WebTestCase {

	/**
	 * Create the test case.
	 * 
	 * @param message
	 *            The test name
	 */
	public HangManTest(String message) {
		super(message);
	}

	public void testHangmanWinGame() throws Exception {
		Hangman hangman = new Hangman(5, new String[] { "testing" });

		hangman.newGame();
		Assert.assertEquals(5, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isGuessed());
		Assert.assertFalse(hangman.isAllGuessesUsed());

		doGuessTest(hangman, 'a', false);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isGuessed());
		Assert.assertFalse(hangman.isAllGuessesUsed());

		hangman.guessLetter('a');
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isGuessed());
		Assert.assertFalse(hangman.isAllGuessesUsed());

		doGuessTest(hangman, 't', true);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isGuessed());
		Assert.assertFalse(hangman.isAllGuessesUsed());

		doGuessTest(hangman, 'e', true);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isGuessed());
		Assert.assertFalse(hangman.isAllGuessesUsed());

		doGuessTest(hangman, 's', true);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isGuessed());
		Assert.assertFalse(hangman.isAllGuessesUsed());

		doGuessTest(hangman, 'i', true);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isGuessed());
		Assert.assertFalse(hangman.isAllGuessesUsed());

		doGuessTest(hangman, 'n', true);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isGuessed());
		Assert.assertFalse(hangman.isAllGuessesUsed());

		doGuessTest(hangman, 'g', true);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertTrue(hangman.isGuessed());
		Assert.assertFalse(hangman.isAllGuessesUsed());
	}

	public void testHangmanLooseGame() throws Exception {
		Hangman hangman = new Hangman(2);

		hangman.newGame();
		Assert.assertEquals(2, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isGuessed());
		Assert.assertFalse(hangman.isAllGuessesUsed());

		doGuessTest(hangman, 'z', false);
		Assert.assertEquals(1, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isGuessed());
		Assert.assertFalse(hangman.isAllGuessesUsed());

		doGuessTest(hangman, 'v', false);
		Assert.assertEquals(0, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isGuessed());
		Assert.assertTrue(hangman.isAllGuessesUsed());
	}

	public void testHangmanSuccessWebGame() {
		getTestContext().setBaseUrl("http://localhost:8098/wicket-examples");
		beginAt("/hangman?setWord=hangman");

		assertTitleEquals("Wicket Hangman");
		assertLinkPresent("start");
		clickLink("start");
		assertTitleEquals("Wicket Hangman");
		assertTextPresent("guesses remaining");

		assertElementPresent("guessesRemaining");
		assertTextInElement("guessesRemaining", "5");
		
		clickLink("letter_f");
		
		assertElementPresent("guessesRemaining");
		assertTextInElement("guessesRemaining", "4");

		clickLink("letter_h");
		assertElementPresent("guessesRemaining");
		assertTextInElement("guessesRemaining", "4");

		clickLink("letter_a");
		clickLink("letter_n");
		clickLink("letter_g");
		clickLink("letter_m");
		assertTextPresent("Congratulations! You guessed that the word was ");
	}

	public void testHangmanFailureWebGame() {
		getTestContext().setBaseUrl("http://localhost:8098/wicket-examples");
		beginAt("/hangman?setWord=hangman");

		assertTitleEquals("Wicket Hangman");
		assertLinkPresent("start");
		clickLink("start");
		assertTitleEquals("Wicket Hangman");
		assertTextPresent("guesses remaining");

		assertElementPresent("guessesRemaining");
		assertTextInElement("guessesRemaining", "5");
		
		clickLink("letter_f");
		
		assertElementPresent("guessesRemaining");
		assertTextInElement("guessesRemaining", "4");

		assertLinkNotPresent("letter_f");
		clickLink("letter_x");
		assertTextInElement("guessesRemaining", "3");

		clickLink("letter_e");
		assertTextInElement("guessesRemaining", "2");

		clickLink("letter_t");
		assertTextInElement("guessesRemaining", "1");

		clickLink("letter_v");
		assertTextPresent("Bad Luck! You failed to guess that the word was");
	}

	private void doGuessTest(Hangman hangman, char c, boolean expected) {
		Assert.assertFalse(hangman.isGuessed(c));
		Assert.assertEquals(expected, hangman.guessLetter(c));
		Assert.assertTrue(hangman.isGuessed(c));
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(HangManTest.class);

		JettyDecorator deco = new JettyDecorator(suite);
		deco.setPort(8098);
		deco.setWebappContextRoot("src/webapp");
		deco.setContextPath("/wicket-examples");
		return deco;
	}
}
