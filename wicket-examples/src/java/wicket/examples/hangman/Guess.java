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

import wicket.markup.html.basic.Label;
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
	/**
	 * Create the guess page.
	 */
	public Guess()
	{
		// Components for displaying the guesses remaining & the hangman
		add(new Label("guessesRemaining", new PropertyModel(getGame(), "guessesRemaining")));

		// Components for displaying the current word
		add(new Label("word", new PropertyModel(getGame(), "word.asString(true)")));

		// Show the game's letters
		add(new ListView("letters", getGame().getLetters())
		{
			protected void populateItem(ListItem listItem)
			{
				final Letter letter = (Letter)listItem.getModelObject();
				listItem.add(letter.getGuessLink(getGame()));
			}
		});
	}
}
