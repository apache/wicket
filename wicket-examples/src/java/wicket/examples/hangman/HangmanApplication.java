// (c)2004 Templemore Technologies Limited. All Rights Reserved
// http://www.templemore.co.uk/copyright.html
package wicket.examples.hangman;

import wicket.ApplicationSettings;
import wicket.protocol.http.WebApplication;
import wicket.util.time.Duration;


/**
 * Class defining the main Hangman application.
 * 
 * @author Chris Turner
 * @version 1.0
 */
public class HangmanApplication extends WebApplication
{
    /**
     * Create the hangman application.
     */
    public HangmanApplication()
    {
        // Initialise Wicket settings
        getPages().setHomePage(Home.class);

        ApplicationSettings settings = getSettings();
        if (!Boolean.getBoolean("cache-templates"))
        {
            settings.setResourcePollFrequency(Duration.ONE_SECOND);
        }
    }
}