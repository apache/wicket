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

import wicket.RequestCycle;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.model.Model;

/**
 * The main guess page for the hangman application.
 *
 * @author Chris Turner
 * @version 1.0
 */
public class Guess extends WicketExamplePage {

    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Create the guess page.
     *
     * @param hangman The hangman game instance to use as a model
     */
    public Guess(final Hangman hangman) {
        super();
        System.err.println("Created the guess page");
        setModel(new Model(hangman));

        // Components for displaying the guesses remaining & the hangman
        add(new Label("guessesRemaining", hangman, "guessesRemaining"));
        
        // Components for displaying the current word
        add(new Label("letters", hangman, "letters"));

        // Components for displaying the letters that can be selected
        for ( int i = 0; i < 26; i++ ) {
            char ch = (char)('a' + i);
            add(new SelectableLetterLink("letter_" + ch, ch));
        }
    }

    /**
     * Method to reset all of the selectable letters at the start of a game.
     */
    public void resetLetters() {
        for ( int i = 0; i < 26; i++ ) {
            char ch = (char)('a' + i);
            SelectableLetterLink link = (SelectableLetterLink)get("letter_" + ch);
            link.setEnabled(true);
        }
    }

    /**
     * Link representing a letter that can be selected in the game.
     */
    private class SelectableLetterLink extends Link {

        /**
		 * Comment for <code>serialVersionUID</code>
		 */
		private static final long serialVersionUID = 1L;
		private char letter;

        /**
         * Create a new selectable letter link given the supplied parameters.
         *
         * @param componentName The component name
         * @param letter The letter that this link represents
         */
        public SelectableLetterLink(final String componentName,
                                    final char letter) {
            super(componentName);
            this.letter = letter;
            setEnabled(true);
            setAutoEnable(false);
            setAfterDisabledLink("<i>" + Character.toUpperCase(letter) + "</i>");
        }

        /**
         * Handle clicking of this link. Redirects the request cycle based
         * on the current state of the game.
         */
        public void onLinkClicked() {
            final RequestCycle requestCycle = getRequestCycle();
            System.err.println("Linked clicked for letter: " + letter);
            setEnabled(false);
            Guess guessPage = (Guess)requestCycle.getPage();
            Hangman hangman = (Hangman)guessPage.getModelObject();
            hangman.guessLetter(letter);
            if ( hangman.isGuessed() ) {
                // Redirect to win page
                requestCycle.setPage(new Win(guessPage));
            }
            else if ( hangman.isAllGuessesUsed() ) {
                // Redirect to loose page
                requestCycle.setPage(new Lose(guessPage));
            }
            // else return to guess page with new state to display
        }


    }
}
