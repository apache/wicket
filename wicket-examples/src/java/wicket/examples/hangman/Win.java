// (c)2004 Templemore Technologies Limited. All Rights Reserved
// http://www.templemore.co.uk/copyright.html
package wicket.examples.hangman;

import wicket.examples.util.NavigationPanel;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;

/**
 * Page called when the user has correctly guessed the word.
 *
 * @author Chris Turner
 * @version 1.0
 */
public class Win extends HtmlPage {

    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Create the win page and its associated components.
     *
     * @param guessPage The guess page that we came from and return back to
     */
    public Win(final Guess guessPage) {
        Hangman hangman = (Hangman)guessPage.getModelObject();
        add(new NavigationPanel("mainNavigation", "Hangman example"));
        add(new Label("guessesRemaining", hangman.getCurrentWord()));
        add(new Label("currentWord", hangman.getCurrentWord()));
        add(new Link("playAgain") {
            /**
			 * Comment for <code>serialVersionUID</code>
			 */
			private static final long serialVersionUID = 1L;

			public void linkClicked() {
                Hangman hangman = (Hangman)guessPage.getModelObject();
                hangman.newGame();
                guessPage.resetLetters();
                getRequestCycle().setPage(guessPage);
            }
        });
    }
}
