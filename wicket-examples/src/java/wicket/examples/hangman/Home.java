/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:57:30 +0200 (vr, 26 mei 2006) $
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

import wicket.PageParameters;
import wicket.markup.html.link.PageLink;

/**
 * The home page for the hangman application. Contains just a single hyperlink
 * to the actual guess page that runs the game.
 * 
 * @author Chris Turner
 * @author Jonathan Locke
 */
public class Home extends HangmanPage
{
	/**
	 * Create the home page.
	 * 
	 * @param parameters
	 *            The parameters for the page (not used)
	 */
	public Home(final PageParameters parameters)
	{
		final String word = parameters.getString("word");
		if (word == null)
		{
			getGame().newGame(5, new WordGenerator());
		}
		else
		{
			getGame().newGame(5, new WordGenerator(new String[] { word }));
		}
		new PageLink(this, "start", Guess.class);
	}
}
