/*
 * $Id$ $Revision$
 * $Date$
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.RequestCycle;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.model.PropertyModel;

/**
 * The main guess page for the hangman application.
 * 
 * @author Chris Turner
 * @author Jonathan Locke
 */
public class Guess extends HangmanPage
{
	private static Log log = LogFactory.getLog(Guess.class);

	/**
	 * Create the guess page.
	 */
	public Guess()
	{
		log.error("Created the guess page");

		// Components for displaying the guesses remaining & the hangman
		add(new Label("guessesRemaining", new PropertyModel(getHangman(), "guessesRemaining")));

		// Components for displaying the current word
		add(new Label("letters", new PropertyModel(getHangman(), "letters")));

		// Components for displaying the letters that can be selected
		for (int i = 0; i < 26; i++)
		{
			final char ch = (char)('a' + i);
			add(new SelectableLetterLink("letter_" + ch, ch));
		}
	}

	/**
	 * Method to reset all of the selectable letters at the start of a game.
	 */
	public void resetLetters()
	{
		for (int i = 0; i < 26; i++)
		{
			final char ch = (char)('a' + i);
			SelectableLetterLink link = (SelectableLetterLink)get("letter_" + ch);
			link.setEnabled(true);
		}
	}

	/**
	 * Link representing a letter that can be selected in the game.
	 */
	private class SelectableLetterLink extends Link
	{
		private char letter;

		/**
		 * Create a new selectable letter link given the supplied parameters.
		 * 
		 * @param componentName
		 *            The component name
		 * @param letter
		 *            The letter that this link represents
		 */
		public SelectableLetterLink(final String componentName, final char letter)
		{
			super(componentName);
			this.letter = letter;
			setEnabled(true);
			setAutoEnable(false);
			setAfterDisabledLink("<i>" + Character.toUpperCase(letter) + "</i>");
		}

		/**
		 * Handle clicking of this link. Redirects the request cycle based on
		 * the current state of the game.
		 */
		public void onClick()
		{
			final RequestCycle requestCycle = getRequestCycle();
			log.error("Linked clicked for letter: " + letter);
			setEnabled(false);
			getHangman().guessLetter(letter);
			if (getHangman().isGuessed())
			{
				// Redirect to win page
				requestCycle.setResponsePage(new Win(Guess.this));
			}
			else if (getHangman().isAllGuessesUsed())
			{
				// Redirect to loose page
				requestCycle.setResponsePage(new Lose(Guess.this));
			}
			// else return to guess page with new state to display
		}
	}
}
