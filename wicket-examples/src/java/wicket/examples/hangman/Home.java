/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.hangman;

import wicket.Page;
import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.link.IPageLink;
import wicket.markup.html.link.PageLink;

/**
 * The home page for the hangman application. Contains just a single hyperlink
 * to the actual guess page that runs the game.
 * 
 * @author Chris Turner
 * @version 1.0
 */
public class Home extends WicketExamplePage {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_ALLOWED_GUESSES = 5;

	private Hangman hangman;

	/**
	 * Create the home page.
	 * 
	 * @param parameters
	 *            The parameters for the page (not used)
	 */
	public Home(final PageParameters parameters) {
		if (parameters.containsKey("setWord")) {
			String[] words = new String[] { parameters.getString("setWord") };
			hangman = new Hangman(DEFAULT_ALLOWED_GUESSES, words);
		} else {
			hangman = new Hangman(DEFAULT_ALLOWED_GUESSES);
		}
		hangman.newGame();
		add(new PageLink("start", new IPageLink() {
			/**
			 * Comment for <code>serialVersionUID</code>
			 */
			private static final long serialVersionUID = 1L;

			public Page getPage() {
				return new Guess(hangman);
			}

			public Class getPageIdentity() {
				return Guess.class;
			}
		}));
	}

}
