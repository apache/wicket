/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
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

import java.awt.Color;
import java.io.Serializable;

import wicket.markup.html.image.Image;
import wicket.markup.html.image.resource.DefaultButtonImageResource;
import wicket.markup.html.link.Link;

/**
 * Model for a letter in the game of hangman
 * 
 * @author Jonathan Locke
 */
public class Letter implements Serializable
{

	/** Link to guess this letter */
	private SelectableLetterLink guessLink;
	/** True if the letter has been guessed */
	private boolean isGuessed;

	/** The letter */
	private char letter;

	/**
	 * Link representing a letter that can be selected in the game.
	 */
	private class SelectableLetterLink extends Link
	{
		/** The game this link is attached to */
		private final Game game;

		/**
		 * Create a new selectable letter link given the supplied parameters.
		 * 
		 * @param game
		 *            The game for this link
		 */
		public SelectableLetterLink(final Game game)
		{
			super("letter");

			// Save game
			this.game = game;

			// We want this link to be manually enabled
			setEnabled(true);
			setAutoEnable(false);

			// Install enabled button image
			DefaultButtonImageResource enabled = new DefaultButtonImageResource(30, 30, Letter.this
					.asString());
			add(new Image("enabled", enabled));

			// Add disabled image
			DefaultButtonImageResource disabled = new DefaultButtonImageResource(30, 30,
					Letter.this.asString());
			disabled.setColor(Color.GRAY);
			add(new Image("disabled", disabled));
		}

		/**
		 * Handle clicking of this link. Redirects the request cycle based on
		 * the current state of the game.
		 */
		public void onClick()
		{
			// Guess the letter
			game.guess(Letter.this);

			// This letter can no longer be guessed
			setEnabled(false);

			// Is the game over?
			if (game.isWon())
			{
				// Redirect to win page
				setResponsePage(new Win());
			}
			else if (game.isLost())
			{
				// Redirect to lose page
				setResponsePage(new Lose());
			}
			else
			{
				// Return to guess page with new state to display
			}
		}
	}

	/**
	 * Constructor
	 * 
	 * @param letter
	 *            The letter
	 */
	public Letter(final char letter)
	{
		this.letter = letter;
	}

	/**
	 * @return This letter as a string
	 */
	public String asString()
	{
		return Character.toString(letter);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object that)
	{
		if (that instanceof Letter)
		{
			return ((Letter)that).letter == this.letter;
		}
		return false;
	}

	/**
	 * @param game
	 *            The game this link is in
	 * @return Link that guesses this letter
	 */
	public Link getGuessLink(final Game game)
	{
		if (guessLink == null)
		{
			guessLink = new SelectableLetterLink(game);
		}
		return guessLink;
	}

	/**
	 * Guess this letter
	 */
	public void guess()
	{
		this.isGuessed = true;
	}

	/**
	 * @return Returns the isGuessed.
	 */
	public boolean isGuessed()
	{
		return isGuessed;
	}

	/**
	 * Resets this letter into the default state
	 */
	public void reset()
	{
		this.isGuessed = false;
		this.guessLink.setEnabled(true);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[Letter letter = " + letter + ", guessed = " + isGuessed + "]";
	}
}
