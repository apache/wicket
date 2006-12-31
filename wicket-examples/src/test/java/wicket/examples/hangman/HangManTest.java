/*
 * $Id: HangManTest.java 5395 2006-04-16 13:42:28 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-16 13:42:28 +0000 (Sun, 16 Apr
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.hangman;

import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.Test;
import wicket.examples.WicketWebTestCase;

/**
 * Testcase for the <code>Game</code> class.
 * 
 * @author Chris Turner
 * @version 1.0
 */
public class HangManTest extends WicketWebTestCase
{
	/**
	 * 
	 * @return Test
	 */
	public static Test suite()
	{
		return suite(HangManTest.class);
	}

	/**
	 * Create the test case.
	 * 
	 * @param message
	 *            The test name
	 */
	public HangManTest(final String message)
	{
		super(message);
	}

	/**
	 * Tests the hangman class directly for a winning game.
	 * 
	 * @throws Exception
	 */
	public void test_1() throws Exception
	{
		Game hangman = new Game();
		hangman.newGame(5, new WordGenerator(new String[] { "testing" }));

		Assert.assertEquals(5, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isWon());
		Assert.assertFalse(hangman.isLost());

		doGuessTest(hangman, 'a', false);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isWon());
		Assert.assertFalse(hangman.isLost());

		guess(hangman, 'a');
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isWon());
		Assert.assertFalse(hangman.isLost());

		doGuessTest(hangman, 't', true);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isWon());
		Assert.assertFalse(hangman.isLost());

		doGuessTest(hangman, 'e', true);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isWon());
		Assert.assertFalse(hangman.isLost());

		doGuessTest(hangman, 's', true);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isWon());
		Assert.assertFalse(hangman.isLost());

		doGuessTest(hangman, 'i', true);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isWon());
		Assert.assertFalse(hangman.isLost());

		doGuessTest(hangman, 'n', true);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isWon());
		Assert.assertFalse(hangman.isLost());

		doGuessTest(hangman, 'g', true);
		Assert.assertEquals(4, hangman.getGuessesRemaining());
		Assert.assertTrue(hangman.isWon());
		Assert.assertFalse(hangman.isLost());
	}

	private Letter letter(final Game hangman, final char c)
	{
		for (Iterator iter = hangman.getLetters().iterator(); iter.hasNext();)
		{
			Letter letter = (Letter)iter.next();
			if (letter.asString().equalsIgnoreCase(Character.toString(c)))
			{
				return letter;
			}
		}
		return null;
	}

	private boolean guess(final Game hangman, final char c)
	{
		return hangman.guess(letter(hangman, c));
	}

	/**
	 * Tests the hangman class directly for a lost game.
	 * 
	 * @throws Exception
	 */
	public void testHangmanLoseGame() throws Exception
	{
		Game hangman = new Game();
		hangman.newGame(2, new WordGenerator(new String[] { "foo" }));

		Assert.assertEquals(2, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isWon());
		Assert.assertFalse(hangman.isLost());

		doGuessTest(hangman, 'z', false);
		Assert.assertEquals(1, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isWon());
		Assert.assertFalse(hangman.isLost());

		doGuessTest(hangman, 'v', false);
		Assert.assertEquals(0, hangman.getGuessesRemaining());
		Assert.assertFalse(hangman.isWon());
		Assert.assertTrue(hangman.isLost());
	}

	/**
	 * Tests the webapplication for a successful match.
	 */
	public void testHangmanSuccessWebGame()
	{
		beginAt("/hangman?word=hangman");

		assertTitleEquals("Wicket Examples - hangman");
		assertLinkPresent("start");
		clickLink("start");
		assertTitleEquals("Wicket Examples - hangman");
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

	/**
	 * Tests the webapplication for an unsuccessful match.
	 */
	public void testHangmanFailureWebGame()
	{
		beginAt("/hangman?word=hangman");

		assertTitleEquals("Wicket Examples - hangman");
		assertLinkPresent("start");
		clickLink("start");
		assertTitleEquals("Wicket Examples - hangman");
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
		assertTextPresent("Bad luck. You failed to guess that the word was");
	}

	/**
	 * Performs a guess.
	 * 
	 * @param hangman
	 * @param c
	 * @param expected
	 */
	private void doGuessTest(final Game hangman, final char c, final boolean expected)
	{
		Assert.assertFalse(letter(hangman, c).isGuessed());
		Assert.assertEquals(expected, guess(hangman, c));
		Assert.assertTrue(letter(hangman, c).isGuessed());
	}
}
