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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.basic.Label;
import wicket.markup.html.image.Image;
import wicket.markup.html.image.resource.DefaultButtonImageResource;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
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
		add(new Label("correctLetters", new PropertyModel(getHangman(), "correctLetters")));

		// Components for displaying the letters that can be selected
		final List letters = new ArrayList();
		for (char c = 'a'; c <= 'z'; c++)
		{
			letters.add(new Character(c));
		}	
		
		final ListView listView = new ListView("letters", letters)
		{
			protected void populateItem(ListItem listItem)
			{
				listItem.add(new SelectableLetterLink((Character)listItem.getModelObject()));
			}
		};

		listView.setOptimizeRenderProcess(true);
		add(listView);
	}

	/**
	 * Method to reset all of the selectable letters at the start of a game.
	 */
	public void resetLetters()
	{
		for (char c = 'a'; c <= 'z'; c++)
		{
			SelectableLetterLink link = (SelectableLetterLink)get("letter_" + c);
			link.setEnabled(true);
		}
	}

	/**
	 * Link representing a letter that can be selected in the game.
	 */
	private class SelectableLetterLink extends Link
	{
		/** The letter for this link */
		private char letter;

		/**
		 * Create a new selectable letter link given the supplied parameters.
		 * 
		 * @param letter
		 *            The letter that this link represents
		 */
		public SelectableLetterLink(final Character letter)
		{
			super("letter");
			
			// Save letter
			this.letter = letter.charValue();
			
			// We want this link to be manually enabled
			setEnabled(true);
			setAutoEnable(false);
			
			// Install enabled button image
			DefaultButtonImageResource enabled = new DefaultButtonImageResource(30, 30, letter.toString());
			add(new Image("enabled", enabled));
			
			// Add disabled image
			DefaultButtonImageResource disabled = new DefaultButtonImageResource(30, 30, letter.toString());
			disabled.setColor(Color.GRAY);
			add(new Image("disabled", disabled));
		}

		/**
		 * Handle clicking of this link. Redirects the request cycle based on
		 * the current state of the game.
		 */
		public void onClick()
		{
			log.error("Linked clicked for letter: " + letter);
			setEnabled(false);
			getHangman().guess(letter);
			if (getHangman().isWon())
			{
				// Redirect to win page
				setResponsePage(new Win(Guess.this));
			}
			else if (getHangman().isLost())
			{
				// Redirect to loose page
				setResponsePage(new Lose(Guess.this));
			}
			else
			{
				// Return to guess page with new state to display
			}
		}
	}
}
