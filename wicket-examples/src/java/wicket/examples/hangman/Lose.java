// (c)2004 Templemore Technologies Limited. All Rights Reserved
// http://www.templemore.co.uk/copyright.html
package wicket.examples.hangman;

import wicket.examples.util.NavigationPanel;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;

/**
 * Page that handles the looser failing to guess the word.
 * 
 * @author Chris Turner
 * @version 1.0
 */
public class Lose extends HtmlPage {

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
		add(new NavigationPanel("mainNavigation", "Hangman example"));
		add(new Label("currentWord", hangman.getCurrentWord()));
		add(new Link("playAgain") {
			/**
			 * Comment for <code>serialVersionUID</code>
			 */
			private static final long serialVersionUID = 1L;

			public void linkClicked() {
				Hangman hangman = (Hangman) guessPage.getModelObject();
				hangman.newGame();
				guessPage.resetLetters();
				getRequestCycle().setPage(guessPage);
			}
		});
	}

}
