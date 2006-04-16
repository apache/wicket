/*
 * $Id$ $Revision$ $Date:
 * 2005-11-07 20:59:30 +0100 (Mo, 07 Nov 2005) $
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
		add(new Label("guessesRemaining", new PropertyModel(getGame(), "guessesRemaining")));

		// Components for displaying the current word
		add(new Label("word", new Model()
		{
			public Object getObject(wicket.Component component)
			{
				return getGame().getWord().asString(true);
			}
		}));

		// Show the game's letters
		add(new ListView("letters", getGame().getLetters())
		{
			protected void populateItem(final ListItem listItem)
			{
				final Letter letter = (Letter)listItem.getModelObject();
				final Link link = new Link("letter")
				{
					protected void onBeginRequest()
					{
						// Set enable state of link
						setAutoEnable(false);
						setEnabled(!letter.isGuessed());
					}

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
				};
				link
						.add(new AttributeModifier("id", true, new Model("letter_"
								+ letter.asString())));
				link.add(new Image("image", letter.getSharedImageResource()));
				listItem.add(link);
			}
		});
	}
}
