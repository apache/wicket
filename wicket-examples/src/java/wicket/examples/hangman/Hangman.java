/*
 * $Id$ $Revision:
 * 1.3 $ $Date$
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

import java.io.Serializable;

/**
 * Implementation of the actual hangman game model. The model holds the word
 * generator, the current word, retries remaining and the correctLetters that have been
 * guessed. It also answers questions such as whether all retries have been
 * used.
 * 
 * @author Chris Turner
 * @author Jonathan Locke
 */
public class Hangman implements Serializable
{
	/** Serial version UID */
	private static final long serialVersionUID = 1L;

	/** Correct correctLetters */
	private char[] correctLetters;
	
	/** Letters guessed by the user */
	private boolean[] guessedLetters;
	
	/** Number of guesses allowed */
	private int guessesAllowed = 5;  // default
	
	/** Number of guesses remaining */
	private int guessesRemaining;
	
	/** The word being guessed by the user */
	private String word;
	
	/** Word generator */
	private WordGenerator wordGenerator;

	/**
	 * Get the state of the guessed correctLetters for the word.
	 * 
	 * @return The guessed correctLetters
	 */
	public String getCorrectLetters()
	{
		return new String(correctLetters);
	}

	/**
	 * Return the number of guesses remaining.
	 * 
	 * @return The number of guesses
	 */
	public int getGuessesRemaining()
	{
		return guessesRemaining;
	}

	/**
	 * Get the current word that is being guessed or has been guessed.
	 * 
	 * @return The current word
	 */
	public String getWord()
	{
		return word;
	}

	/**
	 * Guess the given letter for the current word. If the letter matches then
	 * the word is updated otherwise the guesses remaining counter is reduced.
	 * The letter guessed is also recorded.
	 * 
	 * @param letter
	 *            The letter being guessed
	 * @return Whether the letter was in the word or not
	 */
	public boolean guess(char letter)
	{
		letter = Character.toLowerCase(letter);
		boolean correctGuess = false;
		for (int i = 0; i < word.length(); i++)
		{
			if (word.charAt(i) == letter)
			{
				correctGuess = true;
				correctLetters[i] = letter;
			}
		}
		if (!correctGuess && guessedLetters[letter - 'a'] == false)
		{
			guessesRemaining--;
		}
		guessedLetters[letter - 'a'] = true;
		return correctGuess;
	}

	/**
	 * Return whether the user has guessed the given letter or not.
	 * 
	 * @param letter
	 *            The letter to check
	 * @return Whether this letter has been guessed or not
	 */
	public boolean isGuessed(char letter)
	{
		letter = Character.toLowerCase(letter);
		return guessedLetters[letter - 'a'];
	}

	/**
	 * Check whether the user has used up all of their guesses.
	 * 
	 * @return Whether all of the user's guesses have been used
	 */
	public boolean isLost()
	{
		return guessesRemaining == 0;
	}

	/**
	 * Check whether the user has successfully guessed all of the correctLetters in the
	 * word.
	 * 
	 * @return Whether all of the correctLetters have been guessed or not
	 */
	public boolean isWon()
	{
		for (int i = 0; i < correctLetters.length; i++)
		{
			if (correctLetters[i] == '_')
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Play again with same settings
	 */
	public void newGame()
	{
		newGame(guessesAllowed, new WordGenerator());
	}

	/**
	 * Initialise the hangman read for a new game.
	 * 
	 * @param guessesAllowed
	 *            Number of guesses allowed
	 * @param word
	 *            The word to use or null to pick randomly
	 */
	public void newGame(final int guessesAllowed, final String word)
	{
		newGame(guessesAllowed, new WordGenerator(new String[] { word }));
	}
	

	/**
	 * Initialise the hangman read for a new game.
	 * 
	 * @param guessesAllowed
	 *            Number of guesses allowed
	 * @param wordGenerator
	 *            The word generator
	 */
	public void newGame(final int guessesAllowed, final WordGenerator wordGenerator)
	{
		this.guessesAllowed = guessesAllowed;
		this.word = wordGenerator.nextWord().toLowerCase();
		correctLetters = new char[this.word.length()];
		for (int i = 0; i < correctLetters.length; i++)
		{
			correctLetters[i] = '_';
		}
		guessedLetters = new boolean[26];
		guessesRemaining = guessesAllowed;
	}
}
