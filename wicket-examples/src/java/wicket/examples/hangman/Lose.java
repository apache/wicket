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

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;

/**
 * Page that handles the looser failing to guess the word.
 * 
 * @author Chris Turner
 * @version 1.0
 */
public class Lose extends WicketExamplePage {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create the loose page and its associated components.
	 * 
	 * @param guessPage
	 *            The guess page that we came from and return back to
	 */
	public Lose(final Guess guessPage) {
		Hangman hangman = (Hangman) guessPage.getModelObject();
		add(new Label("currentWord", hangman.getCurrentWord()));
		add(new Link("playAgain") {
			/**
			 * Comment for <code>serialVersionUID</code>
			 */
			private static final long serialVersionUID = 1L;

			public void onClick() {
				Hangman hangman = (Hangman) guessPage.getModelObject();
				hangman.newGame();
				guessPage.resetLetters();
				getRequestCycle().setPage(guessPage);
			}
		});
	}

}
