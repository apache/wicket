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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;

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
		add(new Label("guessesRemaining", new PropertyModel<>(getGame(), "guessesRemaining")));

		// Components for displaying the current word
		add(new Label("word", new Model<String>()
		{
			@Override
			public String getObject()
			{
				return getGame().getWord().asString(true);
			}
		}));

		// Show the game's letters
		add(new ListView<Letter>("letters", getGame().getLetters())
		{
			@Override
			protected void populateItem(final ListItem<Letter> listItem)
			{
				final Letter letter = listItem.getModelObject();
				final Link<Void> link = new Link<Void>("letter")
				{
					@Override
					protected void onBeforeRender()
					{
						super.onBeforeRender();
						// Set enable state of link
						setAutoEnable(false);
						setEnabled(!letter.isGuessed());
					}

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
				};

				PageParameters parameters = new PageParameters();
				parameters.set(Letter.PARAMETER_LETTER, letter.asString(), INamedParameters.Type.MANUAL);
				if (letter.isGuessed())
				{
					parameters.set(Letter.PARAMETER_GUESSED, true, INamedParameters.Type.MANUAL);
				}
				link.add(new Image("image", Letter.LETTER_RESOURCE_REFERENCE, parameters));
				listItem.add(link);

				// append id attribute to link for unit tests
				link.add(AttributeModifier.replace("id", "letter_" + letter.asString()));


			}
		});
	}
}
