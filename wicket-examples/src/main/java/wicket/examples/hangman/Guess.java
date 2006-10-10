/*
 * $Id$
 * $Revision$ $Date$
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

import wicket.AttributeModifier;
import wicket.markup.html.basic.Label;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.Model;
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
		new Label(this, "guessesRemaining", new PropertyModel(getGame(), "guessesRemaining"));

		// Components for displaying the current word
		new Label(this, "word", new Model()
		{
			@Override
			public Object getObject()
			{
				return getGame().getWord().asString(true);
			}
		});

		// Show the game's letters
		new ListView<Letter>(this, "letters", getGame().getLetters())
		{
			@Override
			protected void populateItem(final ListItem<Letter> listItem)
			{
				final Letter letter = listItem.getModelObject();
				final Link link = new Link(listItem, "letter")
				{
					@Override
					public void onClick()
					{
						// Guess the letter
						getGame().guess(letter);

						// Is the game over?
						if (getGame().isWon())
						{
							// Redirect to win page
							setResponsePage(new Win());
						}
						else if (getGame().isLost())
						{
							// Redirect to lose page
							setResponsePage(new Lose());
						}
						else
						{
							// Return to guess page with new state to display
						}
					}

					@Override
					protected void onAttach()
					{
						// Set enable state of link
						setAutoEnable(false);
						setEnabled(!letter.isGuessed());
					}
				};
				link.add(new AttributeModifier("id", true, new Model<String>("letter_"
						+ letter.asString())));
				new Image(link, "image", letter.getSharedImageResource());
			}
		};
	}
}
