// (c)2004 Templemore Technologies Limited. All Rights Reserved
// http://www.templemore.co.uk/copyright.html
package wicket.examples.hangman;

import wicket.Page;
import wicket.PageParameters;
import wicket.examples.util.NavigationPanel;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.link.IPageLink;
import wicket.markup.html.link.PageLink;

/**
 * The home page for the hangman application. Contains just a single hyperlink
 * to the actual guess page that runs the game.
 * 
 * @author Chris Turner
 * @version 1.0
 */
public class Home extends HtmlPage {

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
		add(new NavigationPanel("mainNavigation", "Hangman example"));
		add(new PageLink("start", new IPageLink() {
			/**
			 * Comment for <code>serialVersionUID</code>
			 */
			private static final long serialVersionUID = 1L;

			public Page getPage() {
				return new Guess(hangman);
			}

			public Class getPageClass() {
				return Guess.class;
			}
		}));
	}

}
