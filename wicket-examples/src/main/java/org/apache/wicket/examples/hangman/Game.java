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
package org.apache.wicket.examples.hangman;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.io.IClusterable;


/**
 * Implementation of the actual hangman game model. The model holds the word generator, the current
 * word, retries remaining and the correctLetters that have been guessed. It also answers questions
 * such as whether all retries have been used.
 * 
 * @author Chris Turner
 * @author Jonathan Locke
 */
public class Game implements IClusterable
{
	/** Number of guesses allowed */
	private int guessesAllowed;

	/** Number of guesses remaining */
	private int guessesRemaining;

	/** The letters */
	private final List<Letter> letters = new ArrayList<Letter>();

	/** The word being guessed by the user */
	private Word word;

	/** Word generator */
	private WordGenerator wordGenerator;

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
	 * @return The letters in the game
	 */
	public List<Letter> getLetters()
	{
		return letters;
	}

	/**
	 * Get the current word that is being guessed or has been guessed.
	 * 
	 * @return The current word
	 */
	public Word getWord()
	{
		return word;
	}

	/**
	 * Guess the given letter for the current word. If the letter matches then the word is updated
	 * otherwise the guesses remaining counter is reduced. The letter guessed is also recorded.
	 * 
	 * @param letter
	 *            The letter being guessed
	 * @return True if guess was correct
	 */
	public boolean guess(final Letter letter)
	{
		if (!letter.isGuessed())
		{
			final boolean correct = word.guess(letter);
			if (!correct)
			{
				guessesRemaining--;
			}
			return correct;
		}
		return false;
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
	 * Check whether the user has successfully guessed all of the correctLetters in the word.
	 * 
	 * @return Whether all of the correctLetters have been guessed or not
	 */
	public boolean isWon()
	{
		return word.isGuessed();
	}

	/**
	 * Play another game with same settings
	 */
	public void newGame()
	{
		newGame(guessesAllowed, wordGenerator);
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
		this.guessesRemaining = guessesAllowed;
		this.word = wordGenerator.next();
		this.wordGenerator = wordGenerator;

		// Add letters
		letters.clear();
		for (char c = 'a'; c <= 'z'; c++)
		{
			letters.add(new Letter(c));
		}
	}
}
