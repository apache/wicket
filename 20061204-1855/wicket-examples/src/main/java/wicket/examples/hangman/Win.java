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
import wicket.markup.html.link.Link;

/**
 * Page called when the user has correctly guessed the word.
 * 
 * @author Chris Turner
 * @author Jonathan Locke
 */
public class Win extends HangmanPage
{
	/**
	 * Create the win page and its associated components.
	 */
	public Win()
	{
		add(new Label("guessesRemaining", Integer.toString(getGame().getGuessesRemaining())));
		add(new Label("currentWord", getGame().getWord().asString()));
		add(new Link("playAgain")
		{
			public void onClick()
			{
				getGame().newGame();
				setResponsePage(new Guess());
			}
		});
	}
}
